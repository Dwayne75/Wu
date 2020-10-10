package javax.mail;

public class ReadOnlyFolderException
  extends MessagingException
{
  private transient Folder folder;
  private static final long serialVersionUID = 5711829372799039325L;
  
  public ReadOnlyFolderException(Folder folder)
  {
    this(folder, null);
  }
  
  public ReadOnlyFolderException(Folder folder, String message)
  {
    super(message);
    this.folder = folder;
  }
  
  public Folder getFolder()
  {
    return this.folder;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\ReadOnlyFolderException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */