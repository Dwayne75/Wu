package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.SchemaCache;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.tools.xjc.util.ForkContentHandler;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.ValidatorHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class BindInfo
{
  protected final ErrorReceiver errorReceiver;
  final Model model;
  private final String defaultPackage;
  final JCodeModel codeModel;
  final CodeModelClassFactory classFactory;
  private final Element dom;
  
  public BindInfo(Model model, InputSource source, ErrorReceiver _errorReceiver)
    throws AbortException
  {
    this(model, parse(model, source, _errorReceiver), _errorReceiver);
  }
  
  public BindInfo(Model model, Document _dom, ErrorReceiver _errorReceiver)
  {
    this.model = model;
    this.dom = _dom.getDocumentElement();
    this.codeModel = model.codeModel;
    this.errorReceiver = _errorReceiver;
    this.classFactory = new CodeModelClassFactory(_errorReceiver);
    
    this.defaultPackage = model.options.defaultPackage;
    
    model.getCustomizations().addAll(getGlobalCustomizations());
    for (Element ele : DOMUtil.getChildElements(this.dom, "element"))
    {
      BIElement e = new BIElement(this, ele);
      this.elements.put(e.name(), e);
    }
    BIUserConversion.addBuiltinConversions(this, this.conversions);
    for (Element cnv : DOMUtil.getChildElements(this.dom, "conversion"))
    {
      BIConversion c = new BIUserConversion(this, cnv);
      this.conversions.put(c.name(), c);
    }
    for (Element en : DOMUtil.getChildElements(this.dom, "enumeration"))
    {
      BIConversion c = BIEnumeration.create(en, this);
      this.conversions.put(c.name(), c);
    }
    for (Element itf : DOMUtil.getChildElements(this.dom, "interface"))
    {
      BIInterface c = new BIInterface(itf);
      this.interfaces.put(c.name(), c);
    }
  }
  
  private final Map<String, BIConversion> conversions = new HashMap();
  private final Map<String, BIElement> elements = new HashMap();
  private final Map<String, BIInterface> interfaces = new HashMap();
  private static final String XJC_NS = "http://java.sun.com/xml/ns/jaxb/xjc";
  
  public Long getSerialVersionUID()
  {
    Element serial = DOMUtil.getElement(this.dom, "http://java.sun.com/xml/ns/jaxb/xjc", "serializable");
    if (serial == null) {
      return null;
    }
    String v = DOMUtil.getAttribute(serial, "uid");
    if (v == null) {
      v = "1";
    }
    return new Long(v);
  }
  
  public JClass getSuperClass()
  {
    Element sc = DOMUtil.getElement(this.dom, "http://java.sun.com/xml/ns/jaxb/xjc", "superClass");
    if (sc == null) {
      return null;
    }
    JDefinedClass c;
    try
    {
      String v = DOMUtil.getAttribute(sc, "name");
      if (v == null) {
        return null;
      }
      c = this.codeModel._class(v);
      c.hide();
    }
    catch (JClassAlreadyExistsException e)
    {
      c = e.getExistingClass();
    }
    return c;
  }
  
  public JClass getSuperInterface()
  {
    Element sc = DOMUtil.getElement(this.dom, "http://java.sun.com/xml/ns/jaxb/xjc", "superInterface");
    if (sc == null) {
      return null;
    }
    String name = DOMUtil.getAttribute(sc, "name");
    if (name == null) {
      return null;
    }
    JDefinedClass c;
    try
    {
      c = this.codeModel._class(name, ClassType.INTERFACE);
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
    if (this.model.options.defaultPackage != null) {
      return this.codeModel._package(this.model.options.defaultPackage);
    }
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
      throw new AssertionError("undefined conversion name: this should be checked by the validator before we read it");
    }
    return r;
  }
  
  public BIElement element(String name)
  {
    return (BIElement)this.elements.get(name);
  }
  
  public Collection<BIElement> elements()
  {
    return this.elements.values();
  }
  
  public Collection<BIInterface> interfaces()
  {
    return this.interfaces.values();
  }
  
  private CCustomizations getGlobalCustomizations()
  {
    CCustomizations r = null;
    for (Element e : DOMUtil.getChildElements(this.dom)) {
      if (this.model.options.pluginURIs.contains(e.getNamespaceURI()))
      {
        if (r == null) {
          r = new CCustomizations();
        }
        r.add(new CPluginCustomization(e, DOMLocator.getLocationInfo(e)));
      }
    }
    if (r == null) {
      r = CCustomizations.EMPTY;
    }
    return new CCustomizations(r);
  }
  
  private String getOption(String attName, String defaultValue)
  {
    Element opt = DOMUtil.getElement(this.dom, "options");
    if (opt != null)
    {
      String s = DOMUtil.getAttribute(opt, attName);
      if (s != null) {
        return s;
      }
    }
    return defaultValue;
  }
  
  private static SchemaCache bindingFileSchema = new SchemaCache(BindInfo.class.getResource("bindingfile.xsd"));
  
  private static Document parse(Model model, InputSource is, ErrorReceiver receiver)
    throws AbortException
  {
    try
    {
      ValidatorHandler validator = bindingFileSchema.newValidator();
      
      SAXParserFactory pf = SAXParserFactory.newInstance();
      pf.setNamespaceAware(true);
      DOMBuilder builder = new DOMBuilder();
      
      ErrorReceiverFilter controller = new ErrorReceiverFilter(receiver);
      validator.setErrorHandler(controller);
      XMLReader reader = pf.newSAXParser().getXMLReader();
      reader.setErrorHandler(controller);
      
      DTDExtensionBindingChecker checker = new DTDExtensionBindingChecker("", model.options, controller);
      checker.setContentHandler(validator);
      
      reader.setContentHandler(new ForkContentHandler(checker, builder));
      
      reader.parse(is);
      if (controller.hadError()) {
        throw new AbortException();
      }
      return (Document)builder.getDOM();
    }
    catch (IOException e)
    {
      receiver.error(new SAXParseException2(e.getMessage(), null, e));
    }
    catch (SAXException e)
    {
      receiver.error(new SAXParseException2(e.getMessage(), null, e));
    }
    catch (ParserConfigurationException e)
    {
      receiver.error(new SAXParseException2(e.getMessage(), null, e));
    }
    throw new AbortException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\BindInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */