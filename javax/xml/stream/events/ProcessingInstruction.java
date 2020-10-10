package javax.xml.stream.events;

public abstract interface ProcessingInstruction
  extends XMLEvent
{
  public abstract String getTarget();
  
  public abstract String getData();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\ProcessingInstruction.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */