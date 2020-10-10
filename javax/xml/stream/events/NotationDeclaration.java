package javax.xml.stream.events;

public abstract interface NotationDeclaration
  extends XMLEvent
{
  public abstract String getName();
  
  public abstract String getPublicId();
  
  public abstract String getSystemId();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\NotationDeclaration.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */