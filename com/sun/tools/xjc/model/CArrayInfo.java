package com.sun.tools.xjc.model;

import com.sun.codemodel.JType;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.impl.ArrayInfoImpl;
import com.sun.xml.xsom.XSComponent;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class CArrayInfo
  extends AbstractCTypeInfoImpl
  implements ArrayInfo<NType, NClass>, CNonElement, NType
{
  private final CNonElement itemType;
  private final QName typeName;
  
  public CArrayInfo(Model model, CNonElement itemType, XSComponent source, CCustomizations customizations)
  {
    super(model, source, customizations);
    this.itemType = itemType;
    assert (itemType.getTypeName() != null);
    this.typeName = ArrayInfoImpl.calcArrayTypeName(itemType.getTypeName());
  }
  
  public CNonElement getItemType()
  {
    return this.itemType;
  }
  
  public QName getTypeName()
  {
    return this.typeName;
  }
  
  public boolean isSimpleType()
  {
    return false;
  }
  
  @Deprecated
  public CNonElement getInfo()
  {
    return this;
  }
  
  public JType toType(Outline o, Aspect aspect)
  {
    return this.itemType.toType(o, aspect).array();
  }
  
  public NType getType()
  {
    return this;
  }
  
  public boolean isBoxedType()
  {
    return false;
  }
  
  public String fullName()
  {
    return ((NType)this.itemType.getType()).fullName() + "[]";
  }
  
  public Locator getLocator()
  {
    return Model.EMPTY_LOCATOR;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CArrayInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */