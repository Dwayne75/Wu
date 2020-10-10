package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("documentation")
public abstract interface Documentation
  extends TypedXmlWriter
{
  @XmlAttribute
  public abstract Documentation source(String paramString);
  
  @XmlAttribute(ns="http://www.w3.org/XML/1998/namespace")
  public abstract Documentation lang(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Documentation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */