package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Ref
  extends Accessor
{
  public MethodAccessor_Ref()
  {
    super(Ref.class);
  }
  
  public Object get(Object bean)
  {
    return ((Bean)bean).get_ref();
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_ref((Ref)value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Ref.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */