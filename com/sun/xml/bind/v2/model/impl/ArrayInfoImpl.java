package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.TODO;
import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.ArrayInfo;
import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.nav.Navigator;
import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import com.sun.xml.bind.v2.runtime.Location;
import javax.xml.namespace.QName;

public class ArrayInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
  extends TypeInfoImpl<TypeT, ClassDeclT, FieldT, MethodT>
  implements ArrayInfo<TypeT, ClassDeclT>, Location
{
  private final NonElement<TypeT, ClassDeclT> itemType;
  private final QName typeName;
  private final TypeT arrayType;
  
  public ArrayInfoImpl(ModelBuilder<TypeT, ClassDeclT, FieldT, MethodT> builder, Locatable upstream, TypeT arrayType)
  {
    super(builder, upstream);
    this.arrayType = arrayType;
    TypeT componentType = nav().getComponentType(arrayType);
    this.itemType = builder.getTypeInfo(componentType, this);
    
    QName n = this.itemType.getTypeName();
    if (n == null)
    {
      builder.reportError(new IllegalAnnotationException(Messages.ANONYMOUS_ARRAY_ITEM.format(new Object[] { nav().getTypeName(componentType) }), this));
      
      n = new QName("#dummy");
    }
    this.typeName = calcArrayTypeName(n);
  }
  
  public static QName calcArrayTypeName(QName n)
  {
    String uri;
    String uri;
    if (n.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema"))
    {
      TODO.checkSpec("this URI");
      uri = "http://jaxb.dev.java.net/array";
    }
    else
    {
      uri = n.getNamespaceURI();
    }
    return new QName(uri, n.getLocalPart() + "Array");
  }
  
  public NonElement<TypeT, ClassDeclT> getItemType()
  {
    return this.itemType;
  }
  
  public QName getTypeName()
  {
    return this.typeName;
  }
  
  public boolean isSimpleType()
  {
    return false;
  }
  
  public TypeT getType()
  {
    return (TypeT)this.arrayType;
  }
  
  /**
   * @deprecated
   */
  public final boolean canBeReferencedByIDREF()
  {
    return false;
  }
  
  public Location getLocation()
  {
    return this;
  }
  
  public String toString()
  {
    return nav().getTypeName(this.arrayType);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\ArrayInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */