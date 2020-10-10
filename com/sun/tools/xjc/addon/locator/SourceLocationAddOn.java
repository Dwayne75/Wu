package com.sun.tools.xjc.addon.locator;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.bind.Locatable;
import com.sun.xml.bind.annotation.XmlLocation;
import java.io.IOException;
import javax.xml.bind.annotation.XmlTransient;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;

public class SourceLocationAddOn
  extends Plugin
{
  private static final String fieldName = "locator";
  
  public String getOptionName()
  {
    return "Xlocator";
  }
  
  public String getUsage()
  {
    return "  -Xlocator          :  enable source location support for generated code";
  }
  
  public int parseArgument(Options opt, String[] args, int i)
    throws BadCommandLineException, IOException
  {
    return 0;
  }
  
  public boolean run(Outline outline, Options opt, ErrorHandler errorHandler)
  {
    for (ClassOutline ci : outline.getClasses())
    {
      JDefinedClass impl = ci.implClass;
      if (ci.getSuperClass() == null)
      {
        JVar $loc = impl.field(2, Locator.class, "locator");
        $loc.annotate(XmlLocation.class);
        $loc.annotate(XmlTransient.class);
        
        impl._implements(Locatable.class);
        
        impl.method(1, Locator.class, "sourceLocation").body()._return($loc);
        
        JMethod setter = impl.method(1, Void.TYPE, "setSourceLocation");
        JVar $newLoc = setter.param(Locator.class, "newLocator");
        setter.body().assign($loc, $newLoc);
      }
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\addon\locator\SourceLocationAddOn.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */