package com.sun.tools.xjc.reader.xmlschema;

import com.sun.codemodel.JDefinedClass;
import org.xml.sax.Locator;

public abstract interface JClassFactory
{
  public abstract JDefinedClass create(String paramString, Locator paramLocator);
  
  public abstract JClassFactory getParentFactory();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\JClassFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */