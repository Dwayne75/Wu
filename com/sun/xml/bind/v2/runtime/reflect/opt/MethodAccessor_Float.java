package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Float
  extends Accessor
{
  public MethodAccessor_Float()
  {
    super(Float.class);
  }
  
  public Object get(Object bean)
  {
    return Float.valueOf(((Bean)bean).get_float());
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_float(value == null ? Const.default_value_float : ((Float)value).floatValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Float.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */