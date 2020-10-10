package com.sun.tools.xjc;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.org.apache.xerces.internal.impl.Version;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.reader.dtd.TDTDReader;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.DOMForestScanner;
import com.sun.tools.xjc.reader.internalizer.InternalizationLogic;
import com.sun.tools.xjc.reader.relaxng.CustomizationConverter;
import com.sun.tools.xjc.reader.relaxng.RELAXNGInternalizationLogic;
import com.sun.tools.xjc.reader.relaxng.TRELAXNGReader;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.AnnotationParserFactoryImpl;
import com.sun.tools.xjc.reader.xmlschema.parser.SchemaConstraintChecker;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.util.Which;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.parser.SAXParserFactoryAdaptor;
import com.sun.xml.xsom.parser.XMLParser;
import com.sun.xml.xsom.parser.XSOMParser;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.dom4j.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public final class GrammarLoader
{
  private final Options opt;
  private final ErrorReceiverFilter errorReceiver;
  
  public static AnnotatedGrammar load(Options opt, ErrorReceiver er)
    throws SAXException, IOException
  {
    return new GrammarLoader(opt, er).load();
  }
  
  public GrammarLoader(Options _opt, ErrorReceiver er)
  {
    this.opt = _opt;
    this.errorReceiver = new ErrorReceiverFilter(er);
  }
  
  private AnnotatedGrammar load()
    throws IOException
  {
    if (!sanityCheck()) {
      return null;
    }
    try
    {
      JCodeModel codeModel = new JCodeModel();
      AnnotatedGrammar grammar;
      AnnotatedGrammar grammar;
      AnnotatedGrammar grammar;
      AnnotatedGrammar grammar;
      switch (this.opt.getSchemaLanguage())
      {
      case 0: 
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
      case 2: 
        checkTooManySchemaErrors();
        grammar = loadRELAXNG();
        break;
      case 3: 
        checkTooManySchemaErrors();
        grammar = annotateXMLSchema(loadWSDL(codeModel), codeModel);
        break;
      case 1: 
        grammar = annotateXMLSchema(loadXMLSchema(codeModel), codeModel);
        break;
      default: 
        throw new JAXBAssertionError();
      }
      if (this.errorReceiver.hadError()) {}
      return null;
    }
    catch (SAXException e)
    {
      if (this.opt.debugMode) {
        if (e.getException() != null) {
          e.getException().printStackTrace();
        } else {
          e.printStackTrace();
        }
      }
    }
    return null;
  }
  
  private boolean sanityCheck()
  {
    if (this.opt.getSchemaLanguage() == 0) {
      try
      {
        new DocumentFactory();
      }
      catch (NoClassDefFoundError e)
      {
        this.errorReceiver.error(null, Messages.format("Driver.MissingDOM4J"));
        return false;
      }
    }
    if (this.opt.getSchemaLanguage() == 1)
    {
      int guess = this.opt.guessSchemaLanguage();
      
      String[] msg = null;
      switch (guess)
      {
      case 0: 
        msg = new String[] { "DTD", "-dtd" };
        break;
      case 2: 
        msg = new String[] { "RELAX NG", "-relaxng" };
      }
      if (msg != null) {
        this.errorReceiver.warning(null, Messages.format("Driver.ExperimentalLanguageWarning", msg[0], msg[1]));
      }
    }
    return true;
  }
  
  private void checkTooManySchemaErrors()
  {
    if (this.opt.getGrammars().length != 1) {
      this.errorReceiver.error(null, Messages.format("GrammarLoader.TooManySchema"));
    }
  }
  
  private AnnotatedGrammar loadDTD(InputSource source, InputSource bindFile)
  {
    return TDTDReader.parse(source, bindFile, this.errorReceiver, this.opt, new ExpressionPool());
  }
  
  public DOMForest buildDOMForest(InternalizationLogic logic)
    throws SAXException, IOException
  {
    try
    {
      forest = new DOMForest(logic);
    }
    catch (ParserConfigurationException e)
    {
      DOMForest forest;
      throw new SAXException(e);
    }
    DOMForest forest;
    forest.setErrorHandler(this.errorReceiver);
    if (this.opt.entityResolver != null) {
      forest.setEntityResolver(this.opt.entityResolver);
    }
    InputSource[] sources = this.opt.getGrammars();
    for (int i = 0; i < sources.length; i++) {
      forest.parse(sources[i]);
    }
    InputSource[] externalBindingFiles = this.opt.getBindFiles();
    for (int i = 0; i < externalBindingFiles.length; i++)
    {
      Element root = forest.parse(externalBindingFiles[i]).getDocumentElement();
      if ((!root.getNamespaceURI().equals("http://java.sun.com/xml/ns/jaxb")) || (!root.getLocalName().equals("bindings"))) {
        this.errorReceiver.error(new SAXParseException(Messages.format("Driver.NotABindingFile", root.getNamespaceURI(), root.getLocalName()), null, externalBindingFiles[i].getSystemId(), -1, -1));
      }
    }
    forest.transform();
    
    return forest;
  }
  
  private XSSchemaSet loadXMLSchema(JCodeModel codeModel)
    throws SAXException, IOException
  {
    try
    {
      this.errorReceiver.info(new SAXParseException("Using Xerces from " + Which.which(Version.class), null));
    }
    catch (Throwable t) {}
    try
    {
      if ((this.opt.strictCheck) && (!SchemaConstraintChecker.check(this.opt.getGrammars(), this.errorReceiver, this.opt.entityResolver))) {
        return null;
      }
    }
    catch (LinkageError e)
    {
      this.errorReceiver.warning(new SAXParseException(Messages.format("GrammarLoader.IncompatibleXerces", e.toString()), null));
      try
      {
        this.errorReceiver.warning(new SAXParseException("Using Xerces from " + Which.which(Version.class), null));
      }
      catch (Throwable t) {}
      if (this.opt.debugMode) {
        throw e;
      }
    }
    DOMForest forest = buildDOMForest(new XMLSchemaInternalizationLogic());
    
    XSOMParser xsomParser = createXSOMParser(forest, codeModel);
    
    InputSource[] grammars = this.opt.getGrammars();
    for (int i = 0; i < grammars.length; i++) {
      xsomParser.parse(grammars[i]);
    }
    return xsomParser.getResult();
  }
  
  private XSSchemaSet loadWSDL(JCodeModel codeModel)
    throws SAXException, IOException
  {
    DOMForest forest = buildDOMForest(new XMLSchemaInternalizationLogic());
    
    DOMForestScanner scanner = new DOMForestScanner(forest);
    
    XSOMParser xsomParser = createXSOMParser(forest, codeModel);
    
    InputSource[] grammars = this.opt.getGrammars();
    Document wsdlDom = forest.get(grammars[0].getSystemId());
    
    NodeList schemas = wsdlDom.getElementsByTagNameNS("http://www.w3.org/2001/XMLSchema", "schema");
    for (int i = 0; i < schemas.getLength(); i++) {
      scanner.scan((Element)schemas.item(i), xsomParser.getParserHandler());
    }
    return xsomParser.getResult();
  }
  
  public AnnotatedGrammar annotateXMLSchema(XSSchemaSet xs, JCodeModel codeModel)
    throws SAXException
  {
    if (xs == null) {
      return null;
    }
    return BGMBuilder.build(xs, codeModel, this.errorReceiver, this.opt.defaultPackage, this.opt.compatibilityMode == 2);
  }
  
  public XSOMParser createXSOMParser(DOMForest forest, JCodeModel codeModel)
  {
    XSOMParser reader = new XSOMParser(new GrammarLoader.XMLSchemaForestParser(this, forest, null));
    reader.setAnnotationParser(new AnnotationParserFactoryImpl(codeModel, this.opt));
    reader.setErrorHandler(this.errorReceiver);
    return reader;
  }
  
  private AnnotatedGrammar loadRELAXNG()
    throws IOException, SAXException
  {
    DOMForest forest = buildDOMForest(new RELAXNGInternalizationLogic());
    
    new CustomizationConverter(this.opt).fixup(forest);
    
    XMLParser parser = new GrammarLoader.1(this, forest);
    
    SAXParserFactory parserFactory = new SAXParserFactoryAdaptor(parser);
    parserFactory.setNamespaceAware(true);
    
    TRELAXNGReader reader = new TRELAXNGReader(this.errorReceiver, this.opt.entityResolver, parserFactory, this.opt.defaultPackage);
    
    reader.parse(this.opt.getGrammars()[0]);
    
    return reader.getAnnotatedResult();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\GrammarLoader.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */