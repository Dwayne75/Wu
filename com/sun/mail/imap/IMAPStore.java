package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.Namespaces;
import com.sun.mail.imap.protocol.Namespaces.Namespace;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Quota;
import javax.mail.QuotaAwareStore;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.StoreClosedException;
import javax.mail.URLName;

public class IMAPStore
  extends Store
  implements QuotaAwareStore, ResponseHandler
{
  public static final int RESPONSE = 1000;
  protected final String name;
  protected final int defaultPort;
  protected final boolean isSSL;
  private final int blksize;
  private boolean ignoreSize;
  private final int statusCacheTimeout;
  private final int appendBufferSize;
  private final int minIdleTime;
  private volatile int port = -1;
  protected String host;
  protected String user;
  protected String password;
  protected String proxyAuthUser;
  protected String authorizationID;
  protected String saslRealm;
  private Namespaces namespaces;
  private boolean disableAuthLogin = false;
  private boolean disableAuthPlain = false;
  private boolean disableAuthNtlm = false;
  private boolean enableStartTLS = false;
  private boolean requireStartTLS = false;
  private boolean usingSSL = false;
  private boolean enableSASL = false;
  private String[] saslMechanisms;
  private boolean forcePasswordRefresh = false;
  private boolean enableImapEvents = false;
  private String guid;
  private volatile boolean connectionFailed = false;
  private volatile boolean forceClose = false;
  private final Object connectionFailedLock = new Object();
  private boolean debugusername;
  private boolean debugpassword;
  protected MailLogger logger;
  private boolean messageCacheDebug;
  private volatile Constructor folderConstructor = null;
  private volatile Constructor folderConstructorLI = null;
  private final ConnectionPool pool;
  
  static class ConnectionPool
  {
    private Vector authenticatedConnections = new Vector();
    private Vector folders;
    private boolean storeConnectionInUse = false;
    private long lastTimePruned;
    private final boolean separateStoreConnection;
    private final long clientTimeoutInterval;
    private final long serverTimeoutInterval;
    private final int poolSize;
    private final long pruningInterval;
    private final MailLogger logger;
    private static final int RUNNING = 0;
    private static final int IDLE = 1;
    private static final int ABORTING = 2;
    private int idleState = 0;
    private IMAPProtocol idleProtocol;
    
    ConnectionPool(String name, MailLogger plogger, Session session)
    {
      this.lastTimePruned = System.currentTimeMillis();
      
      boolean debug = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".connectionpool.debug", false);
      
      this.logger = plogger.getSubLogger("connectionpool", "DEBUG IMAP CP", debug);
      
      int size = PropUtil.getIntSessionProperty(session, "mail." + name + ".connectionpoolsize", -1);
      if (size > 0)
      {
        this.poolSize = size;
        if (this.logger.isLoggable(Level.CONFIG)) {
          this.logger.config("mail.imap.connectionpoolsize: " + this.poolSize);
        }
      }
      else
      {
        this.poolSize = 1;
      }
      int connectionPoolTimeout = PropUtil.getIntSessionProperty(session, "mail." + name + ".connectionpooltimeout", -1);
      if (connectionPoolTimeout > 0)
      {
        this.clientTimeoutInterval = connectionPoolTimeout;
        if (this.logger.isLoggable(Level.CONFIG)) {
          this.logger.config("mail.imap.connectionpooltimeout: " + this.clientTimeoutInterval);
        }
      }
      else
      {
        this.clientTimeoutInterval = 45000L;
      }
      int serverTimeout = PropUtil.getIntSessionProperty(session, "mail." + name + ".servertimeout", -1);
      if (serverTimeout > 0)
      {
        this.serverTimeoutInterval = serverTimeout;
        if (this.logger.isLoggable(Level.CONFIG)) {
          this.logger.config("mail.imap.servertimeout: " + this.serverTimeoutInterval);
        }
      }
      else
      {
        this.serverTimeoutInterval = 1800000L;
      }
      int pruning = PropUtil.getIntSessionProperty(session, "mail." + name + ".pruninginterval", -1);
      if (pruning > 0)
      {
        this.pruningInterval = pruning;
        if (this.logger.isLoggable(Level.CONFIG)) {
          this.logger.config("mail.imap.pruninginterval: " + this.pruningInterval);
        }
      }
      else
      {
        this.pruningInterval = 60000L;
      }
      this.separateStoreConnection = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".separatestoreconnection", false);
      if (this.separateStoreConnection) {
        this.logger.config("dedicate a store connection");
      }
    }
  }
  
  private ResponseHandler nonStoreResponseHandler = new ResponseHandler()
  {
    public void handleResponse(Response r)
    {
      if ((r.isOK()) || (r.isNO()) || (r.isBAD()) || (r.isBYE())) {
        IMAPStore.this.handleResponseCode(r);
      }
      if (r.isBYE()) {
        IMAPStore.this.logger.fine("IMAPStore non-store connection dead");
      }
    }
  };
  
  public IMAPStore(Session session, URLName url)
  {
    this(session, url, "imap", false);
  }
  
  protected IMAPStore(Session session, URLName url, String name, boolean isSSL)
  {
    super(session, url);
    if (url != null) {
      name = url.getProtocol();
    }
    this.name = name;
    if (!isSSL) {
      isSSL = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".ssl.enable", false);
    }
    if (isSSL) {
      this.defaultPort = 993;
    } else {
      this.defaultPort = 143;
    }
    this.isSSL = isSSL;
    
    this.debug = session.getDebug();
    this.debugusername = PropUtil.getBooleanSessionProperty(session, "mail.debug.auth.username", true);
    
    this.debugpassword = PropUtil.getBooleanSessionProperty(session, "mail.debug.auth.password", false);
    
    this.logger = new MailLogger(getClass(), "DEBUG " + name.toUpperCase(), session);
    
    boolean partialFetch = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".partialfetch", true);
    if (!partialFetch)
    {
      this.blksize = -1;
      this.logger.config("mail.imap.partialfetch: false");
    }
    else
    {
      this.blksize = PropUtil.getIntSessionProperty(session, "mail." + name + ".fetchsize", 16384);
      if (this.logger.isLoggable(Level.CONFIG)) {
        this.logger.config("mail.imap.fetchsize: " + this.blksize);
      }
    }
    this.ignoreSize = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".ignorebodystructuresize", false);
    if (this.logger.isLoggable(Level.CONFIG)) {
      this.logger.config("mail.imap.ignorebodystructuresize: " + this.ignoreSize);
    }
    this.statusCacheTimeout = PropUtil.getIntSessionProperty(session, "mail." + name + ".statuscachetimeout", 1000);
    if (this.logger.isLoggable(Level.CONFIG)) {
      this.logger.config("mail.imap.statuscachetimeout: " + this.statusCacheTimeout);
    }
    this.appendBufferSize = PropUtil.getIntSessionProperty(session, "mail." + name + ".appendbuffersize", -1);
    if (this.logger.isLoggable(Level.CONFIG)) {
      this.logger.config("mail.imap.appendbuffersize: " + this.appendBufferSize);
    }
    this.minIdleTime = PropUtil.getIntSessionProperty(session, "mail." + name + ".minidletime", 10);
    if (this.logger.isLoggable(Level.CONFIG)) {
      this.logger.config("mail.imap.minidletime: " + this.minIdleTime);
    }
    String s = session.getProperty("mail." + name + ".proxyauth.user");
    if (s != null)
    {
      this.proxyAuthUser = s;
      if (this.logger.isLoggable(Level.CONFIG)) {
        this.logger.config("mail.imap.proxyauth.user: " + this.proxyAuthUser);
      }
    }
    this.disableAuthLogin = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".auth.login.disable", false);
    if (this.disableAuthLogin) {
      this.logger.config("disable AUTH=LOGIN");
    }
    this.disableAuthPlain = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".auth.plain.disable", false);
    if (this.disableAuthPlain) {
      this.logger.config("disable AUTH=PLAIN");
    }
    this.disableAuthNtlm = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".auth.ntlm.disable", false);
    if (this.disableAuthNtlm) {
      this.logger.config("disable AUTH=NTLM");
    }
    this.enableStartTLS = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".starttls.enable", false);
    if (this.enableStartTLS) {
      this.logger.config("enable STARTTLS");
    }
    this.requireStartTLS = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".starttls.required", false);
    if (this.requireStartTLS) {
      this.logger.config("require STARTTLS");
    }
    this.enableSASL = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".sasl.enable", false);
    if (this.enableSASL) {
      this.logger.config("enable SASL");
    }
    if (this.enableSASL)
    {
      s = session.getProperty("mail." + name + ".sasl.mechanisms");
      if ((s != null) && (s.length() > 0))
      {
        if (this.logger.isLoggable(Level.CONFIG)) {
          this.logger.config("SASL mechanisms allowed: " + s);
        }
        Vector v = new Vector(5);
        StringTokenizer st = new StringTokenizer(s, " ,");
        while (st.hasMoreTokens())
        {
          String m = st.nextToken();
          if (m.length() > 0) {
            v.addElement(m);
          }
        }
        this.saslMechanisms = new String[v.size()];
        v.copyInto(this.saslMechanisms);
      }
    }
    s = session.getProperty("mail." + name + ".sasl.authorizationid");
    if (s != null)
    {
      this.authorizationID = s;
      this.logger.log(Level.CONFIG, "mail.imap.sasl.authorizationid: {0}", this.authorizationID);
    }
    s = session.getProperty("mail." + name + ".sasl.realm");
    if (s != null)
    {
      this.saslRealm = s;
      this.logger.log(Level.CONFIG, "mail.imap.sasl.realm: {0}", this.saslRealm);
    }
    this.forcePasswordRefresh = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".forcepasswordrefresh", false);
    if (this.forcePasswordRefresh) {
      this.logger.config("enable forcePasswordRefresh");
    }
    this.enableImapEvents = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".enableimapevents", false);
    if (this.enableImapEvents) {
      this.logger.config("enable IMAP events");
    }
    this.messageCacheDebug = PropUtil.getBooleanSessionProperty(session, "mail." + name + ".messagecache.debug", false);
    
    this.guid = session.getProperty("mail." + name + ".yahoo.guid");
    if (this.guid != null) {
      this.logger.log(Level.CONFIG, "mail.imap.yahoo.guid: {0}", this.guid);
    }
    s = session.getProperty("mail." + name + ".folder.class");
    if (s != null)
    {
      this.logger.log(Level.CONFIG, "IMAP: folder class: {0}", s);
      try
      {
        ClassLoader cl = getClass().getClassLoader();
        
        Class folderClass = null;
        try
        {
          folderClass = Class.forName(s, false, cl);
        }
        catch (ClassNotFoundException ex1)
        {
          folderClass = Class.forName(s);
        }
        Class[] c = { String.class, Character.TYPE, IMAPStore.class, Boolean.class };
        
        this.folderConstructor = folderClass.getConstructor(c);
        Class[] c2 = { ListInfo.class, IMAPStore.class };
        this.folderConstructorLI = folderClass.getConstructor(c2);
      }
      catch (Exception ex)
      {
        this.logger.log(Level.CONFIG, "IMAP: failed to load folder class", ex);
      }
    }
    this.pool = new ConnectionPool(name, this.logger, session);
  }
  
  protected synchronized boolean protocolConnect(String host, int pport, String user, String password)
    throws MessagingException
  {
    IMAPProtocol protocol = null;
    if ((host == null) || (password == null) || (user == null))
    {
      if (this.logger.isLoggable(Level.FINE)) {
        this.logger.fine("protocolConnect returning false, host=" + host + ", user=" + traceUser(user) + ", password=" + tracePassword(password));
      }
      return false;
    }
    if (pport != -1) {
      this.port = pport;
    } else {
      this.port = PropUtil.getIntSessionProperty(this.session, "mail." + this.name + ".port", this.port);
    }
    if (this.port == -1) {
      this.port = this.defaultPort;
    }
    try
    {
      boolean poolEmpty;
      synchronized (this.pool)
      {
        poolEmpty = this.pool.authenticatedConnections.isEmpty();
      }
      if (poolEmpty)
      {
        if (this.logger.isLoggable(Level.FINE)) {
          this.logger.fine("trying to connect to host \"" + host + "\", port " + this.port + ", isSSL " + this.isSSL);
        }
        protocol = newIMAPProtocol(host, this.port);
        if (this.logger.isLoggable(Level.FINE)) {
          this.logger.fine("protocolConnect login, host=" + host + ", user=" + traceUser(user) + ", password=" + tracePassword(password));
        }
        login(protocol, user, password);
        
        protocol.addResponseHandler(this);
        
        this.usingSSL = protocol.isSSL();
        
        this.host = host;
        this.user = user;
        this.password = password;
        synchronized (this.pool)
        {
          this.pool.authenticatedConnections.addElement(protocol);
        }
      }
    }
    catch (CommandFailedException cex)
    {
      if (protocol != null) {
        protocol.disconnect();
      }
      protocol = null;
      throw new AuthenticationFailedException(cex.getResponse().getRest());
    }
    catch (ProtocolException pex)
    {
      if (protocol != null) {
        protocol.disconnect();
      }
      protocol = null;
      throw new MessagingException(pex.getMessage(), pex);
    }
    catch (IOException ioex)
    {
      throw new MessagingException(ioex.getMessage(), ioex);
    }
    return true;
  }
  
  protected IMAPProtocol newIMAPProtocol(String host, int port)
    throws IOException, ProtocolException
  {
    return new IMAPProtocol(this.name, host, port, this.session.getProperties(), this.isSSL, this.logger);
  }
  
  private void login(IMAPProtocol p, String u, String pw)
    throws ProtocolException
  {
    if ((this.enableStartTLS) || (this.requireStartTLS)) {
      if (p.hasCapability("STARTTLS"))
      {
        p.startTLS();
        
        p.capability();
      }
      else if (this.requireStartTLS)
      {
        this.logger.fine("STARTTLS required but not supported by server");
        throw new ProtocolException("STARTTLS required but not supported by server");
      }
    }
    if (p.isAuthenticated()) {
      return;
    }
    preLogin(p);
    if (this.guid != null) {
      p.id(this.guid);
    }
    p.getCapabilities().put("__PRELOGIN__", "");
    String authzid;
    String authzid;
    if (this.authorizationID != null)
    {
      authzid = this.authorizationID;
    }
    else
    {
      String authzid;
      if (this.proxyAuthUser != null) {
        authzid = this.proxyAuthUser;
      } else {
        authzid = null;
      }
    }
    if (this.enableSASL) {
      p.sasllogin(this.saslMechanisms, this.saslRealm, authzid, u, pw);
    }
    if (!p.isAuthenticated()) {
      if ((p.hasCapability("AUTH=PLAIN")) && (!this.disableAuthPlain)) {
        p.authplain(authzid, u, pw);
      } else if (((p.hasCapability("AUTH-LOGIN")) || (p.hasCapability("AUTH=LOGIN"))) && (!this.disableAuthLogin)) {
        p.authlogin(u, pw);
      } else if ((p.hasCapability("AUTH=NTLM")) && (!this.disableAuthNtlm)) {
        p.authntlm(authzid, u, pw);
      } else if (!p.hasCapability("LOGINDISABLED")) {
        p.login(u, pw);
      } else {
        throw new ProtocolException("No login methods supported!");
      }
    }
    if (this.proxyAuthUser != null) {
      p.proxyauth(this.proxyAuthUser);
    }
    if (p.hasCapability("__PRELOGIN__")) {
      try
      {
        p.capability();
      }
      catch (ConnectionException cex)
      {
        throw cex;
      }
      catch (ProtocolException pex) {}
    }
  }
  
  protected void preLogin(IMAPProtocol p)
    throws ProtocolException
  {}
  
  public boolean isSSL()
  {
    return this.usingSSL;
  }
  
  public synchronized void setUsername(String user)
  {
    this.user = user;
  }
  
  public synchronized void setPassword(String password)
  {
    this.password = password;
  }
  
  IMAPProtocol getProtocol(IMAPFolder folder)
    throws MessagingException
  {
    IMAPProtocol p = null;
    while (p == null) {
      synchronized (this.pool)
      {
        if ((this.pool.authenticatedConnections.isEmpty()) || ((this.pool.authenticatedConnections.size() == 1) && ((this.pool.separateStoreConnection) || (this.pool.storeConnectionInUse))))
        {
          this.logger.fine("no connections in the pool, creating a new one");
          try
          {
            if (this.forcePasswordRefresh) {
              refreshPassword();
            }
            p = newIMAPProtocol(this.host, this.port);
            
            login(p, this.user, this.password);
          }
          catch (Exception ex1)
          {
            if (p != null) {
              try
              {
                p.disconnect();
              }
              catch (Exception ex2) {}
            }
            p = null;
          }
          if (p == null) {
            throw new MessagingException("connection failure");
          }
        }
        else
        {
          if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("connection available -- size: " + this.pool.authenticatedConnections.size());
          }
          p = (IMAPProtocol)this.pool.authenticatedConnections.lastElement();
          this.pool.authenticatedConnections.removeElement(p);
          
          long lastUsed = System.currentTimeMillis() - p.getTimestamp();
          if (lastUsed > this.pool.serverTimeoutInterval) {
            try
            {
              p.removeResponseHandler(this);
              p.addResponseHandler(this.nonStoreResponseHandler);
              p.noop();
              p.removeResponseHandler(this.nonStoreResponseHandler);
              p.addResponseHandler(this);
            }
            catch (ProtocolException pex)
            {
              try
              {
                p.removeResponseHandler(this.nonStoreResponseHandler);
                p.disconnect();
              }
              finally
              {
                p = null;
              }
            }
          }
          p.removeResponseHandler(this);
        }
        timeoutConnections();
        if (folder != null)
        {
          if (this.pool.folders == null) {
            this.pool.folders = new Vector();
          }
          this.pool.folders.addElement(folder);
        }
      }
    }
    return p;
  }
  
  private IMAPProtocol getStoreProtocol()
    throws ProtocolException
  {
    IMAPProtocol p = null;
    while (p == null) {
      synchronized (this.pool)
      {
        waitIfIdle();
        if (this.pool.authenticatedConnections.isEmpty())
        {
          this.pool.logger.fine("getStoreProtocol() - no connections in the pool, creating a new one");
          try
          {
            if (this.forcePasswordRefresh) {
              refreshPassword();
            }
            p = newIMAPProtocol(this.host, this.port);
            
            login(p, this.user, this.password);
          }
          catch (Exception ex1)
          {
            if (p != null) {
              try
              {
                p.logout();
              }
              catch (Exception ex2) {}
            }
            p = null;
          }
          if (p == null) {
            throw new ConnectionException("failed to create new store connection");
          }
          p.addResponseHandler(this);
          this.pool.authenticatedConnections.addElement(p);
        }
        else
        {
          if (this.pool.logger.isLoggable(Level.FINE)) {
            this.pool.logger.fine("getStoreProtocol() - connection available -- size: " + this.pool.authenticatedConnections.size());
          }
          p = (IMAPProtocol)this.pool.authenticatedConnections.firstElement();
        }
        if (this.pool.storeConnectionInUse)
        {
          try
          {
            p = null;
            this.pool.wait();
          }
          catch (InterruptedException ex) {}
        }
        else
        {
          this.pool.storeConnectionInUse = true;
          
          this.pool.logger.fine("getStoreProtocol() -- storeConnectionInUse");
        }
        timeoutConnections();
      }
    }
    return p;
  }
  
  IMAPProtocol getFolderStoreProtocol()
    throws ProtocolException
  {
    IMAPProtocol p = getStoreProtocol();
    p.removeResponseHandler(this);
    p.addResponseHandler(this.nonStoreResponseHandler);
    return p;
  }
  
  private void refreshPassword()
  {
    if (this.logger.isLoggable(Level.FINE)) {
      this.logger.fine("refresh password, user: " + traceUser(this.user));
    }
    InetAddress addr;
    try
    {
      addr = InetAddress.getByName(this.host);
    }
    catch (UnknownHostException e)
    {
      addr = null;
    }
    PasswordAuthentication pa = this.session.requestPasswordAuthentication(addr, this.port, this.name, null, this.user);
    if (pa != null)
    {
      this.user = pa.getUserName();
      this.password = pa.getPassword();
    }
  }
  
  boolean allowReadOnlySelect()
  {
    return PropUtil.getBooleanSessionProperty(this.session, "mail." + this.name + ".allowreadonlyselect", false);
  }
  
  boolean hasSeparateStoreConnection()
  {
    return this.pool.separateStoreConnection;
  }
  
  MailLogger getConnectionPoolLogger()
  {
    return this.pool.logger;
  }
  
  boolean getMessageCacheDebug()
  {
    return this.messageCacheDebug;
  }
  
  boolean isConnectionPoolFull()
  {
    synchronized (this.pool)
    {
      if (this.pool.logger.isLoggable(Level.FINE)) {
        this.pool.logger.fine("connection pool current size: " + this.pool.authenticatedConnections.size() + "   pool size: " + this.pool.poolSize);
      }
      return this.pool.authenticatedConnections.size() >= this.pool.poolSize;
    }
  }
  
  void releaseProtocol(IMAPFolder folder, IMAPProtocol protocol)
  {
    synchronized (this.pool)
    {
      if (protocol != null) {
        if (!isConnectionPoolFull())
        {
          protocol.addResponseHandler(this);
          this.pool.authenticatedConnections.addElement(protocol);
          if (this.logger.isLoggable(Level.FINE)) {
            this.logger.fine("added an Authenticated connection -- size: " + this.pool.authenticatedConnections.size());
          }
        }
        else
        {
          this.logger.fine("pool is full, not adding an Authenticated connection");
          try
          {
            protocol.logout();
          }
          catch (ProtocolException pex) {}
        }
      }
      if (this.pool.folders != null) {
        this.pool.folders.removeElement(folder);
      }
      timeoutConnections();
    }
  }
  
  private void releaseStoreProtocol(IMAPProtocol protocol)
  {
    if (protocol == null)
    {
      cleanup(); return;
    }
    boolean failed;
    synchronized (this.connectionFailedLock)
    {
      failed = this.connectionFailed;
      this.connectionFailed = false;
    }
    synchronized (this.pool)
    {
      this.pool.storeConnectionInUse = false;
      this.pool.notifyAll();
      
      this.pool.logger.fine("releaseStoreProtocol()");
      
      timeoutConnections();
    }
    assert (!Thread.holdsLock(this.pool));
    if (failed) {
      cleanup();
    }
  }
  
  void releaseFolderStoreProtocol(IMAPProtocol protocol)
  {
    if (protocol == null) {
      return;
    }
    protocol.removeResponseHandler(this.nonStoreResponseHandler);
    protocol.addResponseHandler(this);
    synchronized (this.pool)
    {
      this.pool.storeConnectionInUse = false;
      this.pool.notifyAll();
      
      this.pool.logger.fine("releaseFolderStoreProtocol()");
      
      timeoutConnections();
    }
  }
  
  private void emptyConnectionPool(boolean force)
  {
    synchronized (this.pool)
    {
      for (int index = this.pool.authenticatedConnections.size() - 1; index >= 0; index--) {
        try
        {
          IMAPProtocol p = (IMAPProtocol)this.pool.authenticatedConnections.elementAt(index);
          
          p.removeResponseHandler(this);
          if (force) {
            p.disconnect();
          } else {
            p.logout();
          }
        }
        catch (ProtocolException pex) {}
      }
      this.pool.authenticatedConnections.removeAllElements();
    }
    this.pool.logger.fine("removed all authenticated connections from pool");
  }
  
  private void timeoutConnections()
  {
    synchronized (this.pool)
    {
      if ((System.currentTimeMillis() - this.pool.lastTimePruned > this.pool.pruningInterval) && (this.pool.authenticatedConnections.size() > 1))
      {
        if (this.pool.logger.isLoggable(Level.FINE))
        {
          this.pool.logger.fine("checking for connections to prune: " + (System.currentTimeMillis() - this.pool.lastTimePruned));
          
          this.pool.logger.fine("clientTimeoutInterval: " + this.pool.clientTimeoutInterval);
        }
        for (int index = this.pool.authenticatedConnections.size() - 1; index > 0; index--)
        {
          IMAPProtocol p = (IMAPProtocol)this.pool.authenticatedConnections.elementAt(index);
          if (this.pool.logger.isLoggable(Level.FINE)) {
            this.pool.logger.fine("protocol last used: " + (System.currentTimeMillis() - p.getTimestamp()));
          }
          if (System.currentTimeMillis() - p.getTimestamp() > this.pool.clientTimeoutInterval)
          {
            this.pool.logger.fine("authenticated connection timed out, logging out the connection");
            
            p.removeResponseHandler(this);
            this.pool.authenticatedConnections.removeElementAt(index);
            try
            {
              p.logout();
            }
            catch (ProtocolException pex) {}
          }
        }
        this.pool.lastTimePruned = System.currentTimeMillis();
      }
    }
  }
  
  int getFetchBlockSize()
  {
    return this.blksize;
  }
  
  boolean ignoreBodyStructureSize()
  {
    return this.ignoreSize;
  }
  
  Session getSession()
  {
    return this.session;
  }
  
  int getStatusCacheTimeout()
  {
    return this.statusCacheTimeout;
  }
  
  int getAppendBufferSize()
  {
    return this.appendBufferSize;
  }
  
  int getMinIdleTime()
  {
    return this.minIdleTime;
  }
  
  public synchronized boolean hasCapability(String capability)
    throws MessagingException
  {
    IMAPProtocol p = null;
    try
    {
      p = getStoreProtocol();
      return p.hasCapability(capability);
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    finally
    {
      releaseStoreProtocol(p);
    }
  }
  
  public synchronized boolean isConnected()
  {
    if (!super.isConnected()) {
      return false;
    }
    IMAPProtocol p = null;
    try
    {
      p = getStoreProtocol();
      p.noop();
    }
    catch (ProtocolException pex) {}finally
    {
      releaseStoreProtocol(p);
    }
    return super.isConnected();
  }
  
  public synchronized void close()
    throws MessagingException
  {
    if (!super.isConnected()) {
      return;
    }
    IMAPProtocol protocol = null;
    try
    {
      boolean isEmpty;
      synchronized (this.pool)
      {
        isEmpty = this.pool.authenticatedConnections.isEmpty();
      }
      if (isEmpty)
      {
        this.pool.logger.fine("close() - no connections ");
        cleanup();
        return;
      }
      protocol = getStoreProtocol();
      synchronized (this.pool)
      {
        this.pool.authenticatedConnections.removeElement(protocol);
      }
      protocol.logout();
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    finally
    {
      releaseStoreProtocol(protocol);
    }
  }
  
  protected void finalize()
    throws Throwable
  {
    super.finalize();
    close();
  }
  
  private synchronized void cleanup()
  {
    if (!super.isConnected())
    {
      this.logger.fine("IMAPStore cleanup, not connected"); return;
    }
    boolean force;
    synchronized (this.connectionFailedLock)
    {
      force = this.forceClose;
      this.forceClose = false;
      this.connectionFailed = false;
    }
    if (this.logger.isLoggable(Level.FINE)) {
      this.logger.fine("IMAPStore cleanup, force " + force);
    }
    Vector foldersCopy = null;
    boolean done = true;
    for (;;)
    {
      synchronized (this.pool)
      {
        if (this.pool.folders != null)
        {
          done = false;
          foldersCopy = this.pool.folders;
          this.pool.folders = null;
        }
        else
        {
          done = true;
        }
      }
      if (done) {
        break;
      }
      int i = 0;
      for (int fsize = foldersCopy.size(); i < fsize; i++)
      {
        IMAPFolder f = (IMAPFolder)foldersCopy.elementAt(i);
        try
        {
          if (force)
          {
            this.logger.fine("force folder to close");
            
            f.forceClose();
          }
          else
          {
            this.logger.fine("close folder");
            f.close(false);
          }
        }
        catch (MessagingException mex) {}catch (IllegalStateException ex) {}
      }
    }
    synchronized (this.pool)
    {
      emptyConnectionPool(force);
    }
    try
    {
      super.close();
    }
    catch (MessagingException mex) {}
    this.logger.fine("IMAPStore cleanup done");
  }
  
  public synchronized Folder getDefaultFolder()
    throws MessagingException
  {
    checkConnected();
    return new DefaultFolder(this);
  }
  
  public synchronized Folder getFolder(String name)
    throws MessagingException
  {
    checkConnected();
    return newIMAPFolder(name, 65535);
  }
  
  public synchronized Folder getFolder(URLName url)
    throws MessagingException
  {
    checkConnected();
    return newIMAPFolder(url.getFile(), 65535);
  }
  
  protected IMAPFolder newIMAPFolder(String fullName, char separator, Boolean isNamespace)
  {
    IMAPFolder f = null;
    if (this.folderConstructor != null) {
      try
      {
        Object[] o = { fullName, new Character(separator), this, isNamespace };
        
        f = (IMAPFolder)this.folderConstructor.newInstance(o);
      }
      catch (Exception ex)
      {
        this.logger.log(Level.FINE, "exception creating IMAPFolder class", ex);
      }
    }
    if (f == null) {
      f = new IMAPFolder(fullName, separator, this, isNamespace);
    }
    return f;
  }
  
  protected IMAPFolder newIMAPFolder(String fullName, char separator)
  {
    return newIMAPFolder(fullName, separator, null);
  }
  
  protected IMAPFolder newIMAPFolder(ListInfo li)
  {
    IMAPFolder f = null;
    if (this.folderConstructorLI != null) {
      try
      {
        Object[] o = { li, this };
        f = (IMAPFolder)this.folderConstructorLI.newInstance(o);
      }
      catch (Exception ex)
      {
        this.logger.log(Level.FINE, "exception creating IMAPFolder class LI", ex);
      }
    }
    if (f == null) {
      f = new IMAPFolder(li, this);
    }
    return f;
  }
  
  public Folder[] getPersonalNamespaces()
    throws MessagingException
  {
    Namespaces ns = getNamespaces();
    if ((ns == null) || (ns.personal == null)) {
      return super.getPersonalNamespaces();
    }
    return namespaceToFolders(ns.personal, null);
  }
  
  public Folder[] getUserNamespaces(String user)
    throws MessagingException
  {
    Namespaces ns = getNamespaces();
    if ((ns == null) || (ns.otherUsers == null)) {
      return super.getUserNamespaces(user);
    }
    return namespaceToFolders(ns.otherUsers, user);
  }
  
  public Folder[] getSharedNamespaces()
    throws MessagingException
  {
    Namespaces ns = getNamespaces();
    if ((ns == null) || (ns.shared == null)) {
      return super.getSharedNamespaces();
    }
    return namespaceToFolders(ns.shared, null);
  }
  
  private synchronized Namespaces getNamespaces()
    throws MessagingException
  {
    checkConnected();
    
    IMAPProtocol p = null;
    if (this.namespaces == null) {
      try
      {
        p = getStoreProtocol();
        this.namespaces = p.namespace();
      }
      catch (BadCommandException bex) {}catch (ConnectionException cex)
      {
        throw new StoreClosedException(this, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
      finally
      {
        releaseStoreProtocol(p);
      }
    }
    return this.namespaces;
  }
  
  private Folder[] namespaceToFolders(Namespaces.Namespace[] ns, String user)
  {
    Folder[] fa = new Folder[ns.length];
    for (int i = 0; i < fa.length; i++)
    {
      String name = ns[i].prefix;
      if (user == null)
      {
        int len = name.length();
        if ((len > 0) && (name.charAt(len - 1) == ns[i].delimiter)) {
          name = name.substring(0, len - 1);
        }
      }
      else
      {
        name = name + user;
      }
      fa[i] = newIMAPFolder(name, ns[i].delimiter, Boolean.valueOf(user == null ? 1 : false));
    }
    return fa;
  }
  
  public synchronized Quota[] getQuota(String root)
    throws MessagingException
  {
    checkConnected();
    Quota[] qa = null;
    
    IMAPProtocol p = null;
    try
    {
      p = getStoreProtocol();
      qa = p.getQuotaRoot(root);
    }
    catch (BadCommandException bex)
    {
      throw new MessagingException("QUOTA not supported", bex);
    }
    catch (ConnectionException cex)
    {
      throw new StoreClosedException(this, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    finally
    {
      releaseStoreProtocol(p);
    }
    return qa;
  }
  
  public synchronized void setQuota(Quota quota)
    throws MessagingException
  {
    checkConnected();
    IMAPProtocol p = null;
    try
    {
      p = getStoreProtocol();
      p.setQuota(quota);
    }
    catch (BadCommandException bex)
    {
      throw new MessagingException("QUOTA not supported", bex);
    }
    catch (ConnectionException cex)
    {
      throw new StoreClosedException(this, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    finally
    {
      releaseStoreProtocol(p);
    }
  }
  
  private void checkConnected()
  {
    assert (Thread.holdsLock(this));
    if (!super.isConnected()) {
      throw new IllegalStateException("Not connected");
    }
  }
  
  public void handleResponse(Response r)
  {
    if ((r.isOK()) || (r.isNO()) || (r.isBAD()) || (r.isBYE())) {
      handleResponseCode(r);
    }
    if (r.isBYE())
    {
      this.logger.fine("IMAPStore connection dead");
      synchronized (this.connectionFailedLock)
      {
        this.connectionFailed = true;
        if (r.isSynthetic()) {
          this.forceClose = true;
        }
      }
      return;
    }
  }
  
  /* Error */
  public void idle()
    throws MessagingException
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_1
    //   2: getstatic 254	com/sun/mail/imap/IMAPStore:$assertionsDisabled	Z
    //   5: ifne +21 -> 26
    //   8: aload_0
    //   9: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   12: invokestatic 255	java/lang/Thread:holdsLock	(Ljava/lang/Object;)Z
    //   15: ifeq +11 -> 26
    //   18: new 256	java/lang/AssertionError
    //   21: dup
    //   22: invokespecial 257	java/lang/AssertionError:<init>	()V
    //   25: athrow
    //   26: aload_0
    //   27: dup
    //   28: astore_2
    //   29: monitorenter
    //   30: aload_0
    //   31: invokespecial 288	com/sun/mail/imap/IMAPStore:checkConnected	()V
    //   34: aload_2
    //   35: monitorexit
    //   36: goto +8 -> 44
    //   39: astore_3
    //   40: aload_2
    //   41: monitorexit
    //   42: aload_3
    //   43: athrow
    //   44: aload_0
    //   45: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   48: dup
    //   49: astore_2
    //   50: monitorenter
    //   51: aload_0
    //   52: invokespecial 237	com/sun/mail/imap/IMAPStore:getStoreProtocol	()Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   55: astore_1
    //   56: aload_0
    //   57: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   60: invokestatic 334	com/sun/mail/imap/IMAPStore$ConnectionPool:access$1000	(Lcom/sun/mail/imap/IMAPStore$ConnectionPool;)I
    //   63: ifne +19 -> 82
    //   66: aload_1
    //   67: invokevirtual 335	com/sun/mail/imap/protocol/IMAPProtocol:idleStart	()V
    //   70: aload_0
    //   71: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   74: iconst_1
    //   75: invokestatic 336	com/sun/mail/imap/IMAPStore$ConnectionPool:access$1002	(Lcom/sun/mail/imap/IMAPStore$ConnectionPool;I)I
    //   78: pop
    //   79: goto +20 -> 99
    //   82: aload_0
    //   83: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   86: invokevirtual 233	java/lang/Object:wait	()V
    //   89: goto +4 -> 93
    //   92: astore_3
    //   93: aload_2
    //   94: monitorexit
    //   95: jsr +183 -> 278
    //   98: return
    //   99: aload_0
    //   100: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   103: aload_1
    //   104: invokestatic 337	com/sun/mail/imap/IMAPStore$ConnectionPool:access$1102	(Lcom/sun/mail/imap/IMAPStore$ConnectionPool;Lcom/sun/mail/imap/protocol/IMAPProtocol;)Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   107: pop
    //   108: aload_2
    //   109: monitorexit
    //   110: goto +10 -> 120
    //   113: astore 4
    //   115: aload_2
    //   116: monitorexit
    //   117: aload 4
    //   119: athrow
    //   120: aload_1
    //   121: invokevirtual 338	com/sun/mail/imap/protocol/IMAPProtocol:readIdleResponse	()Lcom/sun/mail/iap/Response;
    //   124: astore_2
    //   125: aload_0
    //   126: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   129: dup
    //   130: astore_3
    //   131: monitorenter
    //   132: aload_2
    //   133: ifnull +11 -> 144
    //   136: aload_1
    //   137: aload_2
    //   138: invokevirtual 339	com/sun/mail/imap/protocol/IMAPProtocol:processIdleResponse	(Lcom/sun/mail/iap/Response;)Z
    //   141: ifne +24 -> 165
    //   144: aload_0
    //   145: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   148: iconst_0
    //   149: invokestatic 336	com/sun/mail/imap/IMAPStore$ConnectionPool:access$1002	(Lcom/sun/mail/imap/IMAPStore$ConnectionPool;I)I
    //   152: pop
    //   153: aload_0
    //   154: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   157: invokevirtual 252	java/lang/Object:notifyAll	()V
    //   160: aload_3
    //   161: monitorexit
    //   162: goto +43 -> 205
    //   165: aload_3
    //   166: monitorexit
    //   167: goto +10 -> 177
    //   170: astore 5
    //   172: aload_3
    //   173: monitorexit
    //   174: aload 5
    //   176: athrow
    //   177: aload_0
    //   178: getfield 18	com/sun/mail/imap/IMAPStore:enableImapEvents	Z
    //   181: ifeq +21 -> 202
    //   184: aload_2
    //   185: invokevirtual 340	com/sun/mail/iap/Response:isUnTagged	()Z
    //   188: ifeq +14 -> 202
    //   191: aload_0
    //   192: sipush 1000
    //   195: aload_2
    //   196: invokevirtual 341	com/sun/mail/iap/Response:toString	()Ljava/lang/String;
    //   199: invokevirtual 342	com/sun/mail/imap/IMAPStore:notifyStoreListeners	(ILjava/lang/String;)V
    //   202: goto -82 -> 120
    //   205: aload_0
    //   206: invokevirtual 343	com/sun/mail/imap/IMAPStore:getMinIdleTime	()I
    //   209: istore_2
    //   210: iload_2
    //   211: ifle +12 -> 223
    //   214: iload_2
    //   215: i2l
    //   216: invokestatic 344	java/lang/Thread:sleep	(J)V
    //   219: goto +4 -> 223
    //   222: astore_3
    //   223: jsr +55 -> 278
    //   226: goto +92 -> 318
    //   229: astore_2
    //   230: new 173	javax/mail/MessagingException
    //   233: dup
    //   234: ldc_w 345
    //   237: aload_2
    //   238: invokespecial 175	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   241: athrow
    //   242: astore_2
    //   243: new 313	javax/mail/StoreClosedException
    //   246: dup
    //   247: aload_0
    //   248: aload_2
    //   249: invokevirtual 314	com/sun/mail/iap/ConnectionException:getMessage	()Ljava/lang/String;
    //   252: invokespecial 315	javax/mail/StoreClosedException:<init>	(Ljavax/mail/Store;Ljava/lang/String;)V
    //   255: athrow
    //   256: astore_2
    //   257: new 173	javax/mail/MessagingException
    //   260: dup
    //   261: aload_2
    //   262: invokevirtual 174	com/sun/mail/iap/ProtocolException:getMessage	()Ljava/lang/String;
    //   265: aload_2
    //   266: invokespecial 175	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   269: athrow
    //   270: astore 6
    //   272: jsr +6 -> 278
    //   275: aload 6
    //   277: athrow
    //   278: astore 7
    //   280: aload_0
    //   281: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   284: dup
    //   285: astore 8
    //   287: monitorenter
    //   288: aload_0
    //   289: getfield 143	com/sun/mail/imap/IMAPStore:pool	Lcom/sun/mail/imap/IMAPStore$ConnectionPool;
    //   292: aconst_null
    //   293: invokestatic 337	com/sun/mail/imap/IMAPStore$ConnectionPool:access$1102	(Lcom/sun/mail/imap/IMAPStore$ConnectionPool;Lcom/sun/mail/imap/protocol/IMAPProtocol;)Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   296: pop
    //   297: aload 8
    //   299: monitorexit
    //   300: goto +11 -> 311
    //   303: astore 9
    //   305: aload 8
    //   307: monitorexit
    //   308: aload 9
    //   310: athrow
    //   311: aload_0
    //   312: aload_1
    //   313: invokespecial 272	com/sun/mail/imap/IMAPStore:releaseStoreProtocol	(Lcom/sun/mail/imap/protocol/IMAPProtocol;)V
    //   316: ret 7
    //   318: return
    // Line number table:
    //   Java source line #1793	-> byte code offset #0
    //   Java source line #1796	-> byte code offset #2
    //   Java source line #1797	-> byte code offset #26
    //   Java source line #1798	-> byte code offset #30
    //   Java source line #1799	-> byte code offset #34
    //   Java source line #1801	-> byte code offset #44
    //   Java source line #1802	-> byte code offset #51
    //   Java source line #1803	-> byte code offset #56
    //   Java source line #1804	-> byte code offset #66
    //   Java source line #1805	-> byte code offset #70
    //   Java source line #1812	-> byte code offset #82
    //   Java source line #1813	-> byte code offset #89
    //   Java source line #1814	-> byte code offset #93
    //   Java source line #1816	-> byte code offset #99
    //   Java source line #1817	-> byte code offset #108
    //   Java source line #1833	-> byte code offset #120
    //   Java source line #1834	-> byte code offset #125
    //   Java source line #1835	-> byte code offset #132
    //   Java source line #1836	-> byte code offset #144
    //   Java source line #1837	-> byte code offset #153
    //   Java source line #1838	-> byte code offset #160
    //   Java source line #1840	-> byte code offset #165
    //   Java source line #1841	-> byte code offset #177
    //   Java source line #1842	-> byte code offset #191
    //   Java source line #1844	-> byte code offset #202
    //   Java source line #1851	-> byte code offset #205
    //   Java source line #1852	-> byte code offset #210
    //   Java source line #1854	-> byte code offset #214
    //   Java source line #1855	-> byte code offset #219
    //   Java source line #1858	-> byte code offset #223
    //   Java source line #1869	-> byte code offset #226
    //   Java source line #1858	-> byte code offset #229
    //   Java source line #1859	-> byte code offset #230
    //   Java source line #1860	-> byte code offset #242
    //   Java source line #1861	-> byte code offset #243
    //   Java source line #1862	-> byte code offset #256
    //   Java source line #1863	-> byte code offset #257
    //   Java source line #1865	-> byte code offset #270
    //   Java source line #1866	-> byte code offset #288
    //   Java source line #1867	-> byte code offset #297
    //   Java source line #1868	-> byte code offset #311
    //   Java source line #1870	-> byte code offset #318
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	319	0	this	IMAPStore
    //   1	312	1	p	IMAPProtocol
    //   28	13	2	Ljava/lang/Object;	Object
    //   124	72	2	r	Response
    //   209	6	2	minidle	int
    //   229	9	2	bex	BadCommandException
    //   242	7	2	cex	ConnectionException
    //   256	10	2	pex	ProtocolException
    //   39	4	3	localObject1	Object
    //   92	2	3	ex	InterruptedException
    //   222	2	3	ex	InterruptedException
    //   113	5	4	localObject2	Object
    //   170	5	5	localObject3	Object
    //   270	6	6	localObject4	Object
    //   278	1	7	localObject5	Object
    //   303	6	9	localObject6	Object
    // Exception table:
    //   from	to	target	type
    //   30	36	39	finally
    //   39	42	39	finally
    //   82	89	92	java/lang/InterruptedException
    //   51	95	113	finally
    //   99	110	113	finally
    //   113	117	113	finally
    //   132	162	170	finally
    //   165	167	170	finally
    //   170	174	170	finally
    //   214	219	222	java/lang/InterruptedException
    //   44	98	229	com/sun/mail/iap/BadCommandException
    //   99	223	229	com/sun/mail/iap/BadCommandException
    //   44	98	242	com/sun/mail/iap/ConnectionException
    //   99	223	242	com/sun/mail/iap/ConnectionException
    //   44	98	256	com/sun/mail/iap/ProtocolException
    //   99	223	256	com/sun/mail/iap/ProtocolException
    //   44	98	270	finally
    //   99	226	270	finally
    //   229	275	270	finally
    //   288	300	303	finally
    //   303	308	303	finally
  }
  
  private void waitIfIdle()
    throws ProtocolException
  {
    assert (Thread.holdsLock(this.pool));
    while (this.pool.idleState != 0)
    {
      if (this.pool.idleState == 1)
      {
        this.pool.idleProtocol.idleAbort();
        this.pool.idleState = 2;
      }
      try
      {
        this.pool.wait();
      }
      catch (InterruptedException ex) {}
    }
  }
  
  void handleResponseCode(Response r)
  {
    String s = r.getRest();
    boolean isAlert = false;
    if (s.startsWith("["))
    {
      int i = s.indexOf(']');
      if ((i > 0) && (s.substring(0, i + 1).equalsIgnoreCase("[ALERT]"))) {
        isAlert = true;
      }
      s = s.substring(i + 1).trim();
    }
    if (isAlert) {
      notifyStoreListeners(1, s);
    } else if ((r.isUnTagged()) && (s.length() > 0)) {
      notifyStoreListeners(2, s);
    }
  }
  
  private String traceUser(String user)
  {
    return this.debugusername ? user : "<user name suppressed>";
  }
  
  private String tracePassword(String password)
  {
    return password == null ? "<null>" : this.debugpassword ? password : "<non-null>";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\IMAPStore.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */