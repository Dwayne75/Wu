package org.fourthline.cling.model;

import java.util.List;

public class ValidationException
  extends Exception
{
  public List<ValidationError> errors;
  
  public ValidationException(String s)
  {
    super(s);
  }
  
  public ValidationException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
  
  public ValidationException(String s, List<ValidationError> errors)
  {
    super(s);
    this.errors = errors;
  }
  
  public List<ValidationError> getErrors()
  {
    return this.errors;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\ValidationException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */