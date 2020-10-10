package com.sun.tools.xjc.generator.bean;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.generator.annotation.spec.XmlSchemaWriter;
import com.sun.tools.xjc.model.CAttributePropertyInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElement;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CPropertyVisitor;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.PackageOutline;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.namespace.QName;

final class PackageOutlineImpl
  implements PackageOutline
{
  private final Model _model;
  private final JPackage _package;
  private final ObjectFactoryGenerator objectFactoryGenerator;
  final Set<ClassOutlineImpl> classes = new HashSet();
  private final Set<ClassOutlineImpl> classesView = Collections.unmodifiableSet(this.classes);
  private String mostUsedNamespaceURI;
  private XmlNsForm elementFormDefault;
  
  public String getMostUsedNamespaceURI()
  {
    return this.mostUsedNamespaceURI;
  }
  
  public XmlNsForm getElementFormDefault()
  {
    assert (this.elementFormDefault != null);
    return this.elementFormDefault;
  }
  
  public JPackage _package()
  {
    return this._package;
  }
  
  public ObjectFactoryGenerator objectFactoryGenerator()
  {
    return this.objectFactoryGenerator;
  }
  
  public Set<ClassOutlineImpl> getClasses()
  {
    return this.classesView;
  }
  
  public JDefinedClass objectFactory()
  {
    return this.objectFactoryGenerator.getObjectFactory();
  }
  
  protected PackageOutlineImpl(BeanGenerator outline, Model model, JPackage _pkg)
  {
    this._model = model;
    this._package = _pkg;
    switch (model.strategy)
    {
    case BEAN_ONLY: 
      this.objectFactoryGenerator = new PublicObjectFactoryGenerator(outline, model, _pkg);
      break;
    case INTF_AND_IMPL: 
      this.objectFactoryGenerator = new DualObjectFactoryGenerator(outline, model, _pkg);
      break;
    default: 
      throw new IllegalStateException();
    }
  }
  
  public void calcDefaultValues()
  {
    if (!this._model.isPackageLevelAnnotations())
    {
      this.mostUsedNamespaceURI = "";
      this.elementFormDefault = XmlNsForm.UNQUALIFIED;
      return;
    }
    CPropertyVisitor<Void> propVisitor = new CPropertyVisitor()
    {
      public Void onElement(CElementPropertyInfo p)
      {
        for (CTypeRef tr : p.getTypes()) {
          PackageOutlineImpl.this.countURI(PackageOutlineImpl.this.propUriCountMap, tr.getTagName());
        }
        return null;
      }
      
      public Void onReference(CReferencePropertyInfo p)
      {
        for (CElement e : p.getElements()) {
          PackageOutlineImpl.this.countURI(PackageOutlineImpl.this.propUriCountMap, e.getElementName());
        }
        return null;
      }
      
      public Void onAttribute(CAttributePropertyInfo p)
      {
        return null;
      }
      
      public Void onValue(CValuePropertyInfo p)
      {
        return null;
      }
    };
    for (ClassOutlineImpl co : this.classes)
    {
      CClassInfo ci = co.target;
      countURI(this.uriCountMap, ci.getTypeName());
      countURI(this.uriCountMap, ci.getElementName());
      for (CPropertyInfo p : ci.getProperties()) {
        p.accept(propVisitor);
      }
    }
    this.mostUsedNamespaceURI = getMostUsedURI(this.uriCountMap);
    this.elementFormDefault = getFormDefault();
    if ((!this.mostUsedNamespaceURI.equals("")) || (this.elementFormDefault == XmlNsForm.QUALIFIED))
    {
      XmlSchemaWriter w = (XmlSchemaWriter)this._model.strategy.getPackage(this._package, Aspect.IMPLEMENTATION).annotate2(XmlSchemaWriter.class);
      if (!this.mostUsedNamespaceURI.equals("")) {
        w.namespace(this.mostUsedNamespaceURI);
      }
      if (this.elementFormDefault == XmlNsForm.QUALIFIED) {
        w.elementFormDefault(this.elementFormDefault);
      }
    }
  }
  
  private HashMap<String, Integer> uriCountMap = new HashMap();
  private HashMap<String, Integer> propUriCountMap = new HashMap();
  
  private void countURI(HashMap<String, Integer> map, QName qname)
  {
    if (qname == null) {
      return;
    }
    String uri = qname.getNamespaceURI();
    if (map.containsKey(uri)) {
      map.put(uri, Integer.valueOf(((Integer)map.get(uri)).intValue() + 1));
    } else {
      map.put(uri, Integer.valueOf(1));
    }
  }
  
  private String getMostUsedURI(HashMap<String, Integer> map)
  {
    String mostPopular = null;
    int count = 0;
    for (Map.Entry<String, Integer> e : map.entrySet())
    {
      String uri = (String)e.getKey();
      int uriCount = ((Integer)e.getValue()).intValue();
      if (mostPopular == null)
      {
        mostPopular = uri;
        count = uriCount;
      }
      else if ((uriCount > count) || ((uriCount == count) && (mostPopular.equals(""))))
      {
        mostPopular = uri;
        count = uriCount;
      }
    }
    if (mostPopular == null) {
      return "";
    }
    return mostPopular;
  }
  
  private XmlNsForm getFormDefault()
  {
    if (getMostUsedURI(this.propUriCountMap).equals("")) {
      return XmlNsForm.UNQUALIFIED;
    }
    return XmlNsForm.QUALIFIED;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\generator\bean\PackageOutlineImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */