package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;

public class DumbEscapeHandler
  implements CharacterEscapeHandler
{
  public static final CharacterEscapeHandler theInstance = new DumbEscapeHandler();
  
  public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out)
    throws IOException
  {
    int limit = start + length;
    for (int i = start; i < limit; i++) {
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
        if (isAttVal) {
          out.write("&quot;");
        } else {
          out.write(34);
        }
        break;
      default: 
        if (ch[i] > '')
        {
          out.write("&#");
          out.write(Integer.toString(ch[i]));
          out.write(59);
        }
        else
        {
          out.write(ch[i]);
        }
        break;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\marshaller\DumbEscapeHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */