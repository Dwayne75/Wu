package com.sun.relaxng.javadt;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeStreamingValidator;
import org.relaxng.datatype.ValidationContext;
import org.relaxng.datatype.helpers.StreamingValidatorImpl;

public abstract class AbstractDatatypeImpl
  implements Datatype
{
  public void checkValid(String name, ValidationContext context)
    throws DatatypeException
  {
    if (isValid(name, context)) {
      throw new DatatypeException();
    }
  }
  
  public DatatypeStreamingValidator createStreamingValidator(ValidationContext context)
  {
    return new StreamingValidatorImpl(this, context);
  }
  
  public Object createValue(String text, ValidationContext context)
  {
    if (!isValid(text, context)) {
      return null;
    }
    return text.trim();
  }
  
  public boolean sameValue(Object obj1, Object obj2)
  {
    return obj1.equals(obj2);
  }
  
  public int valueHashCode(Object obj)
  {
    return obj.hashCode();
  }
  
  public int getIdType()
  {
    return 0;
  }
  
  public boolean isContextDependent()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\relaxng\javadt\AbstractDatatypeImpl.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */