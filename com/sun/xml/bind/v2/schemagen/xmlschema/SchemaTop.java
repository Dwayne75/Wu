package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

public abstract interface SchemaTop
  extends Redefinable, TypedXmlWriter
{
  @XmlElement
  public abstract TopLevelAttribute attribute();
  
  @XmlElement
  public abstract TopLevelElement element();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\SchemaTop.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */