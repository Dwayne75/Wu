package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.core.NonElement;
import com.sun.xml.bind.v2.model.core.PropertyInfo;
import com.sun.xml.bind.v2.model.core.TypeRef;
import javax.xml.namespace.QName;

class TypeRefImpl<TypeT, ClassDeclT>
  implements TypeRef<TypeT, ClassDeclT>
{
  private final QName elementName;
  private final TypeT type;
  protected final ElementPropertyInfoImpl<TypeT, ClassDeclT, ?, ?> owner;
  private NonElement<TypeT, ClassDeclT> ref;
  private final boolean isNillable;
  private String defaultValue;
  
  public TypeRefImpl(ElementPropertyInfoImpl<TypeT, ClassDeclT, ?, ?> owner, QName elementName, TypeT type, boolean isNillable, String defaultValue)
  {
    this.owner = owner;
    this.elementName = elementName;
    this.type = type;
    this.isNillable = isNillable;
    this.defaultValue = defaultValue;
    assert (owner != null);
    assert (elementName != null);
    assert (type != null);
  }
  
  public NonElement<TypeT, ClassDeclT> getTarget()
  {
    if (this.ref == null) {
      calcRef();
    }
    return this.ref;
  }
  
  public QName getTagName()
  {
    return this.elementName;
  }
  
  public boolean isNillable()
  {
    return this.isNillable;
  }
  
  public String getDefaultValue()
  {
    return this.defaultValue;
  }
  
  protected void link()
  {
    calcRef();
  }
  
  private void calcRef()
  {
    this.ref = this.owner.parent.builder.getTypeInfo(this.type, this.owner);
    assert (this.ref != null);
  }
  
  public PropertyInfo<TypeT, ClassDeclT> getSource()
  {
    return this.owner;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\TypeRefImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */