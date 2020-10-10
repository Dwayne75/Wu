package org.fourthline.cling.registry;

import java.util.List;
import org.fourthline.cling.model.ValidationError;

public class RegistrationException
  extends RuntimeException
{
  public List<ValidationError> errors;
  
  public RegistrationException(String s)
  {
    super(s);
  }
  
  public RegistrationException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
  
  public RegistrationException(String s, List<ValidationError> errors)
  {
    super(s);
    this.errors = errors;
  }
  
  public List<ValidationError> getErrors()
  {
    return this.errors;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\registry\RegistrationException.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */