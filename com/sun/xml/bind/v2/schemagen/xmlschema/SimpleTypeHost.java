package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

public abstract interface SimpleTypeHost
  extends TypeHost, TypedXmlWriter
{
  @XmlElement
  public abstract SimpleType simpleType();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\SimpleTypeHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */