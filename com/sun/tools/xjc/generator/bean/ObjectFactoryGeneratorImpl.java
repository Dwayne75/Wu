package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JCommentPart;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JDocComment;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementDeclWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlRegistryWriter;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.Constructor;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.v2.TODO;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.namespace.QName;

abstract class ObjectFactoryGeneratorImpl
  extends ObjectFactoryGenerator
{
  private final BeanGenerator outline;
  private final Model model;
  private final JCodeModel codeModel;
  private final JClass classRef;
  private final JDefinedClass objectFactory;
  private final HashMap<QName, JFieldVar> qnameMap = new HashMap();
  private final Map<String, CElementInfo> elementFactoryNames = new HashMap();
  private final Map<String, ClassOutlineImpl> valueFactoryNames = new HashMap();
  
  public JDefinedClass getObjectFactory()
  {
    return this.objectFactory;
  }
  
  public ObjectFactoryGeneratorImpl(BeanGenerator outline, Model model, JPackage targetPackage)
  {
    this.outline = outline;
    this.model = model;
    this.codeModel = this.model.codeModel;
    this.classRef = this.codeModel.ref(Class.class);
    
    this.objectFactory = this.outline.getClassFactory().createClass(targetPackage, "ObjectFactory", null);
    
    this.objectFactory.annotate2(XmlRegistryWriter.class);
    
    JMethod m1 = this.objectFactory.constructor(1);
    m1.javadoc().append("Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: " + targetPackage.name());
    
    this.objectFactory.javadoc().append("This object contains factory methods for each \nJava content interface and Java element interface \ngenerated in the " + targetPackage.name() + " package. \n" + "<p>An ObjectFactory allows you to programatically \n" + "construct new instances of the Java representation \n" + "for XML content. The Java representation of XML \n" + "content can consist of schema derived interfaces \n" + "and classes representing the binding of schema \n" + "type definitions, element declarations and model \n" + "groups.  Factory methods for each of these are \n" + "provided in this class.");
  }
  
  protected final void populate(CElementInfo ei, Aspect impl, Aspect exposed)
  {
    JType exposedElementType = ei.toType(this.outline, exposed);
    JType exposedType = ei.getContentInMemoryType().toType(this.outline, exposed);
    JType implType = ei.getContentInMemoryType().toType(this.outline, impl);
    String namespaceURI = ei.getElementName().getNamespaceURI();
    String localPart = ei.getElementName().getLocalPart();
    
    JClass scope = null;
    if (ei.getScope() != null) {
      scope = this.outline.getClazz(ei.getScope()).implClass;
    }
    if (ei.isAbstract()) {
      TODO.checkSpec();
    }
    CElementInfo existing = (CElementInfo)this.elementFactoryNames.put(ei.getSqueezedName(), ei);
    if (existing != null)
    {
      this.outline.getErrorReceiver().error(existing.getLocator(), Messages.OBJECT_FACTORY_CONFLICT.format(new Object[] { ei.getSqueezedName() }));
      
      this.outline.getErrorReceiver().error(ei.getLocator(), Messages.OBJECT_FACTORY_CONFLICT_RELATED.format(new Object[0]));
      
      return;
    }
    JMethod m = this.objectFactory.method(1, exposedElementType, "create" + ei.getSqueezedName());
    JVar $value = m.param(exposedType, "value");
    JExpression declaredType;
    JExpression declaredType;
    if ((implType.boxify().isParameterized()) || (!exposedType.equals(implType))) {
      declaredType = JExpr.cast(this.classRef, implType.boxify().dotclass());
    } else {
      declaredType = implType.boxify().dotclass();
    }
    JExpression scopeClass = scope == null ? JExpr._null() : scope.dotclass();
    
    JInvocation exp = JExpr._new(exposedElementType);
    if (!ei.hasClass())
    {
      exp.arg(getQNameInvocation(ei));
      exp.arg(declaredType);
      exp.arg(scopeClass);
    }
    if (implType == exposedType) {
      exp.arg($value);
    } else {
      exp.arg(JExpr.cast(implType, $value));
    }
    m.body()._return(exp);
    
    m.javadoc().append("Create an instance of ").append(exposedElementType).append("}");
    
    XmlElementDeclWriter xemw = (XmlElementDeclWriter)m.annotate2(XmlElementDeclWriter.class);
    xemw.namespace(namespaceURI).name(localPart);
    if (scope != null) {
      xemw.scope(scope);
    }
    if (ei.getSubstitutionHead() != null)
    {
      QName n = ei.getSubstitutionHead().getElementName();
      xemw.substitutionHeadNamespace(n.getNamespaceURI());
      xemw.substitutionHeadName(n.getLocalPart());
    }
    if (ei.getDefaultValue() != null) {
      xemw.defaultValue(ei.getDefaultValue());
    }
    if (ei.getProperty().inlineBinaryData()) {
      m.annotate(XmlInlineBinaryData.class);
    }
    this.outline.generateAdapterIfNecessary(ei.getProperty(), m);
  }
  
  private JExpression getQNameInvocation(CElementInfo ei)
  {
    QName name = ei.getElementName();
    if (this.qnameMap.containsKey(name)) {
      return (JExpression)this.qnameMap.get(name);
    }
    if (this.qnameMap.size() > 1024) {
      return createQName(name);
    }
    JFieldVar qnameField = this.objectFactory.field(28, QName.class, '_' + ei.getSqueezedName() + "_QNAME", createQName(name));
    
    this.qnameMap.put(name, qnameField);
    
    return qnameField;
  }
  
  private JInvocation createQName(QName name)
  {
    return JExpr._new(this.codeModel.ref(QName.class)).arg(name.getNamespaceURI()).arg(name.getLocalPart());
  }
  
  protected final void populate(ClassOutlineImpl cc, JClass sigType)
  {
    if (!cc.target.isAbstract())
    {
      JMethod m = this.objectFactory.method(1, sigType, "create" + cc.target.getSqueezedName());
      
      m.body()._return(JExpr._new(cc.implRef));
      
      m.javadoc().append("Create an instance of ").append(cc.ref);
    }
    Collection<? extends Constructor> consl = cc.target.getConstructors();
    if (consl.size() != 0) {
      cc.implClass.constructor(1);
    }
    String name = cc.target.getSqueezedName();
    ClassOutlineImpl existing = (ClassOutlineImpl)this.valueFactoryNames.put(name, cc);
    if (existing != null)
    {
      this.outline.getErrorReceiver().error(existing.target.getLocator(), Messages.OBJECT_FACTORY_CONFLICT.format(new Object[] { name }));
      
      this.outline.getErrorReceiver().error(cc.target.getLocator(), Messages.OBJECT_FACTORY_CONFLICT_RELATED.format(new Object[0]));
      
      return;
    }
    for (Constructor cons : consl)
    {
      JMethod m = this.objectFactory.method(1, cc.ref, "create" + cc.target.getSqueezedName());
      
      JInvocation inv = JExpr._new(cc.implRef);
      m.body()._return(inv);
      
      m.javadoc().append("Create an instance of ").append(cc.ref).addThrows(JAXBException.class).append("if an error occurs");
      
      JMethod c = cc.implClass.constructor(1);
      for (String fieldName : cons.fields)
      {
        CPropertyInfo field = cc.target.getProperty(fieldName);
        if (field == null)
        {
          this.outline.getErrorReceiver().error(cc.target.getLocator(), Messages.ILLEGAL_CONSTRUCTOR_PARAM.format(new Object[] { fieldName }));
        }
        else
        {
          fieldName = camelize(fieldName);
          
          FieldOutline fo = this.outline.getField(field);
          FieldAccessor accessor = fo.create(JExpr._this());
          
          inv.arg(m.param(fo.getRawType(), fieldName));
          
          JVar $var = c.param(fo.getRawType(), fieldName);
          accessor.fromRawValue(c.body(), '_' + fieldName, $var);
        }
      }
    }
  }
  
  private static String camelize(String s)
  {
    return Character.toLowerCase(s.charAt(0)) + s.substring(1);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\ObjectFactoryGeneratorImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */