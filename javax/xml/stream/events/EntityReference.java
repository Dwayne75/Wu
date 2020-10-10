package javax.xml.stream.events;

public abstract interface EntityReference
  extends XMLEvent
{
  public abstract EntityDeclaration getDeclaration();
  
  public abstract String getName();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\EntityReference.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */