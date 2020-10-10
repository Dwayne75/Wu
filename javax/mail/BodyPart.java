package javax.mail;

public abstract class BodyPart
  implements Part
{
  protected Multipart parent;
  
  public Multipart getParent()
  {
    return this.parent;
  }
  
  void setParent(Multipart parent)
  {
    this.parent = parent;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\BodyPart.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */