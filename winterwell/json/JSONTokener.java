package winterwell.json;

public class JSONTokener
{
  private int myIndex;
  private String mySource;
  
  public JSONTokener(String s)
  {
    this.myIndex = 0;
    this.mySource = s;
  }
  
  public void back()
  {
    if (this.myIndex > 0) {
      this.myIndex -= 1;
    }
  }
  
  public static int dehexchar(char c)
  {
    if ((c >= '0') && (c <= '9')) {
      return c - '0';
    }
    if ((c >= 'A') && (c <= 'F')) {
      return c - '7';
    }
    if ((c >= 'a') && (c <= 'f')) {
      return c - 'W';
    }
    return -1;
  }
  
  public boolean more()
  {
    return this.myIndex < this.mySource.length();
  }
  
  public char next()
  {
    if (more())
    {
      char c = this.mySource.charAt(this.myIndex);
      this.myIndex += 1;
      return c;
    }
    return '\000';
  }
  
  public char next(char c)
    throws JSONException
  {
    char n = next();
    if (n != c) {
      throw syntaxError("Expected '" + c + "' and instead saw '" + 
        n + "'");
    }
    return n;
  }
  
  public String next(int n)
    throws JSONException
  {
    int i = this.myIndex;
    int j = i + n;
    if (j >= this.mySource.length()) {
      throw syntaxError("Substring bounds error");
    }
    this.myIndex += n;
    return this.mySource.substring(i, j);
  }
  
  public char nextClean()
    throws JSONException
  {
    char c;
    do
    {
      for (;;)
      {
        c = next();
        if (c == '/')
        {
          switch (next())
          {
          case '/': 
            do
            {
              c = next();
              if ((c == '\n') || (c == '\r')) {
                break;
              }
            } while (c != 0);
            break;
          case '*': 
            for (;;)
            {
              c = next();
              if (c == 0) {
                throw syntaxError("Unclosed comment");
              }
              if (c == '*')
              {
                if (next() == '/') {
                  break;
                }
                back();
              }
            }
          default: 
            back();
            return '/';
            
            break;
          }
        }
        else
        {
          if (c != '#') {
            break;
          }
          do
          {
            c = next();
            if ((c == '\n') || (c == '\r')) {
              break;
            }
          } while (c != 0);
        }
      }
    } while ((c != 0) && (c <= ' '));
    return c;
  }
  
  public String nextString(char quote)
    throws JSONException
  {
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      char c = next();
      switch (c)
      {
      case '\000': 
      case '\n': 
      case '\r': 
        throw syntaxError("Unterminated string");
      case '\\': 
        c = next();
        switch (c)
        {
        case 'b': 
          sb.append('\b');
          break;
        case 't': 
          sb.append('\t');
          break;
        case 'n': 
          sb.append('\n');
          break;
        case 'f': 
          sb.append('\f');
          break;
        case 'r': 
          sb.append('\r');
          break;
        case 'u': 
          sb.append((char)Integer.parseInt(next(4), 16));
          break;
        case 'x': 
          sb.append((char)Integer.parseInt(next(2), 16));
          break;
        default: 
          sb.append(c);
        }
        break;
      default: 
        if (c == quote) {
          return sb.toString();
        }
        sb.append(c);
      }
    }
  }
  
  public String nextTo(char d)
  {
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      char c = next();
      if ((c == d) || (c == 0) || (c == '\n') || (c == '\r'))
      {
        if (c != 0) {
          back();
        }
        return sb.toString().trim();
      }
      sb.append(c);
    }
  }
  
  public String nextTo(String delimiters)
  {
    StringBuffer sb = new StringBuffer();
    for (;;)
    {
      char c = next();
      if ((delimiters.indexOf(c) >= 0) || (c == 0) || 
        (c == '\n') || (c == '\r'))
      {
        if (c != 0) {
          back();
        }
        return sb.toString().trim();
      }
      sb.append(c);
    }
  }
  
  public Object nextValue()
    throws JSONException
  {
    char c = nextClean();
    switch (c)
    {
    case '"': 
    case '\'': 
      return nextString(c);
    case '{': 
      back();
      return new JSONObject(this);
    case '[': 
      back();
      return new JSONArray(this);
    }
    StringBuffer sb = new StringBuffer();
    char b = c;
    while ((c >= ' ') && (",:]}/\\\"[{;=#".indexOf(c) < 0))
    {
      sb.append(c);
      c = next();
    }
    back();
    
    String s = sb.toString().trim();
    if (s.equals("")) {
      throw syntaxError("Missing value");
    }
    if (s.equalsIgnoreCase("true")) {
      return Boolean.TRUE;
    }
    if (s.equalsIgnoreCase("false")) {
      return Boolean.FALSE;
    }
    if (s.equalsIgnoreCase("null")) {
      return JSONObject.NULL;
    }
    if (((b >= '0') && (b <= '9')) || (b == '.') || (b == '-') || (b == '+'))
    {
      if (b == '0') {
        if ((s.length() > 2) && (
          (s.charAt(1) == 'x') || (s.charAt(1) == 'X'))) {
          try
          {
            return new Integer(Integer.parseInt(s.substring(2), 
              16));
          }
          catch (Exception localException1) {}
        } else {
          try
          {
            return new Integer(Integer.parseInt(s, 8));
          }
          catch (Exception localException2) {}
        }
      }
      try
      {
        return new Integer(s);
      }
      catch (Exception e)
      {
        try
        {
          return new Long(s);
        }
        catch (Exception f)
        {
          try
          {
            return new Double(s);
          }
          catch (Exception g)
          {
            return s;
          }
        }
      }
    }
    return s;
  }
  
  public char skipTo(char to)
  {
    int index = this.myIndex;
    char c;
    do
    {
      c = next();
      if (c == 0)
      {
        this.myIndex = index;
        return c;
      }
    } while (c != to);
    back();
    return c;
  }
  
  public boolean skipPast(String to)
  {
    this.myIndex = this.mySource.indexOf(to, this.myIndex);
    if (this.myIndex < 0)
    {
      this.myIndex = this.mySource.length();
      return false;
    }
    this.myIndex += to.length();
    return true;
  }
  
  public JSONException syntaxError(String message)
  {
    return new JSONException(message + toString());
  }
  
  public String toString()
  {
    return " at character " + this.myIndex + " of " + this.mySource;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\winterwell\json\JSONTokener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */