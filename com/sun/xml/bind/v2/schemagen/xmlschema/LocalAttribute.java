package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement("attribute")
public abstract interface LocalAttribute
  extends Annotated, AttributeType, FixedOrDefault, TypedXmlWriter
{
  @XmlAttribute
  public abstract LocalAttribute form(String paramString);
  
  @XmlAttribute
  public abstract LocalAttribute name(String paramString);
  
  @XmlAttribute
  public abstract LocalAttribute ref(QName paramQName);
  
  @XmlAttribute
  public abstract LocalAttribute use(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\LocalAttribute.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */