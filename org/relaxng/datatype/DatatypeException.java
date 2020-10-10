package org.relaxng.datatype;

public class DatatypeException
  extends Exception
{
  private final int index;
  public static final int UNKNOWN = -1;
  
  public DatatypeException(int paramInt, String paramString)
  {
    super(paramString);
    this.index = paramInt;
  }
  
  public DatatypeException(String paramString)
  {
    this(-1, paramString);
  }
  
  public DatatypeException()
  {
    this(-1, null);
  }
  
  public int getIndex()
  {
    return this.index;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\relaxng\datatype\DatatypeException.class
 * Java compiler version: 1 (45.3)
 * JD-Core Version:       0.7.1
 */