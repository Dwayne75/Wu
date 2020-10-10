package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("simpleContent")
public abstract interface SimpleContent
  extends Annotated, TypedXmlWriter
{
  @XmlElement
  public abstract SimpleExtension extension();
  
  @XmlElement
  public abstract SimpleRestriction restriction();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\SimpleContent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */