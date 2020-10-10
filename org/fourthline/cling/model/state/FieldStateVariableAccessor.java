package org.fourthline.cling.model.state;

import java.lang.reflect.Field;
import org.seamless.util.Reflections;

public class FieldStateVariableAccessor
  extends StateVariableAccessor
{
  protected Field field;
  
  public FieldStateVariableAccessor(Field field)
  {
    this.field = field;
  }
  
  public Field getField()
  {
    return this.field;
  }
  
  public Class<?> getReturnType()
  {
    return getField().getType();
  }
  
  public Object read(Object serviceImpl)
    throws Exception
  {
    return Reflections.get(this.field, serviceImpl);
  }
  
  public String toString()
  {
    return super.toString() + " Field: " + getField();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\state\FieldStateVariableAccessor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */