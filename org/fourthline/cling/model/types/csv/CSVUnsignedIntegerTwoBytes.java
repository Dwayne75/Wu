package org.fourthline.cling.model.types.csv;

import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UnsignedIntegerTwoBytes;

public class CSVUnsignedIntegerTwoBytes
  extends CSV<UnsignedIntegerTwoBytes>
{
  public CSVUnsignedIntegerTwoBytes() {}
  
  public CSVUnsignedIntegerTwoBytes(String s)
    throws InvalidValueException
  {
    super(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\csv\CSVUnsignedIntegerTwoBytes.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */