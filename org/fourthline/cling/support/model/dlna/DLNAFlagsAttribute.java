package org.fourthline.cling.support.model.dlna;

import java.util.EnumSet;
import java.util.Locale;

public class DLNAFlagsAttribute
  extends DLNAAttribute<EnumSet<DLNAFlags>>
{
  public DLNAFlagsAttribute()
  {
    setValue(EnumSet.noneOf(DLNAFlags.class));
  }
  
  public DLNAFlagsAttribute(DLNAFlags... flags)
  {
    if ((flags != null) && (flags.length > 0))
    {
      DLNAFlags first = flags[0];
      if (flags.length > 1)
      {
        System.arraycopy(flags, 1, flags, 0, flags.length - 1);
        setValue(EnumSet.of(first, flags));
      }
      else
      {
        setValue(EnumSet.of(first));
      }
    }
  }
  
  public void setString(String s, String cf)
    throws InvalidDLNAProtocolAttributeException
  {
    EnumSet<DLNAFlags> value = EnumSet.noneOf(DLNAFlags.class);
    try
    {
      int parseInt = Integer.parseInt(s.substring(0, s.length() - 24), 16);
      for (DLNAFlags op : DLNAFlags.values())
      {
        int code = op.getCode() & parseInt;
        if (op.getCode() == code) {
          value.add(op);
        }
      }
    }
    catch (Exception localException) {}
    if (value.isEmpty()) {
      throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA flags integer from: " + s);
    }
    setValue(value);
  }
  
  public String getString()
  {
    int code = 0;
    for (DLNAFlags op : (EnumSet)getValue()) {
      code |= op.getCode();
    }
    return String.format(Locale.ROOT, "%08x%024x", new Object[] { Integer.valueOf(code), Integer.valueOf(0) });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAFlagsAttribute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */