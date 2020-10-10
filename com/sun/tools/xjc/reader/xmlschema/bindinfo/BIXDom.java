package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="dom", namespace="http://java.sun.com/xml/ns/jaxb/xjc")
public class BIXDom
  extends BIDom
{
  @XmlAttribute
  String type = "w3c";
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIXDom.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */