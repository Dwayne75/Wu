package com.sun.tools.xjc.reader.internalizer;

import com.sun.istack.NotNull;
import com.sun.istack.XMLStreamReaderToContentHandler;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.marshaller.DataWriter;
import com.sun.xml.xsom.parser.JAXPParser;
import com.sun.xml.xsom.parser.XMLParser;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public final class DOMForest
{
  private final Map<String, Document> core = new HashMap();
  private final Set<String> rootDocuments = new HashSet();
  public final LocatorTable locatorTable = new LocatorTable();
  public final Set<Element> outerMostBindings = new HashSet();
  private EntityResolver entityResolver = null;
  private ErrorReceiver errorReceiver = null;
  protected final InternalizationLogic logic;
  private final SAXParserFactory parserFactory;
  private final DocumentBuilder documentBuilder;
  
  public DOMForest(SAXParserFactory parserFactory, DocumentBuilder documentBuilder, InternalizationLogic logic)
  {
    this.parserFactory = parserFactory;
    this.documentBuilder = documentBuilder;
    this.logic = logic;
  }
  
  public DOMForest(InternalizationLogic logic)
  {
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      this.documentBuilder = dbf.newDocumentBuilder();
      
      this.parserFactory = SAXParserFactory.newInstance();
      this.parserFactory.setNamespaceAware(true);
    }
    catch (ParserConfigurationException e)
    {
      throw new AssertionError(e);
    }
    this.logic = logic;
  }
  
  public Document get(String systemId)
  {
    Document doc = (Document)this.core.get(systemId);
    if ((doc == null) && (systemId.startsWith("file:/")) && (!systemId.startsWith("file://"))) {
      doc = (Document)this.core.get("file://" + systemId.substring(5));
    }
    String systemPath;
    if ((doc == null) && (systemId.startsWith("file:")))
    {
      systemPath = getPath(systemId);
      for (String key : this.core.keySet()) {
        if ((key.startsWith("file:")) && (getPath(key).equalsIgnoreCase(systemPath)))
        {
          doc = (Document)this.core.get(key);
          break;
        }
      }
    }
    return doc;
  }
  
  private String getPath(String key)
  {
    key = key.substring(5);
    while ((key.length() > 0) && (key.charAt(0) == '/')) {
      key = key.substring(1);
    }
    return key;
  }
  
  public Set<String> getRootDocuments()
  {
    return Collections.unmodifiableSet(this.rootDocuments);
  }
  
  public Document getOneDocument()
  {
    for (Document dom : this.core.values()) {
      if (!dom.getDocumentElement().getNamespaceURI().equals("http://java.sun.com/xml/ns/jaxb")) {
        return dom;
      }
    }
    throw new AssertionError();
  }
  
  public boolean checkSchemaCorrectness(ErrorReceiver errorHandler)
  {
    try
    {
      SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
      ErrorReceiverFilter filter = new ErrorReceiverFilter(errorHandler);
      sf.setErrorHandler(filter);
      Set<String> roots = getRootDocuments();
      Source[] sources = new Source[roots.size()];
      int i = 0;
      for (String root : roots) {
        sources[(i++)] = new DOMSource(get(root), root);
      }
      sf.newSchema(sources);
      return !filter.hadError();
    }
    catch (SAXException e) {}
    return false;
  }
  
  public String getSystemId(Document dom)
  {
    for (Map.Entry<String, Document> e : this.core.entrySet()) {
      if (e.getValue() == dom) {
        return (String)e.getKey();
      }
    }
    return null;
  }
  
  public Document parse(InputSource source, boolean root)
    throws SAXException
  {
    if (source.getSystemId() == null) {
      throw new IllegalArgumentException();
    }
    return parse(source.getSystemId(), source, root);
  }
  
  public Document parse(String systemId, boolean root)
    throws SAXException, IOException
  {
    systemId = normalizeSystemId(systemId);
    if (this.core.containsKey(systemId)) {
      return (Document)this.core.get(systemId);
    }
    InputSource is = null;
    if (this.entityResolver != null) {
      is = this.entityResolver.resolveEntity(null, systemId);
    }
    if (is == null) {
      is = new InputSource(systemId);
    }
    return parse(systemId, is, root);
  }
  
  private ContentHandler getParserHandler(Document dom)
  {
    ContentHandler handler = new DOMBuilder(dom, this.locatorTable, this.outerMostBindings);
    handler = new WhitespaceStripper(handler, this.errorReceiver, this.entityResolver);
    handler = new VersionChecker(handler, this.errorReceiver, this.entityResolver);
    
    XMLFilterImpl f = this.logic.createExternalReferenceFinder(this);
    f.setContentHandler(handler);
    if (this.errorReceiver != null) {
      f.setErrorHandler(this.errorReceiver);
    }
    if (this.entityResolver != null) {
      f.setEntityResolver(this.entityResolver);
    }
    return f;
  }
  
  public Handler getParserHandler(String systemId, boolean root)
  {
    final Document dom = this.documentBuilder.newDocument();
    this.core.put(systemId, dom);
    if (root) {
      this.rootDocuments.add(systemId);
    }
    ContentHandler handler = getParserHandler(dom);
    
    HandlerImpl x = new HandlerImpl(dom)
    {
      public Document getDocument()
      {
        return dom;
      }
    };
    x.setContentHandler(handler);
    
    return x;
  }
  
  public Document parse(String systemId, InputSource inputSource, boolean root)
    throws SAXException
  {
    Document dom = this.documentBuilder.newDocument();
    
    systemId = normalizeSystemId(systemId);
    
    this.core.put(systemId, dom);
    if (root) {
      this.rootDocuments.add(systemId);
    }
    try
    {
      XMLReader reader = this.parserFactory.newSAXParser().getXMLReader();
      reader.setContentHandler(getParserHandler(dom));
      if (this.errorReceiver != null) {
        reader.setErrorHandler(this.errorReceiver);
      }
      if (this.entityResolver != null) {
        reader.setEntityResolver(this.entityResolver);
      }
      reader.parse(inputSource);
    }
    catch (ParserConfigurationException e)
    {
      this.errorReceiver.error(e.getMessage(), e);
      this.core.remove(systemId);
      this.rootDocuments.remove(systemId);
      return null;
    }
    catch (IOException e)
    {
      this.errorReceiver.error(e.getMessage(), e);
      this.core.remove(systemId);
      this.rootDocuments.remove(systemId);
      return null;
    }
    return dom;
  }
  
  private String normalizeSystemId(String systemId)
  {
    try
    {
      systemId = new URI(systemId).normalize().toString();
    }
    catch (URISyntaxException e) {}
    return systemId;
  }
  
  public Document parse(String systemId, XMLStreamReader parser, boolean root)
    throws XMLStreamException
  {
    Document dom = this.documentBuilder.newDocument();
    
    systemId = normalizeSystemId(systemId);
    if (root) {
      this.rootDocuments.add(systemId);
    }
    if (systemId == null) {
      throw new IllegalArgumentException("system id cannot be null");
    }
    this.core.put(systemId, dom);
    
    new XMLStreamReaderToContentHandler(parser, getParserHandler(dom), false, false).bridge();
    
    return dom;
  }
  
  public SCDBasedBindingSet transform(boolean enableSCD)
  {
    return Internalizer.transform(this, enableSCD);
  }
  
  public void weakSchemaCorrectnessCheck(SchemaFactory sf)
  {
    List<SAXSource> sources = new ArrayList();
    for (String systemId : getRootDocuments())
    {
      Document dom = get(systemId);
      if (!dom.getDocumentElement().getNamespaceURI().equals("http://java.sun.com/xml/ns/jaxb"))
      {
        SAXSource ss = createSAXSource(systemId);
        try
        {
          ss.getXMLReader().setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        }
        catch (SAXException e)
        {
          throw new AssertionError(e);
        }
        sources.add(ss);
      }
    }
    try
    {
      sf.newSchema((Source[])sources.toArray(new SAXSource[0]));
    }
    catch (SAXException e) {}catch (RuntimeException e)
    {
      try
      {
        sf.getErrorHandler().warning(new SAXParseException(Messages.format("ERR_GENERAL_SCHEMA_CORRECTNESS_ERROR", new Object[] { e.getMessage() }), null, null, -1, -1, e));
      }
      catch (SAXException _) {}
    }
  }
  
  @NotNull
  public SAXSource createSAXSource(String systemId)
  {
    ContentHandlerNamespacePrefixAdapter reader = new ContentHandlerNamespacePrefixAdapter(new XMLFilterImpl()
    {
      public void parse(InputSource input)
        throws SAXException, IOException
      {
        DOMForest.this.createParser().parse(input, this, this, this);
      }
      
      public void parse(String systemId)
        throws SAXException, IOException
      {
        parse(new InputSource(systemId));
      }
    });
    return new SAXSource(reader, new InputSource(systemId));
  }
  
  public XMLParser createParser()
  {
    return new DOMForestParser(this, new JAXPParser());
  }
  
  public EntityResolver getEntityResolver()
  {
    return this.entityResolver;
  }
  
  public void setEntityResolver(EntityResolver entityResolver)
  {
    this.entityResolver = entityResolver;
  }
  
  public ErrorReceiver getErrorHandler()
  {
    return this.errorReceiver;
  }
  
  public void setErrorHandler(ErrorReceiver errorHandler)
  {
    this.errorReceiver = errorHandler;
  }
  
  public Document[] listDocuments()
  {
    return (Document[])this.core.values().toArray(new Document[this.core.size()]);
  }
  
  public String[] listSystemIDs()
  {
    return (String[])this.core.keySet().toArray(new String[this.core.keySet().size()]);
  }
  
  public void dump(OutputStream out)
    throws IOException
  {
    try
    {
      it = TransformerFactory.newInstance().newTransformer();
      for (Map.Entry<String, Document> e : this.core.entrySet())
      {
        out.write(("---<< " + (String)e.getKey() + '\n').getBytes());
        
        DataWriter dw = new DataWriter(new OutputStreamWriter(out), null);
        dw.setIndentStep("  ");
        it.transform(new DOMSource((Node)e.getValue()), new SAXResult(dw));
        
        out.write("\n\n\n".getBytes());
      }
    }
    catch (TransformerException e)
    {
      Transformer it;
      e.printStackTrace();
    }
  }
  
  private static abstract class HandlerImpl
    extends XMLFilterImpl
    implements DOMForest.Handler
  {}
  
  public static abstract interface Handler
    extends ContentHandler
  {
    public abstract Document getDocument();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\internalizer\DOMForest.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */