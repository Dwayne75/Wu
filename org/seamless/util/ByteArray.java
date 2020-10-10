package org.seamless.util;

public class ByteArray
{
  public static byte[] toPrimitive(Byte[] array)
  {
    byte[] bytes = new byte[array.length];
    for (int i = 0; i < array.length; i++) {
      bytes[i] = array[i].byteValue();
    }
    return bytes;
  }
  
  public static Byte[] toWrapper(byte[] array)
  {
    Byte[] wrappers = new Byte[array.length];
    for (int i = 0; i < array.length; i++) {
      wrappers[i] = Byte.valueOf(array[i]);
    }
    return wrappers;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\ByteArray.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */