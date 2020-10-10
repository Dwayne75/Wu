package com.sun.tools.xjc;

import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.Outline;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

public abstract class Plugin
{
  public abstract String getOptionName();
  
  public abstract String getUsage();
  
  public int parseArgument(Options opt, String[] args, int i)
    throws BadCommandLineException, IOException
  {
    return 0;
  }
  
  public List<String> getCustomizationURIs()
  {
    return Collections.emptyList();
  }
  
  public boolean isCustomizationTagName(String nsUri, String localName)
  {
    return false;
  }
  
  public void onActivated(Options opts)
    throws BadCommandLineException
  {}
  
  public void postProcessModel(Model model, ErrorHandler errorHandler) {}
  
  public abstract boolean run(Outline paramOutline, Options paramOptions, ErrorHandler paramErrorHandler)
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\Plugin.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */