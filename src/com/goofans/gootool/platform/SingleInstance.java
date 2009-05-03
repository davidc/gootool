package com.goofans.gootool.platform;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.goofans.gootool.GooTool;

/**
 * Handles ensuring that only a single instance of the tool is running, for Windows and Linux platforms.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class SingleInstance
{
  private static final Logger log = Logger.getLogger(SingleInstance.class.getName());

  private static final String LOCK_FILE = "gootool.lock";
  private static final String SOCKET_FILE = "gootool.socket";
  private static final String TEST_FILE = "gootool.test";

  private final File lockFile;
  private RandomAccessFile lockFileRAF;

  public SingleInstance()
  {
    lockFile = getTempFile(LOCK_FILE);
  }

  public boolean singleInstance(String[] args)
  {
    // First do a quick test to make sure we can write anything to the tmpdir
    testTempDir();

    // Now try opening the real lockfile, if we can't write, another process has it open.
    final FileLock lock = tryLock();

    if (lock != null) {
      primaryInstance(lock, args);
      return true;
    }
    else {
      secondaryInstance(args);
      return false;
    }
  }

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
      fos.write('a');

      // Must release it or stupid Linux version of Java thinks that our lock on the REAL lock file is this lock!
      lock.release();
      fos.close();
    }
    catch (IOException e) {
      log.log(Level.SEVERE, "Unable to write to temp directory " + testFile, e);
      throw new RuntimeException("Unable to write to temp directory " + testFile);
    }
    testFile.delete();
  }

  private FileLock tryLock()
  {
    log.finest("Attempting lock at " + lockFile);

    final FileLock lock;

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

  private void primaryInstance(final FileLock lock, String[] args)
  {
    log.finer("We're the primary instance");
    try {
      new PrimaryInstanceSocket(lock).start();
    }
    catch (IOException e) {
      throw new RuntimeException("Can't start primary instance server", e);
    }


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
        lockFile.delete();
      }
    });

    handleCommandLineArgs(args);
  }

  private void secondaryInstance(String[] args)
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

  private File getTempFile(String fileName)
  {
    String tmpDir = System.getProperty("java.io.tmpdir");
    return new File(tmpDir, fileName);
  }

  /**
   * Handles the command line arguments, both if we're the primary instance, and if these args were passed
   * over the socket.
   *
   * @param args the command-line arguments.
   */
  private void handleCommandLineArgs(String[] args)
  {
    log.finer("Processing arguments:");
    for (int i = 0; i < args.length; i++) {
      log.finer("args[" + i + "] = " + args[i]);
    }

    if (args.length > 0) {
      final File addinFile = new File(args[0]);
      GooTool.queueTask(new Runnable()
      {
        public void run()
        {
          GooTool.getController().bringToForeground();
          GooTool.getController().installAddin(addinFile);
        }
      });
    }
    else {
      GooTool.queueTask(new Runnable()
      {
        public void run()
        {
          GooTool.getController().bringToForeground();
        }
      });
    }
  }

  private InetAddress getLoopbackAddress() throws UnknownHostException
  {
    return InetAddress.getByAddress(new byte[]{127, 0, 0, 1});
  }

  private class PrimaryInstanceSocket extends Thread
  {
    private final ServerSocketChannel serverSocketChannel;

    public PrimaryInstanceSocket(FileLock lock) throws IOException
    {
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
              String[] args = (String[]) ois.readObject();
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
