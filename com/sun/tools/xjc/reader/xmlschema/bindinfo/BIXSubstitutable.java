package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

@XmlRootElement(name="substitutable", namespace="http://java.sun.com/xml/ns/jaxb/xjc")
public final class BIXSubstitutable
  extends AbstractDeclarationImpl
{
  public final QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb/xjc", "substitutable");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIXSubstitutable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */