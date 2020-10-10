package org.relaxng.datatype;

public abstract interface DatatypeLibrary
{
  public abstract DatatypeBuilder createDatatypeBuilder(String paramString)
    throws DatatypeException;
  
  public abstract Datatype createDatatype(String paramString)
    throws DatatypeException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\relaxng\datatype\DatatypeLibrary.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */