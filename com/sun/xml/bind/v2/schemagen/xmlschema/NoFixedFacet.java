package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

public abstract interface NoFixedFacet
  extends Annotated, TypedXmlWriter
{
  @XmlAttribute
  public abstract NoFixedFacet value(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\NoFixedFacet.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */