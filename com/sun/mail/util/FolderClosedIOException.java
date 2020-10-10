package com.sun.mail.util;

import java.io.IOException;
import javax.mail.Folder;

public class FolderClosedIOException
  extends IOException
{
  private transient Folder folder;
  private static final long serialVersionUID = 4281122580365555735L;
  
  public FolderClosedIOException(Folder folder)
  {
    this(folder, null);
  }
  
  public FolderClosedIOException(Folder folder, String message)
  {
    super(message);
    this.folder = folder;
  }
  
  public Folder getFolder()
  {
    return this.folder;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\FolderClosedIOException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */