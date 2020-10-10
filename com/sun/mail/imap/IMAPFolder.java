package com.sun.mail.imap;

import com.sun.mail.iap.BadCommandException;
import com.sun.mail.iap.CommandFailedException;
import com.sun.mail.iap.ConnectionException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import com.sun.mail.iap.ResponseHandler;
import com.sun.mail.imap.protocol.FetchItem;
import com.sun.mail.imap.protocol.FetchResponse;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.IMAPResponse;
import com.sun.mail.imap.protocol.Item;
import com.sun.mail.imap.protocol.ListInfo;
import com.sun.mail.imap.protocol.MailboxInfo;
import com.sun.mail.imap.protocol.MessageSet;
import com.sun.mail.imap.protocol.Status;
import com.sun.mail.imap.protocol.UID;
import com.sun.mail.util.MailLogger;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.StoreClosedException;
import javax.mail.UIDFolder;
import javax.mail.UIDFolder.FetchProfileItem;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchException;
import javax.mail.search.SearchTerm;

public class IMAPFolder
  extends Folder
  implements UIDFolder, ResponseHandler
{
  protected String fullName;
  protected String name;
  protected int type;
  protected char separator;
  protected Flags availableFlags;
  protected Flags permanentFlags;
  protected volatile boolean exists;
  protected boolean isNamespace = false;
  protected volatile String[] attributes;
  protected volatile IMAPProtocol protocol;
  protected MessageCache messageCache;
  protected final Object messageCacheLock = new Object();
  protected Hashtable uidTable;
  protected static final char UNKNOWN_SEPARATOR = '￿';
  private volatile boolean opened = false;
  private boolean reallyClosed = true;
  private static final int RUNNING = 0;
  private static final int IDLE = 1;
  private static final int ABORTING = 2;
  private int idleState = 0;
  private volatile int total = -1;
  private volatile int recent = -1;
  private int realTotal = -1;
  private long uidvalidity = -1L;
  private long uidnext = -1L;
  private boolean doExpungeNotification = true;
  private Status cachedStatus = null;
  private long cachedStatusTime = 0L;
  private boolean hasMessageCountListener = false;
  protected MailLogger logger;
  private MailLogger connectionPoolLogger;
  
  public static abstract interface ProtocolCommand
  {
    public abstract Object doCommand(IMAPProtocol paramIMAPProtocol)
      throws ProtocolException;
  }
  
  public static class FetchProfileItem
    extends FetchProfile.Item
  {
    protected FetchProfileItem(String name)
    {
      super();
    }
    
    public static final FetchProfileItem HEADERS = new FetchProfileItem("HEADERS");
    public static final FetchProfileItem SIZE = new FetchProfileItem("SIZE");
  }
  
  protected IMAPFolder(String fullName, char separator, IMAPStore store, Boolean isNamespace)
  {
    super(store);
    if (fullName == null) {
      throw new NullPointerException("Folder name is null");
    }
    this.fullName = fullName;
    this.separator = separator;
    this.logger = new MailLogger(getClass(), "DEBUG IMAP", store.getSession());
    
    this.connectionPoolLogger = store.getConnectionPoolLogger();
    
    this.isNamespace = false;
    if ((separator != 65535) && (separator != 0))
    {
      int i = this.fullName.indexOf(separator);
      if ((i > 0) && (i == this.fullName.length() - 1))
      {
        this.fullName = this.fullName.substring(0, i);
        this.isNamespace = true;
      }
    }
    if (isNamespace != null) {
      this.isNamespace = isNamespace.booleanValue();
    }
  }
  
  protected IMAPFolder(ListInfo li, IMAPStore store)
  {
    this(li.name, li.separator, store, null);
    if (li.hasInferiors) {
      this.type |= 0x2;
    }
    if (li.canOpen) {
      this.type |= 0x1;
    }
    this.exists = true;
    this.attributes = li.attrs;
  }
  
  protected void checkExists()
    throws MessagingException
  {
    if ((!this.exists) && (!exists())) {
      throw new FolderNotFoundException(this, this.fullName + " not found");
    }
  }
  
  protected void checkClosed()
  {
    if (this.opened) {
      throw new IllegalStateException("This operation is not allowed on an open folder");
    }
  }
  
  protected void checkOpened()
    throws FolderClosedException
  {
    assert (Thread.holdsLock(this));
    if (!this.opened)
    {
      if (this.reallyClosed) {
        throw new IllegalStateException("This operation is not allowed on a closed folder");
      }
      throw new FolderClosedException(this, "Lost folder connection to server");
    }
  }
  
  protected void checkRange(int msgno)
    throws MessagingException
  {
    if (msgno < 1) {
      throw new IndexOutOfBoundsException("message number < 1");
    }
    if (msgno <= this.total) {
      return;
    }
    synchronized (this.messageCacheLock)
    {
      try
      {
        keepConnectionAlive(false);
      }
      catch (ConnectionException cex)
      {
        throw new FolderClosedException(this, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
    if (msgno > this.total) {
      throw new IndexOutOfBoundsException(msgno + " > " + this.total);
    }
  }
  
  private void checkFlags(Flags flags)
    throws MessagingException
  {
    assert (Thread.holdsLock(this));
    if (this.mode != 2) {
      throw new IllegalStateException("Cannot change flags on READ_ONLY folder: " + this.fullName);
    }
  }
  
  public synchronized String getName()
  {
    if (this.name == null) {
      try
      {
        this.name = this.fullName.substring(this.fullName.lastIndexOf(getSeparator()) + 1);
      }
      catch (MessagingException mex) {}
    }
    return this.name;
  }
  
  public synchronized String getFullName()
  {
    return this.fullName;
  }
  
  public synchronized Folder getParent()
    throws MessagingException
  {
    char c = getSeparator();
    int index;
    if ((index = this.fullName.lastIndexOf(c)) != -1) {
      return ((IMAPStore)this.store).newIMAPFolder(this.fullName.substring(0, index), c);
    }
    return new DefaultFolder((IMAPStore)this.store);
  }
  
  public synchronized boolean exists()
    throws MessagingException
  {
    ListInfo[] li = null;
    String lname;
    final String lname;
    if ((this.isNamespace) && (this.separator != 0)) {
      lname = this.fullName + this.separator;
    } else {
      lname = this.fullName;
    }
    li = (ListInfo[])doCommand(new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        return p.list("", lname);
      }
    });
    if (li != null)
    {
      int i = findName(li, lname);
      this.fullName = li[i].name;
      this.separator = li[i].separator;
      int len = this.fullName.length();
      if ((this.separator != 0) && (len > 0) && (this.fullName.charAt(len - 1) == this.separator)) {
        this.fullName = this.fullName.substring(0, len - 1);
      }
      this.type = 0;
      if (li[i].hasInferiors) {
        this.type |= 0x2;
      }
      if (li[i].canOpen) {
        this.type |= 0x1;
      }
      this.exists = true;
      this.attributes = li[i].attrs;
    }
    else
    {
      this.exists = this.opened;
      this.attributes = null;
    }
    return this.exists;
  }
  
  private int findName(ListInfo[] li, String lname)
  {
    for (int i = 0; i < li.length; i++) {
      if (li[i].name.equals(lname)) {
        break;
      }
    }
    if (i >= li.length) {
      i = 0;
    }
    return i;
  }
  
  public Folder[] list(String pattern)
    throws MessagingException
  {
    return doList(pattern, false);
  }
  
  public Folder[] listSubscribed(String pattern)
    throws MessagingException
  {
    return doList(pattern, true);
  }
  
  private synchronized Folder[] doList(final String pattern, final boolean subscribed)
    throws MessagingException
  {
    checkExists();
    if ((this.attributes != null) && (!isDirectory())) {
      return new Folder[0];
    }
    final char c = getSeparator();
    
    ListInfo[] li = (ListInfo[])doCommandIgnoreFailure(new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        if (subscribed) {
          return p.lsub("", IMAPFolder.this.fullName + c + pattern);
        }
        return p.list("", IMAPFolder.this.fullName + c + pattern);
      }
    });
    if (li == null) {
      return new Folder[0];
    }
    int start = 0;
    if ((li.length > 0) && (li[0].name.equals(this.fullName + c))) {
      start = 1;
    }
    IMAPFolder[] folders = new IMAPFolder[li.length - start];
    IMAPStore st = (IMAPStore)this.store;
    for (int i = start; i < li.length; i++) {
      folders[(i - start)] = st.newIMAPFolder(li[i]);
    }
    return folders;
  }
  
  public synchronized char getSeparator()
    throws MessagingException
  {
    if (this.separator == 65535)
    {
      ListInfo[] li = null;
      
      li = (ListInfo[])doCommand(new ProtocolCommand()
      {
        public Object doCommand(IMAPProtocol p)
          throws ProtocolException
        {
          if (p.isREV1()) {
            return p.list(IMAPFolder.this.fullName, "");
          }
          return p.list("", IMAPFolder.this.fullName);
        }
      });
      if (li != null) {
        this.separator = li[0].separator;
      } else {
        this.separator = '/';
      }
    }
    return this.separator;
  }
  
  public synchronized int getType()
    throws MessagingException
  {
    if (this.opened)
    {
      if (this.attributes == null) {
        exists();
      }
    }
    else {
      checkExists();
    }
    return this.type;
  }
  
  public synchronized boolean isSubscribed()
  {
    ListInfo[] li = null;
    String lname;
    final String lname;
    if ((this.isNamespace) && (this.separator != 0)) {
      lname = this.fullName + this.separator;
    } else {
      lname = this.fullName;
    }
    try
    {
      li = (ListInfo[])doProtocolCommand(new ProtocolCommand()
      {
        public Object doCommand(IMAPProtocol p)
          throws ProtocolException
        {
          return p.lsub("", lname);
        }
      });
    }
    catch (ProtocolException pex) {}
    if (li != null)
    {
      int i = findName(li, lname);
      return li[i].canOpen;
    }
    return false;
  }
  
  public synchronized void setSubscribed(final boolean subscribe)
    throws MessagingException
  {
    doCommandIgnoreFailure(new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        if (subscribe) {
          p.subscribe(IMAPFolder.this.fullName);
        } else {
          p.unsubscribe(IMAPFolder.this.fullName);
        }
        return null;
      }
    });
  }
  
  public synchronized boolean create(final int type)
    throws MessagingException
  {
    char c = '\000';
    if ((type & 0x1) == 0) {
      c = getSeparator();
    }
    final char sep = c;
    Object ret = doCommandIgnoreFailure(new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        if ((type & 0x1) == 0)
        {
          p.create(IMAPFolder.this.fullName + sep);
        }
        else
        {
          p.create(IMAPFolder.this.fullName);
          if ((type & 0x2) != 0)
          {
            ListInfo[] li = p.list("", IMAPFolder.this.fullName);
            if ((li != null) && (!li[0].hasInferiors))
            {
              p.delete(IMAPFolder.this.fullName);
              throw new ProtocolException("Unsupported type");
            }
          }
        }
        return Boolean.TRUE;
      }
    });
    if (ret == null) {
      return false;
    }
    boolean retb = exists();
    if (retb) {
      notifyFolderListeners(1);
    }
    return retb;
  }
  
  public synchronized boolean hasNewMessages()
    throws MessagingException
  {
    if (this.opened) {
      synchronized (this.messageCacheLock)
      {
        try
        {
          keepConnectionAlive(true);
        }
        catch (ConnectionException cex)
        {
          throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex)
        {
          throw new MessagingException(pex.getMessage(), pex);
        }
        return this.recent > 0;
      }
    }
    ListInfo[] li = null;
    String lname;
    final String lname;
    if ((this.isNamespace) && (this.separator != 0)) {
      lname = this.fullName + this.separator;
    } else {
      lname = this.fullName;
    }
    li = (ListInfo[])doCommandIgnoreFailure(new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        return p.list("", lname);
      }
    });
    if (li == null) {
      throw new FolderNotFoundException(this, this.fullName + " not found");
    }
    int i = findName(li, lname);
    if (li[i].changeState == 1) {
      return true;
    }
    if (li[i].changeState == 2) {
      return false;
    }
    try
    {
      Status status = getStatus();
      if (status.recent > 0) {
        return true;
      }
      return false;
    }
    catch (BadCommandException bex)
    {
      return false;
    }
    catch (ConnectionException cex)
    {
      throw new StoreClosedException(this.store, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
  }
  
  public synchronized Folder getFolder(String name)
    throws MessagingException
  {
    if ((this.attributes != null) && (!isDirectory())) {
      throw new MessagingException("Cannot contain subfolders");
    }
    char c = getSeparator();
    return ((IMAPStore)this.store).newIMAPFolder(this.fullName + c + name, c);
  }
  
  public synchronized boolean delete(boolean recurse)
    throws MessagingException
  {
    checkClosed();
    if (recurse)
    {
      Folder[] f = list();
      for (int i = 0; i < f.length; i++) {
        f[i].delete(recurse);
      }
    }
    Object ret = doCommandIgnoreFailure(new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        p.delete(IMAPFolder.this.fullName);
        return Boolean.TRUE;
      }
    });
    if (ret == null) {
      return false;
    }
    this.exists = false;
    this.attributes = null;
    
    notifyFolderListeners(2);
    return true;
  }
  
  public synchronized boolean renameTo(final Folder f)
    throws MessagingException
  {
    checkClosed();
    checkExists();
    if (f.getStore() != this.store) {
      throw new MessagingException("Can't rename across Stores");
    }
    Object ret = doCommandIgnoreFailure(new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        p.rename(IMAPFolder.this.fullName, f.getFullName());
        return Boolean.TRUE;
      }
    });
    if (ret == null) {
      return false;
    }
    this.exists = false;
    this.attributes = null;
    notifyFolderRenamedListeners(f);
    return true;
  }
  
  public synchronized void open(int mode)
    throws MessagingException
  {
    checkClosed();
    
    MailboxInfo mi = null;
    
    this.protocol = ((IMAPStore)this.store).getProtocol(this);
    synchronized (this.messageCacheLock)
    {
      this.protocol.addResponseHandler(this);
      try
      {
        if (mode == 1) {
          mi = this.protocol.examine(this.fullName);
        } else {
          mi = this.protocol.select(this.fullName);
        }
      }
      catch (CommandFailedException cex)
      {
        try
        {
          checkExists();
          if ((this.type & 0x1) == 0) {
            throw new MessagingException("folder cannot contain messages");
          }
          throw new MessagingException(cex.getMessage(), cex);
        }
        finally
        {
          this.exists = false;
          this.attributes = null;
          this.type = 0;
          
          releaseProtocol(true);
        }
      }
      catch (ProtocolException pex) {}
      try
      {
        this.protocol.logout();
      }
      catch (ProtocolException pex2) {}finally
      {
        releaseProtocol(false);
        throw new MessagingException(pex.getMessage(), pex);
        if (mi.mode == mode) {
          break label318;
        }
        if ((mode != 2) || (mi.mode != 1) || (!((IMAPStore)this.store).allowReadOnlySelect())) {}
      }
      try
      {
        this.protocol.close();
        releaseProtocol(true);
      }
      catch (ProtocolException pex)
      {
        try
        {
          this.protocol.logout();
        }
        catch (ProtocolException pex2) {}finally
        {
          releaseProtocol(false);
        }
      }
      finally {}
      label318:
      this.opened = true;
      this.reallyClosed = false;
      this.mode = mi.mode;
      this.availableFlags = mi.availableFlags;
      this.permanentFlags = mi.permanentFlags;
      this.total = (this.realTotal = mi.total);
      this.recent = mi.recent;
      this.uidvalidity = mi.uidvalidity;
      this.uidnext = mi.uidnext;
      
      this.messageCache = new MessageCache(this, (IMAPStore)this.store, this.total);
    }
    this.exists = true;
    this.attributes = null;
    this.type = 1;
    
    notifyConnectionListeners(1);
  }
  
  public synchronized void fetch(Message[] msgs, FetchProfile fp)
    throws MessagingException
  {
    checkOpened();
    
    StringBuffer command = new StringBuffer();
    boolean first = true;
    boolean allHeaders = false;
    if (fp.contains(FetchProfile.Item.ENVELOPE))
    {
      command.append(getEnvelopeCommand());
      first = false;
    }
    if (fp.contains(FetchProfile.Item.FLAGS))
    {
      command.append(first ? "FLAGS" : " FLAGS");
      first = false;
    }
    if (fp.contains(FetchProfile.Item.CONTENT_INFO))
    {
      command.append(first ? "BODYSTRUCTURE" : " BODYSTRUCTURE");
      first = false;
    }
    if (fp.contains(UIDFolder.FetchProfileItem.UID))
    {
      command.append(first ? "UID" : " UID");
      first = false;
    }
    if (fp.contains(FetchProfileItem.HEADERS))
    {
      allHeaders = true;
      if (this.protocol.isREV1()) {
        command.append(first ? "BODY.PEEK[HEADER]" : " BODY.PEEK[HEADER]");
      } else {
        command.append(first ? "RFC822.HEADER" : " RFC822.HEADER");
      }
      first = false;
    }
    if (fp.contains(FetchProfileItem.SIZE))
    {
      command.append(first ? "RFC822.SIZE" : " RFC822.SIZE");
      first = false;
    }
    String[] hdrs = null;
    if (!allHeaders)
    {
      hdrs = fp.getHeaderNames();
      if (hdrs.length > 0)
      {
        if (!first) {
          command.append(" ");
        }
        command.append(createHeaderCommand(hdrs));
      }
    }
    FetchItem[] fitems = this.protocol.getFetchItems();
    for (int i = 0; i < fitems.length; i++) {
      if (fp.contains(fitems[i].getFetchProfileItem()))
      {
        if (command.length() != 0) {
          command.append(" ");
        }
        command.append(fitems[i].getName());
      }
    }
    Utility.Condition condition = new IMAPMessage.FetchProfileCondition(fp, fitems);
    synchronized (this.messageCacheLock)
    {
      MessageSet[] msgsets = Utility.toMessageSet(msgs, condition);
      if (msgsets == null) {
        return;
      }
      Response[] r = null;
      Vector v = new Vector();
      try
      {
        r = getProtocol().fetch(msgsets, command.toString());
      }
      catch (ConnectionException cex)
      {
        throw new FolderClosedException(this, cex.getMessage());
      }
      catch (CommandFailedException cfx) {}catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
      if (r == null) {
        return;
      }
      for (int i = 0; i < r.length; i++) {
        if (r[i] != null) {
          if (!(r[i] instanceof FetchResponse))
          {
            v.addElement(r[i]);
          }
          else
          {
            FetchResponse f = (FetchResponse)r[i];
            
            IMAPMessage msg = getMessageBySeqNumber(f.getNumber());
            
            int count = f.getItemCount();
            boolean unsolicitedFlags = false;
            for (int j = 0; j < count; j++)
            {
              Item item = f.getItem(j);
              if (((item instanceof Flags)) && ((!fp.contains(FetchProfile.Item.FLAGS)) || (msg == null))) {
                unsolicitedFlags = true;
              } else if (msg != null) {
                msg.handleFetchItem(item, hdrs, allHeaders);
              }
            }
            if (msg != null) {
              msg.handleExtensionFetchItems(f.getExtensionItems());
            }
            if (unsolicitedFlags) {
              v.addElement(f);
            }
          }
        }
      }
      int size = v.size();
      if (size != 0)
      {
        Response[] responses = new Response[size];
        v.copyInto(responses);
        handleResponses(responses);
      }
    }
  }
  
  protected String getEnvelopeCommand()
  {
    return "ENVELOPE INTERNALDATE RFC822.SIZE";
  }
  
  protected IMAPMessage newIMAPMessage(int msgnum)
  {
    return new IMAPMessage(this, msgnum);
  }
  
  private String createHeaderCommand(String[] hdrs)
  {
    StringBuffer sb;
    StringBuffer sb;
    if (this.protocol.isREV1()) {
      sb = new StringBuffer("BODY.PEEK[HEADER.FIELDS (");
    } else {
      sb = new StringBuffer("RFC822.HEADER.LINES (");
    }
    for (int i = 0; i < hdrs.length; i++)
    {
      if (i > 0) {
        sb.append(" ");
      }
      sb.append(hdrs[i]);
    }
    if (this.protocol.isREV1()) {
      sb.append(")]");
    } else {
      sb.append(")");
    }
    return sb.toString();
  }
  
  public synchronized void setFlags(Message[] msgs, Flags flag, boolean value)
    throws MessagingException
  {
    checkOpened();
    checkFlags(flag);
    if (msgs.length == 0) {
      return;
    }
    synchronized (this.messageCacheLock)
    {
      try
      {
        IMAPProtocol p = getProtocol();
        MessageSet[] ms = Utility.toMessageSet(msgs, null);
        if (ms == null) {
          throw new MessageRemovedException("Messages have been removed");
        }
        p.storeFlags(ms, flag, value);
      }
      catch (ConnectionException cex)
      {
        throw new FolderClosedException(this, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
  }
  
  public synchronized void setFlags(int start, int end, Flags flag, boolean value)
    throws MessagingException
  {
    checkOpened();
    Message[] msgs = new Message[end - start + 1];
    int i = 0;
    for (int n = start; n <= end; n++) {
      msgs[(i++)] = getMessage(n);
    }
    setFlags(msgs, flag, value);
  }
  
  public synchronized void setFlags(int[] msgnums, Flags flag, boolean value)
    throws MessagingException
  {
    checkOpened();
    Message[] msgs = new Message[msgnums.length];
    for (int i = 0; i < msgnums.length; i++) {
      msgs[i] = getMessage(msgnums[i]);
    }
    setFlags(msgs, flag, value);
  }
  
  public synchronized void close(boolean expunge)
    throws MessagingException
  {
    close(expunge, false);
  }
  
  public synchronized void forceClose()
    throws MessagingException
  {
    close(false, true);
  }
  
  private void close(boolean expunge, boolean force)
    throws MessagingException
  {
    assert (Thread.holdsLock(this));
    synchronized (this.messageCacheLock)
    {
      if ((!this.opened) && (this.reallyClosed)) {
        throw new IllegalStateException("This operation is not allowed on a closed folder");
      }
      this.reallyClosed = true;
      if (!this.opened) {
        return;
      }
      try
      {
        waitIfIdle();
        if (force)
        {
          this.logger.log(Level.FINE, "forcing folder {0} to close", this.fullName);
          if (this.protocol != null) {
            this.protocol.disconnect();
          }
        }
        else if (((IMAPStore)this.store).isConnectionPoolFull())
        {
          this.logger.fine("pool is full, not adding an Authenticated connection");
          if ((expunge) && (this.protocol != null)) {
            this.protocol.close();
          }
          if (this.protocol != null) {
            this.protocol.logout();
          }
        }
        else if ((!expunge) && (this.mode == 2))
        {
          try
          {
            if ((this.protocol != null) && (this.protocol.hasCapability("UNSELECT")))
            {
              this.protocol.unselect();
            }
            else if (this.protocol != null)
            {
              MailboxInfo mi = this.protocol.examine(this.fullName);
              if (this.protocol != null) {
                this.protocol.close();
              }
            }
          }
          catch (ProtocolException pex2)
          {
            if (this.protocol != null) {
              this.protocol.disconnect();
            }
          }
        }
        else if (this.protocol != null)
        {
          this.protocol.close();
        }
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
      finally
      {
        if (this.opened) {
          cleanup(true);
        }
      }
    }
  }
  
  private void cleanup(boolean returnToPool)
  {
    assert (Thread.holdsLock(this.messageCacheLock));
    releaseProtocol(returnToPool);
    this.messageCache = null;
    this.uidTable = null;
    this.exists = false;
    this.attributes = null;
    this.opened = false;
    this.idleState = 0;
    notifyConnectionListeners(3);
  }
  
  public synchronized boolean isOpen()
  {
    synchronized (this.messageCacheLock)
    {
      if (this.opened) {
        try
        {
          keepConnectionAlive(false);
        }
        catch (ProtocolException pex) {}
      }
    }
    return this.opened;
  }
  
  public synchronized Flags getPermanentFlags()
  {
    if (this.permanentFlags == null) {
      return null;
    }
    return (Flags)this.permanentFlags.clone();
  }
  
  public synchronized int getMessageCount()
    throws MessagingException
  {
    if (!this.opened)
    {
      checkExists();
      try
      {
        Status status = getStatus();
        return status.total;
      }
      catch (BadCommandException bex)
      {
        IMAPProtocol p = null;
        try
        {
          p = getStoreProtocol();
          MailboxInfo minfo = p.examine(this.fullName);
          p.close();
          return minfo.total;
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
      catch (ConnectionException cex)
      {
        throw new StoreClosedException(this.store, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
    synchronized (this.messageCacheLock)
    {
      try
      {
        keepConnectionAlive(true);
        return this.total;
      }
      catch (ConnectionException cex)
      {
        throw new FolderClosedException(this, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
  }
  
  public synchronized int getNewMessageCount()
    throws MessagingException
  {
    if (!this.opened)
    {
      checkExists();
      try
      {
        Status status = getStatus();
        return status.recent;
      }
      catch (BadCommandException bex)
      {
        IMAPProtocol p = null;
        try
        {
          p = getStoreProtocol();
          MailboxInfo minfo = p.examine(this.fullName);
          p.close();
          return minfo.recent;
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
      catch (ConnectionException cex)
      {
        throw new StoreClosedException(this.store, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
    synchronized (this.messageCacheLock)
    {
      try
      {
        keepConnectionAlive(true);
        return this.recent;
      }
      catch (ConnectionException cex)
      {
        throw new FolderClosedException(this, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
  }
  
  /* Error */
  public synchronized int getUnreadMessageCount()
    throws MessagingException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 12	com/sun/mail/imap/IMAPFolder:opened	Z
    //   4: ifne +51 -> 55
    //   7: aload_0
    //   8: invokevirtual 103	com/sun/mail/imap/IMAPFolder:checkExists	()V
    //   11: aload_0
    //   12: invokespecial 124	com/sun/mail/imap/IMAPFolder:getStatus	()Lcom/sun/mail/imap/protocol/Status;
    //   15: astore_1
    //   16: aload_1
    //   17: getfield 253	com/sun/mail/imap/protocol/Status:unseen	I
    //   20: ireturn
    //   21: astore_1
    //   22: iconst_m1
    //   23: ireturn
    //   24: astore_1
    //   25: new 127	javax/mail/StoreClosedException
    //   28: dup
    //   29: aload_0
    //   30: getfield 89	com/sun/mail/imap/IMAPFolder:store	Ljavax/mail/Store;
    //   33: aload_1
    //   34: invokevirtual 76	com/sun/mail/iap/ConnectionException:getMessage	()Ljava/lang/String;
    //   37: invokespecial 128	javax/mail/StoreClosedException:<init>	(Ljavax/mail/Store;Ljava/lang/String;)V
    //   40: athrow
    //   41: astore_1
    //   42: new 78	javax/mail/MessagingException
    //   45: dup
    //   46: aload_1
    //   47: invokevirtual 79	com/sun/mail/iap/ProtocolException:getMessage	()Ljava/lang/String;
    //   50: aload_1
    //   51: invokespecial 80	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   54: athrow
    //   55: new 211	javax/mail/Flags
    //   58: dup
    //   59: invokespecial 254	javax/mail/Flags:<init>	()V
    //   62: astore_1
    //   63: aload_1
    //   64: getstatic 255	javax/mail/Flags$Flag:SEEN	Ljavax/mail/Flags$Flag;
    //   67: invokevirtual 256	javax/mail/Flags:add	(Ljavax/mail/Flags$Flag;)V
    //   70: aload_0
    //   71: getfield 11	com/sun/mail/imap/IMAPFolder:messageCacheLock	Ljava/lang/Object;
    //   74: dup
    //   75: astore_2
    //   76: monitorenter
    //   77: aload_0
    //   78: invokevirtual 203	com/sun/mail/imap/IMAPFolder:getProtocol	()Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   81: new 257	javax/mail/search/FlagTerm
    //   84: dup
    //   85: aload_1
    //   86: iconst_0
    //   87: invokespecial 258	javax/mail/search/FlagTerm:<init>	(Ljavax/mail/Flags;Z)V
    //   90: invokevirtual 259	com/sun/mail/imap/protocol/IMAPProtocol:search	(Ljavax/mail/search/SearchTerm;)[I
    //   93: astore_3
    //   94: aload_3
    //   95: arraylength
    //   96: aload_2
    //   97: monitorexit
    //   98: ireturn
    //   99: astore 4
    //   101: aload_2
    //   102: monitorexit
    //   103: aload 4
    //   105: athrow
    //   106: astore_2
    //   107: new 68	javax/mail/FolderClosedException
    //   110: dup
    //   111: aload_0
    //   112: aload_2
    //   113: invokevirtual 76	com/sun/mail/iap/ConnectionException:getMessage	()Ljava/lang/String;
    //   116: invokespecial 70	javax/mail/FolderClosedException:<init>	(Ljavax/mail/Folder;Ljava/lang/String;)V
    //   119: athrow
    //   120: astore_2
    //   121: new 78	javax/mail/MessagingException
    //   124: dup
    //   125: aload_2
    //   126: invokevirtual 79	com/sun/mail/iap/ProtocolException:getMessage	()Ljava/lang/String;
    //   129: aload_2
    //   130: invokespecial 80	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   133: athrow
    // Line number table:
    //   Java source line #1492	-> byte code offset #0
    //   Java source line #1493	-> byte code offset #7
    //   Java source line #1497	-> byte code offset #11
    //   Java source line #1498	-> byte code offset #16
    //   Java source line #1499	-> byte code offset #21
    //   Java source line #1503	-> byte code offset #22
    //   Java source line #1504	-> byte code offset #24
    //   Java source line #1505	-> byte code offset #25
    //   Java source line #1506	-> byte code offset #41
    //   Java source line #1507	-> byte code offset #42
    //   Java source line #1513	-> byte code offset #55
    //   Java source line #1514	-> byte code offset #63
    //   Java source line #1516	-> byte code offset #70
    //   Java source line #1517	-> byte code offset #77
    //   Java source line #1518	-> byte code offset #94
    //   Java source line #1519	-> byte code offset #99
    //   Java source line #1520	-> byte code offset #106
    //   Java source line #1521	-> byte code offset #107
    //   Java source line #1522	-> byte code offset #120
    //   Java source line #1524	-> byte code offset #121
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	134	0	this	IMAPFolder
    //   15	2	1	status	Status
    //   21	2	1	bex	BadCommandException
    //   24	10	1	cex	ConnectionException
    //   41	10	1	pex	ProtocolException
    //   62	24	1	f	Flags
    //   106	7	2	cex	ConnectionException
    //   120	10	2	pex	ProtocolException
    //   93	2	3	matches	int[]
    //   99	5	4	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   11	20	21	com/sun/mail/iap/BadCommandException
    //   11	20	24	com/sun/mail/iap/ConnectionException
    //   11	20	41	com/sun/mail/iap/ProtocolException
    //   77	98	99	finally
    //   99	103	99	finally
    //   70	98	106	com/sun/mail/iap/ConnectionException
    //   99	106	106	com/sun/mail/iap/ConnectionException
    //   70	98	120	com/sun/mail/iap/ProtocolException
    //   99	106	120	com/sun/mail/iap/ProtocolException
  }
  
  /* Error */
  public synchronized int getDeletedMessageCount()
    throws MessagingException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 12	com/sun/mail/imap/IMAPFolder:opened	Z
    //   4: ifne +9 -> 13
    //   7: aload_0
    //   8: invokevirtual 103	com/sun/mail/imap/IMAPFolder:checkExists	()V
    //   11: iconst_m1
    //   12: ireturn
    //   13: new 211	javax/mail/Flags
    //   16: dup
    //   17: invokespecial 254	javax/mail/Flags:<init>	()V
    //   20: astore_1
    //   21: aload_1
    //   22: getstatic 260	javax/mail/Flags$Flag:DELETED	Ljavax/mail/Flags$Flag;
    //   25: invokevirtual 256	javax/mail/Flags:add	(Ljavax/mail/Flags$Flag;)V
    //   28: aload_0
    //   29: getfield 11	com/sun/mail/imap/IMAPFolder:messageCacheLock	Ljava/lang/Object;
    //   32: dup
    //   33: astore_2
    //   34: monitorenter
    //   35: aload_0
    //   36: invokevirtual 203	com/sun/mail/imap/IMAPFolder:getProtocol	()Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   39: new 257	javax/mail/search/FlagTerm
    //   42: dup
    //   43: aload_1
    //   44: iconst_1
    //   45: invokespecial 258	javax/mail/search/FlagTerm:<init>	(Ljavax/mail/Flags;Z)V
    //   48: invokevirtual 259	com/sun/mail/imap/protocol/IMAPProtocol:search	(Ljavax/mail/search/SearchTerm;)[I
    //   51: astore_3
    //   52: aload_3
    //   53: arraylength
    //   54: aload_2
    //   55: monitorexit
    //   56: ireturn
    //   57: astore 4
    //   59: aload_2
    //   60: monitorexit
    //   61: aload 4
    //   63: athrow
    //   64: astore_2
    //   65: new 68	javax/mail/FolderClosedException
    //   68: dup
    //   69: aload_0
    //   70: aload_2
    //   71: invokevirtual 76	com/sun/mail/iap/ConnectionException:getMessage	()Ljava/lang/String;
    //   74: invokespecial 70	javax/mail/FolderClosedException:<init>	(Ljavax/mail/Folder;Ljava/lang/String;)V
    //   77: athrow
    //   78: astore_2
    //   79: new 78	javax/mail/MessagingException
    //   82: dup
    //   83: aload_2
    //   84: invokevirtual 79	com/sun/mail/iap/ProtocolException:getMessage	()Ljava/lang/String;
    //   87: aload_2
    //   88: invokespecial 80	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   91: athrow
    // Line number table:
    //   Java source line #1533	-> byte code offset #0
    //   Java source line #1534	-> byte code offset #7
    //   Java source line #1536	-> byte code offset #11
    //   Java source line #1541	-> byte code offset #13
    //   Java source line #1542	-> byte code offset #21
    //   Java source line #1544	-> byte code offset #28
    //   Java source line #1545	-> byte code offset #35
    //   Java source line #1546	-> byte code offset #52
    //   Java source line #1547	-> byte code offset #57
    //   Java source line #1548	-> byte code offset #64
    //   Java source line #1549	-> byte code offset #65
    //   Java source line #1550	-> byte code offset #78
    //   Java source line #1552	-> byte code offset #79
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	92	0	this	IMAPFolder
    //   20	24	1	f	Flags
    //   64	7	2	cex	ConnectionException
    //   78	10	2	pex	ProtocolException
    //   51	2	3	matches	int[]
    //   57	5	4	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   35	56	57	finally
    //   57	61	57	finally
    //   28	56	64	com/sun/mail/iap/ConnectionException
    //   57	64	64	com/sun/mail/iap/ConnectionException
    //   28	56	78	com/sun/mail/iap/ProtocolException
    //   57	64	78	com/sun/mail/iap/ProtocolException
  }
  
  private Status getStatus()
    throws ProtocolException
  {
    int statusCacheTimeout = ((IMAPStore)this.store).getStatusCacheTimeout();
    if ((statusCacheTimeout > 0) && (this.cachedStatus != null) && (System.currentTimeMillis() - this.cachedStatusTime < statusCacheTimeout)) {
      return this.cachedStatus;
    }
    IMAPProtocol p = null;
    try
    {
      p = getStoreProtocol();
      Status s = p.status(this.fullName, null);
      if (statusCacheTimeout > 0)
      {
        this.cachedStatus = s;
        this.cachedStatusTime = System.currentTimeMillis();
      }
      return s;
    }
    finally
    {
      releaseStoreProtocol(p);
    }
  }
  
  public synchronized Message getMessage(int msgnum)
    throws MessagingException
  {
    checkOpened();
    checkRange(msgnum);
    
    return this.messageCache.getMessage(msgnum);
  }
  
  public synchronized void appendMessages(Message[] msgs)
    throws MessagingException
  {
    checkExists();
    
    int maxsize = ((IMAPStore)this.store).getAppendBufferSize();
    for (int i = 0; i < msgs.length; i++)
    {
      Message m = msgs[i];
      Date d = m.getReceivedDate();
      if (d == null) {
        d = m.getSentDate();
      }
      final Date dd = d;
      final Flags f = m.getFlags();
      final MessageLiteral mos;
      try
      {
        mos = new MessageLiteral(m, m.getSize() > maxsize ? 0 : maxsize);
      }
      catch (IOException ex)
      {
        throw new MessagingException("IOException while appending messages", ex);
      }
      catch (MessageRemovedException mrex)
      {
        continue;
      }
      doCommand(new ProtocolCommand()
      {
        public Object doCommand(IMAPProtocol p)
          throws ProtocolException
        {
          p.append(IMAPFolder.this.fullName, f, dd, mos);
          return null;
        }
      });
    }
  }
  
  public synchronized AppendUID[] appendUIDMessages(Message[] msgs)
    throws MessagingException
  {
    checkExists();
    
    int maxsize = ((IMAPStore)this.store).getAppendBufferSize();
    
    AppendUID[] uids = new AppendUID[msgs.length];
    for (int i = 0; i < msgs.length; i++)
    {
      Message m = msgs[i];
      final MessageLiteral mos;
      try
      {
        mos = new MessageLiteral(m, m.getSize() > maxsize ? 0 : maxsize);
      }
      catch (IOException ex)
      {
        throw new MessagingException("IOException while appending messages", ex);
      }
      catch (MessageRemovedException mrex)
      {
        continue;
      }
      Date d = m.getReceivedDate();
      if (d == null) {
        d = m.getSentDate();
      }
      final Date dd = d;
      final Flags f = m.getFlags();
      AppendUID auid = (AppendUID)doCommand(new ProtocolCommand()
      {
        public Object doCommand(IMAPProtocol p)
          throws ProtocolException
        {
          return p.appenduid(IMAPFolder.this.fullName, f, dd, mos);
        }
      });
      uids[i] = auid;
    }
    return uids;
  }
  
  public synchronized Message[] addMessages(Message[] msgs)
    throws MessagingException
  {
    checkOpened();
    Message[] rmsgs = new MimeMessage[msgs.length];
    AppendUID[] uids = appendUIDMessages(msgs);
    for (int i = 0; i < uids.length; i++)
    {
      AppendUID auid = uids[i];
      if ((auid != null) && 
        (auid.uidvalidity == this.uidvalidity)) {
        try
        {
          rmsgs[i] = getMessageByUID(auid.uid);
        }
        catch (MessagingException mex) {}
      }
    }
    return rmsgs;
  }
  
  public synchronized void copyMessages(Message[] msgs, Folder folder)
    throws MessagingException
  {
    checkOpened();
    if (msgs.length == 0) {
      return;
    }
    if (folder.getStore() == this.store) {
      synchronized (this.messageCacheLock)
      {
        try
        {
          IMAPProtocol p = getProtocol();
          MessageSet[] ms = Utility.toMessageSet(msgs, null);
          if (ms == null) {
            throw new MessageRemovedException("Messages have been removed");
          }
          p.copy(ms, folder.getFullName());
        }
        catch (CommandFailedException cfx)
        {
          if (cfx.getMessage().indexOf("TRYCREATE") != -1) {
            throw new FolderNotFoundException(folder, folder.getFullName() + " does not exist");
          }
          throw new MessagingException(cfx.getMessage(), cfx);
        }
        catch (ConnectionException cex)
        {
          throw new FolderClosedException(this, cex.getMessage());
        }
        catch (ProtocolException pex)
        {
          throw new MessagingException(pex.getMessage(), pex);
        }
      }
    } else {
      super.copyMessages(msgs, folder);
    }
  }
  
  public synchronized Message[] expunge()
    throws MessagingException
  {
    return expunge(null);
  }
  
  public synchronized Message[] expunge(Message[] msgs)
    throws MessagingException
  {
    checkOpened();
    if (msgs != null)
    {
      FetchProfile fp = new FetchProfile();
      fp.add(UIDFolder.FetchProfileItem.UID);
      fetch(msgs, fp);
    }
    IMAPMessage[] rmsgs;
    synchronized (this.messageCacheLock)
    {
      this.doExpungeNotification = false;
      try
      {
        IMAPProtocol p = getProtocol();
        if (msgs != null) {
          p.uidexpunge(Utility.toUIDSet(msgs));
        } else {
          p.expunge();
        }
      }
      catch (CommandFailedException cfx)
      {
        if (this.mode != 2) {
          throw new IllegalStateException("Cannot expunge READ_ONLY folder: " + this.fullName);
        }
        throw new MessagingException(cfx.getMessage(), cfx);
      }
      catch (ConnectionException cex)
      {
        throw new FolderClosedException(this, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
      finally
      {
        this.doExpungeNotification = true;
      }
      IMAPMessage[] rmsgs;
      if (msgs != null) {
        rmsgs = this.messageCache.removeExpungedMessages(msgs);
      } else {
        rmsgs = this.messageCache.removeExpungedMessages();
      }
      if (this.uidTable != null) {
        for (int i = 0; i < rmsgs.length; i++)
        {
          IMAPMessage m = rmsgs[i];
          
          long uid = m.getUID();
          if (uid != -1L) {
            this.uidTable.remove(new Long(uid));
          }
        }
      }
      this.total = this.messageCache.size();
    }
    if (rmsgs.length > 0) {
      notifyMessageRemovedListeners(true, rmsgs);
    }
    return rmsgs;
  }
  
  public synchronized Message[] search(SearchTerm term)
    throws MessagingException
  {
    checkOpened();
    try
    {
      Message[] matchMsgs = null;
      synchronized (this.messageCacheLock)
      {
        int[] matches = getProtocol().search(term);
        if (matches != null)
        {
          matchMsgs = new IMAPMessage[matches.length];
          for (int i = 0; i < matches.length; i++) {
            matchMsgs[i] = getMessageBySeqNumber(matches[i]);
          }
        }
      }
      return matchMsgs;
    }
    catch (CommandFailedException cfx)
    {
      return super.search(term);
    }
    catch (SearchException sex)
    {
      return super.search(term);
    }
    catch (ConnectionException cex)
    {
      throw new FolderClosedException(this, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
  }
  
  public synchronized Message[] search(SearchTerm term, Message[] msgs)
    throws MessagingException
  {
    checkOpened();
    if (msgs.length == 0) {
      return msgs;
    }
    try
    {
      Message[] matchMsgs = null;
      synchronized (this.messageCacheLock)
      {
        IMAPProtocol p = getProtocol();
        MessageSet[] ms = Utility.toMessageSet(msgs, null);
        if (ms == null) {
          throw new MessageRemovedException("Messages have been removed");
        }
        int[] matches = p.search(ms, term);
        if (matches != null)
        {
          matchMsgs = new IMAPMessage[matches.length];
          for (int i = 0; i < matches.length; i++) {
            matchMsgs[i] = getMessageBySeqNumber(matches[i]);
          }
        }
      }
      return matchMsgs;
    }
    catch (CommandFailedException cfx)
    {
      return super.search(term, msgs);
    }
    catch (SearchException sex)
    {
      return super.search(term, msgs);
    }
    catch (ConnectionException cex)
    {
      throw new FolderClosedException(this, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
  }
  
  public synchronized Message[] getSortedMessages(SortTerm[] term)
    throws MessagingException
  {
    return getSortedMessages(term, null);
  }
  
  public synchronized Message[] getSortedMessages(SortTerm[] term, SearchTerm sterm)
    throws MessagingException
  {
    checkOpened();
    try
    {
      Message[] matchMsgs = null;
      synchronized (this.messageCacheLock)
      {
        int[] matches = getProtocol().sort(term, sterm);
        if (matches != null)
        {
          matchMsgs = new IMAPMessage[matches.length];
          for (int i = 0; i < matches.length; i++) {
            matchMsgs[i] = getMessageBySeqNumber(matches[i]);
          }
        }
      }
      return matchMsgs;
    }
    catch (CommandFailedException cfx)
    {
      throw new MessagingException(cfx.getMessage(), cfx);
    }
    catch (SearchException sex)
    {
      throw new MessagingException(sex.getMessage(), sex);
    }
    catch (ConnectionException cex)
    {
      throw new FolderClosedException(this, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
  }
  
  public synchronized void addMessageCountListener(MessageCountListener l)
  {
    super.addMessageCountListener(l);
    this.hasMessageCountListener = true;
  }
  
  public synchronized long getUIDValidity()
    throws MessagingException
  {
    if (this.opened) {
      return this.uidvalidity;
    }
    IMAPProtocol p = null;
    Status status = null;
    try
    {
      p = getStoreProtocol();
      String[] item = { "UIDVALIDITY" };
      status = p.status(this.fullName, item);
    }
    catch (BadCommandException bex)
    {
      throw new MessagingException("Cannot obtain UIDValidity", bex);
    }
    catch (ConnectionException cex)
    {
      throwClosedException(cex);
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    finally
    {
      releaseStoreProtocol(p);
    }
    return status.uidvalidity;
  }
  
  public synchronized long getUIDNext()
    throws MessagingException
  {
    if (this.opened) {
      return this.uidnext;
    }
    IMAPProtocol p = null;
    Status status = null;
    try
    {
      p = getStoreProtocol();
      String[] item = { "UIDNEXT" };
      status = p.status(this.fullName, item);
    }
    catch (BadCommandException bex)
    {
      throw new MessagingException("Cannot obtain UIDNext", bex);
    }
    catch (ConnectionException cex)
    {
      throwClosedException(cex);
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    finally
    {
      releaseStoreProtocol(p);
    }
    return status.uidnext;
  }
  
  public synchronized Message getMessageByUID(long uid)
    throws MessagingException
  {
    checkOpened();
    
    IMAPMessage m = null;
    try
    {
      synchronized (this.messageCacheLock)
      {
        Long l = new Long(uid);
        if (this.uidTable != null)
        {
          m = (IMAPMessage)this.uidTable.get(l);
          if (m != null) {
            return m;
          }
        }
        else
        {
          this.uidTable = new Hashtable();
        }
        UID u = getProtocol().fetchSequenceNumber(uid);
        if ((u != null) && (u.seqnum <= this.total))
        {
          m = getMessageBySeqNumber(u.seqnum);
          m.setUID(u.uid);
          
          this.uidTable.put(l, m);
        }
      }
    }
    catch (ConnectionException cex)
    {
      throw new FolderClosedException(this, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    return m;
  }
  
  public synchronized Message[] getMessagesByUID(long start, long end)
    throws MessagingException
  {
    checkOpened();
    Message[] msgs;
    try
    {
      synchronized (this.messageCacheLock)
      {
        if (this.uidTable == null) {
          this.uidTable = new Hashtable();
        }
        UID[] ua = getProtocol().fetchSequenceNumbers(start, end);
        
        msgs = new Message[ua.length];
        for (int i = 0; i < ua.length; i++)
        {
          IMAPMessage m = getMessageBySeqNumber(ua[i].seqnum);
          m.setUID(ua[i].uid);
          msgs[i] = m;
          this.uidTable.put(new Long(ua[i].uid), m);
        }
      }
    }
    catch (ConnectionException cex)
    {
      throw new FolderClosedException(this, cex.getMessage());
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    return msgs;
  }
  
  /* Error */
  public synchronized Message[] getMessagesByUID(long[] uids)
    throws MessagingException
  {
    // Byte code:
    //   0: aload_0
    //   1: invokevirtual 169	com/sun/mail/imap/IMAPFolder:checkOpened	()V
    //   4: aload_0
    //   5: getfield 11	com/sun/mail/imap/IMAPFolder:messageCacheLock	Ljava/lang/Object;
    //   8: dup
    //   9: astore_2
    //   10: monitorenter
    //   11: aload_1
    //   12: astore_3
    //   13: aload_0
    //   14: getfield 248	com/sun/mail/imap/IMAPFolder:uidTable	Ljava/util/Hashtable;
    //   17: ifnull +107 -> 124
    //   20: new 201	java/util/Vector
    //   23: dup
    //   24: invokespecial 202	java/util/Vector:<init>	()V
    //   27: astore 4
    //   29: iconst_0
    //   30: istore 6
    //   32: iload 6
    //   34: aload_1
    //   35: arraylength
    //   36: if_icmpge +40 -> 76
    //   39: aload_0
    //   40: getfield 248	com/sun/mail/imap/IMAPFolder:uidTable	Ljava/util/Hashtable;
    //   43: new 303	java/lang/Long
    //   46: dup
    //   47: aload_1
    //   48: iload 6
    //   50: laload
    //   51: invokespecial 304	java/lang/Long:<init>	(J)V
    //   54: dup
    //   55: astore 5
    //   57: invokevirtual 333	java/util/Hashtable:containsKey	(Ljava/lang/Object;)Z
    //   60: ifne +10 -> 70
    //   63: aload 4
    //   65: aload 5
    //   67: invokevirtual 206	java/util/Vector:addElement	(Ljava/lang/Object;)V
    //   70: iinc 6 1
    //   73: goto -41 -> 32
    //   76: aload 4
    //   78: invokevirtual 215	java/util/Vector:size	()I
    //   81: istore 6
    //   83: iload 6
    //   85: newarray <illegal type>
    //   87: astore_3
    //   88: iconst_0
    //   89: istore 7
    //   91: iload 7
    //   93: iload 6
    //   95: if_icmpge +26 -> 121
    //   98: aload_3
    //   99: iload 7
    //   101: aload 4
    //   103: iload 7
    //   105: invokevirtual 334	java/util/Vector:elementAt	(I)Ljava/lang/Object;
    //   108: checkcast 303	java/lang/Long
    //   111: invokevirtual 335	java/lang/Long:longValue	()J
    //   114: lastore
    //   115: iinc 7 1
    //   118: goto -27 -> 91
    //   121: goto +14 -> 135
    //   124: aload_0
    //   125: new 325	java/util/Hashtable
    //   128: dup
    //   129: invokespecial 326	java/util/Hashtable:<init>	()V
    //   132: putfield 248	com/sun/mail/imap/IMAPFolder:uidTable	Ljava/util/Hashtable;
    //   135: aload_3
    //   136: arraylength
    //   137: ifle +82 -> 219
    //   140: aload_0
    //   141: invokevirtual 203	com/sun/mail/imap/IMAPFolder:getProtocol	()Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   144: aload_3
    //   145: invokevirtual 336	com/sun/mail/imap/protocol/IMAPProtocol:fetchSequenceNumbers	([J)[Lcom/sun/mail/imap/protocol/UID;
    //   148: astore 4
    //   150: iconst_0
    //   151: istore 6
    //   153: iload 6
    //   155: aload 4
    //   157: arraylength
    //   158: if_icmpge +61 -> 219
    //   161: aload_0
    //   162: aload 4
    //   164: iload 6
    //   166: aaload
    //   167: getfield 328	com/sun/mail/imap/protocol/UID:seqnum	I
    //   170: invokevirtual 208	com/sun/mail/imap/IMAPFolder:getMessageBySeqNumber	(I)Lcom/sun/mail/imap/IMAPMessage;
    //   173: astore 5
    //   175: aload 5
    //   177: aload 4
    //   179: iload 6
    //   181: aaload
    //   182: getfield 329	com/sun/mail/imap/protocol/UID:uid	J
    //   185: invokevirtual 330	com/sun/mail/imap/IMAPMessage:setUID	(J)V
    //   188: aload_0
    //   189: getfield 248	com/sun/mail/imap/IMAPFolder:uidTable	Ljava/util/Hashtable;
    //   192: new 303	java/lang/Long
    //   195: dup
    //   196: aload 4
    //   198: iload 6
    //   200: aaload
    //   201: getfield 329	com/sun/mail/imap/protocol/UID:uid	J
    //   204: invokespecial 304	java/lang/Long:<init>	(J)V
    //   207: aload 5
    //   209: invokevirtual 331	java/util/Hashtable:put	(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    //   212: pop
    //   213: iinc 6 1
    //   216: goto -63 -> 153
    //   219: aload_1
    //   220: arraylength
    //   221: anewarray 232	javax/mail/Message
    //   224: astore 4
    //   226: iconst_0
    //   227: istore 5
    //   229: iload 5
    //   231: aload_1
    //   232: arraylength
    //   233: if_icmpge +35 -> 268
    //   236: aload 4
    //   238: iload 5
    //   240: aload_0
    //   241: getfield 248	com/sun/mail/imap/IMAPFolder:uidTable	Ljava/util/Hashtable;
    //   244: new 303	java/lang/Long
    //   247: dup
    //   248: aload_1
    //   249: iload 5
    //   251: laload
    //   252: invokespecial 304	java/lang/Long:<init>	(J)V
    //   255: invokevirtual 324	java/util/Hashtable:get	(Ljava/lang/Object;)Ljava/lang/Object;
    //   258: checkcast 232	javax/mail/Message
    //   261: aastore
    //   262: iinc 5 1
    //   265: goto -36 -> 229
    //   268: aload 4
    //   270: aload_2
    //   271: monitorexit
    //   272: areturn
    //   273: astore 8
    //   275: aload_2
    //   276: monitorexit
    //   277: aload 8
    //   279: athrow
    //   280: astore_2
    //   281: new 68	javax/mail/FolderClosedException
    //   284: dup
    //   285: aload_0
    //   286: aload_2
    //   287: invokevirtual 76	com/sun/mail/iap/ConnectionException:getMessage	()Ljava/lang/String;
    //   290: invokespecial 70	javax/mail/FolderClosedException:<init>	(Ljavax/mail/Folder;Ljava/lang/String;)V
    //   293: athrow
    //   294: astore_2
    //   295: new 78	javax/mail/MessagingException
    //   298: dup
    //   299: aload_2
    //   300: invokevirtual 79	com/sun/mail/iap/ProtocolException:getMessage	()Ljava/lang/String;
    //   303: aload_2
    //   304: invokespecial 80	javax/mail/MessagingException:<init>	(Ljava/lang/String;Ljava/lang/Exception;)V
    //   307: athrow
    // Line number table:
    //   Java source line #2163	-> byte code offset #0
    //   Java source line #2166	-> byte code offset #4
    //   Java source line #2167	-> byte code offset #11
    //   Java source line #2168	-> byte code offset #13
    //   Java source line #2169	-> byte code offset #20
    //   Java source line #2171	-> byte code offset #29
    //   Java source line #2172	-> byte code offset #39
    //   Java source line #2174	-> byte code offset #63
    //   Java source line #2171	-> byte code offset #70
    //   Java source line #2177	-> byte code offset #76
    //   Java source line #2178	-> byte code offset #83
    //   Java source line #2179	-> byte code offset #88
    //   Java source line #2180	-> byte code offset #98
    //   Java source line #2179	-> byte code offset #115
    //   Java source line #2181	-> byte code offset #121
    //   Java source line #2182	-> byte code offset #124
    //   Java source line #2184	-> byte code offset #135
    //   Java source line #2186	-> byte code offset #140
    //   Java source line #2188	-> byte code offset #150
    //   Java source line #2189	-> byte code offset #161
    //   Java source line #2190	-> byte code offset #175
    //   Java source line #2191	-> byte code offset #188
    //   Java source line #2188	-> byte code offset #213
    //   Java source line #2196	-> byte code offset #219
    //   Java source line #2197	-> byte code offset #226
    //   Java source line #2198	-> byte code offset #236
    //   Java source line #2197	-> byte code offset #262
    //   Java source line #2199	-> byte code offset #268
    //   Java source line #2200	-> byte code offset #273
    //   Java source line #2201	-> byte code offset #280
    //   Java source line #2202	-> byte code offset #281
    //   Java source line #2203	-> byte code offset #294
    //   Java source line #2204	-> byte code offset #295
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	308	0	this	IMAPFolder
    //   0	308	1	uids	long[]
    //   280	7	2	cex	ConnectionException
    //   294	10	2	pex	ProtocolException
    //   12	133	3	unavailUids	long[]
    //   27	75	4	v	Vector
    //   148	49	4	ua	UID[]
    //   224	45	4	msgs	Message[]
    //   55	11	5	l	Long
    //   173	35	5	m	IMAPMessage
    //   227	36	5	i	int
    //   30	41	6	i	int
    //   81	13	6	vsize	int
    //   151	63	6	i	int
    //   89	27	7	i	int
    //   273	5	8	localObject1	Object
    // Exception table:
    //   from	to	target	type
    //   11	272	273	finally
    //   273	277	273	finally
    //   4	272	280	com/sun/mail/iap/ConnectionException
    //   273	280	280	com/sun/mail/iap/ConnectionException
    //   4	272	294	com/sun/mail/iap/ProtocolException
    //   273	280	294	com/sun/mail/iap/ProtocolException
  }
  
  public synchronized long getUID(Message message)
    throws MessagingException
  {
    if (message.getFolder() != this) {
      throw new NoSuchElementException("Message does not belong to this folder");
    }
    checkOpened();
    
    IMAPMessage m = (IMAPMessage)message;
    long uid;
    if ((uid = m.getUID()) != -1L) {
      return uid;
    }
    synchronized (this.messageCacheLock)
    {
      try
      {
        IMAPProtocol p = getProtocol();
        m.checkExpunged();
        UID u = p.fetchUID(m.getSequenceNumber());
        if (u != null)
        {
          uid = u.uid;
          m.setUID(uid);
          if (this.uidTable == null) {
            this.uidTable = new Hashtable();
          }
          this.uidTable.put(new Long(uid), m);
        }
      }
      catch (ConnectionException cex)
      {
        throw new FolderClosedException(this, cex.getMessage());
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
    return uid;
  }
  
  public Quota[] getQuota()
    throws MessagingException
  {
    (Quota[])doOptionalCommand("QUOTA not supported", new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        return p.getQuotaRoot(IMAPFolder.this.fullName);
      }
    });
  }
  
  public void setQuota(final Quota quota)
    throws MessagingException
  {
    doOptionalCommand("QUOTA not supported", new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        p.setQuota(quota);
        return null;
      }
    });
  }
  
  public ACL[] getACL()
    throws MessagingException
  {
    (ACL[])doOptionalCommand("ACL not supported", new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        return p.getACL(IMAPFolder.this.fullName);
      }
    });
  }
  
  public void addACL(ACL acl)
    throws MessagingException
  {
    setACL(acl, '\000');
  }
  
  public void removeACL(final String name)
    throws MessagingException
  {
    doOptionalCommand("ACL not supported", new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        p.deleteACL(IMAPFolder.this.fullName, name);
        return null;
      }
    });
  }
  
  public void addRights(ACL acl)
    throws MessagingException
  {
    setACL(acl, '+');
  }
  
  public void removeRights(ACL acl)
    throws MessagingException
  {
    setACL(acl, '-');
  }
  
  public Rights[] listRights(final String name)
    throws MessagingException
  {
    (Rights[])doOptionalCommand("ACL not supported", new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        return p.listRights(IMAPFolder.this.fullName, name);
      }
    });
  }
  
  public Rights myRights()
    throws MessagingException
  {
    (Rights)doOptionalCommand("ACL not supported", new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        return p.myRights(IMAPFolder.this.fullName);
      }
    });
  }
  
  private void setACL(final ACL acl, final char mod)
    throws MessagingException
  {
    doOptionalCommand("ACL not supported", new ProtocolCommand()
    {
      public Object doCommand(IMAPProtocol p)
        throws ProtocolException
      {
        p.setACL(IMAPFolder.this.fullName, mod, acl);
        return null;
      }
    });
  }
  
  public synchronized String[] getAttributes()
    throws MessagingException
  {
    checkExists();
    if (this.attributes == null) {
      exists();
    }
    return this.attributes == null ? new String[0] : (String[])this.attributes.clone();
  }
  
  public void idle()
    throws MessagingException
  {
    idle(false);
  }
  
  public void idle(boolean once)
    throws MessagingException
  {
    assert (!Thread.holdsLock(this));
    synchronized (this)
    {
      checkOpened();
      Boolean started = (Boolean)doOptionalCommand("IDLE not supported", new ProtocolCommand()
      {
        public Object doCommand(IMAPProtocol p)
          throws ProtocolException
        {
          if (IMAPFolder.this.idleState == 0)
          {
            p.idleStart();
            IMAPFolder.this.idleState = 1;
            return Boolean.TRUE;
          }
          try
          {
            IMAPFolder.this.messageCacheLock.wait();
          }
          catch (InterruptedException ex) {}
          return Boolean.FALSE;
        }
      });
      if (!started.booleanValue()) {
        return;
      }
    }
    for (;;)
    {
      Response r = this.protocol.readIdleResponse();
      try
      {
        synchronized (this.messageCacheLock)
        {
          try
          {
            if ((r == null) || (this.protocol == null) || (!this.protocol.processIdleResponse(r)))
            {
              this.idleState = 0;
              this.messageCacheLock.notifyAll();
              break;
            }
          }
          catch (ProtocolException pex)
          {
            this.idleState = 0;
            this.messageCacheLock.notifyAll();
            throw pex;
          }
          if ((once) && 
            (this.idleState == 1))
          {
            this.protocol.idleAbort();
            this.idleState = 2;
          }
        }
      }
      catch (ConnectionException cex)
      {
        throwClosedException(cex);
      }
      catch (ProtocolException pex)
      {
        throw new MessagingException(pex.getMessage(), pex);
      }
    }
    int minidle = ((IMAPStore)this.store).getMinIdleTime();
    if (minidle > 0) {
      try
      {
        Thread.sleep(minidle);
      }
      catch (InterruptedException ex) {}
    }
  }
  
  void waitIfIdle()
    throws ProtocolException
  {
    assert (Thread.holdsLock(this.messageCacheLock));
    while (this.idleState != 0)
    {
      if (this.idleState == 1)
      {
        this.protocol.idleAbort();
        this.idleState = 2;
      }
      try
      {
        this.messageCacheLock.wait();
      }
      catch (InterruptedException ex) {}
    }
  }
  
  public void handleResponse(Response r)
  {
    assert (Thread.holdsLock(this.messageCacheLock));
    if ((r.isOK()) || (r.isNO()) || (r.isBAD()) || (r.isBYE())) {
      ((IMAPStore)this.store).handleResponseCode(r);
    }
    if (r.isBYE())
    {
      if (this.opened) {
        cleanup(false);
      }
      return;
    }
    if (r.isOK()) {
      return;
    }
    if (!r.isUnTagged()) {
      return;
    }
    if (!(r instanceof IMAPResponse))
    {
      this.logger.fine("UNEXPECTED RESPONSE : " + r.toString());
      return;
    }
    IMAPResponse ir = (IMAPResponse)r;
    if (ir.keyEquals("EXISTS"))
    {
      int exists = ir.getNumber();
      if (exists <= this.realTotal) {
        return;
      }
      int count = exists - this.realTotal;
      Message[] msgs = new Message[count];
      
      this.messageCache.addMessages(count, this.realTotal + 1);
      int oldtotal = this.total;
      this.realTotal += count;
      this.total += count;
      if (this.hasMessageCountListener)
      {
        for (int i = 0; i < count; i++) {
          msgs[i] = this.messageCache.getMessage(++oldtotal);
        }
        notifyMessageAddedListeners(msgs);
      }
    }
    else if (ir.keyEquals("EXPUNGE"))
    {
      int seqnum = ir.getNumber();
      Message[] msgs = null;
      if ((this.doExpungeNotification) && (this.hasMessageCountListener)) {
        msgs = new Message[] { getMessageBySeqNumber(seqnum) };
      }
      this.messageCache.expungeMessage(seqnum);
      
      this.realTotal -= 1;
      if (msgs != null) {
        notifyMessageRemovedListeners(false, msgs);
      }
    }
    else if (ir.keyEquals("FETCH"))
    {
      assert ((ir instanceof FetchResponse)) : "!ir instanceof FetchResponse";
      FetchResponse f = (FetchResponse)ir;
      
      Flags flags = (Flags)f.getItem(Flags.class);
      if (flags != null)
      {
        IMAPMessage msg = getMessageBySeqNumber(f.getNumber());
        if (msg != null)
        {
          msg._setFlags(flags);
          notifyMessageChangedListeners(1, msg);
        }
      }
    }
    else if (ir.keyEquals("RECENT"))
    {
      this.recent = ir.getNumber();
    }
  }
  
  void handleResponses(Response[] r)
  {
    for (int i = 0; i < r.length; i++) {
      if (r[i] != null) {
        handleResponse(r[i]);
      }
    }
  }
  
  protected synchronized IMAPProtocol getStoreProtocol()
    throws ProtocolException
  {
    this.connectionPoolLogger.fine("getStoreProtocol() borrowing a connection");
    return ((IMAPStore)this.store).getFolderStoreProtocol();
  }
  
  protected synchronized void throwClosedException(ConnectionException cex)
    throws FolderClosedException, StoreClosedException
  {
    if (((this.protocol != null) && (cex.getProtocol() == this.protocol)) || ((this.protocol == null) && (!this.reallyClosed))) {
      throw new FolderClosedException(this, cex.getMessage());
    }
    throw new StoreClosedException(this.store, cex.getMessage());
  }
  
  protected IMAPProtocol getProtocol()
    throws ProtocolException
  {
    assert (Thread.holdsLock(this.messageCacheLock));
    waitIfIdle();
    return this.protocol;
  }
  
  public Object doCommand(ProtocolCommand cmd)
    throws MessagingException
  {
    try
    {
      return doProtocolCommand(cmd);
    }
    catch (ConnectionException cex)
    {
      throwClosedException(cex);
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    return null;
  }
  
  public Object doOptionalCommand(String err, ProtocolCommand cmd)
    throws MessagingException
  {
    try
    {
      return doProtocolCommand(cmd);
    }
    catch (BadCommandException bex)
    {
      throw new MessagingException(err, bex);
    }
    catch (ConnectionException cex)
    {
      throwClosedException(cex);
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    return null;
  }
  
  public Object doCommandIgnoreFailure(ProtocolCommand cmd)
    throws MessagingException
  {
    try
    {
      return doProtocolCommand(cmd);
    }
    catch (CommandFailedException cfx)
    {
      return null;
    }
    catch (ConnectionException cex)
    {
      throwClosedException(cex);
    }
    catch (ProtocolException pex)
    {
      throw new MessagingException(pex.getMessage(), pex);
    }
    return null;
  }
  
  /* Error */
  protected Object doProtocolCommand(ProtocolCommand cmd)
    throws ProtocolException
  {
    // Byte code:
    //   0: aload_0
    //   1: dup
    //   2: astore_2
    //   3: monitorenter
    //   4: aload_0
    //   5: getfield 142	com/sun/mail/imap/IMAPFolder:protocol	Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   8: ifnull +32 -> 40
    //   11: aload_0
    //   12: getfield 11	com/sun/mail/imap/IMAPFolder:messageCacheLock	Ljava/lang/Object;
    //   15: dup
    //   16: astore_3
    //   17: monitorenter
    //   18: aload_1
    //   19: aload_0
    //   20: invokevirtual 203	com/sun/mail/imap/IMAPFolder:getProtocol	()Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   23: invokeinterface 411 2 0
    //   28: aload_3
    //   29: monitorexit
    //   30: aload_2
    //   31: monitorexit
    //   32: areturn
    //   33: astore 4
    //   35: aload_3
    //   36: monitorexit
    //   37: aload 4
    //   39: athrow
    //   40: aload_2
    //   41: monitorexit
    //   42: goto +10 -> 52
    //   45: astore 5
    //   47: aload_2
    //   48: monitorexit
    //   49: aload 5
    //   51: athrow
    //   52: aconst_null
    //   53: astore_2
    //   54: aload_0
    //   55: invokevirtual 251	com/sun/mail/imap/IMAPFolder:getStoreProtocol	()Lcom/sun/mail/imap/protocol/IMAPProtocol;
    //   58: astore_2
    //   59: aload_1
    //   60: aload_2
    //   61: invokeinterface 411 2 0
    //   66: astore_3
    //   67: jsr +13 -> 80
    //   70: aload_3
    //   71: areturn
    //   72: astore 6
    //   74: jsr +6 -> 80
    //   77: aload 6
    //   79: athrow
    //   80: astore 7
    //   82: aload_0
    //   83: aload_2
    //   84: invokevirtual 252	com/sun/mail/imap/IMAPFolder:releaseStoreProtocol	(Lcom/sun/mail/imap/protocol/IMAPProtocol;)V
    //   87: ret 7
    // Line number table:
    //   Java source line #2910	-> byte code offset #0
    //   Java source line #2916	-> byte code offset #4
    //   Java source line #2917	-> byte code offset #11
    //   Java source line #2918	-> byte code offset #18
    //   Java source line #2919	-> byte code offset #33
    //   Java source line #2921	-> byte code offset #40
    //   Java source line #2924	-> byte code offset #52
    //   Java source line #2927	-> byte code offset #54
    //   Java source line #2928	-> byte code offset #59
    //   Java source line #2930	-> byte code offset #72
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	89	0	this	IMAPFolder
    //   0	89	1	cmd	ProtocolCommand
    //   2	46	2	Ljava/lang/Object;	Object
    //   53	31	2	p	IMAPProtocol
    //   16	55	3	Ljava/lang/Object;	Object
    //   33	5	4	localObject1	Object
    //   45	5	5	localObject2	Object
    //   72	6	6	localObject3	Object
    //   80	1	7	localObject4	Object
    // Exception table:
    //   from	to	target	type
    //   18	30	33	finally
    //   33	37	33	finally
    //   4	32	45	finally
    //   33	42	45	finally
    //   45	49	45	finally
    //   54	70	72	finally
    //   72	77	72	finally
  }
  
  protected synchronized void releaseStoreProtocol(IMAPProtocol p)
  {
    if (p != this.protocol) {
      ((IMAPStore)this.store).releaseFolderStoreProtocol(p);
    } else {
      this.logger.fine("releasing our protocol as store protocol?");
    }
  }
  
  protected void releaseProtocol(boolean returnToPool)
  {
    if (this.protocol != null)
    {
      this.protocol.removeResponseHandler(this);
      if (returnToPool)
      {
        ((IMAPStore)this.store).releaseProtocol(this, this.protocol);
      }
      else
      {
        this.protocol.disconnect();
        ((IMAPStore)this.store).releaseProtocol(this, null);
      }
      this.protocol = null;
    }
  }
  
  protected void keepConnectionAlive(boolean keepStoreAlive)
    throws ProtocolException
  {
    if (System.currentTimeMillis() - this.protocol.getTimestamp() > 1000L)
    {
      waitIfIdle();
      if (this.protocol != null) {
        this.protocol.noop();
      }
    }
    if ((keepStoreAlive) && (((IMAPStore)this.store).hasSeparateStoreConnection()))
    {
      IMAPProtocol p = null;
      try
      {
        p = ((IMAPStore)this.store).getFolderStoreProtocol();
        if (System.currentTimeMillis() - p.getTimestamp() > 1000L) {
          p.noop();
        }
      }
      finally
      {
        ((IMAPStore)this.store).releaseFolderStoreProtocol(p);
      }
    }
  }
  
  protected IMAPMessage getMessageBySeqNumber(int seqnum)
  {
    return this.messageCache.getMessageBySeqnum(seqnum);
  }
  
  private boolean isDirectory()
  {
    return (this.type & 0x2) != 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\imap\IMAPFolder.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */