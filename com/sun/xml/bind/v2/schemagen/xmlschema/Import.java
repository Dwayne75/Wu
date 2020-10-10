package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("import")
public abstract interface Import
  extends Annotated, TypedXmlWriter
{
  @XmlAttribute
  public abstract Import namespace(String paramString);
  
  @XmlAttribute
  public abstract Import schemaLocation(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Import.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */