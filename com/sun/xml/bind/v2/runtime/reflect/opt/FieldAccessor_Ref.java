package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class FieldAccessor_Ref
  extends Accessor
{
  public FieldAccessor_Ref()
  {
    super(Ref.class);
  }
  
  public Object get(Object bean)
  {
    return ((Bean)bean).f_ref;
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).f_ref = ((Ref)value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\FieldAccessor_Ref.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */