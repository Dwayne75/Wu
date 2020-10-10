package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement("list")
public abstract interface List
  extends Annotated, SimpleTypeHost, TypedXmlWriter
{
  @XmlAttribute
  public abstract List itemType(QName paramQName);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\List.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */