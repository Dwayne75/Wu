package com.sun.tools.xjc.reader.annotator;

import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.reader.TypeUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SymbolSpaceTypeAssigner
{
  public static void assign(AnnotatedGrammar grammar, AnnotatorController controller)
  {
    Map applicableTypes = new HashMap();
    
    ClassItem[] classes = grammar.getClasses();
    for (int i = 0; i < classes.length; i++)
    {
      ClassItem ci = classes[i];
      ci.exp.visit(new SymbolSpaceTypeAssigner.1(applicableTypes, ci));
    }
    Iterator itr = applicableTypes.entrySet().iterator();
    while (itr.hasNext())
    {
      Map.Entry e = (Map.Entry)itr.next();
      
      ((SymbolSpace)e.getKey()).setType(TypeUtil.getCommonBaseType(grammar.codeModel, (JType[])((Set)e.getValue()).toArray(new JType[0])));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\SymbolSpaceTypeAssigner.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */