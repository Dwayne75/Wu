package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSDeclaration;
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
import java.util.Set;
import java.util.Stack;
import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Attributes;
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
  private final Stack elementNames = new Stack();
  
  NGCCRuntimeEx(ParserContext _parser)
  {
    this(_parser, false);
  }
  
  private NGCCRuntimeEx(ParserContext _parser, boolean chameleonMode)
  {
    this.parser = _parser;
    this.chameleonMode = chameleonMode;
    
    this.currentContext = new NGCCRuntimeEx.Context("", "", null);
    this.currentContext = new NGCCRuntimeEx.Context("xml", "http://www.w3.org/XML/1998/namespace", this.currentContext);
  }
  
  public void checkDoubleDefError(XSDeclaration c)
    throws SAXException
  {
    if (c == null) {
      return;
    }
    reportError(Messages.format("DoubleDefinition", c.getName()), getLocator());
    reportError(Messages.format("DoubleDefinition.Original"), c.getLocator());
  }
  
  public void addPatcher(Patch patcher)
  {
    this.parser.patcherManager.addPatcher(patcher);
  }
  
  public void reportError(String msg, Locator loc)
    throws SAXException
  {
    this.parser.patcherManager.reportError(msg, loc);
  }
  
  private InputSource resolveRelativeURL(String relativeUri)
    throws SAXException
  {
    String baseUri = getLocator().getSystemId();
    if (baseUri == null) {
      baseUri = this.documentSystemId;
    }
    String systemId = Uri.resolve(baseUri, relativeUri);
    return new InputSource(systemId);
  }
  
  public void includeSchema(String schemaLocation)
    throws SAXException
  {
    NGCCRuntimeEx runtime = new NGCCRuntimeEx(this.parser, this.chameleonMode);
    runtime.currentSchema = this.currentSchema;
    runtime.blockDefault = this.blockDefault;
    runtime.finalDefault = this.finalDefault;
    if (schemaLocation == null)
    {
      SAXParseException e = new SAXParseException(Messages.format("MissingSchemaLocation"), getLocator());
      
      this.parser.errorHandler.fatalError(e);
      throw e;
    }
    runtime.parseEntity(resolveRelativeURL(schemaLocation), true, this.currentSchema.getTargetNamespace(), getLocator());
  }
  
  public void importSchema(String ns, String schemaLocation)
    throws SAXException
  {
    if (schemaLocation == null) {
      return;
    }
    NGCCRuntimeEx newRuntime = new NGCCRuntimeEx(this.parser);
    newRuntime.parseEntity(resolveRelativeURL(schemaLocation), false, ns, getLocator());
  }
  
  public boolean hasAlreadyBeenRead(String targetNamespace)
  {
    if (this.documentSystemId == null) {
      return false;
    }
    if (this.documentSystemId.startsWith("file:///")) {
      this.documentSystemId = ("file:/" + this.documentSystemId.substring(8));
    }
    ParserContext.DocumentIdentity docIdentity = new ParserContext.DocumentIdentity(targetNamespace, this.documentSystemId);
    
    return !this.parser.parsedDocuments.add(docIdentity);
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
  
  private NGCCRuntimeEx.Context currentContext = null;
  public static final String XMLSchemaNSURI = "http://www.w3.org/2001/XMLSchema";
  
  public ValidationContext createValidationContext()
  {
    return this.currentContext;
  }
  
  public void startPrefixMapping(String prefix, String uri)
    throws SAXException
  {
    super.startPrefixMapping(prefix, uri);
    this.currentContext = new NGCCRuntimeEx.Context(prefix, uri, this.currentContext);
  }
  
  public void endPrefixMapping(String prefix)
    throws SAXException
  {
    super.endPrefixMapping(prefix);
    this.currentContext = NGCCRuntimeEx.Context.access$000(this.currentContext);
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
      reportError(Messages.format("UndefinedPrefix", prefix), getLocator());
      
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
    SAXParseException e = new SAXParseException(MessageFormat.format("Unexpected {0} appears at line {1} column {2}", new Object[] { token, new Integer(getLocator().getLineNumber()), new Integer(getLocator().getColumnNumber()) }), getLocator());
    
    this.parser.errorHandler.fatalError(e);
    throw e;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\NGCCRuntimeEx.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */