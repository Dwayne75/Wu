package org.fourthline.cling.model.types.csv;

import org.fourthline.cling.model.types.InvalidValueException;

public class CSVFloat
  extends CSV<Float>
{
  public CSVFloat() {}
  
  public CSVFloat(String s)
    throws InvalidValueException
  {
    super(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\csv\CSVFloat.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */