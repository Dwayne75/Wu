package com.sun.tools.xjc.addon.elementwrapper;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.CodeAugmenter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.runtime.ElementWrapper;
import java.io.IOException;
import org.xml.sax.ErrorHandler;

public class ElementWrapperAddOn
  implements CodeAugmenter
{
  public String getOptionName()
  {
    return "Xelement-wrapper";
  }
  
  public String getUsage()
  {
    return "  -Xelement-wrapper  :  generates the general purpose element wrapper into impl.runtime";
  }
  
  public int parseArgument(Options opt, String[] args, int i)
    throws BadCommandLineException, IOException
  {
    return 0;
  }
  
  public boolean run(AnnotatedGrammar grammar, GeneratorContext context, Options opt, ErrorHandler errorHandler)
  {
    context.getRuntime(ElementWrapper.class);
    
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\addon\elementwrapper\ElementWrapperAddOn.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */