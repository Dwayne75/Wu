package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("simpleType")
public abstract interface SimpleType
  extends Annotated, SimpleDerivation, TypedXmlWriter
{
  @XmlAttribute("final")
  public abstract SimpleType _final(String paramString);
  
  @XmlAttribute("final")
  public abstract SimpleType _final(String[] paramArrayOfString);
  
  @XmlAttribute
  public abstract SimpleType name(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\SimpleType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */