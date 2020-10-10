package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("annotation")
public abstract interface Annotation
  extends TypedXmlWriter
{
  @XmlElement
  public abstract Appinfo appinfo();
  
  @XmlElement
  public abstract Documentation documentation();
  
  @XmlAttribute
  public abstract Annotation id(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Annotation.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */