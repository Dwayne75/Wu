package com.sun.tools.xjc.reader.xmlschema.bindinfo.parser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract interface NGCCEventReceiver
{
  public abstract void enterElement(String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException;
  
  public abstract void leaveElement(String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void text(String paramString)
    throws SAXException;
  
  public abstract void enterAttribute(String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void leaveAttribute(String paramString1, String paramString2, String paramString3)
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\parser\NGCCEventReceiver.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */