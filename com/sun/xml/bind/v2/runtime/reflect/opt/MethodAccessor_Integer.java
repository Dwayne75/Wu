package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Integer
  extends Accessor
{
  public MethodAccessor_Integer()
  {
    super(Integer.class);
  }
  
  public Object get(Object bean)
  {
    return Integer.valueOf(((Bean)bean).get_int());
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_int(value == null ? Const.default_value_int : ((Integer)value).intValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Integer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */