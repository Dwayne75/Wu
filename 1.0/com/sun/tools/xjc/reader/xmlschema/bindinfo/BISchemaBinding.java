package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSType;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class BISchemaBinding
  extends AbstractDeclarationImpl
{
  private final BISchemaBinding.NamingRule typeNamingRule;
  private final BISchemaBinding.NamingRule elementNamingRule;
  private final BISchemaBinding.NamingRule attributeNamingRule;
  private final BISchemaBinding.NamingRule modelGroupNamingRule;
  private final BISchemaBinding.NamingRule anonymousTypeNamingRule;
  private String packageName;
  private final String javadoc;
  private static final BISchemaBinding.NamingRule defaultNamingRule = new BISchemaBinding.NamingRule("", "");
  
  public BISchemaBinding(String _packageName, String _javadoc, BISchemaBinding.NamingRule rType, BISchemaBinding.NamingRule rElement, BISchemaBinding.NamingRule rAttribute, BISchemaBinding.NamingRule rModelGroup, BISchemaBinding.NamingRule rAnonymousType, Locator _loc)
  {
    super(_loc);
    this.packageName = _packageName;
    this.javadoc = _javadoc;
    if (rType == null) {
      rType = defaultNamingRule;
    }
    if (rElement == null) {
      rElement = defaultNamingRule;
    }
    if (rAttribute == null) {
      rAttribute = defaultNamingRule;
    }
    if (rModelGroup == null) {
      rModelGroup = defaultNamingRule;
    }
    if (rAnonymousType == null) {
      rAnonymousType = new BISchemaBinding.NamingRule("", "Type");
    }
    this.typeNamingRule = rType;
    this.elementNamingRule = rElement;
    this.attributeNamingRule = rAttribute;
    this.modelGroupNamingRule = rModelGroup;
    this.anonymousTypeNamingRule = rAnonymousType;
    
    markAsAcknowledged();
  }
  
  public String mangleClassName(String name, XSComponent cmp)
  {
    if ((cmp instanceof XSType)) {
      return this.typeNamingRule.mangle(name);
    }
    if ((cmp instanceof XSElementDecl)) {
      return this.elementNamingRule.mangle(name);
    }
    if ((cmp instanceof XSAttributeDecl)) {
      return this.attributeNamingRule.mangle(name);
    }
    if (((cmp instanceof XSModelGroup)) || ((cmp instanceof XSModelGroupDecl))) {
      return this.modelGroupNamingRule.mangle(name);
    }
    return name;
  }
  
  public String mangleAnonymousTypeClassName(String name)
  {
    return this.anonymousTypeNamingRule.mangle(name);
  }
  
  public void setPackageName(String val)
  {
    this.packageName = val;
  }
  
  public String getPackageName()
  {
    return this.packageName;
  }
  
  public String getJavadoc()
  {
    return this.javadoc;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "schemaBinding");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BISchemaBinding.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */