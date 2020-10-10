package org.relaxng.datatype;

public abstract interface DatatypeBuilder
{
  public abstract void addParameter(String paramString1, String paramString2, ValidationContext paramValidationContext)
    throws DatatypeException;
  
  public abstract Datatype createDatatype()
    throws DatatypeException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\relaxng\datatype\DatatypeBuilder.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */