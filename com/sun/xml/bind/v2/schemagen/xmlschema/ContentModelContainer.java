package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlElement;

public abstract interface ContentModelContainer
  extends TypedXmlWriter
{
  @XmlElement
  public abstract LocalElement element();
  
  @XmlElement
  public abstract Any any();
  
  @XmlElement
  public abstract ExplicitGroup all();
  
  @XmlElement
  public abstract ExplicitGroup sequence();
  
  @XmlElement
  public abstract ExplicitGroup choice();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\ContentModelContainer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */