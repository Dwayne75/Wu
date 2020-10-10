package com.sun.tools.xjc.reader.annotator;

import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassCandidateItem;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.util.Util;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class TemporaryClassItemRemover
{
  private static final PrintStream debug = Util.getSystemProperty(TemporaryClassItemRemover.class, "debug") != null ? System.out : null;
  
  public static void remove(AnnotatedGrammar grammar)
  {
    ClassItem[] items = grammar.getClasses();
    
    TemporaryClassItemRemover.Pass1 p1 = new TemporaryClassItemRemover.Pass1(null);
    
    grammar.visit(p1);
    for (int i = 0; i < items.length; i++) {
      p1.processIfUnvisited(items[i]);
    }
    Set cs = new HashSet(p1.allCandidates);
    cs.removeAll(p1.notRemovableClasses);
    if (debug != null)
    {
      Iterator itr = cs.iterator();
      while (itr.hasNext())
      {
        ClassCandidateItem ci = (ClassCandidateItem)itr.next();
        if (!p1.reachableClasses.contains(ci)) {
          debug.println(displayName(ci) + " : this is unreachable");
        }
      }
    }
    cs.retainAll(p1.reachableClasses);
    if (debug != null)
    {
      Iterator itr = cs.iterator();
      while (itr.hasNext())
      {
        ClassCandidateItem ci = (ClassCandidateItem)itr.next();
        debug.println(" " + displayName(ci) + " will be removed");
      }
    }
    TemporaryClassItemRemover.Pass2 p2 = new TemporaryClassItemRemover.Pass2(grammar.getPool(), cs);
    
    grammar.visit(p2);
    for (int i = 0; i < items.length; i++) {
      items[i].visit(p2);
    }
  }
  
  private static final String displayName(ClassCandidateItem cci)
  {
    return cci.name + '@' + Integer.toHexString(cci.hashCode());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\TemporaryClassItemRemover.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */