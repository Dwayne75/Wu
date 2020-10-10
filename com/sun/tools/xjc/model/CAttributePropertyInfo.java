package com.sun.tools.xjc.model;

import com.sun.istack.Nullable;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.xsom.XSComponent;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class CAttributePropertyInfo
  extends CSingleTypePropertyInfo
  implements AttributePropertyInfo<NType, NClass>
{
  private final QName attName;
  private final boolean isRequired;
  
  public CAttributePropertyInfo(String name, XSComponent source, CCustomizations customizations, Locator locator, QName attName, TypeUse type, @Nullable QName typeName, boolean required)
  {
    super(name, type, typeName, source, customizations, locator);
    this.isRequired = required;
    this.attName = attName;
  }
  
  public boolean isRequired()
  {
    return this.isRequired;
  }
  
  public QName getXmlName()
  {
    return this.attName;
  }
  
  public boolean isUnboxable()
  {
    if (!this.isRequired) {
      return false;
    }
    return super.isUnboxable();
  }
  
  public boolean isOptionalPrimitive()
  {
    return (!this.isRequired) && (super.isUnboxable());
  }
  
  public <V> V accept(CPropertyVisitor<V> visitor)
  {
    return (V)visitor.onAttribute(this);
  }
  
  public final PropertyKind kind()
  {
    return PropertyKind.ATTRIBUTE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CAttributePropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */