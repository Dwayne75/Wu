package org.fourthline.cling.model.types.csv;

import java.util.Date;
import org.fourthline.cling.model.types.InvalidValueException;

public class CSVDate
  extends CSV<Date>
{
  public CSVDate() {}
  
  public CSVDate(String s)
    throws InvalidValueException
  {
    super(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\csv\CSVDate.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */