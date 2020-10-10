package com.sun.tools.xjc.api.impl.s2j;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;
import javax.xml.namespace.QName;

public final class PropertyImpl
  implements Property
{
  protected final FieldOutline fr;
  protected final QName elementName;
  protected final Mapping parent;
  protected final JCodeModel codeModel;
  
  PropertyImpl(Mapping parent, FieldOutline fr, QName elementName)
  {
    this.parent = parent;
    this.fr = fr;
    this.elementName = elementName;
    this.codeModel = fr.getRawType().owner();
  }
  
  public final String name()
  {
    return this.fr.getPropertyInfo().getName(false);
  }
  
  public final QName elementName()
  {
    return this.elementName;
  }
  
  public final JType type()
  {
    return this.fr.getRawType();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\PropertyImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */