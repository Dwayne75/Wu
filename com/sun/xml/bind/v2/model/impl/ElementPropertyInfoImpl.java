package com.sun.xml.bind.v2.model.impl;

import com.sun.istack.FinalArrayList;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.ElementPropertyInfo;
import com.sun.xml.bind.v2.model.core.ID;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.core.TypeInfo;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElement.DEFAULT;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlList;
import javax.xml.namespace.QName;

class ElementPropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
  extends ERPropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
  implements ElementPropertyInfo<TypeT, ClassDeclT>
{
  private List<TypeRefImpl<TypeT, ClassDeclT>> types;
  private final List<TypeInfo<TypeT, ClassDeclT>> ref = new AbstractList()
  {
    public TypeInfo<TypeT, ClassDeclT> get(int index)
    {
      return ((TypeRefImpl)ElementPropertyInfoImpl.this.getTypes().get(index)).getTarget();
    }
    
    public int size()
    {
      return ElementPropertyInfoImpl.this.getTypes().size();
    }
  };
  private Boolean isRequired;
  private final boolean isValueList;
  
  ElementPropertyInfoImpl(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent, PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> propertySeed)
  {
    super(parent, propertySeed);
    
    this.isValueList = this.seed.hasAnnotation(XmlList.class);
  }
  
  public List<? extends TypeRefImpl<TypeT, ClassDeclT>> getTypes()
  {
    if (this.types == null)
    {
      this.types = new FinalArrayList();
      XmlElement[] ann = null;
      
      XmlElement xe = (XmlElement)this.seed.readAnnotation(XmlElement.class);
      XmlElements xes = (XmlElements)this.seed.readAnnotation(XmlElements.class);
      if ((xe != null) && (xes != null)) {
        this.parent.builder.reportError(new IllegalAnnotationException(Messages.MUTUALLY_EXCLUSIVE_ANNOTATIONS.format(new Object[] { nav().getClassName(this.parent.getClazz()) + '#' + this.seed.getName(), xe.annotationType().getName(), xes.annotationType().getName() }), xe, xes));
      }
      this.isRequired = Boolean.valueOf(true);
      if (xe != null) {
        ann = new XmlElement[] { xe };
      } else if (xes != null) {
        ann = xes.value();
      }
      if (ann == null)
      {
        TypeT t = getIndividualType();
        if ((!nav().isPrimitive(t)) || (isCollection())) {
          this.isRequired = Boolean.valueOf(false);
        }
        this.types.add(createTypeRef(calcXmlName((XmlElement)null), t, isCollection(), null));
      }
      else
      {
        for (XmlElement item : ann)
        {
          QName name = calcXmlName(item);
          TypeT type = reader().getClassValue(item, "type");
          if (type.equals(nav().ref(XmlElement.DEFAULT.class))) {
            type = getIndividualType();
          }
          if (((!nav().isPrimitive(type)) || (isCollection())) && (!item.required())) {
            this.isRequired = Boolean.valueOf(false);
          }
          this.types.add(createTypeRef(name, type, item.nillable(), getDefaultValue(item.defaultValue())));
        }
      }
      this.types = Collections.unmodifiableList(this.types);
      assert (!this.types.contains(null));
    }
    return this.types;
  }
  
  private String getDefaultValue(String value)
  {
    if (value.equals("\000")) {
      return null;
    }
    return value;
  }
  
  protected TypeRefImpl<TypeT, ClassDeclT> createTypeRef(QName name, TypeT type, boolean isNillable, String defaultValue)
  {
    return new TypeRefImpl(this, name, type, isNillable, defaultValue);
  }
  
  public boolean isValueList()
  {
    return this.isValueList;
  }
  
  public boolean isRequired()
  {
    if (this.isRequired == null) {
      getTypes();
    }
    return this.isRequired.booleanValue();
  }
  
  public List<? extends TypeInfo<TypeT, ClassDeclT>> ref()
  {
    return this.ref;
  }
  
  public final PropertyKind kind()
  {
    return PropertyKind.ELEMENT;
  }
  
  protected void link()
  {
    super.link();
    for (TypeRefImpl<TypeT, ClassDeclT> ref : getTypes()) {
      ref.link();
    }
    if (isValueList())
    {
      if (id() != ID.IDREF) {
        for (TypeRefImpl<TypeT, ClassDeclT> ref : this.types) {
          if (!ref.getTarget().isSimpleType())
          {
            this.parent.builder.reportError(new IllegalAnnotationException(Messages.XMLLIST_NEEDS_SIMPLETYPE.format(new Object[] { nav().getTypeName(ref.getTarget().getType()) }), this));
            
            break;
          }
        }
      }
      if (!isCollection()) {
        this.parent.builder.reportError(new IllegalAnnotationException(Messages.XMLLIST_ON_SINGLE_PROPERTY.format(new Object[0]), this));
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\ElementPropertyInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */