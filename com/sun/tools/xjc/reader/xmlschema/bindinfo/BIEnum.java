package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;

@XmlRootElement(name="typesafeEnumClass")
public final class BIEnum
  extends AbstractDeclarationImpl
{
  @XmlAttribute(name="map")
  private boolean map;
  @XmlAttribute(name="name")
  public String className;
  @XmlAttribute(name="ref")
  public String ref;
  @XmlElement
  public final String javadoc;
  @XmlTransient
  public final Map<String, BIEnumMember> members;
  
  public boolean isMapped()
  {
    return this.map;
  }
  
  public BIEnum()
  {
    this.map = true;
    
    this.className = null;
    
    this.javadoc = null;
    
    this.members = new HashMap();
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public void setParent(BindInfo p)
  {
    super.setParent(p);
    for (BIEnumMember mem : this.members.values()) {
      mem.setParent(p);
    }
    if (this.ref != null) {
      markAsAcknowledged();
    }
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "enum");
  
  @XmlElement(name="typesafeEnumMember")
  private void setMembers(BIEnumMember2[] mems)
  {
    for (BIEnumMember2 e : mems) {
      this.members.put(e.value, e);
    }
  }
  
  static class BIEnumMember2
    extends BIEnumMember
  {
    @XmlAttribute(required=true)
    String value;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIEnum.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */