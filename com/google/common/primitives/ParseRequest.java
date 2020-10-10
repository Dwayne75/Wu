package com.google.common.primitives;

import com.google.common.annotations.GwtCompatible;

@GwtCompatible
final class ParseRequest
{
  final String rawValue;
  final int radix;
  
  private ParseRequest(String rawValue, int radix)
  {
    this.rawValue = rawValue;
    this.radix = radix;
  }
  
  static ParseRequest fromString(String stringValue)
  {
    if (stringValue.length() == 0) {
      throw new NumberFormatException("empty string");
    }
    char firstChar = stringValue.charAt(0);
    int radix;
    String rawValue;
    int radix;
    if ((stringValue.startsWith("0x")) || (stringValue.startsWith("0X")))
    {
      String rawValue = stringValue.substring(2);
      radix = 16;
    }
    else
    {
      int radix;
      if (firstChar == '#')
      {
        String rawValue = stringValue.substring(1);
        radix = 16;
      }
      else
      {
        int radix;
        if ((firstChar == '0') && (stringValue.length() > 1))
        {
          String rawValue = stringValue.substring(1);
          radix = 8;
        }
        else
        {
          rawValue = stringValue;
          radix = 10;
        }
      }
    }
    return new ParseRequest(rawValue, radix);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\primitives\ParseRequest.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */