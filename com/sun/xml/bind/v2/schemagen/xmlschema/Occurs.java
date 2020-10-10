package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

public abstract interface Occurs
  extends TypedXmlWriter
{
  @XmlAttribute
  public abstract Occurs minOccurs(int paramInt);
  
  @XmlAttribute
  public abstract Occurs maxOccurs(String paramString);
  
  @XmlAttribute
  public abstract Occurs maxOccurs(int paramInt);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Occurs.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */