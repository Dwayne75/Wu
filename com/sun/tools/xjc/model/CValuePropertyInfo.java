package com.sun.tools.xjc.model;

import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ValuePropertyInfo;
import com.sun.xml.xsom.XSComponent;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class CValuePropertyInfo
  extends CSingleTypePropertyInfo
  implements ValuePropertyInfo<NType, NClass>
{
  public CValuePropertyInfo(String name, XSComponent source, CCustomizations customizations, Locator locator, TypeUse type, QName typeName)
  {
    super(name, type, typeName, source, customizations, locator);
  }
  
  public final PropertyKind kind()
  {
    return PropertyKind.VALUE;
  }
  
  public <V> V accept(CPropertyVisitor<V> visitor)
  {
    return (V)visitor.onValue(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CValuePropertyInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */