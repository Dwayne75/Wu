package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import com.sun.xml.txw2.annotation.XmlElement;
import javax.xml.namespace.QName;

public abstract interface SimpleRestrictionModel
  extends SimpleTypeHost, TypedXmlWriter
{
  @XmlAttribute
  public abstract SimpleRestrictionModel base(QName paramQName);
  
  @XmlElement
  public abstract NoFixedFacet enumeration();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\SimpleRestrictionModel.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */