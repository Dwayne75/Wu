package com.sun.tools.xjc.api.impl.s2j;

import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.Property;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;

abstract class AbstractMappingImpl<InfoT extends CElement>
  implements Mapping
{
  protected final JAXBModelImpl parent;
  protected final InfoT clazz;
  private List<Property> drilldown = null;
  private boolean drilldownComputed = false;
  
  protected AbstractMappingImpl(JAXBModelImpl parent, InfoT clazz)
  {
    this.parent = parent;
    this.clazz = clazz;
  }
  
  public final QName getElement()
  {
    return this.clazz.getElementName();
  }
  
  public final String getClazz()
  {
    return ((NType)this.clazz.getType()).fullName();
  }
  
  public final List<? extends Property> getWrapperStyleDrilldown()
  {
    if (!this.drilldownComputed)
    {
      this.drilldownComputed = true;
      this.drilldown = calcDrilldown();
    }
    return this.drilldown;
  }
  
  protected abstract List<Property> calcDrilldown();
  
  protected List<Property> buildDrilldown(CClassInfo typeBean)
  {
    CClassInfo bc = typeBean.getBaseClass();
    List<Property> result;
    if (bc != null)
    {
      List<Property> result = buildDrilldown(bc);
      if (result == null) {
        return null;
      }
    }
    else
    {
      result = new ArrayList();
    }
    for (CPropertyInfo p : typeBean.getProperties()) {
      if ((p instanceof CElementPropertyInfo))
      {
        CElementPropertyInfo ep = (CElementPropertyInfo)p;
        
        List<? extends CTypeRef> ref = ep.getTypes();
        if (ref.size() != 1) {
          return null;
        }
        result.add(createPropertyImpl(ep, ((CTypeRef)ref.get(0)).getTagName()));
      }
      else if ((p instanceof ReferencePropertyInfo))
      {
        CReferencePropertyInfo rp = (CReferencePropertyInfo)p;
        
        Collection<CElement> elements = rp.getElements();
        if (elements.size() != 1) {
          return null;
        }
        CElement ref = (CElement)elements.iterator().next();
        if ((ref instanceof ClassInfo))
        {
          result.add(createPropertyImpl(rp, ref.getElementName()));
        }
        else
        {
          CElementInfo eref = (CElementInfo)ref;
          if (!eref.getSubstitutionMembers().isEmpty()) {
            return null;
          }
          ElementAdapter fr;
          ElementAdapter fr;
          if (rp.isCollection()) {
            fr = new ElementCollectionAdapter(this.parent.outline.getField(rp), eref);
          } else {
            fr = new ElementSingleAdapter(this.parent.outline.getField(rp), eref);
          }
          result.add(new PropertyImpl(this, fr, eref.getElementName()));
        }
      }
      else
      {
        return null;
      }
    }
    return result;
  }
  
  private Property createPropertyImpl(CPropertyInfo p, QName tagName)
  {
    return new PropertyImpl(this, this.parent.outline.getField(p), tagName);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\AbstractMappingImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */