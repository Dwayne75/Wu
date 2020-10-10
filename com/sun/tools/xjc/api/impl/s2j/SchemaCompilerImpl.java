package com.sun.tools.xjc.api.impl.s2j;

import com.sun.codemodel.JCodeModel;
import com.sun.istack.NotNull;
import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.ModelLoader;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.api.ClassNameAllocator;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.SpecVersion;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.internalizer.DOMForest;
import com.sun.tools.xjc.reader.internalizer.SCDBasedBindingSet;
import com.sun.tools.xjc.reader.xmlschema.parser.XMLSchemaInternalizationLogic;
import com.sun.xml.bind.unmarshaller.DOMScanner;
import com.sun.xml.xsom.XSSchemaSet;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public final class SchemaCompilerImpl
  extends ErrorReceiver
  implements SchemaCompiler
{
  private ErrorListener errorListener;
  protected final Options opts = new Options();
  @NotNull
  protected DOMForest forest;
  private boolean hadError;
  
  public SchemaCompilerImpl()
  {
    this.opts.compatibilityMode = 2;
    resetSchema();
    if (System.getProperty("xjc-api.test") != null)
    {
      this.opts.debugMode = true;
      this.opts.verbose = true;
    }
  }
  
  @NotNull
  public Options getOptions()
  {
    return this.opts;
  }
  
  public ContentHandler getParserHandler(String systemId)
  {
    return this.forest.getParserHandler(systemId, true);
  }
  
  public void parseSchema(String systemId, Element element)
  {
    checkAbsoluteness(systemId);
    try
    {
      DOMScanner scanner = new DOMScanner();
      
      LocatorImpl loc = new LocatorImpl();
      loc.setSystemId(systemId);
      scanner.setLocator(loc);
      
      scanner.setContentHandler(getParserHandler(systemId));
      scanner.scan(element);
    }
    catch (SAXException e)
    {
      fatalError(new SAXParseException2(e.getMessage(), null, systemId, -1, -1, e));
    }
  }
  
  public void parseSchema(InputSource source)
  {
    checkAbsoluteness(source.getSystemId());
    try
    {
      this.forest.parse(source, true);
    }
    catch (SAXException e)
    {
      e.printStackTrace();
    }
  }
  
  public void setTargetVersion(SpecVersion version)
  {
    if (version == null) {
      version = SpecVersion.LATEST;
    }
    this.opts.target = version;
  }
  
  public void parseSchema(String systemId, XMLStreamReader reader)
    throws XMLStreamException
  {
    checkAbsoluteness(systemId);
    this.forest.parse(systemId, reader, true);
  }
  
  private void checkAbsoluteness(String systemId)
  {
    try
    {
      new URL(systemId);
    }
    catch (MalformedURLException _)
    {
      try
      {
        new URI(systemId);
      }
      catch (URISyntaxException e)
      {
        throw new IllegalArgumentException("system ID '" + systemId + "' isn't absolute", e);
      }
    }
  }
  
  public void setEntityResolver(EntityResolver entityResolver)
  {
    this.forest.setEntityResolver(entityResolver);
    this.opts.entityResolver = entityResolver;
  }
  
  public void setDefaultPackageName(String packageName)
  {
    this.opts.defaultPackage2 = packageName;
  }
  
  public void forcePackageName(String packageName)
  {
    this.opts.defaultPackage = packageName;
  }
  
  public void setClassNameAllocator(ClassNameAllocator allocator)
  {
    this.opts.classNameAllocator = allocator;
  }
  
  public void resetSchema()
  {
    this.forest = new DOMForest(new XMLSchemaInternalizationLogic());
    this.forest.setErrorHandler(this);
    this.forest.setEntityResolver(this.opts.entityResolver);
  }
  
  public JAXBModelImpl bind()
  {
    SCDBasedBindingSet scdBasedBindingSet = this.forest.transform(this.opts.isExtensionMode());
    if (!NO_CORRECTNESS_CHECK)
    {
      SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
      sf.setErrorHandler(new DowngradingErrorHandler(this));
      this.forest.weakSchemaCorrectnessCheck(sf);
      if (this.hadError) {
        return null;
      }
    }
    JCodeModel codeModel = new JCodeModel();
    
    ModelLoader gl = new ModelLoader(this.opts, codeModel, this);
    try
    {
      XSSchemaSet result = gl.createXSOM(this.forest, scdBasedBindingSet);
      if (result == null) {
        return null;
      }
      Model model = gl.annotateXMLSchema(result);
      if (model == null) {
        return null;
      }
      if (this.hadError) {
        return null;
      }
      Outline context = model.generateCode(this.opts, this);
      if (context == null) {
        return null;
      }
      if (this.hadError) {
        return null;
      }
      return new JAXBModelImpl(context);
    }
    catch (SAXException e) {}
    return null;
  }
  
  public void setErrorListener(ErrorListener errorListener)
  {
    this.errorListener = errorListener;
  }
  
  public void info(SAXParseException exception)
  {
    if (this.errorListener != null) {
      this.errorListener.info(exception);
    }
  }
  
  public void warning(SAXParseException exception)
  {
    if (this.errorListener != null) {
      this.errorListener.warning(exception);
    }
  }
  
  public void error(SAXParseException exception)
  {
    this.hadError = true;
    if (this.errorListener != null) {
      this.errorListener.error(exception);
    }
  }
  
  public void fatalError(SAXParseException exception)
  {
    this.hadError = true;
    if (this.errorListener != null) {
      this.errorListener.fatalError(exception);
    }
  }
  
  private static boolean NO_CORRECTNESS_CHECK = false;
  
  static
  {
    try
    {
      NO_CORRECTNESS_CHECK = Boolean.getBoolean(SchemaCompilerImpl.class + ".noCorrectnessCheck");
    }
    catch (Throwable t) {}
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\SchemaCompilerImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */