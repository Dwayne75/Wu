package javax.xml.stream.events;

public abstract interface Characters
  extends XMLEvent
{
  public abstract String getData();
  
  public abstract boolean isWhiteSpace();
  
  public abstract boolean isCData();
  
  public abstract boolean isIgnorableWhiteSpace();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\Characters.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */