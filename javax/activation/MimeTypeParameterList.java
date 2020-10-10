package javax.activation;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

public class MimeTypeParameterList
{
  private Hashtable parameters;
  private static final String TSPECIALS = "()<>@,;:/[]?=\\\"";
  
  public MimeTypeParameterList()
  {
    this.parameters = new Hashtable();
  }
  
  public MimeTypeParameterList(String parameterList)
    throws MimeTypeParseException
  {
    this.parameters = new Hashtable();
    
    parse(parameterList);
  }
  
  protected void parse(String parameterList)
    throws MimeTypeParseException
  {
    if (parameterList == null) {
      return;
    }
    int length = parameterList.length();
    if (length <= 0) {
      return;
    }
    char c;
    for (int i = skipWhiteSpace(parameterList, 0); (i < length) && ((c = parameterList.charAt(i)) == ';'); i = skipWhiteSpace(parameterList, i))
    {
      i++;
      
      i = skipWhiteSpace(parameterList, i);
      if (i >= length) {
        return;
      }
      int lastIndex = i;
      while ((i < length) && (isTokenChar(parameterList.charAt(i)))) {
        i++;
      }
      String name = parameterList.substring(lastIndex, i).toLowerCase(Locale.ENGLISH);
      
      i = skipWhiteSpace(parameterList, i);
      if ((i >= length) || (parameterList.charAt(i) != '=')) {
        throw new MimeTypeParseException("Couldn't find the '=' that separates a parameter name from its value.");
      }
      i++;
      i = skipWhiteSpace(parameterList, i);
      if (i >= length) {
        throw new MimeTypeParseException("Couldn't find a value for parameter named " + name);
      }
      c = parameterList.charAt(i);
      if (c == '"')
      {
        i++;
        if (i >= length) {
          throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
        }
        lastIndex = i;
        while (i < length)
        {
          c = parameterList.charAt(i);
          if (c == '"') {
            break;
          }
          if (c == '\\') {
            i++;
          }
          i++;
        }
        if (c != '"') {
          throw new MimeTypeParseException("Encountered unterminated quoted parameter value.");
        }
        String value = unquote(parameterList.substring(lastIndex, i));
        
        i++;
      }
      else
      {
        String value;
        if (isTokenChar(c))
        {
          lastIndex = i;
          while ((i < length) && (isTokenChar(parameterList.charAt(i)))) {
            i++;
          }
          value = parameterList.substring(lastIndex, i);
        }
        else
        {
          throw new MimeTypeParseException("Unexpected character encountered at index " + i);
        }
      }
      String value;
      this.parameters.put(name, value);
    }
    if (i < length) {
      throw new MimeTypeParseException("More characters encountered in input than expected.");
    }
  }
  
  public int size()
  {
    return this.parameters.size();
  }
  
  public boolean isEmpty()
  {
    return this.parameters.isEmpty();
  }
  
  public String get(String name)
  {
    return (String)this.parameters.get(name.trim().toLowerCase(Locale.ENGLISH));
  }
  
  public void set(String name, String value)
  {
    this.parameters.put(name.trim().toLowerCase(Locale.ENGLISH), value);
  }
  
  public void remove(String name)
  {
    this.parameters.remove(name.trim().toLowerCase(Locale.ENGLISH));
  }
  
  public Enumeration getNames()
  {
    return this.parameters.keys();
  }
  
  public String toString()
  {
    StringBuffer buffer = new StringBuffer();
    buffer.ensureCapacity(this.parameters.size() * 16);
    
    Enumeration keys = this.parameters.keys();
    while (keys.hasMoreElements())
    {
      String key = (String)keys.nextElement();
      buffer.append("; ");
      buffer.append(key);
      buffer.append('=');
      buffer.append(quote((String)this.parameters.get(key)));
    }
    return buffer.toString();
  }
  
  private static boolean isTokenChar(char c)
  {
    return (c > ' ') && (c < '') && ("()<>@,;:/[]?=\\\"".indexOf(c) < 0);
  }
  
  private static int skipWhiteSpace(String rawdata, int i)
  {
    int length = rawdata.length();
    while ((i < length) && (Character.isWhitespace(rawdata.charAt(i)))) {
      i++;
    }
    return i;
  }
  
  private static String quote(String value)
  {
    boolean needsQuotes = false;
    
    int length = value.length();
    for (int i = 0; (i < length) && (!needsQuotes); i++) {
      needsQuotes = !isTokenChar(value.charAt(i));
    }
    if (needsQuotes)
    {
      StringBuffer buffer = new StringBuffer();
      buffer.ensureCapacity((int)(length * 1.5D));
      
      buffer.append('"');
      for (int i = 0; i < length; i++)
      {
        char c = value.charAt(i);
        if ((c == '\\') || (c == '"')) {
          buffer.append('\\');
        }
        buffer.append(c);
      }
      buffer.append('"');
      
      return buffer.toString();
    }
    return value;
  }
  
  private static String unquote(String value)
  {
    int valueLength = value.length();
    StringBuffer buffer = new StringBuffer();
    buffer.ensureCapacity(valueLength);
    
    boolean escaped = false;
    for (int i = 0; i < valueLength; i++)
    {
      char currentChar = value.charAt(i);
      if ((!escaped) && (currentChar != '\\'))
      {
        buffer.append(currentChar);
      }
      else if (escaped)
      {
        buffer.append(currentChar);
        escaped = false;
      }
      else
      {
        escaped = true;
      }
    }
    return buffer.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\activation\MimeTypeParameterList.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */