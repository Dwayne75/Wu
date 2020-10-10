package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.ClassInfo;
import com.sun.xml.bind.v2.model.core.Element;
import com.sun.xml.bind.v2.model.core.ElementInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.ReferencePropertyInfo;
import com.sun.xml.bind.v2.model.core.WildcardMode;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRef.DEFAULT;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

class ReferencePropertyInfoImpl<T, C, F, M>
  extends ERPropertyInfoImpl<T, C, F, M>
  implements ReferencePropertyInfo<T, C>
{
  private Set<Element<T, C>> types;
  private final boolean isMixed;
  private final WildcardMode wildcard;
  private final C domHandler;
  
  public ReferencePropertyInfoImpl(ClassInfoImpl<T, C, F, M> classInfo, PropertySeed<T, C, F, M> seed)
  {
    super(classInfo, seed);
    
    this.isMixed = (seed.readAnnotation(XmlMixed.class) != null);
    
    XmlAnyElement xae = (XmlAnyElement)seed.readAnnotation(XmlAnyElement.class);
    if (xae == null)
    {
      this.wildcard = null;
      this.domHandler = null;
    }
    else
    {
      this.wildcard = (xae.lax() ? WildcardMode.LAX : WildcardMode.SKIP);
      this.domHandler = nav().asDecl(reader().getClassValue(xae, "value"));
    }
  }
  
  public Set<? extends Element<T, C>> ref()
  {
    return getElements();
  }
  
  public PropertyKind kind()
  {
    return PropertyKind.REFERENCE;
  }
  
  public Set<? extends Element<T, C>> getElements()
  {
    if (this.types == null) {
      calcTypes(false);
    }
    assert (this.types != null);
    return this.types;
  }
  
  private void calcTypes(boolean last)
  {
    this.types = new LinkedHashSet();
    XmlElementRefs refs = (XmlElementRefs)this.seed.readAnnotation(XmlElementRefs.class);
    XmlElementRef ref = (XmlElementRef)this.seed.readAnnotation(XmlElementRef.class);
    if ((refs != null) && (ref != null)) {
      this.parent.builder.reportError(new IllegalAnnotationException(Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(new Object[] { nav().getClassName(this.parent.getClazz()) + '#' + this.seed.getName(), ref.annotationType().getName(), refs.annotationType().getName() }), ref, refs));
    }
    XmlElementRef[] ann;
    XmlElementRef[] ann;
    if (refs != null)
    {
      ann = refs.value();
    }
    else
    {
      XmlElementRef[] ann;
      if (ref != null) {
        ann = new XmlElementRef[] { ref };
      } else {
        ann = null;
      }
    }
    if (ann != null)
    {
      Navigator<T, C, F, M> nav = nav();
      AnnotationReader<T, C, F, M> reader = reader();
      
      T defaultType = nav.ref(XmlElementRef.DEFAULT.class);
      C je = nav.asDecl(JAXBElement.class);
      for (XmlElementRef r : ann)
      {
        T type = reader.getClassValue(r, "type");
        if (type.equals(defaultType)) {
          type = nav.erasure(getIndividualType());
        }
        boolean yield;
        boolean yield;
        if (nav.getBaseClass(type, je) != null) {
          yield = addGenericElement(r);
        } else {
          yield = addAllSubtypes(type);
        }
        if ((last) && (!yield))
        {
          if (type.equals(nav.ref(JAXBElement.class))) {
            this.parent.builder.reportError(new IllegalAnnotationException(Messages.NO_XML_ELEMENT_DECL.format(new Object[] { getEffectiveNamespaceFor(r), r.name() }), this));
          } else {
            this.parent.builder.reportError(new IllegalAnnotationException(Messages.INVALID_XML_ELEMENT_REF.format(new Object[0]), this));
          }
          return;
        }
      }
    }
    this.types = Collections.unmodifiableSet(this.types);
  }
  
  private boolean addGenericElement(XmlElementRef r)
  {
    String nsUri = getEffectiveNamespaceFor(r);
    
    return addGenericElement(this.parent.owner.getElementInfo(this.parent.getClazz(), new QName(nsUri, r.name())));
  }
  
  private String getEffectiveNamespaceFor(XmlElementRef r)
  {
    String nsUri = r.namespace();
    
    XmlSchema xs = (XmlSchema)reader().getPackageAnnotation(XmlSchema.class, this.parent.getClazz(), this);
    if ((xs != null) && (xs.attributeFormDefault() == XmlNsForm.QUALIFIED)) {
      if (nsUri.length() == 0) {
        nsUri = this.parent.builder.defaultNsUri;
      }
    }
    return nsUri;
  }
  
  private boolean addGenericElement(ElementInfo<T, C> ei)
  {
    if (ei == null) {
      return false;
    }
    this.types.add(ei);
    for (ElementInfo<T, C> subst : ei.getSubstitutionMembers()) {
      addGenericElement(subst);
    }
    return true;
  }
  
  private boolean addAllSubtypes(T type)
  {
    Navigator<T, C, F, M> nav = nav();
    
    NonElement<T, C> t = this.parent.builder.getClassInfo(nav.asDecl(type), this);
    if (!(t instanceof ClassInfo)) {
      return false;
    }
    boolean result = false;
    
    ClassInfo<T, C> c = (ClassInfo)t;
    if (c.isElement())
    {
      this.types.add(c.asElement());
      result = true;
    }
    for (ClassInfo<T, C> ci : this.parent.owner.beans().values()) {
      if ((ci.isElement()) && (nav.isSubClassOf(ci.getType(), type)))
      {
        this.types.add(ci.asElement());
        result = true;
      }
    }
    for (ElementInfo<T, C> ei : this.parent.owner.getElementMappings(null).values()) {
      if (nav.isSubClassOf(ei.getType(), type))
      {
        this.types.add(ei);
        result = true;
      }
    }
    return result;
  }
  
  protected void link()
  {
    super.link();
    
    calcTypes(true);
  }
  
  public final boolean isMixed()
  {
    return this.isMixed;
  }
  
  public final WildcardMode getWildcard()
  {
    return this.wildcard;
  }
  
  public final C getDOMHandler()
  {
    return (C)this.domHandler;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\ReferencePropertyInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */