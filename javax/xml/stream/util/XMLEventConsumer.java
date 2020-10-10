package javax.xml.stream.util;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public abstract interface XMLEventConsumer
{
  public abstract void add(XMLEvent paramXMLEvent)
    throws XMLStreamException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\util\XMLEventConsumer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */