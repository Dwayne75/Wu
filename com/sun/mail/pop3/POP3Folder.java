package com.sun.mail.pop3;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.MailLogger;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.FolderClosedException;
import javax.mail.FolderNotFoundException;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import javax.mail.UIDFolder.FetchProfileItem;

public class POP3Folder
  extends Folder
{
  private String name;
  private POP3Store store;
  private volatile Protocol port;
  private int total;
  private int size;
  private boolean exists = false;
  private volatile boolean opened = false;
  private Vector message_cache;
  private boolean doneUidl = false;
  private volatile TempFile fileCache = null;
  MailLogger logger;
  
  POP3Folder(POP3Store store, String name)
  {
    super(store);
    this.name = name;
    this.store = store;
    if (name.equalsIgnoreCase("INBOX")) {
      this.exists = true;
    }
    this.logger = new MailLogger(getClass(), "DEBUG POP3", store.getSession());
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getFullName()
  {
    return this.name;
  }
  
  public Folder getParent()
  {
    return new DefaultFolder(this.store);
  }
  
  public boolean exists()
  {
    return this.exists;
  }
  
  public Folder[] list(String pattern)
    throws MessagingException
  {
    throw new MessagingException("not a directory");
  }
  
  public char getSeparator()
  {
    return '\000';
  }
  
  public int getType()
  {
    return 1;
  }
  
  public boolean create(int type)
    throws MessagingException
  {
    return false;
  }
  
  public boolean hasNewMessages()
    throws MessagingException
  {
    return false;
  }
  
  public Folder getFolder(String name)
    throws MessagingException
  {
    throw new MessagingException("not a directory");
  }
  
  public boolean delete(boolean recurse)
    throws MessagingException
  {
    throw new MethodNotSupportedException("delete");
  }
  
  public boolean renameTo(Folder f)
    throws MessagingException
  {
    throw new MethodNotSupportedException("renameTo");
  }
  
  public synchronized void open(int mode)
    throws MessagingException
  {
    checkClosed();
    if (!this.exists) {
      throw new FolderNotFoundException(this, "folder is not INBOX");
    }
    try
    {
      this.port = this.store.getPort(this);
      Status s = this.port.stat();
      this.total = s.total;
      this.size = s.size;
      this.mode = mode;
      if (this.store.useFileCache) {
        try
        {
          this.fileCache = new TempFile(this.store.fileCacheDir);
        }
        catch (IOException ex)
        {
          this.logger.log(Level.FINE, "failed to create file cache", ex);
          throw ex;
        }
      }
      this.opened = true;
    }
    catch (IOException ioex)
    {
      try
      {
        if (this.port != null) {
          this.port.quit();
        }
      }
      catch (IOException ioex2) {}finally
      {
        this.port = null;
        this.store.closePort(this);
      }
      throw new MessagingException("Open failed", ioex);
    }
    this.message_cache = new Vector(this.total);
    this.message_cache.setSize(this.total);
    this.doneUidl = false;
    
    notifyConnectionListeners(1);
  }
  
  public synchronized void close(boolean expunge)
    throws MessagingException
  {
    checkOpen();
    try
    {
      if (this.store.rsetBeforeQuit) {
        this.port.rset();
      }
      if ((expunge) && (this.mode == 2)) {
        for (int i = 0; i < this.message_cache.size(); i++)
        {
          POP3Message m;
          if (((m = (POP3Message)this.message_cache.elementAt(i)) != null) && 
            (m.isSet(Flags.Flag.DELETED))) {
            try
            {
              this.port.dele(i + 1);
            }
            catch (IOException ioex)
            {
              throw new MessagingException("Exception deleting messages during close", ioex);
            }
          }
        }
      }
      for (int i = 0; i < this.message_cache.size(); i++)
      {
        POP3Message m;
        if ((m = (POP3Message)this.message_cache.elementAt(i)) != null) {
          m.invalidate(true);
        }
      }
      this.port.quit();
    }
    catch (IOException ex) {}finally
    {
      this.port = null;
      this.store.closePort(this);
      this.message_cache = null;
      this.opened = false;
      notifyConnectionListeners(3);
      if (this.fileCache != null)
      {
        this.fileCache.close();
        this.fileCache = null;
      }
    }
  }
  
  public synchronized boolean isOpen()
  {
    if (!this.opened) {
      return false;
    }
    try
    {
      if (!this.port.noop()) {
        throw new IOException("NOOP failed");
      }
    }
    catch (IOException ioex)
    {
      try
      {
        close(false);
      }
      catch (MessagingException mex) {}finally {}
    }
    return true;
  }
  
  public Flags getPermanentFlags()
  {
    return new Flags();
  }
  
  public synchronized int getMessageCount()
    throws MessagingException
  {
    if (!this.opened) {
      return -1;
    }
    checkReadable();
    return this.total;
  }
  
  public synchronized Message getMessage(int msgno)
    throws MessagingException
  {
    checkOpen();
    POP3Message m;
    if ((m = (POP3Message)this.message_cache.elementAt(msgno - 1)) == null)
    {
      m = createMessage(this, msgno);
      this.message_cache.setElementAt(m, msgno - 1);
    }
    return m;
  }
  
  protected POP3Message createMessage(Folder f, int msgno)
    throws MessagingException
  {
    POP3Message m = null;
    Constructor cons = this.store.messageConstructor;
    if (cons != null) {
      try
      {
        Object[] o = { this, new Integer(msgno) };
        m = (POP3Message)cons.newInstance(o);
      }
      catch (Exception ex) {}
    }
    if (m == null) {
      m = new POP3Message(this, msgno);
    }
    return m;
  }
  
  public void appendMessages(Message[] msgs)
    throws MessagingException
  {
    throw new MethodNotSupportedException("Append not supported");
  }
  
  public Message[] expunge()
    throws MessagingException
  {
    throw new MethodNotSupportedException("Expunge not supported");
  }
  
  public synchronized void fetch(Message[] msgs, FetchProfile fp)
    throws MessagingException
  {
    checkReadable();
    if ((!this.doneUidl) && (this.store.supportsUidl) && (fp.contains(UIDFolder.FetchProfileItem.UID)))
    {
      String[] uids = new String[this.message_cache.size()];
      try
      {
        if (!this.port.uidl(uids)) {
          return;
        }
      }
      catch (EOFException eex)
      {
        close(false);
        throw new FolderClosedException(this, eex.toString());
      }
      catch (IOException ex)
      {
        throw new MessagingException("error getting UIDL", ex);
      }
      for (int i = 0; i < uids.length; i++) {
        if (uids[i] != null)
        {
          POP3Message m = (POP3Message)getMessage(i + 1);
          m.uid = uids[i];
        }
      }
      this.doneUidl = true;
    }
    if (fp.contains(FetchProfile.Item.ENVELOPE)) {
      for (int i = 0; i < msgs.length; i++) {
        try
        {
          POP3Message msg = (POP3Message)msgs[i];
          
          msg.getHeader("");
          
          msg.getSize();
        }
        catch (MessageRemovedException mex) {}
      }
    }
  }
  
  public synchronized String getUID(Message msg)
    throws MessagingException
  {
    checkOpen();
    POP3Message m = (POP3Message)msg;
    try
    {
      if (!this.store.supportsUidl) {
        return null;
      }
      if (m.uid == "UNKNOWN") {
        m.uid = this.port.uidl(m.getMessageNumber());
      }
      return m.uid;
    }
    catch (EOFException eex)
    {
      close(false);
      throw new FolderClosedException(this, eex.toString());
    }
    catch (IOException ex)
    {
      throw new MessagingException("error getting UIDL", ex);
    }
  }
  
  public synchronized int getSize()
    throws MessagingException
  {
    checkOpen();
    return this.size;
  }
  
  public synchronized int[] getSizes()
    throws MessagingException
  {
    checkOpen();
    int[] sizes = new int[this.total];
    InputStream is = null;
    LineInputStream lis = null;
    try
    {
      is = this.port.list();
      lis = new LineInputStream(is);
      String line;
      while ((line = lis.readLine()) != null) {
        try
        {
          StringTokenizer st = new StringTokenizer(line);
          int msgnum = Integer.parseInt(st.nextToken());
          int size = Integer.parseInt(st.nextToken());
          if ((msgnum > 0) && (msgnum <= this.total)) {
            sizes[(msgnum - 1)] = size;
          }
        }
        catch (Exception e) {}
      }
    }
    catch (IOException ex) {}finally
    {
      try
      {
        if (lis != null) {
          lis.close();
        }
      }
      catch (IOException cex) {}
      try
      {
        if (is != null) {
          is.close();
        }
      }
      catch (IOException cex) {}
    }
    return sizes;
  }
  
  public synchronized InputStream listCommand()
    throws MessagingException, IOException
  {
    checkOpen();
    return this.port.list();
  }
  
  protected void finalize()
    throws Throwable
  {
    super.finalize();
    close(false);
  }
  
  private void checkOpen()
    throws IllegalStateException
  {
    if (!this.opened) {
      throw new IllegalStateException("Folder is not Open");
    }
  }
  
  private void checkClosed()
    throws IllegalStateException
  {
    if (this.opened) {
      throw new IllegalStateException("Folder is Open");
    }
  }
  
  private void checkReadable()
    throws IllegalStateException
  {
    if ((!this.opened) || ((this.mode != 1) && (this.mode != 2))) {
      throw new IllegalStateException("Folder is not Readable");
    }
  }
  
  Protocol getProtocol()
    throws MessagingException
  {
    Protocol p = this.port;
    checkOpen();
    
    return p;
  }
  
  protected void notifyMessageChangedListeners(int type, Message m)
  {
    super.notifyMessageChangedListeners(type, m);
  }
  
  TempFile getFileCache()
  {
    return this.fileCache;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\pop3\POP3Folder.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */