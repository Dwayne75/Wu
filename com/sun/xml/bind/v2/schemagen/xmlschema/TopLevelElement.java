package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

@XmlElement("element")
public abstract interface TopLevelElement
  extends Element, TypedXmlWriter
{
  @XmlAttribute("final")
  public abstract TopLevelElement _final(String paramString);
  
  @XmlAttribute("final")
  public abstract TopLevelElement _final(String[] paramArrayOfString);
  
  @XmlAttribute("abstract")
  public abstract TopLevelElement _abstract(boolean paramBoolean);
  
  @XmlAttribute
  public abstract TopLevelElement substitutionGroup(QName paramQName);
  
  @XmlAttribute
  public abstract TopLevelElement name(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\TopLevelElement.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */