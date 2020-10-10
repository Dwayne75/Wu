package com.sun.tools.xjc.reader.xmlschema.bindinfo;

import com.sun.tools.xjc.reader.NameConverter;
import com.sun.tools.xjc.reader.xmlschema.BGMBuilder;
import javax.xml.namespace.QName;
import org.xml.sax.Locator;

public final class BIClass
  extends AbstractDeclarationImpl
{
  private final String className;
  private final String userSpecifiedImplClass;
  private final String javadoc;
  
  public BIClass(Locator loc, String _className, String _implClass, String _javadoc)
  {
    super(loc);
    this.className = _className;
    this.javadoc = _javadoc;
    this.userSpecifiedImplClass = _implClass;
  }
  
  public String getClassName()
  {
    if (this.className == null) {
      return null;
    }
    BIGlobalBinding gb = getBuilder().getGlobalBinding();
    if (gb.isJavaNamingConventionEnabled()) {
      return gb.getNameConverter().toClassName(this.className);
    }
    return this.className;
  }
  
  public String getUserSpecifiedImplClass()
  {
    return this.userSpecifiedImplClass;
  }
  
  public String getJavadoc()
  {
    return this.javadoc;
  }
  
  public QName getName()
  {
    return NAME;
  }
  
  public static final QName NAME = new QName("http://java.sun.com/xml/ns/jaxb", "class");
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\bindinfo\BIClass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */