package com.sun.tools.xjc.model;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.EnumOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.EnumLeafInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.runtime.Location;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XmlString;
import java.util.Collection;
import javax.activation.MimeType;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class CEnumLeafInfo
  implements EnumLeafInfo<NType, NClass>, NClass, CNonElement
{
  public final Model model;
  public final CClassInfoParent parent;
  public final String shortName;
  private final QName typeName;
  private final XSComponent source;
  public final CNonElement base;
  public final Collection<CEnumConstant> members;
  private final CCustomizations customizations;
  private final Locator sourceLocator;
  public String javadoc;
  
  public CEnumLeafInfo(Model model, QName typeName, CClassInfoParent container, String shortName, CNonElement base, Collection<CEnumConstant> _members, XSComponent source, CCustomizations customizations, Locator _sourceLocator)
  {
    this.model = model;
    this.parent = container;
    this.shortName = model.allocator.assignClassName(this.parent, shortName);
    this.base = base;
    this.members = _members;
    this.source = source;
    if (customizations == null) {
      customizations = CCustomizations.EMPTY;
    }
    this.customizations = customizations;
    this.sourceLocator = _sourceLocator;
    this.typeName = typeName;
    for (CEnumConstant mem : this.members) {
      mem.setParent(this);
    }
    model.add(this);
  }
  
  public Locator getLocator()
  {
    return this.sourceLocator;
  }
  
  public QName getTypeName()
  {
    return this.typeName;
  }
  
  public NType getType()
  {
    return this;
  }
  
  /**
   * @deprecated
   */
  public boolean canBeReferencedByIDREF()
  {
    return false;
  }
  
  public boolean isElement()
  {
    return false;
  }
  
  public QName getElementName()
  {
    return null;
  }
  
  public Element<NType, NClass> asElement()
  {
    return null;
  }
  
  public NClass getClazz()
  {
    return this;
  }
  
  public XSComponent getSchemaComponent()
  {
    return this.source;
  }
  
  public JClass toType(Outline o, Aspect aspect)
  {
    return o.getEnum(this).clazz;
  }
  
  public boolean isAbstract()
  {
    return false;
  }
  
  public boolean isBoxedType()
  {
    return false;
  }
  
  public String fullName()
  {
    return this.parent.fullName() + '.' + this.shortName;
  }
  
  public boolean isPrimitive()
  {
    return false;
  }
  
  public boolean isSimpleType()
  {
    return true;
  }
  
  public boolean needsValueField()
  {
    for (CEnumConstant cec : this.members) {
      if (!cec.getName().equals(cec.getLexicalValue())) {
        return true;
      }
    }
    return false;
  }
  
  public JExpression createConstant(Outline outline, XmlString literal)
  {
    JClass type = toType(outline, Aspect.EXPOSED);
    for (CEnumConstant mem : this.members) {
      if (mem.getLexicalValue().equals(literal.value)) {
        return type.staticRef(mem.getName());
      }
    }
    return null;
  }
  
  @Deprecated
  public boolean isCollection()
  {
    return false;
  }
  
  @Deprecated
  public CAdapter getAdapterUse()
  {
    return null;
  }
  
  @Deprecated
  public CNonElement getInfo()
  {
    return this;
  }
  
  public ID idUse()
  {
    return ID.NONE;
  }
  
  public MimeType getExpectedMimeType()
  {
    return null;
  }
  
  public Collection<CEnumConstant> getConstants()
  {
    return this.members;
  }
  
  public NonElement<NType, NClass> getBaseType()
  {
    return this.base;
  }
  
  public CCustomizations getCustomizations()
  {
    return this.customizations;
  }
  
  public Locatable getUpstream()
  {
    throw new UnsupportedOperationException();
  }
  
  public Location getLocation()
  {
    throw new UnsupportedOperationException();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CEnumLeafInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */