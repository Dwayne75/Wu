package org.fourthline.cling.model;

public class ValidationError
{
  private Class clazz;
  private String propertyName;
  private String message;
  
  public ValidationError(Class clazz, String message)
  {
    this.clazz = clazz;
    this.message = message;
  }
  
  public ValidationError(Class clazz, String propertyName, String message)
  {
    this.clazz = clazz;
    this.propertyName = propertyName;
    this.message = message;
  }
  
  public Class getClazz()
  {
    return this.clazz;
  }
  
  public String getPropertyName()
  {
    return this.propertyName;
  }
  
  public String getMessage()
  {
    return this.message;
  }
  
  public String toString()
  {
    return getClass().getSimpleName() + " (Class: " + getClazz().getSimpleName() + ", propertyName: " + getPropertyName() + "): " + this.message;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\ValidationError.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */