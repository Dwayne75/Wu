package javax.xml.bind.helpers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Marshaller.Listener;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

public abstract class AbstractMarshallerImpl
  implements Marshaller
{
  private ValidationEventHandler eventHandler = new DefaultValidationEventHandler();
  private String encoding = "UTF-8";
  private String schemaLocation = null;
  private String noNSSchemaLocation = null;
  private boolean formattedOutput = false;
  private boolean fragment = false;
  
  public final void marshal(Object obj, OutputStream os)
    throws JAXBException
  {
    checkNotNull(obj, "obj", os, "os");
    marshal(obj, new StreamResult(os));
  }
  
  public void marshal(Object jaxbElement, File output)
    throws JAXBException
  {
    checkNotNull(jaxbElement, "jaxbElement", output, "output");
    try
    {
      OutputStream os = new BufferedOutputStream(new FileOutputStream(output));
      try
      {
        marshal(jaxbElement, new StreamResult(os));
      }
      finally
      {
        os.close();
      }
    }
    catch (IOException e)
    {
      throw new JAXBException(e);
    }
  }
  
  public final void marshal(Object obj, Writer w)
    throws JAXBException
  {
    checkNotNull(obj, "obj", w, "writer");
    marshal(obj, new StreamResult(w));
  }
  
  public final void marshal(Object obj, ContentHandler handler)
    throws JAXBException
  {
    checkNotNull(obj, "obj", handler, "handler");
    marshal(obj, new SAXResult(handler));
  }
  
  public final void marshal(Object obj, Node node)
    throws JAXBException
  {
    checkNotNull(obj, "obj", node, "node");
    marshal(obj, new DOMResult(node));
  }
  
  public Node getNode(Object obj)
    throws JAXBException
  {
    checkNotNull(obj, "obj", Boolean.TRUE, "foo");
    
    throw new UnsupportedOperationException();
  }
  
  protected String getEncoding()
  {
    return this.encoding;
  }
  
  protected void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }
  
  protected String getSchemaLocation()
  {
    return this.schemaLocation;
  }
  
  protected void setSchemaLocation(String location)
  {
    this.schemaLocation = location;
  }
  
  protected String getNoNSSchemaLocation()
  {
    return this.noNSSchemaLocation;
  }
  
  protected void setNoNSSchemaLocation(String location)
  {
    this.noNSSchemaLocation = location;
  }
  
  protected boolean isFormattedOutput()
  {
    return this.formattedOutput;
  }
  
  protected void setFormattedOutput(boolean v)
  {
    this.formattedOutput = v;
  }
  
  protected boolean isFragment()
  {
    return this.fragment;
  }
  
  protected void setFragment(boolean v)
  {
    this.fragment = v;
  }
  
  static String[] aliases = { "UTF-8", "UTF8", "UTF-16", "Unicode", "UTF-16BE", "UnicodeBigUnmarked", "UTF-16LE", "UnicodeLittleUnmarked", "US-ASCII", "ASCII", "TIS-620", "TIS620", "ISO-10646-UCS-2", "Unicode", "EBCDIC-CP-US", "cp037", "EBCDIC-CP-CA", "cp037", "EBCDIC-CP-NL", "cp037", "EBCDIC-CP-WT", "cp037", "EBCDIC-CP-DK", "cp277", "EBCDIC-CP-NO", "cp277", "EBCDIC-CP-FI", "cp278", "EBCDIC-CP-SE", "cp278", "EBCDIC-CP-IT", "cp280", "EBCDIC-CP-ES", "cp284", "EBCDIC-CP-GB", "cp285", "EBCDIC-CP-FR", "cp297", "EBCDIC-CP-AR1", "cp420", "EBCDIC-CP-HE", "cp424", "EBCDIC-CP-BE", "cp500", "EBCDIC-CP-CH", "cp500", "EBCDIC-CP-ROECE", "cp870", "EBCDIC-CP-YU", "cp870", "EBCDIC-CP-IS", "cp871", "EBCDIC-CP-AR2", "cp918" };
  
  protected String getJavaEncoding(String encoding)
    throws UnsupportedEncodingException
  {
    try
    {
      "1".getBytes(encoding);
      return encoding;
    }
    catch (UnsupportedEncodingException e)
    {
      for (int i = 0; i < aliases.length; i += 2) {
        if (encoding.equals(aliases[i]))
        {
          "1".getBytes(aliases[(i + 1)]);
          return aliases[(i + 1)];
        }
      }
      throw new UnsupportedEncodingException(encoding);
    }
  }
  
  public void setProperty(String name, Object value)
    throws PropertyException
  {
    if (name == null) {
      throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "name"));
    }
    if ("jaxb.encoding".equals(name))
    {
      checkString(name, value);
      setEncoding((String)value);
      return;
    }
    if ("jaxb.formatted.output".equals(name))
    {
      checkBoolean(name, value);
      setFormattedOutput(((Boolean)value).booleanValue());
      return;
    }
    if ("jaxb.noNamespaceSchemaLocation".equals(name))
    {
      checkString(name, value);
      setNoNSSchemaLocation((String)value);
      return;
    }
    if ("jaxb.schemaLocation".equals(name))
    {
      checkString(name, value);
      setSchemaLocation((String)value);
      return;
    }
    if ("jaxb.fragment".equals(name))
    {
      checkBoolean(name, value);
      setFragment(((Boolean)value).booleanValue());
      return;
    }
    throw new PropertyException(name, value);
  }
  
  public Object getProperty(String name)
    throws PropertyException
  {
    if (name == null) {
      throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", "name"));
    }
    if ("jaxb.encoding".equals(name)) {
      return getEncoding();
    }
    if ("jaxb.formatted.output".equals(name)) {
      return isFormattedOutput() ? Boolean.TRUE : Boolean.FALSE;
    }
    if ("jaxb.noNamespaceSchemaLocation".equals(name)) {
      return getNoNSSchemaLocation();
    }
    if ("jaxb.schemaLocation".equals(name)) {
      return getSchemaLocation();
    }
    if ("jaxb.fragment".equals(name)) {
      return isFragment() ? Boolean.TRUE : Boolean.FALSE;
    }
    throw new PropertyException(name);
  }
  
  public ValidationEventHandler getEventHandler()
    throws JAXBException
  {
    return this.eventHandler;
  }
  
  public void setEventHandler(ValidationEventHandler handler)
    throws JAXBException
  {
    if (handler == null) {
      this.eventHandler = new DefaultValidationEventHandler();
    } else {
      this.eventHandler = handler;
    }
  }
  
  private void checkBoolean(String name, Object value)
    throws PropertyException
  {
    if (!(value instanceof Boolean)) {
      throw new PropertyException(Messages.format("AbstractMarshallerImpl.MustBeBoolean", name));
    }
  }
  
  private void checkString(String name, Object value)
    throws PropertyException
  {
    if (!(value instanceof String)) {
      throw new PropertyException(Messages.format("AbstractMarshallerImpl.MustBeString", name));
    }
  }
  
  private void checkNotNull(Object o1, String o1Name, Object o2, String o2Name)
  {
    if (o1 == null) {
      throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", o1Name));
    }
    if (o2 == null) {
      throw new IllegalArgumentException(Messages.format("Shared.MustNotBeNull", o2Name));
    }
  }
  
  public void marshal(Object obj, XMLEventWriter writer)
    throws JAXBException
  {
    throw new UnsupportedOperationException();
  }
  
  public void marshal(Object obj, XMLStreamWriter writer)
    throws JAXBException
  {
    throw new UnsupportedOperationException();
  }
  
  public void setSchema(Schema schema)
  {
    throw new UnsupportedOperationException();
  }
  
  public Schema getSchema()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setAdapter(XmlAdapter adapter)
  {
    if (adapter == null) {
      throw new IllegalArgumentException();
    }
    setAdapter(adapter.getClass(), adapter);
  }
  
  public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter)
  {
    throw new UnsupportedOperationException();
  }
  
  public <A extends XmlAdapter> A getAdapter(Class<A> type)
  {
    throw new UnsupportedOperationException();
  }
  
  public void setAttachmentMarshaller(AttachmentMarshaller am)
  {
    throw new UnsupportedOperationException();
  }
  
  public AttachmentMarshaller getAttachmentMarshaller()
  {
    throw new UnsupportedOperationException();
  }
  
  public void setListener(Marshaller.Listener listener)
  {
    throw new UnsupportedOperationException();
  }
  
  public Marshaller.Listener getListener()
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\helpers\AbstractMarshallerImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */