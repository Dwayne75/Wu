package org.apache.http.impl.conn.tsccm;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.HttpParams;

@Deprecated
@ThreadSafe
public class ThreadSafeClientConnManager
  implements ClientConnectionManager
{
  private final Log log;
  protected final SchemeRegistry schemeRegistry;
  protected final AbstractConnPool connectionPool;
  protected final ConnPoolByRoute pool;
  protected final ClientConnectionOperator connOperator;
  protected final ConnPerRouteBean connPerRoute;
  
  public ThreadSafeClientConnManager(SchemeRegistry schreg)
  {
    this(schreg, -1L, TimeUnit.MILLISECONDS);
  }
  
  public ThreadSafeClientConnManager()
  {
    this(SchemeRegistryFactory.createDefault());
  }
  
  public ThreadSafeClientConnManager(SchemeRegistry schreg, long connTTL, TimeUnit connTTLTimeUnit)
  {
    this(schreg, connTTL, connTTLTimeUnit, new ConnPerRouteBean());
  }
  
  public ThreadSafeClientConnManager(SchemeRegistry schreg, long connTTL, TimeUnit connTTLTimeUnit, ConnPerRouteBean connPerRoute)
  {
    if (schreg == null) {
      throw new IllegalArgumentException("Scheme registry may not be null");
    }
    this.log = LogFactory.getLog(getClass());
    this.schemeRegistry = schreg;
    this.connPerRoute = connPerRoute;
    this.connOperator = createConnectionOperator(schreg);
    this.pool = createConnectionPool(connTTL, connTTLTimeUnit);
    this.connectionPool = this.pool;
  }
  
  /**
   * @deprecated
   */
  public ThreadSafeClientConnManager(HttpParams params, SchemeRegistry schreg)
  {
    if (schreg == null) {
      throw new IllegalArgumentException("Scheme registry may not be null");
    }
    this.log = LogFactory.getLog(getClass());
    this.schemeRegistry = schreg;
    this.connPerRoute = new ConnPerRouteBean();
    this.connOperator = createConnectionOperator(schreg);
    this.pool = ((ConnPoolByRoute)createConnectionPool(params));
    this.connectionPool = this.pool;
  }
  
  protected void finalize()
    throws Throwable
  {
    try
    {
      shutdown();
    }
    finally
    {
      super.finalize();
    }
  }
  
  /**
   * @deprecated
   */
  protected AbstractConnPool createConnectionPool(HttpParams params)
  {
    return new ConnPoolByRoute(this.connOperator, params);
  }
  
  protected ConnPoolByRoute createConnectionPool(long connTTL, TimeUnit connTTLTimeUnit)
  {
    return new ConnPoolByRoute(this.connOperator, this.connPerRoute, 20, connTTL, connTTLTimeUnit);
  }
  
  protected ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg)
  {
    return new DefaultClientConnectionOperator(schreg);
  }
  
  public SchemeRegistry getSchemeRegistry()
  {
    return this.schemeRegistry;
  }
  
  public ClientConnectionRequest requestConnection(final HttpRoute route, Object state)
  {
    final PoolEntryRequest poolRequest = this.pool.requestPoolEntry(route, state);
    
    new ClientConnectionRequest()
    {
      public void abortRequest()
      {
        poolRequest.abortRequest();
      }
      
      public ManagedClientConnection getConnection(long timeout, TimeUnit tunit)
        throws InterruptedException, ConnectionPoolTimeoutException
      {
        if (route == null) {
          throw new IllegalArgumentException("Route may not be null.");
        }
        if (ThreadSafeClientConnManager.this.log.isDebugEnabled()) {
          ThreadSafeClientConnManager.this.log.debug("Get connection: " + route + ", timeout = " + timeout);
        }
        BasicPoolEntry entry = poolRequest.getPoolEntry(timeout, tunit);
        return new BasicPooledConnAdapter(ThreadSafeClientConnManager.this, entry);
      }
    };
  }
  
  public void releaseConnection(ManagedClientConnection conn, long validDuration, TimeUnit timeUnit)
  {
    if (!(conn instanceof BasicPooledConnAdapter)) {
      throw new IllegalArgumentException("Connection class mismatch, connection not obtained from this manager.");
    }
    BasicPooledConnAdapter hca = (BasicPooledConnAdapter)conn;
    if ((hca.getPoolEntry() != null) && (hca.getManager() != this)) {
      throw new IllegalArgumentException("Connection not obtained from this manager.");
    }
    synchronized (hca)
    {
      BasicPoolEntry entry = (BasicPoolEntry)hca.getPoolEntry();
      if (entry == null) {
        return;
      }
      try
      {
        if ((hca.isOpen()) && (!hca.isMarkedReusable())) {
          hca.shutdown();
        }
      }
      catch (IOException iox)
      {
        boolean reusable;
        if (this.log.isDebugEnabled()) {
          this.log.debug("Exception shutting down released connection.", iox);
        }
      }
      finally
      {
        boolean reusable;
        boolean reusable = hca.isMarkedReusable();
        if (this.log.isDebugEnabled()) {
          if (reusable) {
            this.log.debug("Released connection is reusable.");
          } else {
            this.log.debug("Released connection is not reusable.");
          }
        }
        hca.detach();
        this.pool.freeEntry(entry, reusable, validDuration, timeUnit);
      }
    }
  }
  
  public void shutdown()
  {
    this.log.debug("Shutting down");
    this.pool.shutdown();
  }
  
  public int getConnectionsInPool(HttpRoute route)
  {
    return this.pool.getConnectionsInPool(route);
  }
  
  public int getConnectionsInPool()
  {
    return this.pool.getConnectionsInPool();
  }
  
  public void closeIdleConnections(long idleTimeout, TimeUnit tunit)
  {
    if (this.log.isDebugEnabled()) {
      this.log.debug("Closing connections idle longer than " + idleTimeout + " " + tunit);
    }
    this.pool.closeIdleConnections(idleTimeout, tunit);
  }
  
  public void closeExpiredConnections()
  {
    this.log.debug("Closing expired connections");
    this.pool.closeExpiredConnections();
  }
  
  public int getMaxTotal()
  {
    return this.pool.getMaxTotalConnections();
  }
  
  public void setMaxTotal(int max)
  {
    this.pool.setMaxTotalConnections(max);
  }
  
  public int getDefaultMaxPerRoute()
  {
    return this.connPerRoute.getDefaultMaxPerRoute();
  }
  
  public void setDefaultMaxPerRoute(int max)
  {
    this.connPerRoute.setDefaultMaxPerRoute(max);
  }
  
  public int getMaxForRoute(HttpRoute route)
  {
    return this.connPerRoute.getMaxForRoute(route);
  }
  
  public void setMaxForRoute(HttpRoute route, int max)
  {
    this.connPerRoute.setMaxForRoute(route, max);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\conn\tsccm\ThreadSafeClientConnManager.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */