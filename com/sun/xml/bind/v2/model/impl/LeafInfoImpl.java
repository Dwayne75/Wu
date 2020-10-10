package com.sun.xml.bind.v2.model.impl;

import com.sun.xml.bind.v2.model.annotation.Locatable;
import com.sun.xml.bind.v2.model.core.LeafInfo;
import com.sun.xml.bind.v2.runtime.Location;
import javax.xml.namespace.QName;

abstract class LeafInfoImpl<TypeT, ClassDeclT>
  implements LeafInfo<TypeT, ClassDeclT>, Location
{
  private final TypeT type;
  private final QName typeName;
  
  protected LeafInfoImpl(TypeT type, QName typeName)
  {
    assert (type != null);
    
    this.type = type;
    this.typeName = typeName;
  }
  
  public TypeT getType()
  {
    return (TypeT)this.type;
  }
  
  /**
   * @deprecated
   */
  public final boolean canBeReferencedByIDREF()
  {
    return false;
  }
  
  public QName getTypeName()
  {
    return this.typeName;
  }
  
  public Locatable getUpstream()
  {
    return null;
  }
  
  public Location getLocation()
  {
    return this;
  }
  
  public boolean isSimpleType()
  {
    return true;
  }
  
  public String toString()
  {
    return this.type.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\impl\LeafInfoImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */