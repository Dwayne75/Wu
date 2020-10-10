package com.mysql.jdbc;

public class EscapeTokenizer
{
  private int bracesLevel = 0;
  private boolean emittingEscapeCode = false;
  private boolean inComment = false;
  private boolean inQuotes = false;
  private char lastChar = '\000';
  private char lastLastChar = '\000';
  private int pos = 0;
  private char quoteChar = '\000';
  private boolean sawVariableUse = false;
  private String source = null;
  private int sourceLength = 0;
  
  public EscapeTokenizer(String s)
  {
    this.source = s;
    this.sourceLength = s.length();
    this.pos = 0;
  }
  
  public synchronized boolean hasMoreTokens()
  {
    return this.pos < this.sourceLength;
  }
  
  public synchronized String nextToken()
  {
    StringBuffer tokenBuf = new StringBuffer();
    if (this.emittingEscapeCode)
    {
      tokenBuf.append("{");
      this.emittingEscapeCode = false;
    }
    for (; this.pos < this.sourceLength; this.pos += 1)
    {
      char c = this.source.charAt(this.pos);
      if ((!this.inQuotes) && (c == '@')) {
        this.sawVariableUse = true;
      }
      if (((c == '\'') || (c == '"')) && (!this.inComment))
      {
        if ((this.inQuotes) && (c == this.quoteChar) && 
          (this.pos + 1 < this.sourceLength) && 
          (this.source.charAt(this.pos + 1) == this.quoteChar)) {
          if (this.lastChar != '\\')
          {
            tokenBuf.append(this.quoteChar);
            tokenBuf.append(this.quoteChar);
            this.pos += 1;
            continue;
          }
        }
        if (this.lastChar != '\\')
        {
          if (this.inQuotes)
          {
            if (this.quoteChar == c) {
              this.inQuotes = false;
            }
          }
          else
          {
            this.inQuotes = true;
            this.quoteChar = c;
          }
        }
        else if (this.lastLastChar == '\\') {
          if (this.inQuotes)
          {
            if (this.quoteChar == c) {
              this.inQuotes = false;
            }
          }
          else
          {
            this.inQuotes = true;
            this.quoteChar = c;
          }
        }
        tokenBuf.append(c);
      }
      else if (c == '-')
      {
        if ((this.lastChar == '-') && (this.lastLastChar != '\\') && (!this.inQuotes)) {
          this.inComment = true;
        }
        tokenBuf.append(c);
      }
      else if ((c == '\n') || (c == '\r'))
      {
        this.inComment = false;
        
        tokenBuf.append(c);
      }
      else if (c == '{')
      {
        if ((this.inQuotes) || (this.inComment))
        {
          tokenBuf.append(c);
        }
        else
        {
          this.bracesLevel += 1;
          if (this.bracesLevel == 1)
          {
            this.pos += 1;
            this.emittingEscapeCode = true;
            
            return tokenBuf.toString();
          }
          tokenBuf.append(c);
        }
      }
      else if (c == '}')
      {
        tokenBuf.append(c);
        if ((!this.inQuotes) && (!this.inComment))
        {
          this.lastChar = c;
          
          this.bracesLevel -= 1;
          if (this.bracesLevel == 0)
          {
            this.pos += 1;
            
            return tokenBuf.toString();
          }
        }
      }
      else
      {
        tokenBuf.append(c);
      }
      this.lastLastChar = this.lastChar;
      this.lastChar = c;
    }
    return tokenBuf.toString();
  }
  
  boolean sawVariableUse()
  {
    return this.sawVariableUse;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\EscapeTokenizer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */