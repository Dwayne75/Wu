package org.apache.http.conn.scheme;

import java.util.Locale;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.LangUtils;

@Immutable
public final class Scheme
{
  private final String name;
  private final SchemeSocketFactory socketFactory;
  private final int defaultPort;
  private final boolean layered;
  private String stringRep;
  
  public Scheme(String name, int port, SchemeSocketFactory factory)
  {
    if (name == null) {
      throw new IllegalArgumentException("Scheme name may not be null");
    }
    if ((port <= 0) || (port > 65535)) {
      throw new IllegalArgumentException("Port is invalid: " + port);
    }
    if (factory == null) {
      throw new IllegalArgumentException("Socket factory may not be null");
    }
    this.name = name.toLowerCase(Locale.ENGLISH);
    this.defaultPort = port;
    if ((factory instanceof SchemeLayeredSocketFactory))
    {
      this.layered = true;
      this.socketFactory = factory;
    }
    else if ((factory instanceof LayeredSchemeSocketFactory))
    {
      this.layered = true;
      this.socketFactory = new SchemeLayeredSocketFactoryAdaptor2((LayeredSchemeSocketFactory)factory);
    }
    else
    {
      this.layered = false;
      this.socketFactory = factory;
    }
  }
  
  @Deprecated
  public Scheme(String name, SocketFactory factory, int port)
  {
    if (name == null) {
      throw new IllegalArgumentException("Scheme name may not be null");
    }
    if (factory == null) {
      throw new IllegalArgumentException("Socket factory may not be null");
    }
    if ((port <= 0) || (port > 65535)) {
      throw new IllegalArgumentException("Port is invalid: " + port);
    }
    this.name = name.toLowerCase(Locale.ENGLISH);
    if ((factory instanceof LayeredSocketFactory))
    {
      this.socketFactory = new SchemeLayeredSocketFactoryAdaptor((LayeredSocketFactory)factory);
      
      this.layered = true;
    }
    else
    {
      this.socketFactory = new SchemeSocketFactoryAdaptor(factory);
      this.layered = false;
    }
    this.defaultPort = port;
  }
  
  public final int getDefaultPort()
  {
    return this.defaultPort;
  }
  
  @Deprecated
  public final SocketFactory getSocketFactory()
  {
    if ((this.socketFactory instanceof SchemeSocketFactoryAdaptor)) {
      return ((SchemeSocketFactoryAdaptor)this.socketFactory).getFactory();
    }
    if (this.layered) {
      return new LayeredSocketFactoryAdaptor((LayeredSchemeSocketFactory)this.socketFactory);
    }
    return new SocketFactoryAdaptor(this.socketFactory);
  }
  
  public final SchemeSocketFactory getSchemeSocketFactory()
  {
    return this.socketFactory;
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final boolean isLayered()
  {
    return this.layered;
  }
  
  public final int resolvePort(int port)
  {
    return port <= 0 ? this.defaultPort : port;
  }
  
  public final String toString()
  {
    if (this.stringRep == null)
    {
      StringBuilder buffer = new StringBuilder();
      buffer.append(this.name);
      buffer.append(':');
      buffer.append(Integer.toString(this.defaultPort));
      this.stringRep = buffer.toString();
    }
    return this.stringRep;
  }
  
  public final boolean equals(Object obj)
  {
    if (this == obj) {
      return true;
    }
    if ((obj instanceof Scheme))
    {
      Scheme that = (Scheme)obj;
      return (this.name.equals(that.name)) && (this.defaultPort == that.defaultPort) && (this.layered == that.layered);
    }
    return false;
  }
  
  public int hashCode()
  {
    int hash = 17;
    hash = LangUtils.hashCode(hash, this.defaultPort);
    hash = LangUtils.hashCode(hash, this.name);
    hash = LangUtils.hashCode(hash, this.layered);
    return hash;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\conn\scheme\Scheme.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */