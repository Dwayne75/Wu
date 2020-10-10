package javax.xml.stream;

import java.io.OutputStream;
import java.io.Writer;
import javax.xml.transform.Result;

public abstract class XMLOutputFactory
{
  public static final String IS_REPAIRING_NAMESPACES = "javax.xml.stream.isRepairingNamespaces";
  
  public static XMLOutputFactory newInstance()
    throws FactoryConfigurationError
  {
    return (XMLOutputFactory)FactoryFinder.find("javax.xml.stream.XMLOutputFactory", "com.bea.xml.stream.XMLOutputFactoryBase");
  }
  
  public static XMLInputFactory newInstance(String factoryId, ClassLoader classLoader)
    throws FactoryConfigurationError
  {
    return (XMLInputFactory)FactoryFinder.find(factoryId, "com.bea.xml.stream.XMLInputFactoryBase", classLoader);
  }
  
  public abstract XMLStreamWriter createXMLStreamWriter(Writer paramWriter)
    throws XMLStreamException;
  
  public abstract XMLStreamWriter createXMLStreamWriter(OutputStream paramOutputStream)
    throws XMLStreamException;
  
  public abstract XMLStreamWriter createXMLStreamWriter(OutputStream paramOutputStream, String paramString)
    throws XMLStreamException;
  
  public abstract XMLStreamWriter createXMLStreamWriter(Result paramResult)
    throws XMLStreamException;
  
  public abstract XMLEventWriter createXMLEventWriter(Result paramResult)
    throws XMLStreamException;
  
  public abstract XMLEventWriter createXMLEventWriter(OutputStream paramOutputStream)
    throws XMLStreamException;
  
  public abstract XMLEventWriter createXMLEventWriter(OutputStream paramOutputStream, String paramString)
    throws XMLStreamException;
  
  public abstract XMLEventWriter createXMLEventWriter(Writer paramWriter)
    throws XMLStreamException;
  
  public abstract void setProperty(String paramString, Object paramObject)
    throws IllegalArgumentException;
  
  public abstract Object getProperty(String paramString)
    throws IllegalArgumentException;
  
  public abstract boolean isPropertySupported(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\stream\XMLOutputFactory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */