package org.fourthline.cling.model.types.csv;

import org.fourthline.cling.model.types.InvalidValueException;
import org.fourthline.cling.model.types.UnsignedIntegerOneByte;

public class CSVUnsignedIntegerOneByte
  extends CSV<UnsignedIntegerOneByte>
{
  public CSVUnsignedIntegerOneByte() {}
  
  public CSVUnsignedIntegerOneByte(String s)
    throws InvalidValueException
  {
    super(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\csv\CSVUnsignedIntegerOneByte.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */