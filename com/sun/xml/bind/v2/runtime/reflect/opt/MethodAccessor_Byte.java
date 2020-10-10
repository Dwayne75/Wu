package com.sun.xml.bind.v2.runtime.reflect.opt;

import com.sun.xml.bind.v2.runtime.reflect.Accessor;

public class MethodAccessor_Byte
  extends Accessor
{
  public MethodAccessor_Byte()
  {
    super(Byte.class);
  }
  
  public Object get(Object bean)
  {
    return Byte.valueOf(((Bean)bean).get_byte());
  }
  
  public void set(Object bean, Object value)
  {
    ((Bean)bean).set_byte(value == null ? Const.default_value_byte : ((Byte)value).byteValue());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\reflect\opt\MethodAccessor_Byte.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */