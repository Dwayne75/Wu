package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("complexContent")
public abstract interface ComplexContent
  extends Annotated, TypedXmlWriter
{
  @XmlElement
  public abstract ComplexExtension extension();
  
  @XmlElement
  public abstract ComplexRestriction restriction();
  
  @XmlAttribute
  public abstract ComplexContent mixed(boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\ComplexContent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */