package org.fourthline.cling.model.types.csv;

import java.net.URI;
import org.fourthline.cling.model.types.InvalidValueException;

public class CSVURI
  extends CSV<URI>
{
  public CSVURI() {}
  
  public CSVURI(String s)
    throws InvalidValueException
  {
    super(s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\csv\CSVURI.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */