package com.sun.tools.xjc.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JJavaName;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.runtime.RuntimeUtil.ToStringAdapter;
import com.sun.xml.xsom.XSComponent;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public abstract class CPropertyInfo
  implements PropertyInfo<NType, NClass>, CCustomizable
{
  @XmlTransient
  private CClassInfo parent;
  private String privateName;
  private String publicName;
  private final boolean isCollection;
  @XmlTransient
  public final Locator locator;
  private final XSComponent source;
  public JType baseType;
  public String javadoc = "";
  public boolean inlineBinaryData;
  @XmlJavaTypeAdapter(RuntimeUtil.ToStringAdapter.class)
  public FieldRenderer realization;
  public CDefaultValue defaultValue;
  private final CCustomizations customizations;
  
  protected CPropertyInfo(String name, boolean collection, XSComponent source, CCustomizations customizations, Locator locator)
  {
    this.publicName = name;
    String n = NameConverter.standard.toVariableName(name);
    if (!JJavaName.isJavaIdentifier(n)) {
      n = '_' + n;
    }
    this.privateName = n;
    
    this.isCollection = collection;
    this.locator = locator;
    if (customizations == null) {
      this.customizations = CCustomizations.EMPTY;
    } else {
      this.customizations = customizations;
    }
    this.source = source;
  }
  
  final void setParent(CClassInfo parent)
  {
    assert (this.parent == null);
    assert (parent != null);
    this.parent = parent;
    this.customizations.setParent(parent.model, this);
  }
  
  public CTypeInfo parent()
  {
    return this.parent;
  }
  
  public Locator getLocator()
  {
    return this.locator;
  }
  
  public final XSComponent getSchemaComponent()
  {
    return this.source;
  }
  
  public abstract CAdapter getAdapter();
  
  /**
   * @deprecated
   */
  public String getName()
  {
    return getName(false);
  }
  
  public String getName(boolean isPublic)
  {
    return isPublic ? this.publicName : this.privateName;
  }
  
  public void setName(boolean isPublic, String newName)
  {
    if (isPublic) {
      this.publicName = newName;
    } else {
      this.privateName = newName;
    }
  }
  
  public String displayName()
  {
    return this.parent.toString() + '#' + getName(false);
  }
  
  public boolean isCollection()
  {
    return this.isCollection;
  }
  
  public abstract Collection<? extends CTypeInfo> ref();
  
  public boolean isUnboxable()
  {
    Collection<? extends CTypeInfo> ts = ref();
    if (ts.size() != 1) {
      return false;
    }
    if ((this.baseType != null) && ((this.baseType instanceof JClass))) {
      return false;
    }
    CTypeInfo t = (CTypeInfo)ts.iterator().next();
    
    return ((NType)t.getType()).isBoxedType();
  }
  
  public boolean isOptionalPrimitive()
  {
    return false;
  }
  
  public CCustomizations getCustomizations()
  {
    return this.customizations;
  }
  
  public boolean inlineBinaryData()
  {
    return this.inlineBinaryData;
  }
  
  public abstract <V> V accept(CPropertyVisitor<V> paramCPropertyVisitor);
  
  protected static boolean needsExplicitTypeName(TypeUse type, QName typeName)
  {
    if (typeName == null) {
      return false;
    }
    if (!typeName.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema")) {
      return false;
    }
    if (type.isCollection()) {
      return true;
    }
    QName itemType = type.getInfo().getTypeName();
    if (itemType == null) {
      return true;
    }
    return !itemType.equals(typeName);
  }
  
  public QName collectElementNames(Map<QName, CPropertyInfo> table)
  {
    return null;
  }
  
  public final <A extends Annotation> A readAnnotation(Class<A> annotationType)
  {
    throw new UnsupportedOperationException();
  }
  
  public final boolean hasAnnotation(Class<? extends Annotation> annotationType)
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CPropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */