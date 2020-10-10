package com.sun.xml.xsom.impl.parser;

import org.xml.sax.SAXException;

public abstract interface Patch
{
  public abstract void run()
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\xml\xsom\impl\parser\Patch.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */