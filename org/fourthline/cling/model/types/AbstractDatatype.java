package org.fourthline.cling.model.types;

public abstract class AbstractDatatype<V>
  implements Datatype<V>
{
  private Datatype.Builtin builtin;
  
  protected Class<V> getValueType()
  {
    return (Class)((java.lang.reflect.ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
  }
  
  public boolean isHandlingJavaType(Class type)
  {
    return getValueType().isAssignableFrom(type);
  }
  
  public V valueOf(String s)
    throws InvalidValueException
  {
    return null;
  }
  
  public Datatype.Builtin getBuiltin()
  {
    return this.builtin;
  }
  
  public void setBuiltin(Datatype.Builtin builtin)
  {
    this.builtin = builtin;
  }
  
  public String getString(V value)
    throws InvalidValueException
  {
    if (value == null) {
      return "";
    }
    if (!isValid(value)) {
      throw new InvalidValueException("Value is not valid: " + value);
    }
    return value.toString();
  }
  
  public boolean isValid(V value)
  {
    return (value == null) || (getValueType().isAssignableFrom(value.getClass()));
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ")";
  }
  
  public String getDisplayString()
  {
    if ((this instanceof CustomDatatype)) {
      return ((CustomDatatype)this).getName();
    }
    if (getBuiltin() != null) {
      return getBuiltin().getDescriptorName();
    }
    return getValueType().getSimpleName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\AbstractDatatype.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */