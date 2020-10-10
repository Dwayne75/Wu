package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfoParent;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.tools.xjc.reader.xmlschema.ClassSelector;
import com.sun.xml.xsom.XSSimpleType;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public abstract class BIConversion
  extends AbstractDeclarationImpl
{
  @Deprecated
  public BIConversion(Locator loc)
  {
    super(loc);
  }
  
  protected BIConversion() {}
  
  public abstract TypeUse getTypeUse(XSSimpleType paramXSSimpleType);
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "conversion");
  
  public static final class Static
    extends BIConversion
  {
    private final TypeUse transducer;
    
    public Static(Locator loc, TypeUse transducer)
    {
      super();
      this.transducer = transducer;
    }
    
    public TypeUse getTypeUse(XSSimpleType owner)
    {
      return this.transducer;
    }
  }
  
  @XmlRootElement(name="javaType")
  public static class User
    extends BIConversion
  {
    @XmlAttribute
    private String parseMethod;
    @XmlAttribute
    private String printMethod;
    @XmlAttribute(name="name")
    private String type = "java.lang.String";
    private JType inMemoryType;
    private TypeUse typeUse;
    
    public User(Locator loc, String parseMethod, String printMethod, JType inMemoryType)
    {
      super();
      this.parseMethod = parseMethod;
      this.printMethod = printMethod;
      this.inMemoryType = inMemoryType;
    }
    
    public User() {}
    
    public TypeUse getTypeUse(XSSimpleType owner)
    {
      if (this.typeUse != null) {
        return this.typeUse;
      }
      JCodeModel cm = getCodeModel();
      if (this.inMemoryType == null) {
        this.inMemoryType = TypeUtil.getType(cm, this.type, (ErrorReceiver)Ring.get(ErrorReceiver.class), getLocation());
      }
      JDefinedClass adapter = generateAdapter(parseMethodFor(owner), printMethodFor(owner), owner);
      
      this.typeUse = TypeUseFactory.adapt(CBuiltinLeafInfo.STRING, new CAdapter(adapter));
      
      return this.typeUse;
    }
    
    private JDefinedClass generateAdapter(String parseMethod, String printMethod, XSSimpleType owner)
    {
      JDefinedClass adapter = null;
      
      int id = 1;
      while (adapter == null) {
        try
        {
          JPackage pkg = ((ClassSelector)Ring.get(ClassSelector.class)).getClassScope().getOwnerPackage();
          adapter = pkg._class("Adapter" + id);
        }
        catch (JClassAlreadyExistsException e)
        {
          id++;
        }
      }
      JClass bim = this.inMemoryType.boxify();
      
      adapter._extends(getCodeModel().ref(XmlAdapter.class).narrow(String.class).narrow(bim));
      
      JMethod unmarshal = adapter.method(1, bim, "unmarshal");
      JVar $value = unmarshal.param(String.class, "value");
      JExpression inv;
      JExpression inv;
      if (parseMethod.equals("new"))
      {
        inv = JExpr._new(bim).arg($value);
      }
      else
      {
        int idx = parseMethod.lastIndexOf('.');
        JExpression inv;
        if (idx < 0) {
          inv = bim.staticInvoke(parseMethod).arg($value);
        } else {
          inv = JExpr.direct(parseMethod + "(value)");
        }
      }
      unmarshal.body()._return(inv);
      
      JMethod marshal = adapter.method(1, String.class, "marshal");
      $value = marshal.param(bim, "value");
      if (printMethod.startsWith("javax.xml.bind.DatatypeConverter.")) {
        marshal.body()._if($value.eq(JExpr._null()))._then()._return(JExpr._null());
      }
      int idx = printMethod.lastIndexOf('.');
      if (idx < 0)
      {
        inv = $value.invoke(printMethod);
        
        JConditional jcon = marshal.body()._if($value.eq(JExpr._null()));
        jcon._then()._return(JExpr._null());
      }
      else if (this.printMethod == null)
      {
        JType t = this.inMemoryType.unboxify();
        inv = JExpr.direct(printMethod + "((" + findBaseConversion(owner).toLowerCase() + ")(" + t.fullName() + ")value)");
      }
      else
      {
        inv = JExpr.direct(printMethod + "(value)");
      }
      marshal.body()._return(inv);
      
      return adapter;
    }
    
    private String printMethodFor(XSSimpleType owner)
    {
      if (this.printMethod != null) {
        return this.printMethod;
      }
      if (this.inMemoryType.unboxify().isPrimitive())
      {
        String method = getConversionMethod("print", owner);
        if (method != null) {
          return method;
        }
      }
      return "toString";
    }
    
    private String parseMethodFor(XSSimpleType owner)
    {
      if (this.parseMethod != null) {
        return this.parseMethod;
      }
      if (this.inMemoryType.unboxify().isPrimitive())
      {
        String method = getConversionMethod("parse", owner);
        if (method != null) {
          return '(' + this.inMemoryType.unboxify().fullName() + ')' + method;
        }
      }
      return "new";
    }
    
    private static final String[] knownBases = { "Float", "Double", "Byte", "Short", "Int", "Long", "Boolean" };
    
    private String getConversionMethod(String methodPrefix, XSSimpleType owner)
    {
      String bc = findBaseConversion(owner);
      if (bc == null) {
        return null;
      }
      return DatatypeConverter.class.getName() + '.' + methodPrefix + bc;
    }
    
    private String findBaseConversion(XSSimpleType owner)
    {
      for (XSSimpleType st = owner; st != null; st = st.getSimpleBaseType()) {
        if ("http://www.w3.org/2001/XMLSchema".equals(st.getTargetNamespace()))
        {
          String name = st.getName().intern();
          for (String s : knownBases) {
            if (name.equalsIgnoreCase(s)) {
              return s;
            }
          }
        }
      }
      return null;
    }
    
    public QName getName()
    {
      return NAME;
    }
    
    public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "javaType");
  }
  
  @XmlRootElement(name="javaType", namespace="http://java.sun.com/xml/ns/jaxb/xjc")
  public static class UserAdapter
    extends BIConversion
  {
    @XmlAttribute(name="name")
    private String type = null;
    @XmlAttribute
    private String adapter = null;
    private TypeUse typeUse;
    
    public TypeUse getTypeUse(XSSimpleType owner)
    {
      if (this.typeUse != null) {
        return this.typeUse;
      }
      JCodeModel cm = getCodeModel();
      JDefinedClass a;
      try
      {
        a = cm._class(this.adapter);
        a.hide();
        a._extends(cm.ref(XmlAdapter.class).narrow(String.class).narrow(cm.ref(this.type)));
      }
      catch (JClassAlreadyExistsException e)
      {
        a = e.getExistingClass();
      }
      this.typeUse = TypeUseFactory.adapt(CBuiltinLeafInfo.STRING, new CAdapter(a));
      
      return this.typeUse;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIConversion.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */