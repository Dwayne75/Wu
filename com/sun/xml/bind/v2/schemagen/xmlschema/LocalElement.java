package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement("element")
public abstract interface LocalElement
  extends Element, Occurs, TypedXmlWriter
{
  @XmlAttribute
  public abstract LocalElement form(String paramString);
  
  @XmlAttribute
  public abstract LocalElement name(String paramString);
  
  @XmlAttribute
  public abstract LocalElement ref(QName paramQName);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\LocalElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */