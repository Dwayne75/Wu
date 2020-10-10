package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Long
  extends Accessor
{
  public MethodAccessor_Long()
  {
    super(Long.class);
  }
  
  public Object get(Object bean)
  {
    return Long.valueOf(((Bean)bean).get_long());
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_long(value == null ? Const.default_value_long : ((Long)value).longValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Long.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */