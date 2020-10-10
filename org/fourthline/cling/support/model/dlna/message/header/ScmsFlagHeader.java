package org.fourthline.cling.support.model.dlna.message.header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fourthline.cling.model.message.header.InvalidHeaderException;
import org.fourthline.cling.support.model.dlna.types.ScmsFlagType;

public class ScmsFlagHeader
  extends DLNAHeader<ScmsFlagType>
{
  static final Pattern pattern = Pattern.compile("^[01]{2}$", 2);
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (pattern.matcher(s).matches())
    {
      setValue(new ScmsFlagType(s.charAt(0) == '0', s.charAt(1) == '0'));
      return;
    }
    throw new InvalidHeaderException("Invalid ScmsFlag header value: " + s);
  }
  
  public String getString()
  {
    ScmsFlagType v = (ScmsFlagType)getValue();
    return (v.isCopyright() ? "0" : "1") + (v.isOriginal() ? "0" : "1");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\ScmsFlagHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */