package com.sun.xml.bind.v2.runtime.output;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.bind.v2.runtime.XMLSerializer;
import com.sun.xml.bind.v2.runtime.unmarshaller.UnmarshallerImpl;
import java.io.IOException;
import java.lang.reflect.Constructor;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.xml.sax.SAXException;

public class XMLStreamWriterOutput
  extends XmlOutputAbstractImpl
{
  private final XMLStreamWriter out;
  
  public static XmlOutput create(XMLStreamWriter out, JAXBContextImpl context)
  {
    Class writerClass = out.getClass();
    if (writerClass == FI_STAX_WRITER_CLASS) {
      try
      {
        return (XmlOutput)FI_OUTPUT_CTOR.newInstance(new Object[] { out, context });
      }
      catch (Exception e) {}
    }
    if ((STAXEX_WRITER_CLASS != null) && (STAXEX_WRITER_CLASS.isAssignableFrom(writerClass))) {
      try
      {
        return (XmlOutput)STAXEX_OUTPUT_CTOR.newInstance(new Object[] { out });
      }
      catch (Exception e) {}
    }
    return new XMLStreamWriterOutput(out);
  }
  
  protected final char[] buf = new char['Ā'];
  
  protected XMLStreamWriterOutput(XMLStreamWriter out)
  {
    this.out = out;
  }
  
  public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext)
    throws IOException, SAXException, XMLStreamException
  {
    super.startDocument(serializer, fragment, nsUriIndex2prefixIndex, nsContext);
    if (!fragment) {
      this.out.writeStartDocument();
    }
  }
  
  public void endDocument(boolean fragment)
    throws IOException, SAXException, XMLStreamException
  {
    if (!fragment)
    {
      this.out.writeEndDocument();
      this.out.flush();
    }
    super.endDocument(fragment);
  }
  
  public void beginStartTag(int prefix, String localName)
    throws IOException, XMLStreamException
  {
    this.out.writeStartElement(this.nsContext.getPrefix(prefix), localName, this.nsContext.getNamespaceURI(prefix));
    
    NamespaceContextImpl.Element nse = this.nsContext.getCurrent();
    if (nse.count() > 0) {
      for (int i = nse.count() - 1; i >= 0; i--)
      {
        String uri = nse.getNsUri(i);
        if ((uri.length() != 0) || (nse.getBase() != 1)) {
          this.out.writeNamespace(nse.getPrefix(i), uri);
        }
      }
    }
  }
  
  public void attribute(int prefix, String localName, String value)
    throws IOException, XMLStreamException
  {
    if (prefix == -1) {
      this.out.writeAttribute(localName, value);
    } else {
      this.out.writeAttribute(this.nsContext.getPrefix(prefix), this.nsContext.getNamespaceURI(prefix), localName, value);
    }
  }
  
  public void endStartTag()
    throws IOException, SAXException
  {}
  
  public void endTag(int prefix, String localName)
    throws IOException, SAXException, XMLStreamException
  {
    this.out.writeEndElement();
  }
  
  public void text(String value, boolean needsSeparatingWhitespace)
    throws IOException, SAXException, XMLStreamException
  {
    if (needsSeparatingWhitespace) {
      this.out.writeCharacters(" ");
    }
    this.out.writeCharacters(value);
  }
  
  public void text(Pcdata value, boolean needsSeparatingWhitespace)
    throws IOException, SAXException, XMLStreamException
  {
    if (needsSeparatingWhitespace) {
      this.out.writeCharacters(" ");
    }
    int len = value.length();
    if (len < this.buf.length)
    {
      value.writeTo(this.buf, 0);
      this.out.writeCharacters(this.buf, 0, len);
    }
    else
    {
      this.out.writeCharacters(value.toString());
    }
  }
  
  private static final Class FI_STAX_WRITER_CLASS = ;
  private static final Constructor<? extends XmlOutput> FI_OUTPUT_CTOR = initFastInfosetOutputClass();
  
  private static Class initFIStAXWriterClass()
  {
    try
    {
      Class llfisw = MarshallerImpl.class.getClassLoader().loadClass("org.jvnet.fastinfoset.stax.LowLevelFastInfosetStreamWriter");
      
      Class sds = MarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.fastinfoset.stax.StAXDocumentSerializer");
      if (llfisw.isAssignableFrom(sds)) {
        return sds;
      }
      return null;
    }
    catch (Throwable e) {}
    return null;
  }
  
  private static Constructor<? extends XmlOutput> initFastInfosetOutputClass()
  {
    try
    {
      if (FI_STAX_WRITER_CLASS == null) {
        return null;
      }
      Class c = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.bind.v2.runtime.output.FastInfosetStreamWriterOutput");
      return c.getConstructor(new Class[] { FI_STAX_WRITER_CLASS, JAXBContextImpl.class });
    }
    catch (Throwable e) {}
    return null;
  }
  
  private static final Class STAXEX_WRITER_CLASS = initStAXExWriterClass();
  private static final Constructor<? extends XmlOutput> STAXEX_OUTPUT_CTOR = initStAXExOutputClass();
  
  private static Class initStAXExWriterClass()
  {
    try
    {
      return MarshallerImpl.class.getClassLoader().loadClass("org.jvnet.staxex.XMLStreamWriterEx");
    }
    catch (Throwable e) {}
    return null;
  }
  
  private static Constructor<? extends XmlOutput> initStAXExOutputClass()
  {
    try
    {
      Class c = UnmarshallerImpl.class.getClassLoader().loadClass("com.sun.xml.bind.v2.runtime.output.StAXExStreamWriterOutput");
      return c.getConstructor(new Class[] { STAXEX_WRITER_CLASS });
    }
    catch (Throwable e) {}
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\XMLStreamWriterOutput.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */