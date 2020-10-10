package javax.xml.stream.events;

import java.util.List;

public abstract interface DTD
  extends XMLEvent
{
  public abstract String getDocumentTypeDeclaration();
  
  public abstract Object getProcessedDTD();
  
  public abstract List getNotations();
  
  public abstract List getEntities();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\DTD.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */