package com.sun.tools.xjc.api.impl.s2j;

import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.model.CAdapter;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CTypeInfo;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.model.TypeUseFactory;
import java.util.List;

final class ElementMappingImpl
  extends AbstractMappingImpl<CElementInfo>
{
  private final TypeAndAnnotation taa;
  
  protected ElementMappingImpl(JAXBModelImpl parent, CElementInfo elementInfo)
  {
    super(parent, elementInfo);
    
    TypeUse t = ((CElementInfo)this.clazz).getContentType();
    if (((CElementInfo)this.clazz).getProperty().isCollection()) {
      t = TypeUseFactory.makeCollection(t);
    }
    CAdapter a = ((CElementInfo)this.clazz).getProperty().getAdapter();
    if (a != null) {
      t = TypeUseFactory.adapt(t, a);
    }
    this.taa = new TypeAndAnnotationImpl(parent.outline, t);
  }
  
  public TypeAndAnnotation getType()
  {
    return this.taa;
  }
  
  public final List<Property> calcDrilldown()
  {
    CElementPropertyInfo p = ((CElementInfo)this.clazz).getProperty();
    if (p.getAdapter() != null) {
      return null;
    }
    if (p.isCollection()) {
      return null;
    }
    CTypeInfo typeClass = (CTypeInfo)p.ref().get(0);
    if (!(typeClass instanceof CClassInfo)) {
      return null;
    }
    CClassInfo ci = (CClassInfo)typeClass;
    if (ci.isAbstract()) {
      return null;
    }
    if (!ci.isOrdered()) {
      return null;
    }
    return buildDrilldown(ci);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\ElementMappingImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */