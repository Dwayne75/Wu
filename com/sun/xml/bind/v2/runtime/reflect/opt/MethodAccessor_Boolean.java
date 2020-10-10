package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Boolean
  extends Accessor
{
  public MethodAccessor_Boolean()
  {
    super(Boolean.class);
  }
  
  public Object get(Object bean)
  {
    return Boolean.valueOf(((Bean)bean).get_boolean());
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_boolean(value == null ? Const.default_value_boolean : ((Boolean)value).booleanValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Boolean.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */