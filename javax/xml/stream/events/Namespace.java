package javax.xml.stream.events;

public abstract interface Namespace
  extends Attribute
{
  public abstract String getPrefix();
  
  public abstract String getNamespaceURI();
  
  public abstract boolean isDefaultNamespaceDeclaration();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\Namespace.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */