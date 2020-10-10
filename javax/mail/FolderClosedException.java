package javax.mail;

public class FolderClosedException
  extends MessagingException
{
  private transient Folder folder;
  private static final long serialVersionUID = 1687879213433302315L;
  
  public FolderClosedException(Folder folder)
  {
    this(folder, null);
  }
  
  public FolderClosedException(Folder folder, String message)
  {
    super(message);
    this.folder = folder;
  }
  
  public Folder getFolder()
  {
    return this.folder;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\FolderClosedException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */