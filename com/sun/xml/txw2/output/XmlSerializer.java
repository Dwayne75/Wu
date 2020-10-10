package com.sun.xml.txw2.output;

public abstract interface XmlSerializer
{
  public abstract void startDocument();
  
  public abstract void beginStartTag(String paramString1, String paramString2, String paramString3);
  
  public abstract void writeAttribute(String paramString1, String paramString2, String paramString3, StringBuilder paramStringBuilder);
  
  public abstract void writeXmlns(String paramString1, String paramString2);
  
  public abstract void endStartTag(String paramString1, String paramString2, String paramString3);
  
  public abstract void endTag();
  
  public abstract void text(StringBuilder paramStringBuilder);
  
  public abstract void cdata(StringBuilder paramStringBuilder);
  
  public abstract void comment(StringBuilder paramStringBuilder);
  
  public abstract void endDocument();
  
  public abstract void flush();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\output\XmlSerializer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */