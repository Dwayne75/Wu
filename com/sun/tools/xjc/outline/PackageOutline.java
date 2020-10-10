package com.sun.tools.xjc.outline;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.tools.xjc.generator.bean.ObjectFactoryGenerator;
import java.util.Set;
import javax.xml.bind.annotation.XmlNsForm;

public abstract interface PackageOutline
{
  public abstract JPackage _package();
  
  public abstract JDefinedClass objectFactory();
  
  public abstract ObjectFactoryGenerator objectFactoryGenerator();
  
  public abstract Set<? extends ClassOutline> getClasses();
  
  public abstract String getMostUsedNamespaceURI();
  
  public abstract XmlNsForm getElementFormDefault();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\outline\PackageOutline.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */