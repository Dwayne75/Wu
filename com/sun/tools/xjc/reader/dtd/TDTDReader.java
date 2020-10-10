package com.sun.tools.xjc.reader.dtd;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.istack.SAXParseException2;
import com.sun.tools.xjc.AbortException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CDefaultValue;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import com.sun.tools.xjc.reader.ModelChecker;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIAttribute;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIElement;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIInterface;
import com.sun.tools.xjc.reader.dtd.bindinfo.BindInfo;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.dtdparser.DTDHandlerBase;
import com.sun.xml.dtdparser.DTDParser;
import com.sun.xml.dtdparser.InputEntity;
import com.sun.xml.xsom.XmlString;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import javax.xml.namespace.QName;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;

public class TDTDReader
  extends DTDHandlerBase
{
  private final EntityResolver entityResolver;
  final BindInfo bindInfo;
  
  public static Model parse(InputSource dtd, InputSource bindingInfo, ErrorReceiver errorReceiver, Options opts)
  {
    try
    {
      Ring old = Ring.begin();
      try
      {
        ErrorReceiverFilter ef = new ErrorReceiverFilter(errorReceiver);
        
        JCodeModel cm = new JCodeModel();
        Model model = new Model(opts, cm, NameConverter.standard, opts.classNameAllocator, null);
        
        Ring.add(cm);
        Ring.add(model);
        Ring.add(ErrorReceiver.class, ef);
        
        TDTDReader reader = new TDTDReader(ef, opts, bindingInfo);
        
        DTDParser parser = new DTDParser();
        parser.setDtdHandler(reader);
        if (opts.entityResolver != null) {
          parser.setEntityResolver(opts.entityResolver);
        }
        try
        {
          parser.parse(dtd);
        }
        catch (SAXParseException e)
        {
          return null;
        }
        ((ModelChecker)Ring.get(ModelChecker.class)).check();
        if (ef.hadError()) {
          return null;
        }
        return model;
      }
      finally
      {
        Ring.end(old);
      }
      return null;
    }
    catch (IOException e)
    {
      errorReceiver.error(new SAXParseException2(e.getMessage(), null, e));
      return null;
    }
    catch (SAXException e)
    {
      errorReceiver.error(new SAXParseException2(e.getMessage(), null, e));
      return null;
    }
    catch (AbortException e) {}
  }
  
  protected TDTDReader(ErrorReceiver errorReceiver, Options opts, InputSource _bindInfo)
    throws AbortException
  {
    this.entityResolver = opts.entityResolver;
    this.errorReceiver = new ErrorReceiverFilter(errorReceiver);
    this.bindInfo = new BindInfo(this.model, _bindInfo, this.errorReceiver);
    this.classFactory = new CodeModelClassFactory(errorReceiver);
  }
  
  final Model model = (Model)Ring.get(Model.class);
  private final CodeModelClassFactory classFactory;
  private final ErrorReceiverFilter errorReceiver;
  private final Map<String, Element> elements = new HashMap();
  
  public void endDTD()
    throws SAXException
  {
    for (Element e : this.elements.values()) {
      e.bind();
    }
    if (this.errorReceiver.hadError()) {
      return;
    }
    processInterfaceDeclarations();
    
    this.model.serialVersionUID = this.bindInfo.getSerialVersionUID();
    if (this.model.serialVersionUID != null) {
      this.model.serializable = true;
    }
    this.model.rootClass = this.bindInfo.getSuperClass();
    this.model.rootInterface = this.bindInfo.getSuperInterface();
    
    processConstructorDeclarations();
  }
  
  private void processInterfaceDeclarations()
  {
    Map<String, InterfaceAcceptor> fromName = new HashMap();
    
    Map<BIInterface, JClass> decls = new HashMap();
    for (BIInterface decl : this.bindInfo.interfaces())
    {
      final JDefinedClass intf = this.classFactory.createInterface(this.bindInfo.getTargetPackage(), decl.name(), copyLocator());
      
      decls.put(decl, intf);
      fromName.put(decl.name(), new InterfaceAcceptor()
      {
        public void implement(JClass c)
        {
          intf._implements(c);
        }
      });
    }
    for (final CClassInfo ci : this.model.beans().values()) {
      fromName.put(ci.getName(), new InterfaceAcceptor()
      {
        public void implement(JClass c)
        {
          ci._implements(c);
        }
      });
    }
    for (Map.Entry<BIInterface, JClass> e : decls.entrySet())
    {
      BIInterface decl = (BIInterface)e.getKey();
      JClass c = (JClass)e.getValue();
      for (String member : decl.members())
      {
        InterfaceAcceptor acc = (InterfaceAcceptor)fromName.get(member);
        if (acc == null) {
          error(decl.getSourceLocation(), "TDTDReader.BindInfo.NonExistentInterfaceMember", new Object[] { member });
        } else {
          acc.implement(c);
        }
      }
    }
  }
  
  JPackage getTargetPackage()
  {
    return this.bindInfo.getTargetPackage();
  }
  
  private void processConstructorDeclarations()
  {
    for (BIElement decl : this.bindInfo.elements())
    {
      Element e = (Element)this.elements.get(decl.name());
      if (e == null) {
        error(decl.getSourceLocation(), "TDTDReader.BindInfo.NonExistentElementDeclaration", new Object[] { decl.name() });
      } else if (decl.isClass()) {
        decl.declareConstructors(e.getClassInfo());
      }
    }
  }
  
  public void attributeDecl(String elementName, String attributeName, String attributeType, String[] enumeration, short attributeUse, String defaultValue)
    throws SAXException
  {
    getOrCreateElement(elementName).attributes.add(createAttribute(elementName, attributeName, attributeType, enumeration, attributeUse, defaultValue));
  }
  
  protected CPropertyInfo createAttribute(String elementName, String attributeName, String attributeType, String[] enums, short attributeUse, String defaultValue)
    throws SAXException
  {
    boolean required = attributeUse == 3;
    
    BIElement edecl = this.bindInfo.element(elementName);
    BIAttribute decl = null;
    if (edecl != null) {
      decl = edecl.attribute(attributeName);
    }
    String propName;
    String propName;
    if (decl == null) {
      propName = this.model.getNameConverter().toPropertyName(attributeName);
    } else {
      propName = decl.getPropertyName();
    }
    QName qname = new QName("", attributeName);
    TypeUse use;
    TypeUse use;
    if ((decl != null) && (decl.getConversion() != null)) {
      use = decl.getConversion().getTransducer();
    } else {
      use = (TypeUse)builtinConversions.get(attributeType);
    }
    CPropertyInfo r = new CAttributePropertyInfo(propName, null, null, copyLocator(), qname, use, null, required);
    if (defaultValue != null) {
      r.defaultValue = CDefaultValue.create(use, new XmlString(defaultValue));
    }
    return r;
  }
  
  Element getOrCreateElement(String elementName)
  {
    Element r = (Element)this.elements.get(elementName);
    if (r == null)
    {
      r = new Element(this, elementName);
      this.elements.put(elementName, r);
    }
    return r;
  }
  
  public void startContentModel(String elementName, short contentModelType)
    throws SAXException
  {
    assert (this.modelGroups.isEmpty());
    this.modelGroups.push(new ModelGroup());
  }
  
  public void endContentModel(String elementName, short contentModelType)
    throws SAXException
  {
    assert (this.modelGroups.size() == 1);
    Term term = ((ModelGroup)this.modelGroups.pop()).wrapUp();
    
    Element e = getOrCreateElement(elementName);
    e.define(contentModelType, term, copyLocator());
  }
  
  private final Stack<ModelGroup> modelGroups = new Stack();
  private Locator locator;
  private static final Map<String, TypeUse> builtinConversions;
  
  public void startModelGroup()
    throws SAXException
  {
    this.modelGroups.push(new ModelGroup());
  }
  
  public void endModelGroup(short occurence)
    throws SAXException
  {
    Term t = Occurence.wrap(((ModelGroup)this.modelGroups.pop()).wrapUp(), occurence);
    ((ModelGroup)this.modelGroups.peek()).addTerm(t);
  }
  
  public void connector(short connectorType)
    throws SAXException
  {
    ((ModelGroup)this.modelGroups.peek()).setKind(connectorType);
  }
  
  public void childElement(String elementName, short occurence)
    throws SAXException
  {
    Element child = getOrCreateElement(elementName);
    ((ModelGroup)this.modelGroups.peek()).addTerm(Occurence.wrap(child, occurence));
    child.isReferenced = true;
  }
  
  public void setDocumentLocator(Locator loc)
  {
    this.locator = loc;
  }
  
  private Locator copyLocator()
  {
    return new LocatorImpl(this.locator);
  }
  
  static
  {
    Map<String, TypeUse> m = new HashMap();
    
    m.put("CDATA", CBuiltinLeafInfo.NORMALIZED_STRING);
    m.put("ENTITY", CBuiltinLeafInfo.TOKEN);
    m.put("ENTITIES", CBuiltinLeafInfo.STRING.makeCollection());
    m.put("NMTOKEN", CBuiltinLeafInfo.TOKEN);
    m.put("NMTOKENS", CBuiltinLeafInfo.STRING.makeCollection());
    m.put("ID", CBuiltinLeafInfo.ID);
    m.put("IDREF", CBuiltinLeafInfo.IDREF);
    m.put("IDREFS", TypeUseFactory.makeCollection(CBuiltinLeafInfo.IDREF));
    m.put("ENUMERATION", CBuiltinLeafInfo.TOKEN);
    
    builtinConversions = Collections.unmodifiableMap(m);
  }
  
  public void error(SAXParseException e)
    throws SAXException
  {
    this.errorReceiver.error(e);
  }
  
  public void fatalError(SAXParseException e)
    throws SAXException
  {
    this.errorReceiver.fatalError(e);
  }
  
  public void warning(SAXParseException e)
    throws SAXException
  {
    this.errorReceiver.warning(e);
  }
  
  protected final void error(Locator loc, String prop, Object... args)
  {
    this.errorReceiver.error(loc, Messages.format(prop, args));
  }
  
  public void startDTD(InputEntity entity)
    throws SAXException
  {}
  
  private static abstract interface InterfaceAcceptor
  {
    public abstract void implement(JClass paramJClass);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\TDTDReader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */