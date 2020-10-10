package org.fourthline.cling.support.model.dlna.message.header;

import org.fourthline.cling.model.message.header.InvalidHeaderException;

public class TransferModeHeader
  extends DLNAHeader<Type>
{
  public static enum Type
  {
    Streaming,  Interactive,  Background;
    
    private Type() {}
  }
  
  public TransferModeHeader()
  {
    setValue(Type.Interactive);
  }
  
  public TransferModeHeader(Type mode)
  {
    setValue(mode);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    if (s.length() != 0) {
      try
      {
        setValue(Type.valueOf(s));
        return;
      }
      catch (Exception localException) {}
    }
    throw new InvalidHeaderException("Invalid TransferMode header value: " + s);
  }
  
  public String getString()
  {
    return ((Type)getValue()).toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\message\header\TransferModeHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */