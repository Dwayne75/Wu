package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Double
  extends Accessor
{
  public MethodAccessor_Double()
  {
    super(Double.class);
  }
  
  public Object get(Object bean)
  {
    return Double.valueOf(((Bean)bean).get_double());
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_double(value == null ? Const.default_value_double : ((Double)value).doubleValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Double.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */