package com.sun.tools.xjc;

import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import java.io.IOException;
import org.xml.sax.ErrorHandler;

public abstract interface CodeAugmenter
{
  public abstract String getOptionName();
  
  public abstract String getUsage();
  
  public abstract int parseArgument(Options paramOptions, String[] paramArrayOfString, int paramInt)
    throws BadCommandLineException, IOException;
  
  public abstract boolean run(AnnotatedGrammar paramAnnotatedGrammar, GeneratorContext paramGeneratorContext, Options paramOptions, ErrorHandler paramErrorHandler);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\CodeAugmenter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */