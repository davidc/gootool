/*
 * Copyright (c) 2008, 2009, 2010, 2011 David C A Croft. All rights reserved. Your use of this computer software
 * is permitted only in accordance with the GooTool license agreement distributed with this file.
 */

package com.goofans.gootool.ios;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to IosConnections given a set of connection parameters.
 * <p/>
 * Where possible, will try to reuse a connection that already exists and is still connected to avoid connection setup overhead.
 *
 * @author David Croft (davidc@goofans.com)
 * @version $Id$
 */
public class IosConnectionFactory
{
  private static final List<IosConnection> connections = new ArrayList<IosConnection>();

  /**
   * A shutdown function to close any remaining open connections.
   */
  static {
    Runtime.getRuntime().addShutdownHook(new Thread("IosConnection cleanup")
    {
      @Override
      public void run()
      {
        System.out.println("iOS cleaning running");
        for (IosConnection connection : connections) {
          connection.close();
        }
      }
    });
  }

  private IosConnectionFactory()
  {
  }

  /**
   * Remove any connections that we still have cached, but that are somehow disconnected (by the server, network error, timeout, etc).
   */
  private static void removeStaleConnections()
  {
    boolean removedOne;
    do {
      removedOne = false;
      for (IosConnection connection : connections) {
        if (!connection.testConnection()) {
          connection.close();
          connections.remove(connection);
          removedOne = true;
          break;
        }
      }
    } while (removedOne);
  }

  /**
   * Gets a connection to the iOS device with the requested connection parameters. The connection will be connected when it returns, or an
   * exception will be thrown if the connection could not be established.
   * <p/>
   * This tries to reuse an existing connection with the same connection parameters if possible. The connection is removed from the pool when
   * this method is called; it is up to the caller to return it after use (unless the connection is broken).
   *
   * @param params the parameters with which to establish the connection
   * @return a connected IosConnection
   * @throws IosException if the connection could not be established
   */
  public static IosConnection getConnection(IosConnectionParameters params) throws IosException
  {
    removeStaleConnections();

    for (IosConnection connection : connections) {
      if (connection.getParams().equals(params)) {
        connections.remove(connection);
        return connection;
      }
    }

    IosConnection connection = new IosConnection(params);
    connection.connect();
    return connection;
  }

  /**
   * Return a connection to the pool after use. Don't use this if the connection is already broken or closed.
   *
   * @param connection The connection to return to the pool.
   */
  public static void returnConnection(IosConnection connection)
  {
    if (!connection.testConnection()) throw new RuntimeException("Attempt to return a closed connection to the pool");
    if (connections.contains(connection)) throw new RuntimeException("Attempt to return a connection that's already in the pool");

    connections.add(connection);
  }

  public static void main(String[] args) throws InterruptedException, IosException
  {
    IosConnectionParameters params = new IosConnectionParameters("192.168.2.162", null);

    IosConnection connection = getConnection(params);
    System.out.println("connection = " + connection);
    returnConnection(connection);

    Thread.sleep(10 * 1000);

    connection = getConnection(params);
    System.out.println("connection = " + connection);
    returnConnection(connection);
  }
}
