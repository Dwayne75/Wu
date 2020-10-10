package com.sun.tools.xjc.api.impl.s2j;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.outline.PackageOutline;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

final class JAXBModelImpl
  implements S2JJAXBModel
{
  final Outline outline;
  private final Model model;
  private final Map<QName, Mapping> byXmlName = new HashMap();
  
  JAXBModelImpl(Outline outline)
  {
    this.model = outline.getModel();
    this.outline = outline;
    for (CClassInfo ci : this.model.beans().values()) {
      if (ci.isElement()) {
        this.byXmlName.put(ci.getElementName(), new BeanMappingImpl(this, ci));
      }
    }
    for (CElementInfo ei : this.model.getElementMappings(null).values()) {
      this.byXmlName.put(ei.getElementName(), new ElementMappingImpl(this, ei));
    }
  }
  
  public JCodeModel generateCode(Plugin[] extensions, ErrorListener errorListener)
  {
    return this.outline.getCodeModel();
  }
  
  public List<JClass> getAllObjectFactories()
  {
    List<JClass> r = new ArrayList();
    for (PackageOutline pkg : this.outline.getAllPackageContexts()) {
      r.add(pkg.objectFactory());
    }
    return r;
  }
  
  public final Mapping get(QName elementName)
  {
    return (Mapping)this.byXmlName.get(elementName);
  }
  
  public final Collection<? extends Mapping> getMappings()
  {
    return this.byXmlName.values();
  }
  
  public TypeAndAnnotation getJavaType(QName xmlTypeName)
  {
    TypeUse use = (TypeUse)this.model.typeUses().get(xmlTypeName);
    if (use == null) {
      return null;
    }
    return new TypeAndAnnotationImpl(this.outline, use);
  }
  
  public final List<String> getClassList()
  {
    List<String> classList = new ArrayList();
    for (PackageOutline p : this.outline.getAllPackageContexts()) {
      classList.add(p.objectFactory().fullName());
    }
    return classList;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\impl\s2j\JAXBModelImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */