package com.sun.xml.xsom.impl.parser;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.ElementDecl;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.SchemaSetImpl;
import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ParserContext
{
  public final SchemaSetImpl schemaSet = new SchemaSetImpl();
  private final XSOMParser owner;
  final XMLParser parser;
  private final Vector patchers = new Vector();
  protected final Set parsedDocuments = new HashSet();
  
  public ParserContext(XSOMParser owner, XMLParser parser)
  {
    this.owner = owner;
    this.parser = parser;
    try
    {
      parse(new InputSource(ParserContext.class.getResource("datatypes.xsd").toExternalForm()));
      
      SchemaImpl xs = (SchemaImpl)this.schemaSet.getSchema("http://www.w3.org/2001/XMLSchema");
      
      xs.addSimpleType(this.schemaSet.anySimpleType);
      xs.addComplexType(this.schemaSet.anyType);
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
    Iterator itr = this.patchers.iterator();
    while (itr.hasNext()) {
      ((Patch)itr.next()).run();
    }
    this.patchers.clear();
    
    itr = this.schemaSet.iterateElementDecls();
    while (itr.hasNext()) {
      ((ElementDecl)itr.next()).updateSubstitutabilityMap();
    }
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
  
  final PatcherManager patcherManager = new ParserContext.1(this);
  final ErrorHandler errorHandler = new ParserContext.2(this);
  final ErrorHandler noopHandler = new ParserContext.3(this);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\ParserContext.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */