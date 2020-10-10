package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.Name;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import org.xml.sax.SAXException;

public abstract class XmlOutputAbstractImpl
  implements XmlOutput
{
  protected int[] nsUriIndex2prefixIndex;
  protected NamespaceContextImpl nsContext;
  protected XMLSerializer serializer;
  
  public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext)
    throws IOException, SAXException, XMLStreamException
  {
    this.nsUriIndex2prefixIndex = nsUriIndex2prefixIndex;
    this.nsContext = nsContext;
    this.serializer = serializer;
  }
  
  public void endDocument(boolean fragment)
    throws IOException, SAXException, XMLStreamException
  {
    this.serializer = null;
  }
  
  public void beginStartTag(Name name)
    throws IOException, XMLStreamException
  {
    beginStartTag(this.nsUriIndex2prefixIndex[name.nsUriIndex], name.localName);
  }
  
  public abstract void beginStartTag(int paramInt, String paramString)
    throws IOException, XMLStreamException;
  
  public void attribute(Name name, String value)
    throws IOException, XMLStreamException
  {
    short idx = name.nsUriIndex;
    if (idx == -1) {
      attribute(-1, name.localName, value);
    } else {
      attribute(this.nsUriIndex2prefixIndex[idx], name.localName, value);
    }
  }
  
  public abstract void attribute(int paramInt, String paramString1, String paramString2)
    throws IOException, XMLStreamException;
  
  public abstract void endStartTag()
    throws IOException, SAXException;
  
  public void endTag(Name name)
    throws IOException, SAXException, XMLStreamException
  {
    endTag(this.nsUriIndex2prefixIndex[name.nsUriIndex], name.localName);
  }
  
  public abstract void endTag(int paramInt, String paramString)
    throws IOException, SAXException, XMLStreamException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\XmlOutputAbstractImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */