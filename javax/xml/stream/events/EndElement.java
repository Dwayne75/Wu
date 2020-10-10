package javax.xml.stream.events;

import java.util.Iterator;
import javax.xml.namespace.QName;

public abstract interface EndElement
  extends XMLEvent
{
  public abstract QName getName();
  
  public abstract Iterator getNamespaces();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\events\EndElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */