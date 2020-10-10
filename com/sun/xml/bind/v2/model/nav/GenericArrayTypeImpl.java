package com.sun.xml.bind.v2.model.nav;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

final class GenericArrayTypeImpl
  implements GenericArrayType
{
  private Type genericComponentType;
  
  GenericArrayTypeImpl(Type ct)
  {
    assert (ct != null);
    this.genericComponentType = ct;
  }
  
  public Type getGenericComponentType()
  {
    return this.genericComponentType;
  }
  
  public String toString()
  {
    Type componentType = getGenericComponentType();
    StringBuilder sb = new StringBuilder();
    if ((componentType instanceof Class)) {
      sb.append(((Class)componentType).getName());
    } else {
      sb.append(componentType.toString());
    }
    sb.append("[]");
    return sb.toString();
  }
  
  public boolean equals(Object o)
  {
    if ((o instanceof GenericArrayType))
    {
      GenericArrayType that = (GenericArrayType)o;
      
      Type thatComponentType = that.getGenericComponentType();
      return this.genericComponentType.equals(thatComponentType);
    }
    return false;
  }
  
  public int hashCode()
  {
    return this.genericComponentType.hashCode();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\nav\GenericArrayTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */