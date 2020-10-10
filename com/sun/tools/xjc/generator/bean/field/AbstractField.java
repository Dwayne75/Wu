package com.sun.tools.xjc.generator.bean.field;

import com.sun.codemodel.JAnnotatable;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.annotation.spec.XmlAnyElementWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlAttributeWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementRefWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementRefsWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlElementsWriter;
import com.sun.tools.xjc.generator.annotation.spec.XmlSchemaTypeWriter;
import com.sun.tools.xjc.generator.bean.BeanGenerator;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.FieldAccessor;
import com.sun.tools.xjc.outline.FieldOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.W3CDomHandler;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

abstract class AbstractField
  implements FieldOutline
{
  protected final ClassOutlineImpl outline;
  protected final CPropertyInfo prop;
  protected final JCodeModel codeModel;
  protected final JType implType;
  protected final JType exposedType;
  
  protected AbstractField(ClassOutlineImpl outline, CPropertyInfo prop)
  {
    this.outline = outline;
    this.prop = prop;
    this.codeModel = outline.parent().getCodeModel();
    this.implType = getType(Aspect.IMPLEMENTATION);
    this.exposedType = getType(Aspect.EXPOSED);
  }
  
  public final ClassOutline parent()
  {
    return this.outline;
  }
  
  public final CPropertyInfo getPropertyInfo()
  {
    return this.prop;
  }
  
  protected void annotate(JAnnotatable field)
  {
    assert (field != null);
    if ((this.prop instanceof CAttributePropertyInfo)) {
      annotateAttribute(field);
    } else if ((this.prop instanceof CElementPropertyInfo)) {
      annotateElement(field);
    } else if ((this.prop instanceof CValuePropertyInfo)) {
      field.annotate(XmlValue.class);
    } else if ((this.prop instanceof CReferencePropertyInfo)) {
      annotateReference(field);
    }
    this.outline.parent().generateAdapterIfNecessary(this.prop, field);
    
    QName st = this.prop.getSchemaType();
    if (st != null) {
      ((XmlSchemaTypeWriter)field.annotate2(XmlSchemaTypeWriter.class)).name(st.getLocalPart()).namespace(st.getNamespaceURI());
    }
    if (this.prop.inlineBinaryData()) {
      field.annotate(XmlInlineBinaryData.class);
    }
  }
  
  private void annotateReference(JAnnotatable field)
  {
    CReferencePropertyInfo rp = (CReferencePropertyInfo)this.prop;
    
    TODO.prototype();
    
    Collection<CElement> elements = rp.getElements();
    XmlElementRefsWriter refsw;
    if (elements.size() == 1)
    {
      XmlElementRefWriter refw = (XmlElementRefWriter)field.annotate2(XmlElementRefWriter.class);
      CElement e = (CElement)elements.iterator().next();
      refw.name(e.getElementName().getLocalPart()).namespace(e.getElementName().getNamespaceURI()).type(((NType)e.getType()).toType(this.outline.parent(), Aspect.IMPLEMENTATION));
    }
    else if (elements.size() > 1)
    {
      refsw = (XmlElementRefsWriter)field.annotate2(XmlElementRefsWriter.class);
      for (CElement e : elements)
      {
        XmlElementRefWriter refw = refsw.value();
        refw.name(e.getElementName().getLocalPart()).namespace(e.getElementName().getNamespaceURI()).type(((NType)e.getType()).toType(this.outline.parent(), Aspect.IMPLEMENTATION));
      }
    }
    if (rp.isMixed()) {
      field.annotate(XmlMixed.class);
    }
    NClass dh = rp.getDOMHandler();
    if (dh != null)
    {
      XmlAnyElementWriter xaew = (XmlAnyElementWriter)field.annotate2(XmlAnyElementWriter.class);
      xaew.lax(rp.getWildcard().allowTypedObject);
      
      JClass value = dh.toType(this.outline.parent(), Aspect.IMPLEMENTATION);
      if (!value.equals(this.codeModel.ref(W3CDomHandler.class))) {
        xaew.value(value);
      }
    }
  }
  
  private void annotateElement(JAnnotatable field)
  {
    CElementPropertyInfo ep = (CElementPropertyInfo)this.prop;
    List<CTypeRef> types = ep.getTypes();
    if (ep.isValueList()) {
      field.annotate(XmlList.class);
    }
    assert (ep.getXmlName() == null);
    if (types.size() == 1)
    {
      CTypeRef t = (CTypeRef)types.get(0);
      writeXmlElementAnnotation(field, t, resolve(t, Aspect.IMPLEMENTATION), false);
    }
    else
    {
      for (CTypeRef t : types) {
        writeXmlElementAnnotation(field, t, resolve(t, Aspect.IMPLEMENTATION), true);
      }
      this.xesw = null;
    }
  }
  
  private void writeXmlElementAnnotation(JAnnotatable field, CTypeRef ctype, JType jtype, boolean checkWrapper)
  {
    XmlElementWriter xew = null;
    
    XmlNsForm formDefault = parent()._package().getElementFormDefault();
    String propName = this.prop.getName(false);
    String enclosingTypeNS;
    String enclosingTypeNS;
    if (parent().target.getTypeName() == null) {
      enclosingTypeNS = parent()._package().getMostUsedNamespaceURI();
    } else {
      enclosingTypeNS = parent().target.getTypeName().getNamespaceURI();
    }
    String generatedName = ctype.getTagName().getLocalPart();
    if (!generatedName.equals(propName))
    {
      if (xew == null) {
        xew = getXew(checkWrapper, field);
      }
      xew.name(generatedName);
    }
    String generatedNS = ctype.getTagName().getNamespaceURI();
    if (((formDefault == XmlNsForm.QUALIFIED) && (!generatedNS.equals(enclosingTypeNS))) || ((formDefault == XmlNsForm.UNQUALIFIED) && (!generatedNS.equals(""))))
    {
      if (xew == null) {
        xew = getXew(checkWrapper, field);
      }
      xew.namespace(generatedNS);
    }
    CElementPropertyInfo ep = (CElementPropertyInfo)this.prop;
    if ((ep.isRequired()) && (this.exposedType.isReference()))
    {
      if (xew == null) {
        xew = getXew(checkWrapper, field);
      }
      xew.required(true);
    }
    if ((ep.isRequired()) && (!this.prop.isCollection())) {
      jtype = jtype.unboxify();
    }
    if ((!jtype.equals(this.exposedType)) || ((parent().parent().getModel().options.runtime14) && (this.prop.isCollection())))
    {
      if (xew == null) {
        xew = getXew(checkWrapper, field);
      }
      xew.type(jtype);
    }
    String defaultValue = ctype.getDefaultValue();
    if (defaultValue != null)
    {
      if (xew == null) {
        xew = getXew(checkWrapper, field);
      }
      xew.defaultValue(defaultValue);
    }
    if (ctype.isNillable())
    {
      if (xew == null) {
        xew = getXew(checkWrapper, field);
      }
      xew.nillable(true);
    }
  }
  
  private XmlElementsWriter xesw = null;
  
  private XmlElementWriter getXew(boolean checkWrapper, JAnnotatable field)
  {
    XmlElementWriter xew;
    XmlElementWriter xew;
    if (checkWrapper)
    {
      if (this.xesw == null) {
        this.xesw = ((XmlElementsWriter)field.annotate2(XmlElementsWriter.class));
      }
      xew = this.xesw.value();
    }
    else
    {
      xew = (XmlElementWriter)field.annotate2(XmlElementWriter.class);
    }
    return xew;
  }
  
  private void annotateAttribute(JAnnotatable field)
  {
    CAttributePropertyInfo ap = (CAttributePropertyInfo)this.prop;
    QName attName = ap.getXmlName();
    
    XmlAttributeWriter xaw = (XmlAttributeWriter)field.annotate2(XmlAttributeWriter.class);
    
    String generatedName = attName.getLocalPart();
    String generatedNS = attName.getNamespaceURI();
    if (!generatedName.equals(ap.getName(false))) {
      xaw.name(generatedName);
    }
    if (!generatedNS.equals("")) {
      xaw.namespace(generatedNS);
    }
    if (ap.isRequired()) {
      xaw.required(true);
    }
  }
  
  protected abstract class Accessor
    implements FieldAccessor
  {
    protected final JExpression $target;
    
    protected Accessor(JExpression $target)
    {
      this.$target = $target;
    }
    
    public final FieldOutline owner()
    {
      return AbstractField.this;
    }
    
    public final CPropertyInfo getPropertyInfo()
    {
      return AbstractField.this.prop;
    }
  }
  
  protected final JFieldVar generateField(JType type)
  {
    return this.outline.implClass.field(2, type, this.prop.getName(false));
  }
  
  protected final JExpression castToImplType(JExpression exp)
  {
    if (this.implType == this.exposedType) {
      return exp;
    }
    return JExpr.cast(this.implType, exp);
  }
  
  protected JType getType(final Aspect aspect)
  {
    if (this.prop.getAdapter() != null) {
      return ((NType)this.prop.getAdapter().customType).toType(this.outline.parent(), aspect);
    }
    ArrayList r = new ArrayList()
    {
      void add(CTypeInfo t)
      {
        add(((NType)t.getType()).toType(this.this$0.outline.parent(), aspect));
        if ((t instanceof CElementInfo)) {
          add(((CElementInfo)t).getSubstitutionMembers());
        }
      }
      
      void add(Collection<? extends CTypeInfo> col)
      {
        for (CTypeInfo typeInfo : col) {
          add(typeInfo);
        }
      }
    };
    r.add(this.prop.ref());
    JType t;
    JType t;
    if (this.prop.baseType != null) {
      t = this.prop.baseType;
    } else {
      t = TypeUtil.getCommonBaseType(this.codeModel, r);
    }
    if (this.prop.isUnboxable()) {
      t = t.unboxify();
    }
    return t;
  }
  
  protected final List<Object> listPossibleTypes(CPropertyInfo prop)
  {
    List<Object> r = new ArrayList();
    for (CTypeInfo tt : prop.ref())
    {
      JType t = ((NType)tt.getType()).toType(this.outline.parent(), Aspect.EXPOSED);
      if ((t.isPrimitive()) || (t.isArray()))
      {
        r.add(t.fullName());
      }
      else
      {
        r.add(t);
        r.add("\n");
      }
    }
    return r;
  }
  
  private JType resolve(CTypeRef typeRef, Aspect a)
  {
    return this.outline.parent().resolve(typeRef, a);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\field\AbstractField.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */