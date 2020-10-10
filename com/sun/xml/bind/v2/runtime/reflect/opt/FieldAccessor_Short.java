package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class FieldAccessor_Short
  extends Accessor
{
  public FieldAccessor_Short()
  {
    super(Short.class);
  }
  
  public Object get(Object bean)
  {
    return Short.valueOf(((Bean)bean).f_short);
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).f_short = (value == null ? Const.default_value_short : ((Short)value).shortValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\FieldAccessor_Short.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */