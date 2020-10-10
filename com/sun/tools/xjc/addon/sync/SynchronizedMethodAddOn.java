package com.sun.tools.xjc.addon.sync;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMods;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import java.io.IOException;
import org.xml.sax.ErrorHandler;

public class SynchronizedMethodAddOn
  extends Plugin
{
  public String getOptionName()
  {
    return "Xsync-methods";
  }
  
  public String getUsage()
  {
    return "  -Xsync-methods     :  generate accessor methods with the 'synchronized' keyword";
  }
  
  public int parseArgument(Options opt, String[] args, int i)
    throws BadCommandLineException, IOException
  {
    return 0;
  }
  
  public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
  {
    for (ClassOutline co : model.getClasses()) {
      augument(co);
    }
    return true;
  }
  
  private void augument(ClassOutline co)
  {
    for (JMethod m : co.implClass.methods()) {
      m.getMods().setSynchronized(true);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\addon\sync\SynchronizedMethodAddOn.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */