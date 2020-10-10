package com.sun.tools.jxc.gen.config;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract interface NGCCEventSource
{
  public abstract int replace(NGCCEventReceiver paramNGCCEventReceiver1, NGCCEventReceiver paramNGCCEventReceiver2);
  
  public abstract void sendEnterElement(int paramInt, String paramString1, String paramString2, String paramString3, Attributes paramAttributes)
    throws SAXException;
  
  public abstract void sendLeaveElement(int paramInt, String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void sendEnterAttribute(int paramInt, String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void sendLeaveAttribute(int paramInt, String paramString1, String paramString2, String paramString3)
    throws SAXException;
  
  public abstract void sendText(int paramInt, String paramString)
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\gen\config\NGCCEventSource.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */