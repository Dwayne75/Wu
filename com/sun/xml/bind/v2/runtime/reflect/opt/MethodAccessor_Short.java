package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Short
  extends Accessor
{
  public MethodAccessor_Short()
  {
    super(Short.class);
  }
  
  public Object get(Object bean)
  {
    return Short.valueOf(((Bean)bean).get_short());
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_short(value == null ? Const.default_value_short : ((Short)value).shortValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Short.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */