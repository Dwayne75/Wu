package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JCodeModel;
import com.sun.msv.verifier.jarv.RELAXNGFactoryImpl;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.parser.AnnotationState;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;
import java.io.IOException;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.impl.ForkContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public class AnnotationParserImpl
  extends AnnotationParser
{
  public AnnotationParserImpl(JCodeModel cm, Options opts)
  {
    this.codeModel = cm;
    this.options = opts;
  }
  
  private AnnotationState parser = null;
  private final JCodeModel codeModel;
  private final Options options;
  
  public ContentHandler getContentHandler(AnnotationContext context, String parentElementName, ErrorHandler errorHandler, EntityResolver entityResolver)
  {
    try
    {
      if (this.parser != null) {
        throw new JAXBAssertionError();
      }
      NGCCRuntimeEx runtime = new NGCCRuntimeEx(this.codeModel, this.options, errorHandler);
      this.parser = new AnnotationState(runtime);
      runtime.setRootHandler(this.parser);
      
      VerifierFactory factory = new RELAXNGFactoryImpl();
      factory.setProperty("datatypeLibraryFactory", new AnnotationParserImpl.DatatypeLibraryFactoryImpl(null));
      Verifier v = factory.newVerifier(getClass().getClassLoader().getResourceAsStream("com/sun/tools/xjc/reader/xmlschema/bindinfo/binding.purified.rng"));
      
      v.setErrorHandler(errorHandler);
      
      return new ForkContentHandler(v.getVerifierHandler(), runtime);
    }
    catch (VerifierConfigurationException e)
    {
      e.printStackTrace();
      throw new InternalError();
    }
    catch (SAXException e)
    {
      e.printStackTrace();
      throw new InternalError();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new InternalError();
    }
  }
  
  public Object getResult(Object existing)
  {
    if (this.parser == null) {
      throw new JAXBAssertionError();
    }
    if (existing != null)
    {
      BindInfo bie = (BindInfo)existing;
      bie.absorb(this.parser.bi);
      return bie;
    }
    return this.parser.bi;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\AnnotationParserImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */