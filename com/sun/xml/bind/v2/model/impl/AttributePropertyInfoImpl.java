package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.api.impl.NameConverter;
import com.sun.xml.bind.v2.model.annotation.AnnotationReader;
import com.sun.xml.bind.v2.model.core.AttributePropertyInfo;
import com.sun.xml.bind.v2.model.core.PropertyKind;
import com.sun.xml.bind.v2.model.nav.Navigator;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.namespace.QName;

class AttributePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
  extends SingleTypePropertyInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
  implements AttributePropertyInfo<TypeT, ClassDeclT>
{
  private final QName xmlName;
  private final boolean isRequired;
  
  AttributePropertyInfoImpl(ClassInfoImpl<TypeT, ClassDeclT, FieldT, MethodT> parent, PropertySeed<TypeT, ClassDeclT, FieldT, MethodT> seed)
  {
    super(parent, seed);
    XmlAttribute att = (XmlAttribute)seed.readAnnotation(XmlAttribute.class);
    assert (att != null);
    if (att.required()) {
      this.isRequired = true;
    } else {
      this.isRequired = nav().isPrimitive(getIndividualType());
    }
    this.xmlName = calcXmlName(att);
  }
  
  private QName calcXmlName(XmlAttribute att)
  {
    String uri = att.namespace();
    String local = att.name();
    if (local.equals("##default")) {
      local = NameConverter.standard.toVariableName(getName());
    }
    if (uri.equals("##default"))
    {
      XmlSchema xs = (XmlSchema)reader().getPackageAnnotation(XmlSchema.class, this.parent.getClazz(), this);
      if (xs != null) {
        switch (xs.attributeFormDefault())
        {
        case QUALIFIED: 
          uri = this.parent.getTypeName().getNamespaceURI();
          if (uri.length() == 0) {
            uri = this.parent.builder.defaultNsUri;
          }
          break;
        case UNQUALIFIED: 
        case UNSET: 
          uri = "";
        }
      } else {
        uri = "";
      }
    }
    return new QName(uri.intern(), local.intern());
  }
  
  public boolean isRequired()
  {
    return this.isRequired;
  }
  
  public final QName getXmlName()
  {
    return this.xmlName;
  }
  
  public final PropertyKind kind()
  {
    return PropertyKind.ATTRIBUTE;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\AttributePropertyInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */