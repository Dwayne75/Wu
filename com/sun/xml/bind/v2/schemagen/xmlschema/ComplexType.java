package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("complexType")
public abstract interface ComplexType
  extends Annotated, ComplexTypeModel, TypedXmlWriter
{
  @XmlAttribute("final")
  public abstract ComplexType _final(String paramString);
  
  @XmlAttribute("final")
  public abstract ComplexType _final(String[] paramArrayOfString);
  
  @XmlAttribute
  public abstract ComplexType block(String paramString);
  
  @XmlAttribute
  public abstract ComplexType block(String[] paramArrayOfString);
  
  @XmlAttribute("abstract")
  public abstract ComplexType _abstract(boolean paramBoolean);
  
  @XmlAttribute
  public abstract ComplexType name(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\ComplexType.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */