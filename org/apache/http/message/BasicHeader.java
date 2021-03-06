package org.apache.http.message;

import java.io.Serializable;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.annotation.Immutable;
import org.apache.http.util.CharArrayBuffer;

@Immutable
public class BasicHeader
  implements Header, Cloneable, Serializable
{
  private static final long serialVersionUID = -5427236326487562174L;
  private final String name;
  private final String value;
  
  public BasicHeader(String name, String value)
  {
    if (name == null) {
      throw new IllegalArgumentException("Name may not be null");
    }
    this.name = name;
    this.value = value;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public String toString()
  {
    return BasicLineFormatter.DEFAULT.formatHeader(null, this).toString();
  }
  
  public HeaderElement[] getElements()
    throws ParseException
  {
    if (this.value != null) {
      return BasicHeaderValueParser.parseElements(this.value, null);
    }
    return new HeaderElement[0];
  }
  
  public Object clone()
    throws CloneNotSupportedException
  {
    return super.clone();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\message\BasicHeader.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */