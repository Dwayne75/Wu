package javax.xml.stream.events;

public abstract interface EntityDeclaration
  extends XMLEvent
{
  public abstract String getPublicId();
  
  public abstract String getSystemId();
  
  public abstract String getName();
  
  public abstract String getNotationName();
  
  public abstract String getReplacementText();
  
  public abstract String getBaseURI();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\EntityDeclaration.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */