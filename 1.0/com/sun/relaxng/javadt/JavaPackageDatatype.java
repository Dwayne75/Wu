package com.sun.relaxng.javadt;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

public class JavaPackageDatatype
  extends AbstractDatatypeImpl
{
  public static final Datatype theInstance = new JavaPackageDatatype();
  
  public boolean isValid(String token, ValidationContext context)
  {
    return Name.isJavaPackageName(token.trim());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\relaxng\javadt\JavaPackageDatatype.class
 * Java compiler version: 3 (47.0)
 * JD-Core Version:       0.7.1
 */