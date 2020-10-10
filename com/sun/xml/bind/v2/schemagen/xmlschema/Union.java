package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement("union")
public abstract interface Union
  extends Annotated, SimpleTypeHost, TypedXmlWriter
{
  @XmlAttribute
  public abstract Union memberTypes(QName[] paramArrayOfQName);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Union.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */