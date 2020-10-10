package javax.mail.event;

import java.util.EventListener;

public abstract interface FolderListener
  extends EventListener
{
  public abstract void folderCreated(FolderEvent paramFolderEvent);
  
  public abstract void folderDeleted(FolderEvent paramFolderEvent);
  
  public abstract void folderRenamed(FolderEvent paramFolderEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\event\FolderListener.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */