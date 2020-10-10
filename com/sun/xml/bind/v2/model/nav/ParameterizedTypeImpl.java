package com.sun.xml.bind.v2.model.nav;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

class ParameterizedTypeImpl
  implements ParameterizedType
{
  private Type[] actualTypeArguments;
  private Class<?> rawType;
  private Type ownerType;
  
  ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType)
  {
    this.actualTypeArguments = actualTypeArguments;
    this.rawType = rawType;
    if (ownerType != null) {
      this.ownerType = ownerType;
    } else {
      this.ownerType = rawType.getDeclaringClass();
    }
    validateConstructorArguments();
  }
  
  private void validateConstructorArguments()
  {
    TypeVariable[] formals = this.rawType.getTypeParameters();
    if (formals.length != this.actualTypeArguments.length) {
      throw new MalformedParameterizedTypeException();
    }
    for (int i = 0; i < this.actualTypeArguments.length; i++) {}
  }
  
  public Type[] getActualTypeArguments()
  {
    return (Type[])this.actualTypeArguments.clone();
  }
  
  public Class<?> getRawType()
  {
    return this.rawType;
  }
  
  public Type getOwnerType()
  {
    return this.ownerType;
  }
  
  public boolean equals(Object o)
  {
    if ((o instanceof ParameterizedType))
    {
      ParameterizedType that = (ParameterizedType)o;
      if (this == that) {
        return true;
      }
      Type thatOwner = that.getOwnerType();
      Type thatRawType = that.getRawType();
      
      return (this.ownerType == null ? thatOwner == null : this.ownerType.equals(thatOwner)) && (this.rawType == null ? thatRawType == null : this.rawType.equals(thatRawType)) && (Arrays.equals(this.actualTypeArguments, that.getActualTypeArguments()));
    }
    return false;
  }
  
  public int hashCode()
  {
    return Arrays.hashCode(this.actualTypeArguments) ^ (this.ownerType == null ? 0 : this.ownerType.hashCode()) ^ (this.rawType == null ? 0 : this.rawType.hashCode());
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    if (this.ownerType != null)
    {
      if ((this.ownerType instanceof Class)) {
        sb.append(((Class)this.ownerType).getName());
      } else {
        sb.append(this.ownerType.toString());
      }
      sb.append(".");
      if ((this.ownerType instanceof ParameterizedTypeImpl)) {
        sb.append(this.rawType.getName().replace(((ParameterizedTypeImpl)this.ownerType).rawType.getName() + "$", ""));
      } else {
        sb.append(this.rawType.getName());
      }
    }
    else
    {
      sb.append(this.rawType.getName());
    }
    if ((this.actualTypeArguments != null) && (this.actualTypeArguments.length > 0))
    {
      sb.append("<");
      boolean first = true;
      for (Type t : this.actualTypeArguments)
      {
        if (!first) {
          sb.append(", ");
        }
        if ((t instanceof Class)) {
          sb.append(((Class)t).getName());
        } else {
          sb.append(t.toString());
        }
        first = false;
      }
      sb.append(">");
    }
    return sb.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\nav\ParameterizedTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */