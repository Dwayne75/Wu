package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;

public class MinimumEscapeHandler
  implements CharacterEscapeHandler
{
  public static final CharacterEscapeHandler theInstance = new MinimumEscapeHandler();
  
  public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out)
    throws IOException
  {
    int limit = start + length;
    for (int i = start; i < limit; i++)
    {
      char c = ch[i];
      if ((c == '&') || (c == '<') || (c == '>') || ((c == '"') && (isAttVal)))
      {
        if (i != start) {
          out.write(ch, start, i - start);
        }
        start = i + 1;
        switch (ch[i])
        {
        case '&': 
          out.write("&amp;");
          break;
        case '<': 
          out.write("&lt;");
          break;
        case '>': 
          out.write("&gt;");
          break;
        case '"': 
          out.write("&quot;");
        }
      }
    }
    if (start != limit) {
      out.write(ch, start, limit - start);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\marshaller\MinimumEscapeHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */