package org.fourthline.cling.support.model.dlna;

import java.util.EnumSet;
import java.util.Locale;

public class DLNAOperationsAttribute
  extends DLNAAttribute<EnumSet<DLNAOperations>>
{
  public DLNAOperationsAttribute()
  {
    setValue(EnumSet.of(DLNAOperations.NONE));
  }
  
  public DLNAOperationsAttribute(DLNAOperations... op)
  {
    if ((op != null) && (op.length > 0))
    {
      DLNAOperations first = op[0];
      if (op.length > 1)
      {
        System.arraycopy(op, 1, op, 0, op.length - 1);
        setValue(EnumSet.of(first, op));
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
    EnumSet<DLNAOperations> value = EnumSet.noneOf(DLNAOperations.class);
    try
    {
      int parseInt = Integer.parseInt(s, 16);
      for (DLNAOperations op : DLNAOperations.values())
      {
        int code = op.getCode() & parseInt;
        if ((op != DLNAOperations.NONE) && (op.getCode() == code)) {
          value.add(op);
        }
      }
    }
    catch (NumberFormatException localNumberFormatException) {}
    if (value.isEmpty()) {
      throw new InvalidDLNAProtocolAttributeException("Can't parse DLNA operations integer from: " + s);
    }
    setValue(value);
  }
  
  public String getString()
  {
    int code = DLNAOperations.NONE.getCode();
    for (DLNAOperations op : (EnumSet)getValue()) {
      code |= op.getCode();
    }
    return String.format(Locale.ROOT, "%02x", new Object[] { Integer.valueOf(code) });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\DLNAOperationsAttribute.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */