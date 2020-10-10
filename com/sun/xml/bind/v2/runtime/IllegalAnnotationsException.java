package com.sun.xml.bind.v2.runtime;

import com.sun.xml.bind.v2.model.core.ErrorHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;

public class IllegalAnnotationsException
  extends JAXBException
{
  private final List<IllegalAnnotationException> errors;
  private static final long serialVersionUID = 1L;
  
  public IllegalAnnotationsException(List<IllegalAnnotationException> errors)
  {
    super(errors.size() + " counts of IllegalAnnotationExceptions");
    assert (!errors.isEmpty()) : "there must be at least one error";
    this.errors = Collections.unmodifiableList(new ArrayList(errors));
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append('\n');
    for (IllegalAnnotationException error : this.errors) {
      sb.append(error.toString()).append('\n');
    }
    return sb.toString();
  }
  
  public List<IllegalAnnotationException> getErrors()
  {
    return this.errors;
  }
  
  public static class Builder
    implements ErrorHandler
  {
    private final List<IllegalAnnotationException> list = new ArrayList();
    
    public void error(IllegalAnnotationException e)
    {
      this.list.add(e);
    }
    
    public void check()
      throws IllegalAnnotationsException
    {
      if (this.list.isEmpty()) {
        return;
      }
      throw new IllegalAnnotationsException(this.list);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\IllegalAnnotationsException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */