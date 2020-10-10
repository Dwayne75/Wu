package com.sun.tools.xjc.reader.annotator;

import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import java.io.PrintStream;

class ChoiceAnnotator
{
  private static PrintStream debug = null;
  private final AnnotatedGrammar grammar;
  private final CodeModelClassFactory classFactory;
  
  public static void annotate(AnnotatedGrammar g, AnnotatorController _controller)
  {
    ChoiceAnnotator ann = new ChoiceAnnotator(g, _controller); ChoiceAnnotator 
    
      tmp16_15 = ann;tmp16_15.getClass();g.visit(new ChoiceAnnotator.Finder(tmp16_15, null));
  }
  
  private ChoiceAnnotator(AnnotatedGrammar g, AnnotatorController _controller)
  {
    this.grammar = g;
    this.classFactory = new CodeModelClassFactory(_controller.getErrorReceiver());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\ChoiceAnnotator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */