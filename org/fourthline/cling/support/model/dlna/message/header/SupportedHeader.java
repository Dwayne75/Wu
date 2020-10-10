package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class SupportedHeader
  extends DLNAHeader<String[]>
{
  public SupportedHeader()
  {
    setValue(new String[0]);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0)
    {
      if (s.endsWith(";")) {
        s = s.substring(0, s.length() - 1);
      }
      setValue(s.split("\\s*,\\s*"));
      return;
    }
    throw new InvalidHeaderException("Invalid Supported header value: " + s);
  }
  
  public String getString()
  {
    String[] v = (String[])getValue();
    String r = v.length > 0 ? v[0] : "";
    for (int i = 1; i < v.length; i++) {
      r = r + "," + v[i];
    }
    return r;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\SupportedHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */