package javax.mail;

public class FolderNotFoundException
  extends MessagingException
{
  private transient Folder folder;
  private static final long serialVersionUID = 472612108891249403L;
  
  public FolderNotFoundException() {}
  
  public FolderNotFoundException(Folder folder)
  {
    this.folder = folder;
  }
  
  public FolderNotFoundException(Folder folder, String s)
  {
    super(s);
    this.folder = folder;
  }
  
  public FolderNotFoundException(String s, Folder folder)
  {
    super(s);
    this.folder = folder;
  }
  
  public Folder getFolder()
  {
    return this.folder;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\FolderNotFoundException.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */