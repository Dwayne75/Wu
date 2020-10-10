package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ParserContext
{
  public final SchemaSetImpl schemaSet = new SchemaSetImpl();
  private final XSOMParser owner;
  final XMLParser parser;
  private final Vector<Patch> patchers = new Vector();
  private final Vector<Patch> errorCheckers = new Vector();
  public final Map<SchemaDocumentImpl, SchemaDocumentImpl> parsedDocuments = new HashMap();
  
  public ParserContext(XSOMParser owner, XMLParser parser)
  {
    this.owner = owner;
    this.parser = parser;
    try
    {
      parse(new InputSource(ParserContext.class.getResource("datatypes.xsd").toExternalForm()));
      
      SchemaImpl xs = (SchemaImpl)this.schemaSet.getSchema("http://www.w3.org/2001/XMLSchema");
      
      xs.addSimpleType(this.schemaSet.anySimpleType, true);
      xs.addComplexType(this.schemaSet.anyType, true);
    }
    catch (SAXException e)
    {
      if (e.getException() != null) {
        e.getException().printStackTrace();
      } else {
        e.printStackTrace();
      }
      throw new InternalError();
    }
  }
  
  public EntityResolver getEntityResolver()
  {
    return this.owner.getEntityResolver();
  }
  
  public AnnotationParserFactory getAnnotationParserFactory()
  {
    return this.owner.getAnnotationParserFactory();
  }
  
  public void parse(InputSource source)
    throws SAXException
  {
    newNGCCRuntime().parseEntity(source, false, null, null);
  }
  
  public XSSchemaSet getResult()
    throws SAXException
  {
    for (Patch patcher : this.patchers) {
      patcher.run();
    }
    this.patchers.clear();
    
    Iterator itr = this.schemaSet.iterateElementDecls();
    while (itr.hasNext()) {
      ((ElementDecl)itr.next()).updateSubstitutabilityMap();
    }
    for (Patch patcher : this.errorCheckers) {
      patcher.run();
    }
    this.errorCheckers.clear();
    if (this.hadError) {
      return null;
    }
    return this.schemaSet;
  }
  
  public NGCCRuntimeEx newNGCCRuntime()
  {
    return new NGCCRuntimeEx(this);
  }
  
  private boolean hadError = false;
  
  void setErrorFlag()
  {
    this.hadError = true;
  }
  
  final PatcherManager patcherManager = new PatcherManager()
  {
    public void addPatcher(Patch patch)
    {
      ParserContext.this.patchers.add(patch);
    }
    
    public void addErrorChecker(Patch patch)
    {
      ParserContext.this.errorCheckers.add(patch);
    }
    
    public void reportError(String msg, Locator src)
      throws SAXException
    {
      ParserContext.this.setErrorFlag();
      
      SAXParseException e = new SAXParseException(msg, src);
      if (ParserContext.this.errorHandler == null) {
        throw e;
      }
      ParserContext.this.errorHandler.error(e);
    }
  };
  final ErrorHandler errorHandler = new ErrorHandler()
  {
    private ErrorHandler getErrorHandler()
    {
      if (ParserContext.this.owner.getErrorHandler() == null) {
        return ParserContext.this.noopHandler;
      }
      return ParserContext.this.owner.getErrorHandler();
    }
    
    public void warning(SAXParseException e)
      throws SAXException
    {
      getErrorHandler().warning(e);
    }
    
    public void error(SAXParseException e)
      throws SAXException
    {
      ParserContext.this.setErrorFlag();
      getErrorHandler().error(e);
    }
    
    public void fatalError(SAXParseException e)
      throws SAXException
    {
      ParserContext.this.setErrorFlag();
      getErrorHandler().fatalError(e);
    }
  };
  final ErrorHandler noopHandler = new ErrorHandler()
  {
    public void warning(SAXParseException e) {}
    
    public void error(SAXParseException e) {}
    
    public void fatalError(SAXParseException e)
    {
      ParserContext.this.setErrorFlag();
    }
  };
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\ParserContext.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */