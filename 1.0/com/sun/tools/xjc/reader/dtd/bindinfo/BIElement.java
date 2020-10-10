package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.annotator.AnnotatorController;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.dom4j.Element;
import org.xml.sax.Locator;

public final class BIElement
{
  final BindInfo parent;
  private final Element e;
  
  BIElement(BindInfo bi, Element _e)
  {
    this.parent = bi;
    this.e = _e;
    
    Element c = this.e.element("content");
    if (c != null) {
      if (c.attribute("property") != null)
      {
        this.rest = BIContent.create(c, this);
      }
      else
      {
        Iterator itr = c.elementIterator();
        while (itr.hasNext())
        {
          Element p = (Element)itr.next();
          if (p.getName().equals("rest")) {
            this.rest = BIContent.create(p, this);
          } else {
            this.contents.add(BIContent.create(p, this));
          }
        }
      }
    }
    Iterator itr = this.e.elementIterator("attribute");
    while (itr.hasNext())
    {
      BIAttribute a = new BIAttribute(this, (Element)itr.next());
      this.attributes.put(a.name(), a);
    }
    if (isClass())
    {
      String className = this.e.attributeValue("class");
      if (className == null) {
        className = this.parent.nameConverter.toClassName(name());
      }
      this.clazz = this.parent.classFactory.createInterface(this.parent.getTargetPackage(), className, null);
    }
    else
    {
      this.clazz = null;
    }
    itr = this.e.elementIterator("conversion");
    while (itr.hasNext())
    {
      BIConversion c = new BIUserConversion(bi, (Element)itr.next());
      this.conversions.put(c.name(), c);
    }
    itr = this.e.elementIterator("enumeration");
    while (itr.hasNext())
    {
      BIConversion c = BIEnumeration.create((Element)itr.next(), this);
      this.conversions.put(c.name(), c);
    }
    itr = this.e.elementIterator("constructor");
    while (itr.hasNext()) {
      this.constructors.add(new BIConstructor((Element)itr.next()));
    }
  }
  
  private final Vector contents = new Vector();
  private final Map conversions = new HashMap();
  private BIContent rest;
  private final Map attributes = new HashMap();
  private final Vector constructors = new Vector();
  private final JDefinedClass clazz;
  
  public String name()
  {
    return this.e.attributeValue("name");
  }
  
  public boolean isClass()
  {
    return "class".equals(this.e.attributeValue("type"));
  }
  
  public boolean isRoot()
  {
    return "true".equals(this.e.attributeValue("root"));
  }
  
  public JDefinedClass getClassObject()
  {
    return this.clazz;
  }
  
  public void declareConstructors(ClassItem src, AnnotatorController controller)
  {
    for (int i = 0; i < this.constructors.size(); i++) {
      ((BIConstructor)this.constructors.get(i)).createDeclaration(src, controller);
    }
  }
  
  public BIConversion getConversion()
  {
    String cnv = this.e.attributeValue("convert");
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
  
  public Iterator iterateContents()
  {
    return this.contents.iterator();
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
    return DOM4JLocator.getLocationInfo(this.e);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIElement.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */