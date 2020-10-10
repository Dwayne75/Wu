package org.fourthline.cling.model.meta;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.Validatable;
import org.fourthline.cling.model.ValidationError;

public class UDAVersion
  implements Validatable
{
  private int major = 1;
  private int minor = 0;
  
  public UDAVersion() {}
  
  public UDAVersion(int major, int minor)
  {
    this.major = major;
    this.minor = minor;
  }
  
  public int getMajor()
  {
    return this.major;
  }
  
  public int getMinor()
  {
    return this.minor;
  }
  
  public List<ValidationError> validate()
  {
    List<ValidationError> errors = new ArrayList();
    if (getMajor() != 1) {
      errors.add(new ValidationError(
        getClass(), "major", "UDA major spec version must be 1"));
    }
    if (getMajor() < 0) {
      errors.add(new ValidationError(
        getClass(), "minor", "UDA minor spec version must be equal or greater 0"));
    }
    return errors;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\meta\UDAVersion.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */