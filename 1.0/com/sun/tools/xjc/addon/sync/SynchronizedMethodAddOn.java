package com.sun.tools.xjc.addon.sync;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMods;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.CodeAugmenter;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import java.io.IOException;
import java.util.Iterator;
import org.xml.sax.ErrorHandler;

public class SynchronizedMethodAddOn
  implements CodeAugmenter
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
  
  public boolean run(AnnotatedGrammar grammar, GeneratorContext context, Options opt, ErrorHandler errorHandler)
  {
    ClassItem[] cis = grammar.getClasses();
    for (int i = 0; i < cis.length; i++) {
      augument(context.getClassContext(cis[i]));
    }
    return true;
  }
  
  private void augument(ClassContext cc)
  {
    for (Iterator itr = cc.implClass.methods(); itr.hasNext();)
    {
      JMethod m = (JMethod)itr.next();
      m.getMods().setSynchronized(true);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\addon\sync\SynchronizedMethodAddOn.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */