package javax.mail;

import java.util.Vector;
import javax.mail.event.FolderEvent;
import javax.mail.event.FolderListener;
import javax.mail.event.StoreEvent;
import javax.mail.event.StoreListener;

public abstract class Store
  extends Service
{
  protected Store(Session session, URLName urlname)
  {
    super(session, urlname);
  }
  
  public abstract Folder getDefaultFolder()
    throws MessagingException;
  
  public abstract Folder getFolder(String paramString)
    throws MessagingException;
  
  public abstract Folder getFolder(URLName paramURLName)
    throws MessagingException;
  
  public Folder[] getPersonalNamespaces()
    throws MessagingException
  {
    return new Folder[] { getDefaultFolder() };
  }
  
  public Folder[] getUserNamespaces(String user)
    throws MessagingException
  {
    return new Folder[0];
  }
  
  public Folder[] getSharedNamespaces()
    throws MessagingException
  {
    return new Folder[0];
  }
  
  private volatile Vector storeListeners = null;
  
  public synchronized void addStoreListener(StoreListener l)
  {
    if (this.storeListeners == null) {
      this.storeListeners = new Vector();
    }
    this.storeListeners.addElement(l);
  }
  
  public synchronized void removeStoreListener(StoreListener l)
  {
    if (this.storeListeners != null) {
      this.storeListeners.removeElement(l);
    }
  }
  
  protected void notifyStoreListeners(int type, String message)
  {
    if (this.storeListeners == null) {
      return;
    }
    StoreEvent e = new StoreEvent(this, type, message);
    queueEvent(e, this.storeListeners);
  }
  
  private volatile Vector folderListeners = null;
  
  public synchronized void addFolderListener(FolderListener l)
  {
    if (this.folderListeners == null) {
      this.folderListeners = new Vector();
    }
    this.folderListeners.addElement(l);
  }
  
  public synchronized void removeFolderListener(FolderListener l)
  {
    if (this.folderListeners != null) {
      this.folderListeners.removeElement(l);
    }
  }
  
  protected void notifyFolderListeners(int type, Folder folder)
  {
    if (this.folderListeners == null) {
      return;
    }
    FolderEvent e = new FolderEvent(this, folder, type);
    queueEvent(e, this.folderListeners);
  }
  
  protected void notifyFolderRenamedListeners(Folder oldF, Folder newF)
  {
    if (this.folderListeners == null) {
      return;
    }
    FolderEvent e = new FolderEvent(this, oldF, newF, 3);
    queueEvent(e, this.folderListeners);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\Store.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */