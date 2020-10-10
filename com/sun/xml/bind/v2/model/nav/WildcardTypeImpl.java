package com.sun.xml.bind.v2.model.nav;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

final class WildcardTypeImpl
  implements WildcardType
{
  private final Type[] ub;
  private final Type[] lb;
  
  public WildcardTypeImpl(Type[] ub, Type[] lb)
  {
    this.ub = ub;
    this.lb = lb;
  }
  
  public Type[] getUpperBounds()
  {
    return this.ub;
  }
  
  public Type[] getLowerBounds()
  {
    return this.lb;
  }
  
  public int hashCode()
  {
    return Arrays.hashCode(this.lb) ^ Arrays.hashCode(this.ub);
  }
  
  public boolean equals(Object obj)
  {
    if ((obj instanceof WildcardType))
    {
      WildcardType that = (WildcardType)obj;
      return (Arrays.equals(that.getLowerBounds(), this.lb)) && (Arrays.equals(that.getUpperBounds(), this.ub));
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\model\nav\WildcardTypeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */