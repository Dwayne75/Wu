package org.fourthline.cling.support.model.dlna.message.header;

import java.util.ArrayList;
import java.util.List;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.model.types.PragmaType;

public class PragmaHeader
  extends DLNAHeader<List<PragmaType>>
{
  public PragmaHeader()
  {
    setValue(new ArrayList());
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0)
    {
      if (s.endsWith(";")) {
        s = s.substring(0, s.length() - 1);
      }
      String[] list = s.split("\\s*;\\s*");
      List<PragmaType> value = new ArrayList();
      for (String pragma : list) {
        value.add(PragmaType.valueOf(pragma));
      }
      return;
    }
    throw new InvalidHeaderException("Invalid Pragma header value: " + s);
  }
  
  public String getString()
  {
    List<PragmaType> v = (List)getValue();
    String r = "";
    for (PragmaType pragma : v) {
      r = r + (r.length() == 0 ? "" : ",") + pragma.getString();
    }
    return r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\PragmaHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */