package org.fourthline.cling.model.message.header;

import org.seamless.util.io.HexBin;

public class InterfaceMacHeader
  extends UpnpHeader<byte[]>
{
  public InterfaceMacHeader() {}
  
  public InterfaceMacHeader(byte[] value)
  {
    setValue(value);
  }
  
  public InterfaceMacHeader(String s)
  {
    setString(s);
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    byte[] bytes = HexBin.stringToBytes(s, ":");
    setValue(bytes);
    if (bytes.length != 6) {
      throw new InvalidHeaderException("Invalid MAC address: " + s);
    }
  }
  
  public String getString()
  {
    return HexBin.bytesToString((byte[])getValue(), ":");
  }
  
  public String toString()
  {
    return "(" + getClass().getSimpleName() + ") '" + getString() + "'";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\InterfaceMacHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */