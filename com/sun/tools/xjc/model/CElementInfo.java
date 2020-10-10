package com.sun.tools.xjc.model;

import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.istack.Nullable;
import com.sun.tools.xjc.model.nav.NClass;
import com.sun.tools.xjc.model.nav.NType;
import com.sun.tools.xjc.model.nav.NavigatorImpl;
import com.sun.tools.xjc.outline.Aspect;
import com.sun.tools.xjc.outline.ElementOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.reader.Ring;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIFactoryMethod;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BIInlineBinaryData;
import com.sun.tools.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XmlString;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class CElementInfo
  extends AbstractCElement
  implements ElementInfo<NType, NClass>, NType, CClassInfoParent
{
  private final QName tagName;
  private NType type;
  private String className;
  public final CClassInfoParent parent;
  private CElementInfo substitutionHead;
  private Set<CElementInfo> substitutionMembers;
  private final Model model;
  private CElementPropertyInfo property;
  @Nullable
  private String squeezedName;
  
  public CElementInfo(Model model, QName tagName, CClassInfoParent parent, TypeUse contentType, XmlString defaultValue, XSElementDecl source, CCustomizations customizations, Locator location)
  {
    super(model, source, location, customizations);
    this.tagName = tagName;
    this.model = model;
    this.parent = parent;
    if (contentType != null) {
      initContentType(contentType, source, defaultValue);
    }
    model.add(this);
  }
  
  public CElementInfo(Model model, QName tagName, CClassInfoParent parent, String className, CCustomizations customizations, Locator location)
  {
    this(model, tagName, parent, null, null, null, customizations, location);
    this.className = className;
  }
  
  public void initContentType(TypeUse contentType, @Nullable XSElementDecl source, XmlString defaultValue)
  {
    assert (this.property == null);
    
    this.property = new CElementPropertyInfo("Value", contentType.isCollection() ? CElementPropertyInfo.CollectionMode.REPEATED_VALUE : CElementPropertyInfo.CollectionMode.NOT_REPEATED, contentType.idUse(), contentType.getExpectedMimeType(), source, null, getLocator(), true);
    
    this.property.setAdapter(contentType.getAdapterUse());
    BIInlineBinaryData.handle(source, this.property);
    this.property.getTypes().add(new CTypeRef(contentType.getInfo(), this.tagName, CTypeRef.getSimpleTypeName(source), true, defaultValue));
    this.type = NavigatorImpl.createParameterizedType(NavigatorImpl.theInstance.ref(JAXBElement.class), new NType[] { getContentInMemoryType() });
    
    BIFactoryMethod factoryMethod = (BIFactoryMethod)((BGMBuilder)Ring.get(BGMBuilder.class)).getBindInfo(source).get(BIFactoryMethod.class);
    if (factoryMethod != null)
    {
      factoryMethod.markAsAcknowledged();
      this.squeezedName = factoryMethod.name;
    }
  }
  
  public final String getDefaultValue()
  {
    return ((CTypeRef)getProperty().getTypes().get(0)).getDefaultValue();
  }
  
  public final JPackage _package()
  {
    return this.parent.getOwnerPackage();
  }
  
  public CNonElement getContentType()
  {
    return (CNonElement)getProperty().ref().get(0);
  }
  
  public NType getContentInMemoryType()
  {
    if (getProperty().getAdapter() == null)
    {
      NType itemType = (NType)getContentType().getType();
      if (!this.property.isCollection()) {
        return itemType;
      }
      return NavigatorImpl.createParameterizedType(List.class, new NType[] { itemType });
    }
    return (NType)getProperty().getAdapter().customType;
  }
  
  public CElementPropertyInfo getProperty()
  {
    return this.property;
  }
  
  public CClassInfo getScope()
  {
    if ((this.parent instanceof CClassInfo)) {
      return (CClassInfo)this.parent;
    }
    return null;
  }
  
  /**
   * @deprecated
   */
  public NType getType()
  {
    return this;
  }
  
  public QName getElementName()
  {
    return this.tagName;
  }
  
  public JType toType(Outline o, Aspect aspect)
  {
    if (this.className == null) {
      return this.type.toType(o, aspect);
    }
    return o.getElement(this).implClass;
  }
  
  @XmlElement
  public String getSqueezedName()
  {
    if (this.squeezedName != null) {
      return this.squeezedName;
    }
    StringBuilder b = new StringBuilder();
    CClassInfo s = getScope();
    if (s != null) {
      b.append(s.getSqueezedName());
    }
    if (this.className != null) {
      b.append(this.className);
    } else {
      b.append(this.model.getNameConverter().toClassName(this.tagName.getLocalPart()));
    }
    return b.toString();
  }
  
  public CElementInfo getSubstitutionHead()
  {
    return this.substitutionHead;
  }
  
  public Collection<CElementInfo> getSubstitutionMembers()
  {
    if (this.substitutionMembers == null) {
      return Collections.emptyList();
    }
    return this.substitutionMembers;
  }
  
  public void setSubstitutionHead(CElementInfo substitutionHead)
  {
    assert (this.substitutionHead == null);
    assert (substitutionHead != null);
    this.substitutionHead = substitutionHead;
    if (substitutionHead.substitutionMembers == null) {
      substitutionHead.substitutionMembers = new HashSet();
    }
    substitutionHead.substitutionMembers.add(this);
  }
  
  public boolean isBoxedType()
  {
    return false;
  }
  
  public String fullName()
  {
    if (this.className == null) {
      return this.type.fullName();
    }
    String r = this.parent.fullName();
    if (r.length() == 0) {
      return this.className;
    }
    return r + '.' + this.className;
  }
  
  public <T> T accept(CClassInfoParent.Visitor<T> visitor)
  {
    return (T)visitor.onElement(this);
  }
  
  public JPackage getOwnerPackage()
  {
    return this.parent.getOwnerPackage();
  }
  
  public String shortName()
  {
    return this.className;
  }
  
  public boolean hasClass()
  {
    return this.className != null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\model\CElementInfo.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */