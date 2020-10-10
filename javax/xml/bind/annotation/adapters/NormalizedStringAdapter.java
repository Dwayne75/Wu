package javax.xml.bind.annotation.adapters;

public final class NormalizedStringAdapter
  extends XmlAdapter<String, String>
{
  public String unmarshal(String text)
  {
    if (text == null) {
      return null;
    }
    int i = text.length() - 1;
    while ((i >= 0) && (!isWhiteSpaceExceptSpace(text.charAt(i)))) {
      i--;
    }
    if (i < 0) {
      return text;
    }
    char[] buf = text.toCharArray();
    
    buf[(i--)] = ' ';
    for (; i >= 0; i--) {
      if (isWhiteSpaceExceptSpace(buf[i])) {
        buf[i] = ' ';
      }
    }
    return new String(buf);
  }
  
  public String marshal(String s)
  {
    return s;
  }
  
  protected static boolean isWhiteSpaceExceptSpace(char ch)
  {
    if (ch >= ' ') {
      return false;
    }
    return (ch == '\t') || (ch == '\n') || (ch == '\r');
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\xml\bind\annotation\adapters\NormalizedStringAdapter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */