/*
 * Copyright (c) 2008, 2009, 2010, 2019 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.platform;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.Random;
import java.util.Set;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;
import com.goofans.gootool.Controller;

/**
 * Handles ensuring that only a single instance of the tool is running, for Windows and Linux platforms.
 * Operating-system level file locking is used to ensure there are no race conditions and to ensure the lock
 * is guaranteed to be released on JVM termination.
 * <p/>
 * The first copy of the tool that is run will listen to a socket on the loopback interface. Subsequent copies
 * that attempt to start will find the lockfile is locked, and send their arguments to the primary instance over
 * this socket instead (and then exit).
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class SingleInstance
{
  private static final Logger log = Logger.getLogger(SingleInstance.class.getName());

  /**
   * The single file, in the user's temporary directory, that is locked to ensure only one copy is run.
   */
  private static final String LOCK_FILE = "gootool.lock";

  /**
   * The file in which the primary instance writes the socket number it's listening to.
   */
  private static final String SOCKET_FILE = "gootool.socket";

  /**
   * The prefix used by temporary test files to ensure that the temporary directory is writeable. The actual files
   * will have a random integer appended to this.
   */
  private static final String TEST_FILE = "gootool.test";

  private static final SingleInstance theInstance = new SingleInstance();

  private final File lockFile;
  private RandomAccessFile lockFileRAF;

  private SingleInstance()
  {
    lockFile = getTempFile(LOCK_FILE);
  }

  /**
   * Singleton accessor method.
   *
   * @return the singleton instance of the SingleInstance object
   */
  public static SingleInstance getInstance()
  {
    return theInstance;
  }

  /**
   * Ensures that only one copy of the tool is running. If this is the first copy, dispatch the arguments
   * to the Controller, and set up a listening socket on the loopback interface to receive arguments from
   * subsequent instances.
   * <p/>
   * If a copy is already running, pass the arguments to it, then return false indicating the caller should exit.
   *
   * @param args The command-line arguments to use or pass to the primary instance.
   * @return true if this is the primary instance, otherwise false.
   */
  public synchronized boolean singleInstance(List<String> args)
  {
    // First do a quick test to make sure we can write anything to the tmpdir
    testTempDir();

    // Now try opening the real lockfile, if we can't write, another process has it open.
    FileLock lock = tryLock();

    if (lock != null) {
      primaryInstance(lock, args);
      return true;
    }
    else {
      secondaryInstance(args);
      return false;
    }
  }

  /**
   * Ensures that the temporary directory can be written to at all. This ensures the lockfile
   * can later be written. A read-only temporary directory is an unusual and fatal condition that
   * raises a RuntimeException.
   */
  private void testTempDir()
  {
    File testFile = getTempFile(TEST_FILE + new Random().nextInt(Integer.MAX_VALUE));
    log.finest("Testing tmpdir at " + testFile);

    try {
      RandomAccessFile randomAccessFile = new RandomAccessFile(testFile, "rws");
      FileChannel channel = randomAccessFile.getChannel();
      FileLock lock = channel.tryLock();
      if (lock == null) {
        log.log(Level.SEVERE, "Unable to lock temp file " + testFile);
        throw new RuntimeException("Unable to write to lock temp file " + testFile);
      }

      FileOutputStream fos = new FileOutputStream(randomAccessFile.getFD());
      // Write a single character to the file to ensure it's really writable.
      fos.write('a');

      // Must release the lock as the Linux version of Java fails to release it on fos.close(), thereby causing the next fd
      // opened (the real lock file) to be already locked - by us!
      lock.release();
      fos.close();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to write to temp directory " + testFile, e);
      throw new RuntimeException("Unable to write to temp directory " + testFile);
    }
    testFile.delete();
  }

  /**
   * Try to lock the main lockfile. Returns the lock if successful, otherwise null if it's already locked.
   * Failure to open the lockfile at all will result in a RuntimeException. This method is non-blocking.
   *
   * @return the FileLock if locked, otherwise null
   */
  private FileLock tryLock()
  {
    log.finest("Attempting lock at " + lockFile);

    FileLock lock;

    try {
      lockFileRAF = new RandomAccessFile(lockFile, "rws");
      FileChannel channel = lockFileRAF.getChannel();
      lock = channel.tryLock();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to lock lockfile " + lockFile);
      throw new RuntimeException("Unable to lock lockfile " + lockFile);
    }
    return lock;
  }

  /**
   * Called when we are the primary instance. Setup a shutdown hook to release the lock, start up the
   * listening socket, then pass the arguments to the Controller.
   *
   * @param lock The successful FileLock on the lockfile.
   * @param args The command-line arguments.
   */
  private void primaryInstance(final FileLock lock, List<String> args)
  {
    log.finer("We're the primary instance");

    Runtime.getRuntime().addShutdownHook(new Thread()
    {
      @Override
      public void run()
      {
        try {
          lock.release();
          lockFileRAF.close();
        }
        catch (IOException e) {
          log.log(Level.WARNING, "Unable to release lock on shutdown", e);
        }
        //noinspection ResultOfMethodCallIgnored
        lockFile.delete();
      }
    });

    try {
      new PrimaryInstanceSocket().start();
    }
    catch (IOException e) {
      throw new RuntimeException("Can't start primary instance server", e);
    }

    handleCommandLineArgs(args);
  }

  /**
   * Called when we are not the primary instance. Locate the primary instance's socket and send the
   * command-line arguments to it.
   *
   * @param args The command-line arguments.
   */
  private void secondaryInstance(List<String> args)
  {
    // Sleep just a moment in case the primary is still starting up
    try {
      Thread.sleep(200);
    }
    catch (InterruptedException e) {
      // do nothing
    }

    log.info("GooTool already running, send arguments to primary instance");

    int port;
    try {
      lockFileRAF.close();
      FileInputStream fis = new FileInputStream(getTempFile(SOCKET_FILE));
      BufferedReader r = new BufferedReader(new InputStreamReader(fis));
      port = Integer.valueOf(r.readLine());
      r.close();
      fis.close();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to determine socket of primary instance", e);
      throw new RuntimeException("Unable to determine socket of primary GooTool", e);
    }


    try {
      InetAddress addr = getLoopbackAddress();
      log.finer("Connecting to " + addr + " port " + port);

      Socket s = new Socket(addr, port);
      ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
      oos.writeObject(args);
      oos.close();
      s.close();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to send args to primary instance", e);
      throw new RuntimeException("Unable to send args to primary GooTool", e);
    }
    log.finer("Sent arguments, exiting");
  }

  /**
   * Gets the File of the given name in the user's temporary directory.
   *
   * @param fileName File name in the temp directory.
   * @return The File object representing this file.
   */
  private static File getTempFile(String fileName)
  {
    String tmpDir = System.getProperty("java.io.tmpdir");
    return new File(tmpDir, fileName);
  }

  /**
   * Handles the command line arguments, both if we're the primary instance, and if these args were passed
   * over the socket.
   *
   * @param args The command-line arguments.
   */
  private void handleCommandLineArgs(final List<String> args)
  {
    log.finer("Processing arguments:");
    for (int i = 0; i < args.size(); i++) {
      log.finer("args[" + i + "] = " + args.get(i));
    }

    // Queue for the event-dispatch thread to pass to the Controller once startup is fully completed.
    GooTool.queueTask(new Runnable()
    {
      public void run()
      {
        Controller controller = GooTool.getController();
        controller.bringToForeground();
        if (!args.isEmpty()) {
          for (String arg : args) {
            controller.installAddin(new File(arg));
          }
        }
      }
    });
  }

  /**
   * Gets the address of the loopback interface.
   * <p/>
   * Required due to a Vista update in April 2009 adding "::1 localhost" to the user's hosts file.
   * Java InetAddress.getLocalHost() then returns IPv6 ::1, even when IPv6 is disabled.
   * See http://goofans.com/node/292
   *
   * @return The IPv4 loopback address
   */
  private InetAddress getLoopbackAddress()
  {
    try {
      return InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
    }
    catch (UnknownHostException e) {
      // This can never occur because the IP address we pass to InetAddress.getByAddress is always of the correct length
      throw new RuntimeException(e);
    }
  }

  /**
   * A thread run on the primary instance that listens in the background for command-line arguments from
   * secondary instances, deserialising them and dispatching them to the Controller.
   */
  private class PrimaryInstanceSocket extends Thread
  {
    private final ServerSocketChannel serverSocketChannel;

    public PrimaryInstanceSocket() throws IOException
    {
      super("Primary instance socket");

      // Open a listening socket
      serverSocketChannel = openSocket();

      try {
        File socketFile = getTempFile(SOCKET_FILE);
        FileOutputStream fos = new FileOutputStream(socketFile);
        fos.write(Integer.toString(serverSocketChannel.socket().getLocalPort()).getBytes());
        fos.close();
        socketFile.deleteOnExit();
      }
      catch (IOException e) {
        log.log(Level.SEVERE, "Unable to write our socket details to socketfile", e);
        throw new RuntimeException("Unable to write our socket details to socketfile");
      }

      log.log(Level.FINE, "Primary instance listening on " + serverSocketChannel);
    }

    private static final int MIN_PORT = 20000;
    private static final int MAX_PORT = 60000;

    /**
     * Setup a listening socket on a random port. Chooses a random port between MIN_PORT and MAX_PORT
     * to listen to. This is required as nio doesn't allow us to specify a random port with a fixed address,
     * only when using "any available local address". This often results in the user's first Ethernet card
     * address, but for security we want to specify only a loopback address.
     *
     * @return The socket channel listened to.
     * @throws IOException If no available ports could be found or the socket could not be opened.
     */
    private ServerSocketChannel openSocket() throws IOException
    {
      ServerSocketChannel ssc = ServerSocketChannel.open();
      ssc.configureBlocking(false);

      ServerSocket sock = ssc.socket();

      int attempt = 0;
      Random rand = new Random();
      InetAddress addr = getLoopbackAddress();

      while (true) {
        int port = MIN_PORT + rand.nextInt(1 + MAX_PORT - MIN_PORT);
        try {
          sock.bind(new InetSocketAddress(addr, port));
          break;
        }
        catch (IOException e) {
          log.log(Level.WARNING, "Unable to open socket on " + addr + " port " + port, e);
          if (++attempt > 9) throw e;
        }
      }

      return ssc;
    }

    /**
     * Main loop that blocks waiting for args from clients, then dispatchse them.
     */
    @Override
    public void run()
    {
      try {
        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        //noinspection InfiniteLoopStatement
        while (true) {
          selector.select();

          Set<SelectionKey> keys = selector.selectedKeys();
          for (SelectionKey key : keys) {
            if (key.isAcceptable() && key.channel() == serverSocketChannel) {
              SocketChannel clientChannel = serverSocketChannel.accept();
              log.finer("Accepting connection from " + clientChannel.socket().getRemoteSocketAddress());
              clientChannel.configureBlocking(true);
              ObjectInputStream ois = new ObjectInputStream(clientChannel.socket().getInputStream());
              List<String> args = (List<String>) ois.readObject();
              ois.close();
              clientChannel.close();
              log.finest("Got args from client");
              handleCommandLineArgs(args);
            }
          }
        }
      }
      catch (Exception e) {
        log.log(Level.SEVERE, "Exception in primary instance listener thread", e);
      }
    }
  }
}
