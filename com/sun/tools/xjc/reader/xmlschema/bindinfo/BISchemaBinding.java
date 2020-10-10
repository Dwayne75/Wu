package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlRootElement(name="schemaBindings")
public final class BISchemaBinding
  extends AbstractDeclarationImpl
{
  @XmlElement
  private NameRules nameXmlTransform;
  @XmlElement(name="package")
  private PackageInfo packageInfo;
  @XmlAttribute(name="map")
  public boolean map;
  
  @XmlType(propOrder={})
  private static final class NameRules
  {
    @XmlElement
    BISchemaBinding.NamingRule typeName = BISchemaBinding.defaultNamingRule;
    @XmlElement
    BISchemaBinding.NamingRule elementName = BISchemaBinding.defaultNamingRule;
    @XmlElement
    BISchemaBinding.NamingRule attributeName = BISchemaBinding.defaultNamingRule;
    @XmlElement
    BISchemaBinding.NamingRule modelGroupName = BISchemaBinding.defaultNamingRule;
    @XmlElement
    BISchemaBinding.NamingRule anonymousTypeName = BISchemaBinding.defaultNamingRule;
  }
  
  public BISchemaBinding()
  {
    this.nameXmlTransform = new NameRules(null);
    
    this.packageInfo = new PackageInfo(null);
    
    this.map = true;
  }
  
  private static final NamingRule defaultNamingRule = new NamingRule("", "");
  
  private static final class PackageInfo
  {
    @XmlAttribute
    String name;
    @XmlElement
    String javadoc;
  }
  
  public static final class NamingRule
  {
    @XmlAttribute
    private String prefix = "";
    @XmlAttribute
    private String suffix = "";
    
    public NamingRule(String _prefix, String _suffix)
    {
      this.prefix = _prefix;
      this.suffix = _suffix;
    }
    
    public NamingRule() {}
    
    public String mangle(String originalName)
    {
      return this.prefix + originalName + this.suffix;
    }
  }
  
  public String mangleClassName(String name, XSComponent cmp)
  {
    if ((cmp instanceof XSType)) {
      return this.nameXmlTransform.typeName.mangle(name);
    }
    if ((cmp instanceof XSElementDecl)) {
      return this.nameXmlTransform.elementName.mangle(name);
    }
    if ((cmp instanceof XSAttributeDecl)) {
      return this.nameXmlTransform.attributeName.mangle(name);
    }
    if (((cmp instanceof XSModelGroup)) || ((cmp instanceof XSModelGroupDecl))) {
      return this.nameXmlTransform.modelGroupName.mangle(name);
    }
    return name;
  }
  
  public String mangleAnonymousTypeClassName(String name)
  {
    return this.nameXmlTransform.anonymousTypeName.mangle(name);
  }
  
  public String getPackageName()
  {
    return this.packageInfo.name;
  }
  
  public String getJavadoc()
  {
    return this.packageInfo.javadoc;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "schemaBinding");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\bindinfo\BISchemaBinding.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */