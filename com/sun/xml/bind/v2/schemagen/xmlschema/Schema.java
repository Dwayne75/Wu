package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;

@XmlElement("schema")
public abstract interface Schema
  extends SchemaTop, TypedXmlWriter
{
  @XmlElement
  public abstract Annotation annotation();
  
  @XmlElement("import")
  public abstract Import _import();
  
  @XmlAttribute
  public abstract Schema targetNamespace(String paramString);
  
  @XmlAttribute(ns="http://www.w3.org/XML/1998/namespace")
  public abstract Schema lang(String paramString);
  
  @XmlAttribute
  public abstract Schema id(String paramString);
  
  @XmlAttribute
  public abstract Schema elementFormDefault(String paramString);
  
  @XmlAttribute
  public abstract Schema attributeFormDefault(String paramString);
  
  @XmlAttribute
  public abstract Schema blockDefault(String paramString);
  
  @XmlAttribute
  public abstract Schema blockDefault(String[] paramArrayOfString);
  
  @XmlAttribute
  public abstract Schema finalDefault(String paramString);
  
  @XmlAttribute
  public abstract Schema finalDefault(String[] paramArrayOfString);
  
  @XmlAttribute
  public abstract Schema version(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Schema.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */