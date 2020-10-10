package org.fourthline.cling.model.types;

public class PragmaType
{
  private String token;
  private boolean quote;
  private String value;
  
  public PragmaType(String token, String value, boolean quote)
  {
    this.token = token;
    this.value = value;
    this.quote = quote;
  }
  
  public PragmaType(String token, String value)
  {
    this.token = token;
    this.value = value;
  }
  
  public PragmaType(String value)
  {
    this.token = null;
    this.value = value;
  }
  
  public String getToken()
  {
    return this.token;
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public String getString()
  {
    String s = "";
    if (this.token != null) {
      s = s + this.token + "=";
    }
    s = s + (this.quote ? "\"" + this.value + "\"" : this.value);
    return s;
  }
  
  public static PragmaType valueOf(String s)
    throws InvalidValueException
  {
    if (s.length() != 0)
    {
      String token = null;String value = null;
      boolean quote = false;
      String[] params = s.split("=");
      if (params.length > 1)
      {
        token = params[0];
        value = params[1];
        if ((value.startsWith("\"")) && (value.endsWith("\"")))
        {
          quote = true;
          value = value.substring(1, value.length() - 1);
        }
      }
      else
      {
        value = s;
      }
      return new PragmaType(token, value, quote);
    }
    throw new InvalidValueException("Can't parse Bytes Range: " + s);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\types\PragmaType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */