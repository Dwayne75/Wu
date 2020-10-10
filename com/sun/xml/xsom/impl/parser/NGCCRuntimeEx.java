package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.impl.ForeignAttributesImpl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.UName;
import com.sun.xml.xsom.impl.parser.state.NGCCRuntime;
import com.sun.xml.xsom.impl.parser.state.Schema;
import com.sun.xml.xsom.impl.util.Uri;
import com.sun.xml.xsom.parser.AnnotationParser;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XMLParser;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public class NGCCRuntimeEx
  extends NGCCRuntime
  implements PatcherManager
{
  public final ParserContext parser;
  public SchemaImpl currentSchema;
  public int finalDefault = 0;
  public int blockDefault = 0;
  public boolean elementFormDefault = false;
  public boolean attributeFormDefault = false;
  public boolean chameleonMode = false;
  private String documentSystemId;
  private final Stack<String> elementNames = new Stack();
  private final NGCCRuntimeEx referer;
  public SchemaDocumentImpl document;
  
  NGCCRuntimeEx(ParserContext _parser)
  {
    this(_parser, false, null);
  }
  
  private NGCCRuntimeEx(ParserContext _parser, boolean chameleonMode, NGCCRuntimeEx referer)
  {
    this.parser = _parser;
    this.chameleonMode = chameleonMode;
    this.referer = referer;
    
    this.currentContext = new Context("", "", null);
    this.currentContext = new Context("xml", "http://www.w3.org/XML/1998/namespace", this.currentContext);
  }
  
  public void checkDoubleDefError(XSDeclaration c)
    throws SAXException
  {
    if ((c == null) || (ignorableDuplicateComponent(c))) {
      return;
    }
    reportError(Messages.format("DoubleDefinition", new Object[] { c.getName() }));
    reportError(Messages.format("DoubleDefinition.Original", new Object[0]), c.getLocator());
  }
  
  public static boolean ignorableDuplicateComponent(XSDeclaration c)
  {
    if (c.getTargetNamespace().equals("http://www.w3.org/2001/XMLSchema"))
    {
      if ((c instanceof XSSimpleType)) {
        return true;
      }
      if ((c.isGlobal()) && (c.getName().equals("anyType"))) {
        return true;
      }
    }
    return false;
  }
  
  public void addPatcher(Patch patcher)
  {
    this.parser.patcherManager.addPatcher(patcher);
  }
  
  public void addErrorChecker(Patch patcher)
  {
    this.parser.patcherManager.addErrorChecker(patcher);
  }
  
  public void reportError(String msg, Locator loc)
    throws SAXException
  {
    this.parser.patcherManager.reportError(msg, loc);
  }
  
  public void reportError(String msg)
    throws SAXException
  {
    reportError(msg, getLocator());
  }
  
  private InputSource resolveRelativeURL(String namespaceURI, String relativeUri)
    throws SAXException
  {
    try
    {
      String baseUri = getLocator().getSystemId();
      if (baseUri == null) {
        baseUri = this.documentSystemId;
      }
      String systemId = null;
      if (relativeUri != null) {
        systemId = Uri.resolve(baseUri, relativeUri);
      }
      EntityResolver er = this.parser.getEntityResolver();
      if (er != null)
      {
        InputSource is = er.resolveEntity(namespaceURI, systemId);
        if (is != null) {
          return is;
        }
      }
      if (systemId != null) {
        return new InputSource(systemId);
      }
      return null;
    }
    catch (IOException e)
    {
      SAXParseException se = new SAXParseException(e.getMessage(), getLocator(), e);
      this.parser.errorHandler.error(se);
    }
    return null;
  }
  
  public void includeSchema(String schemaLocation)
    throws SAXException
  {
    NGCCRuntimeEx runtime = new NGCCRuntimeEx(this.parser, this.chameleonMode, this);
    runtime.currentSchema = this.currentSchema;
    runtime.blockDefault = this.blockDefault;
    runtime.finalDefault = this.finalDefault;
    if (schemaLocation == null)
    {
      SAXParseException e = new SAXParseException(Messages.format("MissingSchemaLocation", new Object[0]), getLocator());
      
      this.parser.errorHandler.fatalError(e);
      throw e;
    }
    runtime.parseEntity(resolveRelativeURL(null, schemaLocation), true, this.currentSchema.getTargetNamespace(), getLocator());
  }
  
  public void importSchema(String ns, String schemaLocation)
    throws SAXException
  {
    NGCCRuntimeEx newRuntime = new NGCCRuntimeEx(this.parser, false, this);
    InputSource source = resolveRelativeURL(ns, schemaLocation);
    if (source != null) {
      newRuntime.parseEntity(source, false, ns, getLocator());
    }
  }
  
  public boolean hasAlreadyBeenRead()
  {
    if ((this.documentSystemId != null) && 
      (this.documentSystemId.startsWith("file:///"))) {
      this.documentSystemId = ("file:/" + this.documentSystemId.substring(8));
    }
    assert (this.document == null);
    this.document = new SchemaDocumentImpl(this.currentSchema, this.documentSystemId);
    
    SchemaDocumentImpl existing = (SchemaDocumentImpl)this.parser.parsedDocuments.get(this.document);
    if (existing == null) {
      this.parser.parsedDocuments.put(this.document, this.document);
    } else {
      this.document = existing;
    }
    assert (this.document != null);
    if (this.referer != null)
    {
      assert (this.referer.document != null) : ("referer " + this.referer.documentSystemId + " has docIdentity==null");
      this.referer.document.references.add(this.document);
      this.document.referers.add(this.referer.document);
    }
    return existing != null;
  }
  
  public void parseEntity(InputSource source, boolean includeMode, String expectedNamespace, Locator importLocation)
    throws SAXException
  {
    this.documentSystemId = source.getSystemId();
    try
    {
      Schema s = new Schema(this, includeMode, expectedNamespace);
      setRootHandler(s);
      try
      {
        this.parser.parser.parse(source, this, getErrorHandler(), this.parser.getEntityResolver());
      }
      catch (IOException e)
      {
        SAXParseException se = new SAXParseException(e.toString(), importLocation, e);
        
        this.parser.errorHandler.fatalError(se);
        throw se;
      }
    }
    catch (SAXException e)
    {
      this.parser.setErrorFlag();
      throw e;
    }
  }
  
  public AnnotationParser createAnnotationParser()
  {
    if (this.parser.getAnnotationParserFactory() == null) {
      return DefaultAnnotationParser.theInstance;
    }
    return this.parser.getAnnotationParserFactory().create();
  }
  
  public String getAnnotationContextElementName()
  {
    return (String)this.elementNames.get(this.elementNames.size() - 2);
  }
  
  public Locator copyLocator()
  {
    return new LocatorImpl(getLocator());
  }
  
  public ErrorHandler getErrorHandler()
  {
    return this.parser.errorHandler;
  }
  
  public void onEnterElementConsumed(String uri, String localName, String qname, Attributes atts)
    throws SAXException
  {
    super.onEnterElementConsumed(uri, localName, qname, atts);
    this.elementNames.push(localName);
  }
  
  public void onLeaveElementConsumed(String uri, String localName, String qname)
    throws SAXException
  {
    super.onLeaveElementConsumed(uri, localName, qname);
    this.elementNames.pop();
  }
  
  private static class Context
    implements ValidationContext
  {
    private final String prefix;
    private final String uri;
    private final Context previous;
    
    Context(String _prefix, String _uri, Context _context)
    {
      this.previous = _context;
      this.prefix = _prefix;
      this.uri = _uri;
    }
    
    public String resolveNamespacePrefix(String p)
    {
      if (p.equals(this.prefix)) {
        return this.uri;
      }
      if (this.previous == null) {
        return null;
      }
      return this.previous.resolveNamespacePrefix(p);
    }
    
    public String getBaseUri()
    {
      return null;
    }
    
    public boolean isNotation(String arg0)
    {
      return false;
    }
    
    public boolean isUnparsedEntity(String arg0)
    {
      return false;
    }
  }
  
  private Context currentContext = null;
  public static final String XMLSchemaNSURI = "http://www.w3.org/2001/XMLSchema";
  
  public ValidationContext createValidationContext()
  {
    return this.currentContext;
  }
  
  public XmlString createXmlString(String value)
  {
    if (value == null) {
      return null;
    }
    return new XmlString(value, createValidationContext());
  }
  
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    super.startPrefixMapping(prefix, uri);
    this.currentContext = new Context(prefix, uri, this.currentContext);
  }
  
  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    super.endPrefixMapping(prefix);
    this.currentContext = this.currentContext.previous;
  }
  
  public UName parseUName(String qname)
    throws SAXException
  {
    int idx = qname.indexOf(':');
    if (idx < 0)
    {
      String uri = resolveNamespacePrefix("");
      if ((uri.equals("")) && (this.chameleonMode)) {
        uri = this.currentSchema.getTargetNamespace();
      }
      return new UName(uri, qname, qname);
    }
    String prefix = qname.substring(0, idx);
    String uri = this.currentContext.resolveNamespacePrefix(prefix);
    if (uri == null)
    {
      reportError(Messages.format("UndefinedPrefix", new Object[] { prefix }));
      
      uri = "undefined";
    }
    return new UName(uri, qname.substring(idx + 1), qname);
  }
  
  public boolean parseBoolean(String v)
  {
    if (v == null) {
      return false;
    }
    v = v.trim();
    return (v.equals("true")) || (v.equals("1"));
  }
  
  protected void unexpectedX(String token)
    throws SAXException
  {
    SAXParseException e = new SAXParseException(MessageFormat.format("Unexpected {0} appears at line {1} column {2}", new Object[] { token, Integer.valueOf(getLocator().getLineNumber()), Integer.valueOf(getLocator().getColumnNumber()) }), getLocator());
    
    this.parser.errorHandler.fatalError(e);
    throw e;
  }
  
  public ForeignAttributesImpl parseForeignAttributes(ForeignAttributesImpl next)
  {
    ForeignAttributesImpl impl = new ForeignAttributesImpl(createValidationContext(), copyLocator(), next);
    
    Attributes atts = getCurrentAttributes();
    for (int i = 0; i < atts.getLength(); i++) {
      if (atts.getURI(i).length() > 0) {
        impl.addAttribute(atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getType(i), atts.getValue(i));
      }
    }
    return impl;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\NGCCRuntimeEx.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */