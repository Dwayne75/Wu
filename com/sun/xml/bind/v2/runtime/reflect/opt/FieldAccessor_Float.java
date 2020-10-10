package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class FieldAccessor_Float
  extends Accessor
{
  public FieldAccessor_Float()
  {
    super(Float.class);
  }
  
  public Object get(Object bean)
  {
    return Float.valueOf(((Bean)bean).f_float);
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).f_float = (value == null ? Const.default_value_float : ((Float)value).floatValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\FieldAccessor_Float.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */