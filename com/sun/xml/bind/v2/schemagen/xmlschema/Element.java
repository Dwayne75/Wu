package com.sun.xml.bind.v2.schemagen.xmlschema;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;
import javax.xml.namespace.QName;

public abstract interface Element
  extends Annotated, ComplexTypeHost, FixedOrDefault, SimpleTypeHost, TypedXmlWriter
{
  @XmlAttribute
  public abstract Element type(QName paramQName);
  
  @XmlAttribute
  public abstract Element block(String paramString);
  
  @XmlAttribute
  public abstract Element block(String[] paramArrayOfString);
  
  @XmlAttribute
  public abstract Element nillable(boolean paramBoolean);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\xmlschema\Element.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */