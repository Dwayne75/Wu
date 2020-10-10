package winterwell.jtwitter.guts;

import java.io.ByteArrayOutputStream;

public final class Base64Encoder
{
  static final char[] charTab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
  
  public static String encode(String string)
  {
    return encode(string.getBytes()).toString();
  }
  
  public static String encode(byte[] data)
  {
    return encode(data, 0, data.length, null).toString();
  }
  
  public static StringBuffer encode(byte[] data, int start, int len, StringBuffer buf)
  {
    if (buf == null) {
      buf = new StringBuffer(data.length * 3 / 2);
    }
    int end = len - 3;
    int i = start;
    int n = 0;
    while (i <= end)
    {
      int d = (data[i] & 0xFF) << 16 | 
        (data[(i + 1)] & 0xFF) << 8 | 
        data[(i + 2)] & 0xFF;
      
      buf.append(charTab[(d >> 18 & 0x3F)]);
      buf.append(charTab[(d >> 12 & 0x3F)]);
      buf.append(charTab[(d >> 6 & 0x3F)]);
      buf.append(charTab[(d & 0x3F)]);
      
      i += 3;
      if (n++ >= 14)
      {
        n = 0;
        buf.append("\r\n");
      }
    }
    if (i == start + len - 2)
    {
      int d = (data[i] & 0xFF) << 16 | 
        (data[(i + 1)] & 0xFF) << 8;
      
      buf.append(charTab[(d >> 18 & 0x3F)]);
      buf.append(charTab[(d >> 12 & 0x3F)]);
      buf.append(charTab[(d >> 6 & 0x3F)]);
      buf.append("=");
    }
    else if (i == start + len - 1)
    {
      int d = (data[i] & 0xFF) << 16;
      
      buf.append(charTab[(d >> 18 & 0x3F)]);
      buf.append(charTab[(d >> 12 & 0x3F)]);
      buf.append("==");
    }
    return buf;
  }
  
  static int decode(char c)
  {
    if ((c >= 'A') && (c <= 'Z')) {
      return c - 'A';
    }
    if ((c >= 'a') && (c <= 'z')) {
      return c - 'a' + 26;
    }
    if ((c >= '0') && (c <= '9')) {
      return c - '0' + 26 + 26;
    }
    switch (c)
    {
    case '+': 
      return 62;
    case '/': 
      return 63;
    case '=': 
      return 0;
    }
    throw new RuntimeException("unexpected code: " + c);
  }
  
  public static byte[] decode(String s)
  {
    int i = 0;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    int len = s.length();
    for (;; goto 21)
    {
      i++;
      if ((i >= len) || (s.charAt(i) > ' '))
      {
        if (i == len) {
          break;
        }
        int tri = (decode(s.charAt(i)) << 18) + (
          decode(s.charAt(i + 1)) << 12) + (
          decode(s.charAt(i + 2)) << 6) + 
          decode(s.charAt(i + 3));
        
        bos.write(tri >> 16 & 0xFF);
        if (s.charAt(i + 2) == '=') {
          break;
        }
        bos.write(tri >> 8 & 0xFF);
        if (s.charAt(i + 3) == '=') {
          break;
        }
        bos.write(tri & 0xFF);
        
        i += 4;
      }
    }
    return bos.toByteArray();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\jtwitter\guts\Base64Encoder.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */