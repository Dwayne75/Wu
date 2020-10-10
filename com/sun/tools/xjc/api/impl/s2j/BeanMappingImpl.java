package com.sun.tools.xjc.api.impl.s2j;

import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.TypeUse;
import java.util.List;

final class BeanMappingImpl
  extends AbstractMappingImpl<CClassInfo>
{
  private final TypeAndAnnotationImpl taa = new TypeAndAnnotationImpl(this.parent.outline, (TypeUse)this.clazz);
  
  BeanMappingImpl(JAXBModelImpl parent, CClassInfo classInfo)
  {
    super(parent, classInfo);
    assert (classInfo.isElement());
  }
  
  public TypeAndAnnotation getType()
  {
    return this.taa;
  }
  
  public final String getTypeClass()
  {
    return getClazz();
  }
  
  public List<Property> calcDrilldown()
  {
    if (!((CClassInfo)this.clazz).isOrdered()) {
      return null;
    }
    return buildDrilldown((CClassInfo)this.clazz);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\BeanMappingImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */