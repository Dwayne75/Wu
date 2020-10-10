package com.sun.xml.bind.v2.schemagen.episode;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("bindings")
public abstract interface Bindings
  extends TypedXmlWriter
{
  @XmlElement
  public abstract Bindings bindings();
  
  @XmlElement("class")
  public abstract Klass klass();
  
  @XmlElement
  public abstract SchemaBindings schemaBindings();
  
  @XmlAttribute
  public abstract void scd(String paramString);
  
  @XmlAttribute
  public abstract void version(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\episode\Bindings.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */