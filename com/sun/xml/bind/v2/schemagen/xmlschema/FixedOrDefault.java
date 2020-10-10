package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

public abstract interface FixedOrDefault
  extends TypedXmlWriter
{
  @XmlAttribute("default")
  public abstract FixedOrDefault _default(String paramString);
  
  @XmlAttribute
  public abstract FixedOrDefault fixed(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\FixedOrDefault.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */