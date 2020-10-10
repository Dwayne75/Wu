package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.istack.Nullable;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import com.sun.xml.bind.api.impl.NameConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;

@XmlRootElement(name="class")
public final class BIClass
  extends AbstractDeclarationImpl
{
  @XmlAttribute(name="name")
  private String className;
  @XmlAttribute(name="implClass")
  private String userSpecifiedImplClass;
  @XmlAttribute(name="ref")
  private String ref;
  @XmlElement
  private String javadoc;
  
  @Nullable
  public String getClassName()
  {
    if (this.className == null) {
      return null;
    }
    BIGlobalBinding gb = getBuilder().getGlobalBinding();
    NameConverter nc = getBuilder().model.getNameConverter();
    if (gb.isJavaNamingConventionEnabled()) {
      return nc.toClassName(this.className);
    }
    return this.className;
  }
  
  public String getUserSpecifiedImplClass()
  {
    return this.userSpecifiedImplClass;
  }
  
  public String getExistingClassRef()
  {
    return this.ref;
  }
  
  public String getJavadoc()
  {
    return this.javadoc;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public void setParent(BindInfo p)
  {
    super.setParent(p);
    if (this.ref != null) {
      markAsAcknowledged();
    }
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "class");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIClass.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */