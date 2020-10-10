package com.sun.xml.txw2.output;

import java.io.IOException;
import java.io.Writer;

public abstract interface CharacterEscapeHandler
{
  public abstract void escape(char[] paramArrayOfChar, int paramInt1, int paramInt2, boolean paramBoolean, Writer paramWriter)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\output\CharacterEscapeHandler.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */