package com.sun.xml.xsom.impl.parser;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public abstract interface PatcherManager
{
  public abstract void addPatcher(Patch paramPatch);
  
  public abstract void addErrorChecker(Patch paramPatch);
  
  public abstract void reportError(String paramString, Locator paramLocator)
    throws SAXException;
  
  public static abstract interface Patcher
  {
    public abstract void run()
      throws SAXException;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\impl\parser\PatcherManager.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */