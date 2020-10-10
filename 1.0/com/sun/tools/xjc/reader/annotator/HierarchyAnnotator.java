package com.sun.tools.xjc.reader.annotator;

import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.util.Util;
import java.io.PrintStream;

public class HierarchyAnnotator
{
  private static PrintStream debug = Util.getSystemProperty(HierarchyAnnotator.class, "debug") != null ? System.out : null;
  
  public static void annotate(AnnotatedGrammar grammar, AnnotatorController controller)
  {
    BGMWalker annotator = new HierarchyAnnotator.1(controller);
    
    ClassItem[] cs = grammar.getClasses();
    for (int i = 0; i < cs.length; i++) {
      cs[i].visit(annotator);
    }
    InterfaceItem[] is = grammar.getInterfaces();
    for (int i = 0; i < is.length; i++) {
      is[i].visit(annotator);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\HierarchyAnnotator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */