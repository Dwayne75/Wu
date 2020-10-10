package com.sun.xml.bind.marshaller;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class NioEscapeHandler
  implements CharacterEscapeHandler
{
  private final CharsetEncoder encoder;
  
  public NioEscapeHandler(String charsetName)
  {
    this.encoder = Charset.forName(charsetName).newEncoder();
  }
  
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
        if (this.encoder.canEncode(ch[i]))
        {
          out.write(ch[i]);
        }
        else
        {
          out.write("&#");
          out.write(Integer.toString(ch[i]));
          out.write(59);
        }
        break;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\marshaller\NioEscapeHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */