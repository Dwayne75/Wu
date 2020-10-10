package com.sun.tools.xjc.reader.annotator;

import com.sun.codemodel.JDefinedClass;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.util.Util;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

class FieldItemAnnotation
{
  private static PrintStream debug = Util.getSystemProperty(FieldItemAnnotation.class, "debug") != null ? System.err : null;
  private final AnnotatorController controller;
  
  public static void annotate(AnnotatedGrammar g, AnnotatorController controller)
  {
    FieldItemAnnotation ann = new FieldItemAnnotation(controller);
    
    ClassItem[] classes = g.getClasses();
    for (int i = 0; i < classes.length; i++)
    {
      if (debug != null) {
        debug.println(" adding field item for " + classes[i].getTypeAsDefined().name());
      }
      FieldItemAnnotation tmp80_79 = ann;tmp80_79.getClass();classes[i].exp = classes[i].exp.visit(new FieldItemAnnotation.Annotator(tmp80_79, g, classes[i], null));
    }
  }
  
  private FieldItemAnnotation(AnnotatorController _controller)
  {
    this.controller = _controller;
  }
  
  private final Map annotatedRefs = new HashMap();
  
  private static void _assert(boolean b)
  {
    if (!b) {
      throw new Error();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\FieldItemAnnotation.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */