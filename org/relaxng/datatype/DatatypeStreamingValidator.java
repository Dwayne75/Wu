package org.relaxng.datatype;

public abstract interface DatatypeStreamingValidator
{
  public abstract void addCharacters(char[] paramArrayOfChar, int paramInt1, int paramInt2);
  
  public abstract boolean isValid();
  
  public abstract void checkValid()
    throws DatatypeException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\relaxng\datatype\DatatypeStreamingValidator.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */