package org.seamless.xhtml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Option
{
  private String key;
  private String[] values;
  
  public Option(String key, String[] values)
  {
    this.key = key;
    this.values = values;
  }
  
  public static Option[] fromString(String string)
  {
    if ((string == null) || (string.length() == 0)) {
      return new Option[0];
    }
    List<Option> options = new ArrayList();
    try
    {
      String[] fields = string.split(";");
      for (String field : fields)
      {
        field = field.trim();
        if (field.contains(":"))
        {
          String[] keyValues = field.split(":");
          if (keyValues.length == 2)
          {
            String key = keyValues[0].trim();
            String[] values = keyValues[1].split(",");
            List<String> cleanValues = new ArrayList();
            for (String s : values)
            {
              String value = s.trim();
              if (value.length() > 0) {
                cleanValues.add(value);
              }
            }
            options.add(new Option(key, (String[])cleanValues.toArray(new String[cleanValues.size()])));
          }
        }
      }
      return (Option[])options.toArray(new Option[options.size()]);
    }
    catch (Exception ex)
    {
      throw new IllegalArgumentException("Can't parse options string: " + string, ex);
    }
  }
  
  public String getKey()
  {
    return this.key;
  }
  
  public String[] getValues()
  {
    return this.values;
  }
  
  public boolean isTrue()
  {
    return (getValues().length == 1) && (getValues()[0].toLowerCase().equals("true"));
  }
  
  public boolean isFalse()
  {
    return (getValues().length == 1) && (getValues()[0].toLowerCase().equals("false"));
  }
  
  public String getFirstValue()
  {
    return getValues()[0];
  }
  
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(getKey()).append(": ");
    Iterator<String> it = Arrays.asList(getValues()).iterator();
    while (it.hasNext())
    {
      sb.append((String)it.next());
      if (it.hasNext()) {
        sb.append(", ");
      }
    }
    return sb.toString();
  }
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    Option that = (Option)o;
    if (!this.key.equals(that.key)) {
      return false;
    }
    if (!Arrays.equals(this.values, that.values)) {
      return false;
    }
    return true;
  }
  
  public int hashCode()
  {
    int result = this.key.hashCode();
    result = 31 * result + Arrays.hashCode(this.values);
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xhtml\Option.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */