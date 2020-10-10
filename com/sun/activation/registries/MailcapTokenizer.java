package com.sun.activation.registries;

public class MailcapTokenizer
{
  public static final int UNKNOWN_TOKEN = 0;
  public static final int START_TOKEN = 1;
  public static final int STRING_TOKEN = 2;
  public static final int EOI_TOKEN = 5;
  public static final int SLASH_TOKEN = 47;
  public static final int SEMICOLON_TOKEN = 59;
  public static final int EQUALS_TOKEN = 61;
  private String data;
  private int dataIndex;
  private int dataLength;
  private int currentToken;
  private String currentTokenValue;
  private boolean isAutoquoting;
  private char autoquoteChar;
  
  public MailcapTokenizer(String inputString)
  {
    this.data = inputString;
    this.dataIndex = 0;
    this.dataLength = inputString.length();
    
    this.currentToken = 1;
    this.currentTokenValue = "";
    
    this.isAutoquoting = false;
    this.autoquoteChar = ';';
  }
  
  public void setIsAutoquoting(boolean value)
  {
    this.isAutoquoting = value;
  }
  
  public int getCurrentToken()
  {
    return this.currentToken;
  }
  
  public static String nameForToken(int token)
  {
    String name = "really unknown";
    switch (token)
    {
    case 0: 
      name = "unknown";
      break;
    case 1: 
      name = "start";
      break;
    case 2: 
      name = "string";
      break;
    case 5: 
      name = "EOI";
      break;
    case 47: 
      name = "'/'";
      break;
    case 59: 
      name = "';'";
      break;
    case 61: 
      name = "'='";
    }
    return name;
  }
  
  public String getCurrentTokenValue()
  {
    return this.currentTokenValue;
  }
  
  public int nextToken()
  {
    if (this.dataIndex < this.dataLength)
    {
      while ((this.dataIndex < this.dataLength) && (isWhiteSpaceChar(this.data.charAt(this.dataIndex)))) {
        this.dataIndex += 1;
      }
      if (this.dataIndex < this.dataLength)
      {
        char c = this.data.charAt(this.dataIndex);
        if (this.isAutoquoting)
        {
          if ((c == ';') || (c == '='))
          {
            this.currentToken = c;
            this.currentTokenValue = new Character(c).toString();
            this.dataIndex += 1;
          }
          else
          {
            processAutoquoteToken();
          }
        }
        else if (isStringTokenChar(c))
        {
          processStringToken();
        }
        else if ((c == '/') || (c == ';') || (c == '='))
        {
          this.currentToken = c;
          this.currentTokenValue = new Character(c).toString();
          this.dataIndex += 1;
        }
        else
        {
          this.currentToken = 0;
          this.currentTokenValue = new Character(c).toString();
          this.dataIndex += 1;
        }
      }
      else
      {
        this.currentToken = 5;
        this.currentTokenValue = null;
      }
    }
    else
    {
      this.currentToken = 5;
      this.currentTokenValue = null;
    }
    return this.currentToken;
  }
  
  private void processStringToken()
  {
    int initialIndex = this.dataIndex;
    while ((this.dataIndex < this.dataLength) && (isStringTokenChar(this.data.charAt(this.dataIndex)))) {
      this.dataIndex += 1;
    }
    this.currentToken = 2;
    this.currentTokenValue = this.data.substring(initialIndex, this.dataIndex);
  }
  
  private void processAutoquoteToken()
  {
    int initialIndex = this.dataIndex;
    
    boolean foundTerminator = false;
    while ((this.dataIndex < this.dataLength) && (!foundTerminator))
    {
      char c = this.data.charAt(this.dataIndex);
      if (c != this.autoquoteChar) {
        this.dataIndex += 1;
      } else {
        foundTerminator = true;
      }
    }
    this.currentToken = 2;
    this.currentTokenValue = fixEscapeSequences(this.data.substring(initialIndex, this.dataIndex));
  }
  
  private static boolean isSpecialChar(char c)
  {
    boolean lAnswer = false;
    switch (c)
    {
    case '"': 
    case '(': 
    case ')': 
    case ',': 
    case '/': 
    case ':': 
    case ';': 
    case '<': 
    case '=': 
    case '>': 
    case '?': 
    case '@': 
    case '[': 
    case '\\': 
    case ']': 
      lAnswer = true;
    }
    return lAnswer;
  }
  
  private static boolean isControlChar(char c)
  {
    return Character.isISOControl(c);
  }
  
  private static boolean isWhiteSpaceChar(char c)
  {
    return Character.isWhitespace(c);
  }
  
  private static boolean isStringTokenChar(char c)
  {
    return (!isSpecialChar(c)) && (!isControlChar(c)) && (!isWhiteSpaceChar(c));
  }
  
  private static String fixEscapeSequences(String inputString)
  {
    int inputLength = inputString.length();
    StringBuffer buffer = new StringBuffer();
    buffer.ensureCapacity(inputLength);
    for (int i = 0; i < inputLength; i++)
    {
      char currentChar = inputString.charAt(i);
      if (currentChar != '\\')
      {
        buffer.append(currentChar);
      }
      else if (i < inputLength - 1)
      {
        char nextChar = inputString.charAt(i + 1);
        buffer.append(nextChar);
        
        i++;
      }
      else
      {
        buffer.append(currentChar);
      }
    }
    return buffer.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\activation\registries\MailcapTokenizer.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */