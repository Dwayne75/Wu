package com.mysql.jdbc;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

public class StandardSocketFactory
  implements SocketFactory
{
  public static final String TCP_NO_DELAY_PROPERTY_NAME = "tcpNoDelay";
  public static final String TCP_KEEP_ALIVE_DEFAULT_VALUE = "true";
  public static final String TCP_KEEP_ALIVE_PROPERTY_NAME = "tcpKeepAlive";
  public static final String TCP_RCV_BUF_PROPERTY_NAME = "tcpRcvBuf";
  public static final String TCP_SND_BUF_PROPERTY_NAME = "tcpSndBuf";
  public static final String TCP_TRAFFIC_CLASS_PROPERTY_NAME = "tcpTrafficClass";
  public static final String TCP_RCV_BUF_DEFAULT_VALUE = "0";
  public static final String TCP_SND_BUF_DEFAULT_VALUE = "0";
  public static final String TCP_TRAFFIC_CLASS_DEFAULT_VALUE = "0";
  public static final String TCP_NO_DELAY_DEFAULT_VALUE = "true";
  private static Method setTraficClassMethod;
  
  static
  {
    try
    {
      setTraficClassMethod = class$java$net$Socket.getMethod("setTrafficClass", new Class[] { Integer.TYPE });
    }
    catch (SecurityException e)
    {
      setTraficClassMethod = null;
    }
    catch (NoSuchMethodException e)
    {
      setTraficClassMethod = null;
    }
  }
  
  protected String host = null;
  protected int port = 3306;
  protected Socket rawSocket = null;
  
  public Socket afterHandshake()
    throws SocketException, IOException
  {
    return this.rawSocket;
  }
  
  public Socket beforeHandshake()
    throws SocketException, IOException
  {
    return this.rawSocket;
  }
  
  private void configureSocket(Socket sock, Properties props)
    throws SocketException, IOException
  {
    try
    {
      sock.setTcpNoDelay(Boolean.valueOf(props.getProperty("tcpNoDelay", "true")).booleanValue());
      
      String keepAlive = props.getProperty("tcpKeepAlive", "true");
      if ((keepAlive != null) && (keepAlive.length() > 0)) {
        sock.setKeepAlive(Boolean.valueOf(keepAlive).booleanValue());
      }
      int receiveBufferSize = Integer.parseInt(props.getProperty("tcpRcvBuf", "0"));
      if (receiveBufferSize > 0) {
        sock.setReceiveBufferSize(receiveBufferSize);
      }
      int sendBufferSize = Integer.parseInt(props.getProperty("tcpSndBuf", "0"));
      if (sendBufferSize > 0) {
        sock.setSendBufferSize(sendBufferSize);
      }
      int trafficClass = Integer.parseInt(props.getProperty("tcpTrafficClass", "0"));
      if ((trafficClass > 0) && (setTraficClassMethod != null)) {
        setTraficClassMethod.invoke(sock, new Object[] { new Integer(trafficClass) });
      }
    }
    catch (Throwable t)
    {
      unwrapExceptionToProperClassAndThrowIt(t);
    }
  }
  
  public Socket connect(String hostname, int portNumber, Properties props)
    throws SocketException, IOException
  {
    if (props != null)
    {
      this.host = hostname;
      
      this.port = portNumber;
      
      Method connectWithTimeoutMethod = null;
      Method socketBindMethod = null;
      Class socketAddressClass = null;
      
      String localSocketHostname = props.getProperty("localSocketAddress");
      
      String connectTimeoutStr = props.getProperty("connectTimeout");
      
      int connectTimeout = 0;
      
      boolean wantsTimeout = (connectTimeoutStr != null) && (connectTimeoutStr.length() > 0) && (!connectTimeoutStr.equals("0"));
      
      boolean wantsLocalBind = (localSocketHostname != null) && (localSocketHostname.length() > 0);
      
      boolean needsConfigurationBeforeConnect = socketNeedsConfigurationBeforeConnect(props);
      if ((wantsTimeout) || (wantsLocalBind) || (needsConfigurationBeforeConnect))
      {
        if (connectTimeoutStr != null) {
          try
          {
            connectTimeout = Integer.parseInt(connectTimeoutStr);
          }
          catch (NumberFormatException nfe)
          {
            throw new SocketException("Illegal value '" + connectTimeoutStr + "' for connectTimeout");
          }
        }
        try
        {
          socketAddressClass = Class.forName("java.net.SocketAddress");
          
          connectWithTimeoutMethod = class$java$net$Socket.getMethod("connect", new Class[] { socketAddressClass, Integer.TYPE });
          
          socketBindMethod = class$java$net$Socket.getMethod("bind", new Class[] { socketAddressClass });
        }
        catch (NoClassDefFoundError noClassDefFound) {}catch (NoSuchMethodException noSuchMethodEx) {}catch (Throwable catchAll) {}
        if ((wantsLocalBind) && (socketBindMethod == null)) {
          throw new SocketException("Can't specify \"localSocketAddress\" on JVMs older than 1.4");
        }
        if ((wantsTimeout) && (connectWithTimeoutMethod == null)) {
          throw new SocketException("Can't specify \"connectTimeout\" on JVMs older than 1.4");
        }
      }
      if (this.host != null)
      {
        if ((!wantsLocalBind) && (!wantsTimeout) && (!needsConfigurationBeforeConnect))
        {
          InetAddress[] possibleAddresses = InetAddress.getAllByName(this.host);
          
          Throwable caughtWhileConnecting = null;
          for (int i = 0; i < possibleAddresses.length; i++) {
            try
            {
              this.rawSocket = new Socket(possibleAddresses[i], this.port);
              
              configureSocket(this.rawSocket, props);
            }
            catch (Exception ex)
            {
              caughtWhileConnecting = ex;
            }
          }
          if (this.rawSocket == null) {
            unwrapExceptionToProperClassAndThrowIt(caughtWhileConnecting);
          }
        }
        else
        {
          try
          {
            InetAddress[] possibleAddresses = InetAddress.getAllByName(this.host);
            
            Throwable caughtWhileConnecting = null;
            
            Object localSockAddr = null;
            
            Class inetSocketAddressClass = null;
            
            Constructor addrConstructor = null;
            try
            {
              inetSocketAddressClass = Class.forName("java.net.InetSocketAddress");
              
              addrConstructor = inetSocketAddressClass.getConstructor(new Class[] { InetAddress.class, Integer.TYPE });
              if (wantsLocalBind) {
                localSockAddr = addrConstructor.newInstance(new Object[] { InetAddress.getByName(localSocketHostname), new Integer(0) });
              }
            }
            catch (Throwable ex)
            {
              unwrapExceptionToProperClassAndThrowIt(ex);
            }
            for (int i = 0; i < possibleAddresses.length; i++) {
              try
              {
                this.rawSocket = new Socket();
                
                configureSocket(this.rawSocket, props);
                
                Object sockAddr = addrConstructor.newInstance(new Object[] { possibleAddresses[i], new Integer(this.port) });
                
                socketBindMethod.invoke(this.rawSocket, new Object[] { localSockAddr });
                
                connectWithTimeoutMethod.invoke(this.rawSocket, new Object[] { sockAddr, new Integer(connectTimeout) });
              }
              catch (Exception ex)
              {
                this.rawSocket = null;
                
                caughtWhileConnecting = ex;
              }
            }
            if (this.rawSocket == null) {
              unwrapExceptionToProperClassAndThrowIt(caughtWhileConnecting);
            }
          }
          catch (Throwable t)
          {
            unwrapExceptionToProperClassAndThrowIt(t);
          }
        }
        return this.rawSocket;
      }
    }
    throw new SocketException("Unable to create socket");
  }
  
  private boolean socketNeedsConfigurationBeforeConnect(Properties props)
  {
    int receiveBufferSize = Integer.parseInt(props.getProperty("tcpRcvBuf", "0"));
    if (receiveBufferSize > 0) {
      return true;
    }
    int sendBufferSize = Integer.parseInt(props.getProperty("tcpSndBuf", "0"));
    if (sendBufferSize > 0) {
      return true;
    }
    int trafficClass = Integer.parseInt(props.getProperty("tcpTrafficClass", "0"));
    if ((trafficClass > 0) && (setTraficClassMethod != null)) {
      return true;
    }
    return false;
  }
  
  private void unwrapExceptionToProperClassAndThrowIt(Throwable caughtWhileConnecting)
    throws SocketException, IOException
  {
    if ((caughtWhileConnecting instanceof InvocationTargetException)) {
      caughtWhileConnecting = ((InvocationTargetException)caughtWhileConnecting).getTargetException();
    }
    if ((caughtWhileConnecting instanceof SocketException)) {
      throw ((SocketException)caughtWhileConnecting);
    }
    if ((caughtWhileConnecting instanceof IOException)) {
      throw ((IOException)caughtWhileConnecting);
    }
    throw new SocketException(caughtWhileConnecting.toString());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\StandardSocketFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */