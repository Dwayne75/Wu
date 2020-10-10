package org.fourthline.cling.model.types;

public class BytesRange
{
  public static final String PREFIX = "bytes=";
  private Long firstByte;
  private Long lastByte;
  private Long byteLength;
  
  public BytesRange(Long firstByte, Long lastByte)
  {
    this.firstByte = firstByte;
    this.lastByte = lastByte;
    this.byteLength = null;
  }
  
  public BytesRange(Long firstByte, Long lastByte, Long byteLength)
  {
    this.firstByte = firstByte;
    this.lastByte = lastByte;
    this.byteLength = byteLength;
  }
  
  public Long getFirstByte()
  {
    return this.firstByte;
  }
  
  public Long getLastByte()
  {
    return this.lastByte;
  }
  
  public Long getByteLength()
  {
    return this.byteLength;
  }
  
  public String getString()
  {
    return getString(false, null);
  }
  
  public String getString(boolean includeDuration)
  {
    return getString(includeDuration, null);
  }
  
  public String getString(boolean includeDuration, String rangePrefix)
  {
    String s = rangePrefix != null ? rangePrefix : "bytes=";
    if (this.firstByte != null) {
      s = s + this.firstByte.toString();
    }
    s = s + "-";
    if (this.lastByte != null) {
      s = s + this.lastByte.toString();
    }
    if (includeDuration) {
      s = s + "/" + (this.byteLength != null ? this.byteLength.toString() : "*");
    }
    return s;
  }
  
  public static BytesRange valueOf(String s)
    throws InvalidValueException
  {
    return valueOf(s, null);
  }
  
  public static BytesRange valueOf(String s, String rangePrefix)
    throws InvalidValueException
  {
    if (s.startsWith(rangePrefix != null ? rangePrefix : "bytes="))
    {
      Long firstByte = null;Long lastByte = null;Long byteLength = null;
      String[] params = s.substring((rangePrefix != null ? rangePrefix : "bytes=").length()).split("[-/]");
      switch (params.length)
      {
      case 3: 
        if ((params[2].length() != 0) && (!params[2].equals("*"))) {
          byteLength = Long.valueOf(Long.parseLong(params[2]));
        }
      case 2: 
        if (params[1].length() != 0) {
          lastByte = Long.valueOf(Long.parseLong(params[1]));
        }
      case 1: 
        if (params[0].length() != 0) {
          firstByte = Long.valueOf(Long.parseLong(params[0]));
        }
        if ((firstByte != null) || (lastByte != null)) {
          return new BytesRange(firstByte, lastByte, byteLength);
        }
        break;
      }
    }
    throw new InvalidValueException("Can't parse Bytes Range: " + s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\BytesRange.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */