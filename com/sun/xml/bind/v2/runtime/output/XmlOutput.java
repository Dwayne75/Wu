package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract interface XmlOutput
{
  public abstract void startDocument(XMLSerializer paramXMLSerializer, boolean paramBoolean, int[] paramArrayOfInt, NamespaceContextImpl paramNamespaceContextImpl)
    throws IOException, SAXException, XMLStreamException;
  
  public abstract void endDocument(boolean paramBoolean)
    throws IOException, SAXException, XMLStreamException;
  
  public abstract void beginStartTag(Name paramName)
    throws IOException, XMLStreamException;
  
  public abstract void beginStartTag(int paramInt, String paramString)
    throws IOException, XMLStreamException;
  
  public abstract void attribute(Name paramName, String paramString)
    throws IOException, XMLStreamException;
  
  public abstract void attribute(int paramInt, String paramString1, String paramString2)
    throws IOException, XMLStreamException;
  
  public abstract void endStartTag()
    throws IOException, SAXException;
  
  public abstract void endTag(Name paramName)
    throws IOException, SAXException, XMLStreamException;
  
  public abstract void endTag(int paramInt, String paramString)
    throws IOException, SAXException, XMLStreamException;
  
  public abstract void text(String paramString, boolean paramBoolean)
    throws IOException, SAXException, XMLStreamException;
  
  public abstract void text(Pcdata paramPcdata, boolean paramBoolean)
    throws IOException, SAXException, XMLStreamException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\XmlOutput.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */