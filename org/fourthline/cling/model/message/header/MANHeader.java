package org.fourthline.cling.model.message.header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MANHeader
  extends UpnpHeader<String>
{
  public static final Pattern PATTERN = Pattern.compile("\"(.+?)\"(;.+?)??");
  public static final Pattern NAMESPACE_PATTERN = Pattern.compile(";\\s?ns\\s?=\\s?([0-9]{2})");
  public String namespace;
  
  public MANHeader() {}
  
  public MANHeader(String value)
  {
    setValue(value);
  }
  
  public MANHeader(String value, String namespace)
  {
    this(value);
    this.namespace = namespace;
  }
  
  public void setString(String s)
    throws InvalidHeaderException
  {
    Matcher matcher = PATTERN.matcher(s);
    if (matcher.matches())
    {
      setValue(matcher.group(1));
      if (matcher.group(2) != null)
      {
        Matcher nsMatcher = NAMESPACE_PATTERN.matcher(matcher.group(2));
        if (nsMatcher.matches()) {
          setNamespace(nsMatcher.group(1));
        } else {
          throw new InvalidHeaderException("Invalid namespace in MAN header value: " + s);
        }
      }
    }
    else
    {
      throw new InvalidHeaderException("Invalid MAN header value: " + s);
    }
  }
  
  public String getString()
  {
    if (getValue() == null) {
      return null;
    }
    StringBuilder s = new StringBuilder();
    s.append("\"").append((String)getValue()).append("\"");
    if (getNamespace() != null) {
      s.append("; ns=").append(getNamespace());
    }
    return s.toString();
  }
  
  public String getNamespace()
  {
    return this.namespace;
  }
  
  public void setNamespace(String namespace)
  {
    this.namespace = namespace;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\model\message\header\MANHeader.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */