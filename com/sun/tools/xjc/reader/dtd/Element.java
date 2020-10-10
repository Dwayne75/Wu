package com.sun.tools.xjc.reader.dtd;

import com.sun.tools.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo;
import com.sun.tools.xjc.model.CElementPropertyInfo.CollectionMode;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.model.CReferencePropertyInfo;
import com.sun.tools.xjc.model.CTypeRef;
import com.sun.tools.xjc.model.CValuePropertyInfo;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.model.TypeUse;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIConversion;
import com.sun.tools.xjc.reader.dtd.bindinfo.BIElement;
import com.sun.tools.xjc.reader.dtd.bindinfo.BindInfo;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

final class Element
  extends Term
  implements Comparable<Element>
{
  final String name;
  private final TDTDReader owner;
  private short contentModelType;
  private Term contentModel;
  boolean isReferenced;
  private CClassInfo classInfo;
  private boolean classInfoComputed;
  final List<CPropertyInfo> attributes = new ArrayList();
  private final List<Block> normalizedBlocks = new ArrayList();
  private boolean mustBeClass;
  private Locator locator;
  
  public Element(TDTDReader owner, String name)
  {
    this.owner = owner;
    this.name = name;
  }
  
  void normalize(List<Block> r, boolean optional)
  {
    Block o = new Block(optional, false);
    o.elements.add(this);
    r.add(o);
  }
  
  void addAllElements(Block b)
  {
    b.elements.add(this);
  }
  
  boolean isOptional()
  {
    return false;
  }
  
  boolean isRepeated()
  {
    return false;
  }
  
  void define(short contentModelType, Term contentModel, Locator locator)
  {
    assert (this.contentModel == null);
    this.contentModelType = contentModelType;
    this.contentModel = contentModel;
    this.locator = locator;
    contentModel.normalize(this.normalizedBlocks, false);
    for (Block b : this.normalizedBlocks) {
      if ((b.isRepeated) || (b.elements.size() > 1)) {
        for (Element e : b.elements) {
          this.owner.getOrCreateElement(e.name).mustBeClass = true;
        }
      }
    }
  }
  
  private TypeUse getConversion()
  {
    assert (this.contentModel == Term.EMPTY);
    
    BIElement e = this.owner.bindInfo.element(this.name);
    if (e != null)
    {
      BIConversion conv = e.getConversion();
      if (conv != null) {
        return conv.getTransducer();
      }
    }
    return CBuiltinLeafInfo.STRING;
  }
  
  CClassInfo getClassInfo()
  {
    if (!this.classInfoComputed)
    {
      this.classInfoComputed = true;
      this.classInfo = calcClass();
    }
    return this.classInfo;
  }
  
  private CClassInfo calcClass()
  {
    BIElement e = this.owner.bindInfo.element(this.name);
    if (e == null)
    {
      if ((this.contentModelType != 2) || (!this.attributes.isEmpty()) || (this.mustBeClass)) {
        return createDefaultClass();
      }
      if (this.contentModel != Term.EMPTY) {
        throw new UnsupportedOperationException("mixed content model not supported");
      }
      if (this.isReferenced) {
        return null;
      }
      return createDefaultClass();
    }
    return e.clazz;
  }
  
  private CClassInfo createDefaultClass()
  {
    String className = this.owner.model.getNameConverter().toClassName(this.name);
    QName tagName = new QName("", this.name);
    
    return new CClassInfo(this.owner.model, this.owner.getTargetPackage(), className, this.locator, null, tagName, null, null);
  }
  
  void bind()
  {
    CClassInfo ci = getClassInfo();
    assert ((ci != null) || (this.attributes.isEmpty()));
    for (CPropertyInfo p : this.attributes) {
      ci.addProperty(p);
    }
    switch (this.contentModelType)
    {
    case 1: 
      CReferencePropertyInfo rp = new CReferencePropertyInfo("Content", true, true, null, null, this.locator);
      rp.setWildcard(WildcardMode.SKIP);
      ci.addProperty(rp); return;
    case 3: 
      break;
    case 2: 
      if (this.contentModel != Term.EMPTY) {
        throw new UnsupportedOperationException("mixed content model unsupported yet");
      }
      if (ci != null)
      {
        CValuePropertyInfo p = new CValuePropertyInfo("value", null, null, this.locator, getConversion(), null);
        ci.addProperty(p);
      }
      return;
    case 0: 
      assert (ci != null);
      return;
    }
    List<Block> n = new ArrayList();
    this.contentModel.normalize(n, false);
    
    Set<String> names = new HashSet();
    boolean collision = false;
    for (Block b : n) {
      for (Element e : b.elements) {
        if (!names.add(e.name))
        {
          collision = true;
          break label330;
        }
      }
    }
    label330:
    if (collision)
    {
      Block all = new Block(true, true);
      for (Block b : n) {
        all.elements.addAll(b.elements);
      }
      n.clear();
      n.add(all);
    }
    for (Block b : n)
    {
      CElementPropertyInfo p;
      CElementPropertyInfo p;
      if ((b.isRepeated) || (b.elements.size() > 1))
      {
        StringBuilder name = new StringBuilder();
        for (Element e : b.elements)
        {
          if (name.length() > 0) {
            name.append("Or");
          }
          name.append(this.owner.model.getNameConverter().toPropertyName(e.name));
        }
        p = new CElementPropertyInfo(name.toString(), CElementPropertyInfo.CollectionMode.REPEATED_ELEMENT, ID.NONE, null, null, null, this.locator, !b.isOptional);
        for (Element e : b.elements)
        {
          CClassInfo child = this.owner.getOrCreateElement(e.name).getClassInfo();
          assert (child != null);
          p.getTypes().add(new CTypeRef(child, new QName("", e.name), null, false, null));
        }
      }
      else
      {
        String name = ((Element)b.elements.iterator().next()).name;
        String propName = this.owner.model.getNameConverter().toPropertyName(name);
        
        Element ref = this.owner.getOrCreateElement(name);
        TypeUse refType;
        TypeUse refType;
        if (ref.getClassInfo() != null) {
          refType = ref.getClassInfo();
        } else {
          refType = ref.getConversion().getInfo();
        }
        p = new CElementPropertyInfo(propName, refType.isCollection() ? CElementPropertyInfo.CollectionMode.REPEATED_VALUE : CElementPropertyInfo.CollectionMode.NOT_REPEATED, ID.NONE, null, null, null, this.locator, !b.isOptional);
        
        p.getTypes().add(new CTypeRef(refType.getInfo(), new QName("", name), null, false, null));
      }
      ci.addProperty(p);
    }
  }
  
  public int compareTo(Element that)
  {
    return this.name.compareTo(that.name);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\Element.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */