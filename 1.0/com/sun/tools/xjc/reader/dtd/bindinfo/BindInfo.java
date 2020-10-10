package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.msv.reader.AbortException;
import com.sun.msv.verifier.jarv.RELAXNGFactoryImpl;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.JAXBAssertionError;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.iso_relax.verifier.VerifierFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class BindInfo
{
  protected final ErrorReceiver errorReceiver;
  private final Options options;
  private final String defaultPackage;
  final JCodeModel codeModel;
  final CodeModelClassFactory classFactory;
  final NameConverter nameConverter;
  private final Element dom;
  
  public BindInfo(InputSource source, ErrorReceiver _errorReceiver, JCodeModel _codeModel, Options opts)
    throws AbortException
  {
    this(parse(source, _errorReceiver), _errorReceiver, _codeModel, opts);
  }
  
  public BindInfo(Document _dom, ErrorReceiver _errorReceiver, JCodeModel _codeModel, Options opts)
  {
    this.dom = _dom.getRootElement();
    this.codeModel = _codeModel;
    this.options = opts;
    this.errorReceiver = _errorReceiver;
    this.classFactory = new CodeModelClassFactory(_errorReceiver);
    
    this.nameConverter = NameConverter.standard;
    
    this.defaultPackage = opts.defaultPackage;
    
    Iterator itr = this.dom.elementIterator("element");
    while (itr.hasNext())
    {
      BIElement e = new BIElement(this, (Element)itr.next());
      this.elements.put(e.name(), e);
    }
    BIUserConversion.addBuiltinConversions(this, this.conversions);
    
    itr = this.dom.elementIterator("conversion");
    while (itr.hasNext())
    {
      BIConversion c = new BIUserConversion(this, (Element)itr.next());
      this.conversions.put(c.name(), c);
    }
    itr = this.dom.elementIterator("enumeration");
    while (itr.hasNext())
    {
      BIConversion c = BIEnumeration.create((Element)itr.next(), this);
      this.conversions.put(c.name(), c);
    }
    itr = this.dom.elementIterator("interface");
    while (itr.hasNext())
    {
      BIInterface c = new BIInterface((Element)itr.next());
      this.interfaces.put(c.name(), c);
    }
    this.options.generateMarshallingCode = (this.dom.element(new QName("noMarshaller", XJC_NS)) == null);
    
    this.options.generateUnmarshallingCode = (this.dom.element(new QName("noUnmarshaller", XJC_NS)) == null);
    
    this.options.generateValidationCode = (this.dom.element(new QName("noValidator", XJC_NS)) == null);
    
    this.options.generateValidatingUnmarshallingCode = (this.dom.element(new QName("noValidatingUnmarshaller", XJC_NS)) == null);
    if (!this.options.generateUnmarshallingCode) {
      this.options.generateValidatingUnmarshallingCode = false;
    }
  }
  
  private final Map conversions = new HashMap();
  private final Map elements = new HashMap();
  private final Map interfaces = new HashMap();
  private static final Namespace XJC_NS = Namespace.get("http://java.sun.com/xml/ns/jaxb/xjc");
  
  public Long getSerialVersionUID()
  {
    Element serial = this.dom.element(new QName("serializable", XJC_NS));
    if (serial == null) {
      return null;
    }
    return new Long(serial.attributeValue("uid", "1"));
  }
  
  public JClass getSuperClass()
  {
    Element sc = this.dom.element(new QName("superClass", XJC_NS));
    if (sc == null) {
      return null;
    }
    JDefinedClass c;
    try
    {
      JDefinedClass c = this.codeModel._class(sc.attributeValue("name", "java.lang.Object"));
      c.hide();
    }
    catch (JClassAlreadyExistsException e)
    {
      c = e.getExistingClass();
    }
    return c;
  }
  
  public JPackage getTargetPackage()
  {
    String p;
    String p;
    if (this.defaultPackage != null) {
      p = this.defaultPackage;
    } else {
      p = getOption("package", "");
    }
    return this.codeModel._package(p);
  }
  
  public BIConversion conversion(String name)
  {
    BIConversion r = (BIConversion)this.conversions.get(name);
    if (r == null) {
      throw new JAXBAssertionError("undefined conversion name: this should be checked by the validator before we read it");
    }
    return r;
  }
  
  public BIElement element(String name)
  {
    return (BIElement)this.elements.get(name);
  }
  
  public Iterator elements()
  {
    return this.elements.values().iterator();
  }
  
  public Iterator interfaces()
  {
    return this.interfaces.values().iterator();
  }
  
  private String getOption(String attName, String defaultValue)
  {
    Element opt = this.dom.element("options");
    if (opt != null)
    {
      String s = opt.attributeValue(attName);
      if (s != null) {
        return s;
      }
    }
    return defaultValue;
  }
  
  private static Document parse(InputSource is, ErrorReceiver receiver)
    throws AbortException
  {
    try
    {
      VerifierFactory factory = new RELAXNGFactoryImpl();
      VerifierFilter verifier = factory.newVerifier(BindInfo.class.getResourceAsStream("bindingfile.rng")).getVerifierFilter();
      
      SAXParserFactory pf = SAXParserFactory.newInstance();
      pf.setNamespaceAware(true);
      SAXContentHandlerEx builder = SAXContentHandlerEx.create();
      
      ErrorReceiverFilter controller = new ErrorReceiverFilter(receiver);
      verifier.setContentHandler(builder);
      verifier.setErrorHandler(controller);
      verifier.setParent(pf.newSAXParser().getXMLReader());
      verifier.parse(is);
      if (controller.hadError()) {
        throw AbortException.theInstance;
      }
      return builder.getDocument();
    }
    catch (IOException e)
    {
      receiver.error(new SAXParseException(e.getMessage(), null, e));
    }
    catch (SAXException e)
    {
      receiver.error(new SAXParseException(e.getMessage(), null, e));
    }
    catch (VerifierConfigurationException ve)
    {
      ve.printStackTrace();
    }
    catch (ParserConfigurationException e)
    {
      receiver.error(new SAXParseException(e.getMessage(), null, e));
    }
    throw AbortException.theInstance;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BindInfo.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */