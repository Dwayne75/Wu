package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.DatatypeConverterImpl;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.DataWriter;
import com.sun.xml.bind.marshaller.DumbEscapeHandler;
import com.sun.xml.bind.marshaller.MinimumEscapeHandler;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.bind.marshaller.NioEscapeHandler;
import com.sun.xml.bind.marshaller.SAX2DOMEx;
import com.sun.xml.bind.marshaller.XMLWriter;
import com.sun.xml.bind.v2.runtime.output.C14nXmlOutput;
import com.sun.xml.bind.v2.runtime.output.Encoded;
import com.sun.xml.bind.v2.runtime.output.ForkXmlOutput;
import com.sun.xml.bind.v2.runtime.output.IndentingUTF8XmlOutput;
import com.sun.xml.bind.v2.runtime.output.NamespaceContextImpl;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;
import com.sun.xml.bind.v2.runtime.output.UTF8XmlOutput;
import com.sun.xml.bind.v2.runtime.output.XMLEventWriterOutput;
import com.sun.xml.bind.v2.runtime.output.XMLStreamWriterOutput;
import com.sun.xml.bind.v2.runtime.output.XmlOutput;
import com.sun.xml.bind.v2.util.FatalAdapter;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller.Listener;
import javax.xml.bind.PropertyException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.helpers.AbstractMarshallerImpl;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public final class MarshallerImpl
  extends AbstractMarshallerImpl
  implements ValidationEventHandler
{
  private String indent = "    ";
  private NamespacePrefixMapper prefixMapper = null;
  private CharacterEscapeHandler escapeHandler = null;
  private String header = null;
  final JAXBContextImpl context;
  protected final XMLSerializer serializer;
  private Schema schema;
  private Marshaller.Listener externalListener = null;
  private boolean c14nSupport;
  private Flushable toBeFlushed;
  private Closeable toBeClosed;
  protected static final String INDENT_STRING = "com.sun.xml.bind.indentString";
  protected static final String PREFIX_MAPPER = "com.sun.xml.bind.namespacePrefixMapper";
  protected static final String ENCODING_HANDLER = "com.sun.xml.bind.characterEscapeHandler";
  protected static final String ENCODING_HANDLER2 = "com.sun.xml.bind.marshaller.CharacterEscapeHandler";
  protected static final String XMLDECLARATION = "com.sun.xml.bind.xmlDeclaration";
  protected static final String XML_HEADERS = "com.sun.xml.bind.xmlHeaders";
  protected static final String C14N = "com.sun.xml.bind.c14n";
  protected static final String OBJECT_IDENTITY_CYCLE_DETECTION = "com.sun.xml.bind.objectIdentitityCycleDetection";
  
  public MarshallerImpl(JAXBContextImpl c, AssociationMap assoc)
  {
    DatatypeConverter.setDatatypeConverter(DatatypeConverterImpl.theInstance);
    
    this.context = c;
    this.serializer = new XMLSerializer(this);
    this.c14nSupport = this.context.c14nSupport;
    try
    {
      setEventHandler(this);
    }
    catch (JAXBException e)
    {
      throw new AssertionError(e);
    }
  }
  
  public JAXBContextImpl getContext()
  {
    return this.context;
  }
  
  public void marshal(Object obj, OutputStream out, NamespaceContext inscopeNamespace)
    throws JAXBException
  {
    write(obj, createWriter(out), new StAXPostInitAction(inscopeNamespace, this.serializer));
  }
  
  public void marshal(Object obj, XMLStreamWriter writer)
    throws JAXBException
  {
    write(obj, XMLStreamWriterOutput.create(writer, this.context), new StAXPostInitAction(writer, this.serializer));
  }
  
  public void marshal(Object obj, XMLEventWriter writer)
    throws JAXBException
  {
    write(obj, new XMLEventWriterOutput(writer), new StAXPostInitAction(writer, this.serializer));
  }
  
  public void marshal(Object obj, XmlOutput output)
    throws JAXBException
  {
    write(obj, output, null);
  }
  
  final XmlOutput createXmlOutput(Result result)
    throws JAXBException
  {
    if ((result instanceof SAXResult)) {
      return new SAXOutput(((SAXResult)result).getHandler());
    }
    if ((result instanceof DOMResult))
    {
      Node node = ((DOMResult)result).getNode();
      if (node == null)
      {
        Document doc = JAXBContextImpl.createDom();
        ((DOMResult)result).setNode(doc);
        return new SAXOutput(new SAX2DOMEx(doc));
      }
      return new SAXOutput(new SAX2DOMEx(node));
    }
    if ((result instanceof StreamResult))
    {
      StreamResult sr = (StreamResult)result;
      if (sr.getWriter() != null) {
        return createWriter(sr.getWriter());
      }
      if (sr.getOutputStream() != null) {
        return createWriter(sr.getOutputStream());
      }
      if (sr.getSystemId() != null)
      {
        String fileURL = sr.getSystemId();
        if (fileURL.startsWith("file:///")) {
          if (fileURL.substring(8).indexOf(":") > 0) {
            fileURL = fileURL.substring(8);
          } else {
            fileURL = fileURL.substring(7);
          }
        }
        if (fileURL.startsWith("file:/")) {
          if (fileURL.substring(6).indexOf(":") > 0) {
            fileURL = fileURL.substring(6);
          } else {
            fileURL = fileURL.substring(5);
          }
        }
        try
        {
          FileOutputStream fos = new FileOutputStream(fileURL);
          assert (this.toBeClosed == null);
          this.toBeClosed = fos;
          return createWriter(fos);
        }
        catch (IOException e)
        {
          throw new MarshalException(e);
        }
      }
    }
    throw new MarshalException(Messages.UNSUPPORTED_RESULT.format(new Object[0]));
  }
  
  final Runnable createPostInitAction(Result result)
  {
    if ((result instanceof DOMResult))
    {
      Node node = ((DOMResult)result).getNode();
      return new DomPostInitAction(node, this.serializer);
    }
    return null;
  }
  
  public void marshal(Object target, Result result)
    throws JAXBException
  {
    write(target, createXmlOutput(result), createPostInitAction(result));
  }
  
  protected final <T> void write(Name rootTagName, JaxBeanInfo<T> bi, T obj, XmlOutput out, Runnable postInitAction)
    throws JAXBException
  {
    try
    {
      return;
    }
    catch (IOException e) {}finally
    {
      cleanUp();
    }
  }
  
  private void write(Object obj, XmlOutput out, Runnable postInitAction)
    throws JAXBException
  {
    try
    {
      if (obj == null) {
        throw new IllegalArgumentException(Messages.NOT_MARSHALLABLE.format(new Object[0]));
      }
      if (this.schema != null)
      {
        ValidatorHandler validator = this.schema.newValidatorHandler();
        validator.setErrorHandler(new FatalAdapter(this.serializer));
        
        XMLFilterImpl f = new XMLFilterImpl()
        {
          public void startPrefixMapping(String prefix, String uri)
            throws SAXException
          {
            super.startPrefixMapping(prefix.intern(), uri.intern());
          }
        };
        f.setContentHandler(validator);
        out = new ForkXmlOutput(new SAXOutput(f)
        {
          public void startDocument(XMLSerializer serializer, boolean fragment, int[] nsUriIndex2prefixIndex, NamespaceContextImpl nsContext)
            throws SAXException, IOException, XMLStreamException
          {
            super.startDocument(serializer, false, nsUriIndex2prefixIndex, nsContext);
          }
          
          public void endDocument(boolean fragment)
            throws SAXException, IOException, XMLStreamException
          {
            super.endDocument(false);
          }
        }, out);
      }
      try
      {
        prewrite(out, isFragment(), postInitAction);
        this.serializer.childAsRoot(obj);
      }
      catch (SAXException e)
      {
        throw new MarshalException(e);
      }
      catch (IOException e)
      {
        throw new MarshalException(e);
      }
      catch (XMLStreamException e)
      {
        throw new MarshalException(e);
      }
      finally
      {
        this.serializer.close();
      }
    }
    finally
    {
      cleanUp();
    }
  }
  
  private void cleanUp()
  {
    if (this.toBeFlushed != null) {
      try
      {
        this.toBeFlushed.flush();
      }
      catch (IOException e) {}
    }
    if (this.toBeClosed != null) {
      try
      {
        this.toBeClosed.close();
      }
      catch (IOException e) {}
    }
    this.toBeFlushed = null;
    this.toBeClosed = null;
  }
  
  private void prewrite(XmlOutput out, boolean fragment, Runnable postInitAction)
    throws IOException, SAXException, XMLStreamException
  {
    this.serializer.startDocument(out, fragment, getSchemaLocation(), getNoNSSchemaLocation());
    if (postInitAction != null) {
      postInitAction.run();
    }
    if (this.prefixMapper != null)
    {
      String[] decls = this.prefixMapper.getContextualNamespaceDecls();
      if (decls != null) {
        for (int i = 0; i < decls.length; i += 2)
        {
          String prefix = decls[i];
          String nsUri = decls[(i + 1)];
          if ((nsUri != null) && (prefix != null)) {
            this.serializer.addInscopeBinding(nsUri, prefix);
          }
        }
      }
    }
    this.serializer.setPrefixMapper(this.prefixMapper);
  }
  
  private void postwrite()
    throws IOException, SAXException, XMLStreamException
  {
    this.serializer.endDocument();
    this.serializer.reconcileID();
  }
  
  protected CharacterEscapeHandler createEscapeHandler(String encoding)
  {
    if (this.escapeHandler != null) {
      return this.escapeHandler;
    }
    if (encoding.startsWith("UTF")) {
      return MinimumEscapeHandler.theInstance;
    }
    try
    {
      return new NioEscapeHandler(getJavaEncoding(encoding));
    }
    catch (Throwable e) {}
    return DumbEscapeHandler.theInstance;
  }
  
  public XmlOutput createWriter(Writer w, String encoding)
  {
    if (!(w instanceof BufferedWriter)) {
      w = new BufferedWriter(w);
    }
    assert (this.toBeFlushed == null);
    this.toBeFlushed = w;
    
    CharacterEscapeHandler ceh = createEscapeHandler(encoding);
    XMLWriter xw;
    XMLWriter xw;
    if (isFormattedOutput())
    {
      DataWriter d = new DataWriter(w, encoding, ceh);
      d.setIndentStep(this.indent);
      xw = d;
    }
    else
    {
      xw = new XMLWriter(w, encoding, ceh);
    }
    xw.setXmlDecl(!isFragment());
    xw.setHeader(this.header);
    return new SAXOutput(xw);
  }
  
  public XmlOutput createWriter(Writer w)
  {
    return createWriter(w, getEncoding());
  }
  
  public XmlOutput createWriter(OutputStream os)
    throws JAXBException
  {
    return createWriter(os, getEncoding());
  }
  
  public XmlOutput createWriter(OutputStream os, String encoding)
    throws JAXBException
  {
    if (encoding.equals("UTF-8"))
    {
      Encoded[] table = this.context.getUTF8NameTable();
      UTF8XmlOutput out;
      UTF8XmlOutput out;
      if (isFormattedOutput())
      {
        out = new IndentingUTF8XmlOutput(os, this.indent, table);
      }
      else
      {
        UTF8XmlOutput out;
        if (this.c14nSupport) {
          out = new C14nXmlOutput(os, table, this.context.c14nSupport);
        } else {
          out = new UTF8XmlOutput(os, table);
        }
      }
      if (this.header != null) {
        out.setHeader(this.header);
      }
      return out;
    }
    try
    {
      return createWriter(new OutputStreamWriter(os, getJavaEncoding(encoding)), encoding);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new MarshalException(Messages.UNSUPPORTED_ENCODING.format(new Object[] { encoding }), e);
    }
  }
  
  public Object getProperty(String name)
    throws PropertyException
  {
    if ("com.sun.xml.bind.indentString".equals(name)) {
      return this.indent;
    }
    if (("com.sun.xml.bind.characterEscapeHandler".equals(name)) || ("com.sun.xml.bind.marshaller.CharacterEscapeHandler".equals(name))) {
      return this.escapeHandler;
    }
    if ("com.sun.xml.bind.namespacePrefixMapper".equals(name)) {
      return this.prefixMapper;
    }
    if ("com.sun.xml.bind.xmlDeclaration".equals(name)) {
      return Boolean.valueOf(!isFragment());
    }
    if ("com.sun.xml.bind.xmlHeaders".equals(name)) {
      return this.header;
    }
    if ("com.sun.xml.bind.c14n".equals(name)) {
      return Boolean.valueOf(this.c14nSupport);
    }
    if ("com.sun.xml.bind.objectIdentitityCycleDetection".equals(name)) {
      return Boolean.valueOf(this.serializer.getObjectIdentityCycleDetection());
    }
    return super.getProperty(name);
  }
  
  public void setProperty(String name, Object value)
    throws PropertyException
  {
    if ("com.sun.xml.bind.indentString".equals(name))
    {
      checkString(name, value);
      this.indent = ((String)value);
      return;
    }
    if (("com.sun.xml.bind.characterEscapeHandler".equals(name)) || ("com.sun.xml.bind.marshaller.CharacterEscapeHandler".equals(name)))
    {
      if (!(value instanceof CharacterEscapeHandler)) {
        throw new PropertyException(Messages.MUST_BE_X.format(new Object[] { name, CharacterEscapeHandler.class.getName(), value.getClass().getName() }));
      }
      this.escapeHandler = ((CharacterEscapeHandler)value);
      return;
    }
    if ("com.sun.xml.bind.namespacePrefixMapper".equals(name))
    {
      if (!(value instanceof NamespacePrefixMapper)) {
        throw new PropertyException(Messages.MUST_BE_X.format(new Object[] { name, NamespacePrefixMapper.class.getName(), value.getClass().getName() }));
      }
      this.prefixMapper = ((NamespacePrefixMapper)value);
      return;
    }
    if ("com.sun.xml.bind.xmlDeclaration".equals(name))
    {
      checkBoolean(name, value);
      
      super.setProperty("jaxb.fragment", Boolean.valueOf(!((Boolean)value).booleanValue()));
      return;
    }
    if ("com.sun.xml.bind.xmlHeaders".equals(name))
    {
      checkString(name, value);
      this.header = ((String)value);
      return;
    }
    if ("com.sun.xml.bind.c14n".equals(name))
    {
      checkBoolean(name, value);
      this.c14nSupport = ((Boolean)value).booleanValue();
      return;
    }
    if ("com.sun.xml.bind.objectIdentitityCycleDetection".equals(name))
    {
      checkBoolean(name, value);
      this.serializer.setObjectIdentityCycleDetection(((Boolean)value).booleanValue());
      return;
    }
    super.setProperty(name, value);
  }
  
  private void checkBoolean(String name, Object value)
    throws PropertyException
  {
    if (!(value instanceof Boolean)) {
      throw new PropertyException(Messages.MUST_BE_X.format(new Object[] { name, Boolean.class.getName(), value.getClass().getName() }));
    }
  }
  
  private void checkString(String name, Object value)
    throws PropertyException
  {
    if (!(value instanceof String)) {
      throw new PropertyException(Messages.MUST_BE_X.format(new Object[] { name, String.class.getName(), value.getClass().getName() }));
    }
  }
  
  public <A extends XmlAdapter> void setAdapter(Class<A> type, A adapter)
  {
    if (type == null) {
      throw new IllegalArgumentException();
    }
    this.serializer.putAdapter(type, adapter);
  }
  
  public <A extends XmlAdapter> A getAdapter(Class<A> type)
  {
    if (type == null) {
      throw new IllegalArgumentException();
    }
    if (this.serializer.containsAdapter(type)) {
      return this.serializer.getAdapter(type);
    }
    return null;
  }
  
  public void setAttachmentMarshaller(AttachmentMarshaller am)
  {
    this.serializer.attachmentMarshaller = am;
  }
  
  public AttachmentMarshaller getAttachmentMarshaller()
  {
    return this.serializer.attachmentMarshaller;
  }
  
  public Schema getSchema()
  {
    return this.schema;
  }
  
  public void setSchema(Schema s)
  {
    this.schema = s;
  }
  
  public boolean handleEvent(ValidationEvent event)
  {
    return false;
  }
  
  public Marshaller.Listener getListener()
  {
    return this.externalListener;
  }
  
  public void setListener(Marshaller.Listener listener)
  {
    this.externalListener = listener;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\MarshallerImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */