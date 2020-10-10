package com.sun.xml.txw2;

import javax.xml.namespace.QName;

public abstract interface TypedXmlWriter
{
  public abstract void commit();
  
  public abstract void commit(boolean paramBoolean);
  
  public abstract void block();
  
  public abstract Document getDocument();
  
  public abstract void _attribute(String paramString, Object paramObject);
  
  public abstract void _attribute(String paramString1, String paramString2, Object paramObject);
  
  public abstract void _attribute(QName paramQName, Object paramObject);
  
  public abstract void _namespace(String paramString);
  
  public abstract void _namespace(String paramString1, String paramString2);
  
  public abstract void _namespace(String paramString, boolean paramBoolean);
  
  public abstract void _pcdata(Object paramObject);
  
  public abstract void _cdata(Object paramObject);
  
  public abstract void _comment(Object paramObject)
    throws UnsupportedOperationException;
  
  public abstract <T extends TypedXmlWriter> T _element(String paramString, Class<T> paramClass);
  
  public abstract <T extends TypedXmlWriter> T _element(String paramString1, String paramString2, Class<T> paramClass);
  
  public abstract <T extends TypedXmlWriter> T _element(QName paramQName, Class<T> paramClass);
  
  public abstract <T extends TypedXmlWriter> T _element(Class<T> paramClass);
  
  public abstract <T extends TypedXmlWriter> T _cast(Class<T> paramClass);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\txw2\TypedXmlWriter.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */