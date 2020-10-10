package org.relaxng.datatype.helpers;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

public final class ParameterlessDatatypeBuilder
  implements DatatypeBuilder
{
  private final Datatype baseType;
  
  public ParameterlessDatatypeBuilder(Datatype paramDatatype)
  {
    this.baseType = paramDatatype;
  }
  
  public void addParameter(String paramString1, String paramString2, ValidationContext paramValidationContext)
    throws DatatypeException
  {
    throw new DatatypeException();
  }
  
  public Datatype createDatatype()
    throws DatatypeException
  {
    return this.baseType;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\relaxng\datatype\helpers\ParameterlessDatatypeBuilder.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */