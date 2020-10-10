package org.fourthline.cling.model.state;

import java.lang.reflect.Method;
import org.seamless.util.Reflections;

public class GetterStateVariableAccessor
  extends StateVariableAccessor
{
  private Method getter;
  
  public GetterStateVariableAccessor(Method getter)
  {
    this.getter = getter;
  }
  
  public Method getGetter()
  {
    return this.getter;
  }
  
  public Class<?> getReturnType()
  {
    return getGetter().getReturnType();
  }
  
  public Object read(Object serviceImpl)
    throws Exception
  {
    return Reflections.invoke(getGetter(), serviceImpl, new Object[0]);
  }
  
  public String toString()
  {
    return super.toString() + " Method: " + getGetter();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\state\GetterStateVariableAccessor.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */