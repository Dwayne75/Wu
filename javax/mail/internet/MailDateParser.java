package javax.mail.internet;

import java.text.ParseException;

class MailDateParser
{
  int index = 0;
  char[] orig = null;
  
  public MailDateParser(char[] orig, int index)
  {
    this.orig = orig;
    this.index = index;
  }
  
  /* Error */
  public void skipUntilNumber()
    throws ParseException
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 3	javax/mail/internet/MailDateParser:orig	[C
    //   4: aload_0
    //   5: getfield 2	javax/mail/internet/MailDateParser:index	I
    //   8: caload
    //   9: tableswitch	default:+56->65, 48:+55->64, 49:+55->64, 50:+55->64, 51:+55->64, 52:+55->64, 53:+55->64, 54:+55->64, 55:+55->64, 56:+55->64, 57:+55->64
    //   64: return
    //   65: aload_0
    //   66: dup
    //   67: getfield 2	javax/mail/internet/MailDateParser:index	I
    //   70: iconst_1
    //   71: iadd
    //   72: putfield 2	javax/mail/internet/MailDateParser:index	I
    //   75: goto -75 -> 0
    //   78: astore_1
    //   79: new 5	java/text/ParseException
    //   82: dup
    //   83: ldc 6
    //   85: aload_0
    //   86: getfield 2	javax/mail/internet/MailDateParser:index	I
    //   89: invokespecial 7	java/text/ParseException:<init>	(Ljava/lang/String;I)V
    //   92: athrow
    // Line number table:
    //   Java source line #482	-> byte code offset #0
    //   Java source line #493	-> byte code offset #64
    //   Java source line #496	-> byte code offset #65
    //   Java source line #497	-> byte code offset #75
    //   Java source line #500	-> byte code offset #78
    //   Java source line #501	-> byte code offset #79
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	93	0	this	MailDateParser
    //   78	2	1	e	ArrayIndexOutOfBoundsException
    // Exception table:
    //   from	to	target	type
    //   0	64	78	java/lang/ArrayIndexOutOfBoundsException
    //   65	78	78	java/lang/ArrayIndexOutOfBoundsException
  }
  
  public void skipWhiteSpace()
  {
    int len = this.orig.length;
    while (this.index < len) {
      switch (this.orig[this.index])
      {
      case '\t': 
      case '\n': 
      case '\r': 
      case ' ': 
        this.index += 1;
        break;
      }
    }
  }
  
  public int peekChar()
    throws ParseException
  {
    if (this.index < this.orig.length) {
      return this.orig[this.index];
    }
    throw new ParseException("No more characters", this.index);
  }
  
  public void skipChar(char c)
    throws ParseException
  {
    if (this.index < this.orig.length)
    {
      if (this.orig[this.index] == c) {
        this.index += 1;
      } else {
        throw new ParseException("Wrong char", this.index);
      }
    }
    else {
      throw new ParseException("No more characters", this.index);
    }
  }
  
  public boolean skipIfChar(char c)
    throws ParseException
  {
    if (this.index < this.orig.length)
    {
      if (this.orig[this.index] == c)
      {
        this.index += 1;
        return true;
      }
      return false;
    }
    throw new ParseException("No more characters", this.index);
  }
  
  public int parseNumber()
    throws ParseException
  {
    int length = this.orig.length;
    boolean gotNum = false;
    int result = 0;
    while (this.index < length)
    {
      switch (this.orig[this.index])
      {
      case '0': 
        result *= 10;
        gotNum = true;
        break;
      case '1': 
        result = result * 10 + 1;
        gotNum = true;
        break;
      case '2': 
        result = result * 10 + 2;
        gotNum = true;
        break;
      case '3': 
        result = result * 10 + 3;
        gotNum = true;
        break;
      case '4': 
        result = result * 10 + 4;
        gotNum = true;
        break;
      case '5': 
        result = result * 10 + 5;
        gotNum = true;
        break;
      case '6': 
        result = result * 10 + 6;
        gotNum = true;
        break;
      case '7': 
        result = result * 10 + 7;
        gotNum = true;
        break;
      case '8': 
        result = result * 10 + 8;
        gotNum = true;
        break;
      case '9': 
        result = result * 10 + 9;
        gotNum = true;
        break;
      default: 
        if (gotNum) {
          return result;
        }
        throw new ParseException("No Number found", this.index);
      }
      this.index += 1;
    }
    if (gotNum) {
      return result;
    }
    throw new ParseException("No Number found", this.index);
  }
  
  public int parseMonth()
    throws ParseException
  {
    try
    {
      char curr;
      switch (this.orig[(this.index++)])
      {
      case 'J': 
      case 'j': 
        switch (this.orig[(this.index++)])
        {
        case 'A': 
        case 'a': 
          curr = this.orig[(this.index++)];
          if ((curr == 'N') || (curr == 'n')) {
            return 0;
          }
          break;
        case 'U': 
        case 'u': 
          curr = this.orig[(this.index++)];
          if ((curr == 'N') || (curr == 'n')) {
            return 5;
          }
          if ((curr == 'L') || (curr == 'l')) {
            return 6;
          }
          break;
        }
        break;
      case 'F': 
      case 'f': 
        curr = this.orig[(this.index++)];
        if ((curr == 'E') || (curr == 'e'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'B') || (curr == 'b')) {
            return 1;
          }
        }
        break;
      case 'M': 
      case 'm': 
        curr = this.orig[(this.index++)];
        if ((curr == 'A') || (curr == 'a'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'R') || (curr == 'r')) {
            return 2;
          }
          if ((curr == 'Y') || (curr == 'y')) {
            return 4;
          }
        }
        break;
      case 'A': 
      case 'a': 
        curr = this.orig[(this.index++)];
        if ((curr == 'P') || (curr == 'p'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'R') || (curr == 'r')) {
            return 3;
          }
        }
        else if ((curr == 'U') || (curr == 'u'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'G') || (curr == 'g')) {
            return 7;
          }
        }
        break;
      case 'S': 
      case 's': 
        curr = this.orig[(this.index++)];
        if ((curr == 'E') || (curr == 'e'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'P') || (curr == 'p')) {
            return 8;
          }
        }
        break;
      case 'O': 
      case 'o': 
        curr = this.orig[(this.index++)];
        if ((curr == 'C') || (curr == 'c'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'T') || (curr == 't')) {
            return 9;
          }
        }
        break;
      case 'N': 
      case 'n': 
        curr = this.orig[(this.index++)];
        if ((curr == 'O') || (curr == 'o'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'V') || (curr == 'v')) {
            return 10;
          }
        }
        break;
      case 'D': 
      case 'd': 
        curr = this.orig[(this.index++)];
        if ((curr == 'E') || (curr == 'e'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'C') || (curr == 'c')) {
            return 11;
          }
        }
        break;
      }
    }
    catch (ArrayIndexOutOfBoundsException e) {}
    throw new ParseException("Bad Month", this.index);
  }
  
  public int parseTimeZone()
    throws ParseException
  {
    if (this.index >= this.orig.length) {
      throw new ParseException("No more characters", this.index);
    }
    char test = this.orig[this.index];
    if ((test == '+') || (test == '-')) {
      return parseNumericTimeZone();
    }
    return parseAlphaTimeZone();
  }
  
  public int parseNumericTimeZone()
    throws ParseException
  {
    boolean switchSign = false;
    char first = this.orig[(this.index++)];
    if (first == '+') {
      switchSign = true;
    } else if (first != '-') {
      throw new ParseException("Bad Numeric TimeZone", this.index);
    }
    int oindex = this.index;
    int tz = parseNumber();
    if (tz >= 2400) {
      throw new ParseException("Numeric TimeZone out of range", oindex);
    }
    int offset = tz / 100 * 60 + tz % 100;
    if (switchSign) {
      return -offset;
    }
    return offset;
  }
  
  public int parseAlphaTimeZone()
    throws ParseException
  {
    int result = 0;
    boolean foundCommon = false;
    char curr;
    try
    {
      switch (this.orig[(this.index++)])
      {
      case 'U': 
      case 'u': 
        curr = this.orig[(this.index++)];
        if ((curr == 'T') || (curr == 't')) {
          result = 0;
        } else {
          throw new ParseException("Bad Alpha TimeZone", this.index);
        }
        break;
      case 'G': 
      case 'g': 
        curr = this.orig[(this.index++)];
        if ((curr == 'M') || (curr == 'm'))
        {
          curr = this.orig[(this.index++)];
          if ((curr == 'T') || (curr == 't'))
          {
            result = 0;
            break;
          }
        }
        throw new ParseException("Bad Alpha TimeZone", this.index);
      case 'E': 
      case 'e': 
        result = 300;
        foundCommon = true;
        break;
      case 'C': 
      case 'c': 
        result = 360;
        foundCommon = true;
        break;
      case 'M': 
      case 'm': 
        result = 420;
        foundCommon = true;
        break;
      case 'P': 
      case 'p': 
        result = 480;
        foundCommon = true;
        break;
      default: 
        throw new ParseException("Bad Alpha TimeZone", this.index);
      }
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      throw new ParseException("Bad Alpha TimeZone", this.index);
    }
    if (foundCommon)
    {
      curr = this.orig[(this.index++)];
      if ((curr == 'S') || (curr == 's'))
      {
        curr = this.orig[(this.index++)];
        if ((curr != 'T') && (curr != 't')) {
          throw new ParseException("Bad Alpha TimeZone", this.index);
        }
      }
      else if ((curr == 'D') || (curr == 'd'))
      {
        curr = this.orig[(this.index++)];
        if ((curr == 'T') || (curr != 't')) {
          result -= 60;
        } else {
          throw new ParseException("Bad Alpha TimeZone", this.index);
        }
      }
    }
    return result;
  }
  
  int getIndex()
  {
    return this.index;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\internet\MailDateParser.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */