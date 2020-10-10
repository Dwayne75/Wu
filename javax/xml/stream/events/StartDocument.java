package javax.xml.stream.events;

public abstract interface StartDocument
  extends XMLEvent
{
  public abstract String getSystemId();
  
  public abstract String getCharacterEncodingScheme();
  
  public abstract boolean encodingSet();
  
  public abstract boolean isStandalone();
  
  public abstract boolean standaloneSet();
  
  public abstract String getVersion();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\StartDocument.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */