package com.sun.tools.xjc;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.ExtensionBindingChecker;
import com.sun.tools.xjc.reader.dtd.TDTDReader;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.DOMForestScanner;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import com.sun.tools.xjc.reader.internalizer.SCDBasedBindingSet;
import com.sun.tools.xjc.reader.internalizer.VersionChecker;
import com.sun.tools.xjc.reader.relaxng.RELAXNGCompiler;
import com.sun.tools.xjc.reader.relaxng.RELAXNGInternalizationLogic;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.AnnotationParserFactoryImpl;
import com.sun.tools.xjc.reader.xmlschema.parser.CustomizationContextChecker;
import com.sun.tools.xjc.reader.xmlschema.parser.IncorrectNamespaceURIChecker;
import com.sun.tools.xjc.reader.xmlschema.parser.SchemaConstraintChecker;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.JAXPParser;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;
import java.io.IOException;
import java.io.StringReader;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.util.CheckingSchemaBuilder;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DSchemaBuilderImpl;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.kohsuke.rngom.xml.sax.XMLReaderCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public final class ModelLoader
{
  private final Options opt;
  private final ErrorReceiverFilter errorReceiver;
  private final JCodeModel codeModel;
  private SCDBasedBindingSet scdBasedBindingSet;
  
  public static Model load(Options opt, JCodeModel codeModel, ErrorReceiver er)
  {
    return new ModelLoader(opt, codeModel, er).load();
  }
  
  public ModelLoader(Options _opt, JCodeModel _codeModel, ErrorReceiver er)
  {
    this.opt = _opt;
    this.codeModel = _codeModel;
    this.errorReceiver = new ErrorReceiverFilter(er);
  }
  
  private Model load()
  {
    if (!sanityCheck()) {
      return null;
    }
    try
    {
      Model grammar;
      switch (this.opt.getSchemaLanguage())
      {
      case DTD: 
        InputSource bindFile = null;
        if (this.opt.getBindFiles().length > 0) {
          bindFile = this.opt.getBindFiles()[0];
        }
        if (bindFile == null) {
          bindFile = new InputSource(new StringReader("<?xml version='1.0'?><xml-java-binding-schema><options package='" + (this.opt.defaultPackage == null ? "generated" : this.opt.defaultPackage) + "'/></xml-java-binding-schema>"));
        }
        checkTooManySchemaErrors();
        grammar = loadDTD(this.opt.getGrammars()[0], bindFile);
        break;
      case RELAXNG: 
        checkTooManySchemaErrors();
        grammar = loadRELAXNG();
        break;
      case RELAXNG_COMPACT: 
        checkTooManySchemaErrors();
        grammar = loadRELAXNGCompact();
        break;
      case WSDL: 
        grammar = annotateXMLSchema(loadWSDL());
        break;
      case XMLSCHEMA: 
        grammar = annotateXMLSchema(loadXMLSchema());
        break;
      default: 
        throw new AssertionError();
      }
      if (this.errorReceiver.hadError()) {
        grammar = null;
      } else {
        grammar.setPackageLevelAnnotations(this.opt.packageLevelAnnotations);
      }
      return grammar;
    }
    catch (SAXException e)
    {
      if (this.opt.verbose) {
        if (e.getException() != null) {
          e.getException().printStackTrace();
        } else {
          e.printStackTrace();
        }
      }
      return null;
    }
    catch (AbortException e) {}
    return null;
  }
  
  private boolean sanityCheck()
  {
    if (this.opt.getSchemaLanguage() == Language.XMLSCHEMA)
    {
      Language guess = this.opt.guessSchemaLanguage();
      
      String[] msg = null;
      switch (guess)
      {
      case DTD: 
        msg = new String[] { "DTD", "-dtd" };
        break;
      case RELAXNG: 
        msg = new String[] { "RELAX NG", "-relaxng" };
        break;
      case RELAXNG_COMPACT: 
        msg = new String[] { "RELAX NG compact syntax", "-relaxng-compact" };
        break;
      case WSDL: 
        msg = new String[] { "WSDL", "-wsdl" };
      }
      if (msg != null) {
        this.errorReceiver.warning(null, Messages.format("Driver.ExperimentalLanguageWarning", new Object[] { msg[0], msg[1] }));
      }
    }
    return true;
  }
  
  private class XMLSchemaParser
    implements XMLParser
  {
    private final XMLParser baseParser;
    
    private XMLSchemaParser(XMLParser baseParser)
    {
      this.baseParser = baseParser;
    }
    
    public void parse(InputSource source, ContentHandler handler, ErrorHandler errorHandler, EntityResolver entityResolver)
      throws SAXException, IOException
    {
      handler = wrapBy(new ExtensionBindingChecker("http://www.w3.org/2001/XMLSchema", ModelLoader.this.opt, ModelLoader.this.errorReceiver), handler);
      handler = wrapBy(new IncorrectNamespaceURIChecker(ModelLoader.this.errorReceiver), handler);
      handler = wrapBy(new CustomizationContextChecker(ModelLoader.this.errorReceiver), handler);
      
      this.baseParser.parse(source, handler, errorHandler, entityResolver);
    }
    
    private ContentHandler wrapBy(XMLFilterImpl filter, ContentHandler handler)
    {
      filter.setContentHandler(handler);
      return filter;
    }
  }
  
  private void checkTooManySchemaErrors()
  {
    if (this.opt.getGrammars().length != 1) {
      this.errorReceiver.error(null, Messages.format("ModelLoader.TooManySchema", new Object[0]));
    }
  }
  
  private Model loadDTD(InputSource source, InputSource bindFile)
  {
    return TDTDReader.parse(source, bindFile, this.errorReceiver, this.opt);
  }
  
  public DOMForest buildDOMForest(InternalizationLogic logic)
    throws SAXException
  {
    DOMForest forest = new DOMForest(logic);
    
    forest.setErrorHandler(this.errorReceiver);
    if (this.opt.entityResolver != null) {
      forest.setEntityResolver(this.opt.entityResolver);
    }
    for (InputSource value : this.opt.getGrammars())
    {
      this.errorReceiver.pollAbort();
      forest.parse(value, true);
    }
    for (InputSource value : this.opt.getBindFiles())
    {
      this.errorReceiver.pollAbort();
      Document dom = forest.parse(value, true);
      if (dom != null)
      {
        Element root = dom.getDocumentElement();
        if ((!fixNull(root.getNamespaceURI()).equals("http://java.sun.com/xml/ns/jaxb")) || (!root.getLocalName().equals("bindings"))) {
          this.errorReceiver.error(new SAXParseException(Messages.format("Driver.NotABindingFile", new Object[] { root.getNamespaceURI(), root.getLocalName() }), null, value.getSystemId(), -1, -1));
        }
      }
    }
    this.scdBasedBindingSet = forest.transform(this.opt.isExtensionMode());
    
    return forest;
  }
  
  private String fixNull(String s)
  {
    if (s == null) {
      return "";
    }
    return s;
  }
  
  public XSSchemaSet loadXMLSchema()
    throws SAXException
  {
    if ((this.opt.strictCheck) && (!SchemaConstraintChecker.check(this.opt.getGrammars(), this.errorReceiver, this.opt.entityResolver))) {
      return null;
    }
    if (this.opt.getBindFiles().length == 0) {
      try
      {
        return createXSOMSpeculative();
      }
      catch (SpeculationFailure _) {}
    }
    DOMForest forest = buildDOMForest(new XMLSchemaInternalizationLogic());
    return createXSOM(forest, this.scdBasedBindingSet);
  }
  
  private XSSchemaSet loadWSDL()
    throws SAXException
  {
    DOMForest forest = buildDOMForest(new XMLSchemaInternalizationLogic());
    
    DOMForestScanner scanner = new DOMForestScanner(forest);
    
    XSOMParser xsomParser = createXSOMParser(forest);
    for (InputSource grammar : this.opt.getGrammars())
    {
      Document wsdlDom = forest.get(grammar.getSystemId());
      
      NodeList schemas = wsdlDom.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
      for (int i = 0; i < schemas.getLength(); i++) {
        scanner.scan((Element)schemas.item(i), xsomParser.getParserHandler());
      }
    }
    return xsomParser.getResult();
  }
  
  public Model annotateXMLSchema(XSSchemaSet xs)
  {
    if (xs == null) {
      return null;
    }
    return BGMBuilder.build(xs, this.codeModel, this.errorReceiver, this.opt);
  }
  
  public XSOMParser createXSOMParser(XMLParser parser)
  {
    XSOMParser reader = new XSOMParser(new XMLSchemaParser(parser, null));
    reader.setAnnotationParser(new AnnotationParserFactoryImpl(this.opt));
    reader.setErrorHandler(this.errorReceiver);
    reader.setEntityResolver(this.opt.entityResolver);
    return reader;
  }
  
  public XSOMParser createXSOMParser(final DOMForest forest)
  {
    XSOMParser p = createXSOMParser(forest.createParser());
    p.setEntityResolver(new EntityResolver()
    {
      public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException, IOException
      {
        if ((systemId != null) && (forest.get(systemId) != null)) {
          return new InputSource(systemId);
        }
        if (ModelLoader.this.opt.entityResolver != null) {
          return ModelLoader.this.opt.entityResolver.resolveEntity(publicId, systemId);
        }
        return null;
      }
    });
    return p;
  }
  
  private static final class SpeculationFailure
    extends Error
  {}
  
  private static final class SpeculationChecker
    extends XMLFilterImpl
  {
    public void startElement(String uri, String localName, String qName, Attributes attributes)
      throws SAXException
    {
      if ((localName.equals("bindings")) && (uri.equals("http://java.sun.com/xml/ns/jaxb"))) {
        throw new ModelLoader.SpeculationFailure(null);
      }
      super.startElement(uri, localName, qName, attributes);
    }
  }
  
  private XSSchemaSet createXSOMSpeculative()
    throws SAXException, ModelLoader.SpeculationFailure
  {
    XMLParser parser = new XMLParser()
    {
      private final JAXPParser base = new JAXPParser();
      
      public void parse(InputSource source, ContentHandler handler, ErrorHandler errorHandler, EntityResolver entityResolver)
        throws SAXException, IOException
      {
        handler = wrapBy(new ModelLoader.SpeculationChecker(null), handler);
        handler = wrapBy(new VersionChecker(null, ModelLoader.this.errorReceiver, entityResolver), handler);
        
        this.base.parse(source, handler, errorHandler, entityResolver);
      }
      
      private ContentHandler wrapBy(XMLFilterImpl filter, ContentHandler handler)
      {
        filter.setContentHandler(handler);
        return filter;
      }
    };
    XSOMParser reader = createXSOMParser(parser);
    for (InputSource value : this.opt.getGrammars()) {
      reader.parse(value);
    }
    return reader.getResult();
  }
  
  public XSSchemaSet createXSOM(DOMForest forest, SCDBasedBindingSet scdBasedBindingSet)
    throws SAXException
  {
    XSOMParser reader = createXSOMParser(forest);
    for (String systemId : forest.getRootDocuments())
    {
      this.errorReceiver.pollAbort();
      Document dom = forest.get(systemId);
      if (!dom.getDocumentElement().getNamespaceURI().equals("http://java.sun.com/xml/ns/jaxb")) {
        reader.parse(systemId);
      }
    }
    XSSchemaSet result = reader.getResult();
    if (result != null) {
      scdBasedBindingSet.apply(result, this.errorReceiver);
    }
    return result;
  }
  
  private Model loadRELAXNG()
    throws SAXException
  {
    final DOMForest forest = buildDOMForest(new RELAXNGInternalizationLogic());
    
    XMLReaderCreator xrc = new XMLReaderCreator()
    {
      public XMLReader createXMLReader()
      {
        XMLFilter buffer = new XMLFilterImpl()
        {
          public void parse(InputSource source)
            throws IOException, SAXException
          {
            ModelLoader.3.this.val$forest.createParser().parse(source, this, this, this);
          }
        };
        XMLFilter f = new ExtensionBindingChecker("http://relaxng.org/ns/structure/1.0", ModelLoader.this.opt, ModelLoader.this.errorReceiver);
        f.setParent(buffer);
        
        f.setEntityResolver(ModelLoader.this.opt.entityResolver);
        
        return f;
      }
    };
    Parseable p = new SAXParseable(this.opt.getGrammars()[0], this.errorReceiver, xrc);
    
    return loadRELAXNG(p);
  }
  
  private Model loadRELAXNGCompact()
  {
    if (this.opt.getBindFiles().length > 0) {
      this.errorReceiver.error(new SAXParseException(Messages.format("ModelLoader.BindingFileNotSupportedForRNC", new Object[0]), null));
    }
    Parseable p = new CompactParseable(this.opt.getGrammars()[0], this.errorReceiver);
    
    return loadRELAXNG(p);
  }
  
  private Model loadRELAXNG(Parseable p)
  {
    SchemaBuilder sb = new CheckingSchemaBuilder(new DSchemaBuilderImpl(), this.errorReceiver);
    try
    {
      DPattern out = (DPattern)p.parse(sb);
      return RELAXNGCompiler.build(out, this.codeModel, this.opt);
    }
    catch (IllegalSchemaException e)
    {
      this.errorReceiver.error(e.getMessage(), e);
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\ModelLoader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */