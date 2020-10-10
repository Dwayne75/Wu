package com.sun.tools.xjc.addon.code_injector;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CCustomizations;
import com.sun.tools.xjc.model.CPluginCustomization;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.DOMUtils;
import java.util.Collections;
import java.util.List;
import org.xml.sax.ErrorHandler;

public class PluginImpl
  extends Plugin
{
  public String getOptionName()
  {
    return "Xinject-code";
  }
  
  public List<String> getCustomizationURIs()
  {
    return Collections.singletonList("http://jaxb.dev.java.net/plugin/code-injector");
  }
  
  public boolean isCustomizationTagName(String nsUri, String localName)
  {
    return (nsUri.equals("http://jaxb.dev.java.net/plugin/code-injector")) && (localName.equals("code"));
  }
  
  public String getUsage()
  {
    return "  -Xinject-code      :  inject specified Java code fragments into the generated code";
  }
  
  public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
  {
    for (ClassOutline co : model.getClasses())
    {
      CPluginCustomization c = co.target.getCustomizations().find("http://jaxb.dev.java.net/plugin/code-injector", "code");
      if (c != null)
      {
        c.markAsAcknowledged();
        
        String codeFragment = DOMUtils.getElementText(c.element);
        
        co.implClass.direct(codeFragment);
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\addon\code_injector\PluginImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */