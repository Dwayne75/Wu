package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

public abstract interface Annotated
  extends TypedXmlWriter
{
  @XmlElement
  public abstract Annotation annotation();
  
  @XmlAttribute
  public abstract Annotated id(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Annotated.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */