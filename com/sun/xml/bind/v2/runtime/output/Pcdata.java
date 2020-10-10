package com.sun.xml.bind.v2.runtime.output;

import java.io.IOException;

public abstract class Pcdata
  implements CharSequence
{
  public abstract void writeTo(UTF8XmlOutput paramUTF8XmlOutput)
    throws IOException;
  
  public void writeTo(char[] buf, int start)
  {
    toString().getChars(0, length(), buf, start);
  }
  
  public abstract String toString();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\runtime\output\Pcdata.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */