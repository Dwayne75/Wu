package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

@XmlRootElement(name="typesafeEnumMember")
public class BIEnumMember
  extends AbstractDeclarationImpl
{
  @XmlAttribute
  public final String name;
  @XmlElement
  public final String javadoc;
  
  protected BIEnumMember()
  {
    this.name = null;
    this.javadoc = null;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "typesafeEnumMember");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIEnumMember.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */