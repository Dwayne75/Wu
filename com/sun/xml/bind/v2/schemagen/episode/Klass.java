package com.sun.xml.bind.v2.schemagen.episode;

import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.txw2.annotation.XmlAttribute;

public abstract interface Klass
  extends TypedXmlWriter
{
  @XmlAttribute
  public abstract void ref(String paramString);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\episode\Klass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */