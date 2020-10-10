package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

public abstract interface ComplexTypeModel
  extends AttrDecls, TypeDefParticle, TypedXmlWriter
{
  @XmlElement
  public abstract SimpleContent simpleContent();
  
  @XmlElement
  public abstract ComplexContent complexContent();
  
  @XmlAttribute
  public abstract ComplexTypeModel mixed(boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\ComplexTypeModel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */