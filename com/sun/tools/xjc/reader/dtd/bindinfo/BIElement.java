package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.model.CClassInfo;
import com.sun.xml.bind.api.impl.NameConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.w3c.dom.Element;
import org.xml.sax.Locator;

public final class BIElement
{
  final BindInfo parent;
  private final Element e;
  public final CClassInfo clazz;
  
  BIElement(BindInfo bi, Element _e)
  {
    this.parent = bi;
    this.e = _e;
    
    Element c = DOMUtil.getElement(this.e, "content");
    if (c != null) {
      if (DOMUtil.getAttribute(c, "property") != null) {
        this.rest = BIContent.create(c, this);
      } else {
        for (Element p : DOMUtil.getChildElements(c)) {
          if (p.getLocalName().equals("rest")) {
            this.rest = BIContent.create(p, this);
          } else {
            this.contents.add(BIContent.create(p, this));
          }
        }
      }
    }
    for (Element atr : DOMUtil.getChildElements(this.e, "attribute"))
    {
      BIAttribute a = new BIAttribute(this, atr);
      this.attributes.put(a.name(), a);
    }
    if (isClass())
    {
      String className = DOMUtil.getAttribute(this.e, "class");
      if (className == null) {
        className = NameConverter.standard.toClassName(name());
      }
      this.className = className;
    }
    else
    {
      this.className = null;
    }
    for (Element conv : DOMUtil.getChildElements(this.e, "conversion"))
    {
      BIConversion c = new BIUserConversion(bi, conv);
      this.conversions.put(c.name(), c);
    }
    for (Element en : DOMUtil.getChildElements(this.e, "enumeration"))
    {
      BIConversion c = BIEnumeration.create(en, this);
      this.conversions.put(c.name(), c);
    }
    for (Element c : DOMUtil.getChildElements(this.e, "constructor")) {
      this.constructors.add(new BIConstructor(c));
    }
    String name = name();
    QName tagName = new QName("", name);
    
    this.clazz = new CClassInfo(this.parent.model, this.parent.getTargetPackage(), this.className, getLocation(), null, tagName, null, null);
  }
  
  public Locator getLocation()
  {
    return DOMLocator.getLocationInfo(this.e);
  }
  
  private final List<BIContent> contents = new ArrayList();
  private final Map<String, BIConversion> conversions = new HashMap();
  private BIContent rest;
  private final Map<String, BIAttribute> attributes = new HashMap();
  private final List<BIConstructor> constructors = new ArrayList();
  private final String className;
  
  public String name()
  {
    return DOMUtil.getAttribute(this.e, "name");
  }
  
  public boolean isClass()
  {
    return "class".equals(this.e.getAttribute("type"));
  }
  
  public boolean isRoot()
  {
    return "true".equals(this.e.getAttribute("root"));
  }
  
  public String getClassName()
  {
    return this.className;
  }
  
  public void declareConstructors(CClassInfo src)
  {
    for (BIConstructor c : this.constructors) {
      c.createDeclaration(src);
    }
  }
  
  public BIConversion getConversion()
  {
    String cnv = DOMUtil.getAttribute(this.e, "convert");
    if (cnv == null) {
      return null;
    }
    return conversion(cnv);
  }
  
  public BIConversion conversion(String name)
  {
    BIConversion r = (BIConversion)this.conversions.get(name);
    if (r != null) {
      return r;
    }
    return this.parent.conversion(name);
  }
  
  public List<BIContent> getContents()
  {
    return this.contents;
  }
  
  public BIAttribute attribute(String name)
  {
    return (BIAttribute)this.attributes.get(name);
  }
  
  public BIContent getRest()
  {
    return this.rest;
  }
  
  public Locator getSourceLocation()
  {
    return DOMLocator.getLocationInfo(this.e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\bindinfo\BIElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */