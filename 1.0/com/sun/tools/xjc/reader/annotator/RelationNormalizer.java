package com.sun.tools.xjc.reader.annotator;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.IgnoreItem;
import com.sun.tools.xjc.grammar.InterfaceItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.grammar.util.FieldMultiplicityCounter;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.tools.xjc.reader.TypeUtil;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.HashSet;
import java.util.Set;
import org.xml.sax.Locator;

public final class RelationNormalizer
{
  private final AnnotatorController controller;
  private final ExpressionPool pool;
  private final AnnotatedGrammar grammar;
  static Class class$java$lang$Object;
  
  private RelationNormalizer(AnnotatorController _controller, AnnotatedGrammar _grammar)
  {
    this.controller = _controller;
    this.pool = _grammar.getPool();
    this.grammar = _grammar;
  }
  
  public static void normalize(AnnotatedGrammar grammar, AnnotatorController controller)
  {
    RelationNormalizer n = new RelationNormalizer(controller, grammar);
    ClassItem[] classItems = grammar.getClasses();
    InterfaceItem[] interfaceItems = grammar.getInterfaces(); RelationNormalizer 
    
      tmp26_25 = n;tmp26_25.getClass();RelationNormalizer.Pass1 pass1 = new RelationNormalizer.Pass1(tmp26_25, null);
    for (int i = 0; i < classItems.length; i++) {
      pass1.process(classItems[i]);
    }
    for (int i = 0; i < interfaceItems.length; i++) {
      interfaceItems[i].visit(pass1);
    }
    for (int i = 0; i < classItems.length; i++)
    {
      FieldUse[] fieldUses = classItems[i].getDeclaredFieldUses();
      for (int j = 0; j < fieldUses.length; j++)
      {
        fieldUses[j].multiplicity = FieldMultiplicityCounter.count(classItems[i].exp, fieldUses[j]);
        
        Set possibleTypes = new HashSet();
        FieldItem[] fields = (FieldItem[])fieldUses[j].items.toArray(new FieldItem[0]);
        for (int k = 0; k < fields.length; k++) {
          possibleTypes.add(fields[k].getType(grammar.codeModel));
        }
        fieldUses[j].type = TypeUtil.getCommonBaseType(grammar.codeModel, possibleTypes);
        if ((fieldUses[j].isDelegated()) && (!fieldUses[j].multiplicity.isAtMostOnce())) {
          controller.reportError(new Locator[] { classItems[i].locator }, Messages.format("Normalizer.DelegationMultiplicityMustBe1", fieldUses[j].name));
        }
      }
    }
  }
  
  static Class class$(String x0)
  {
    try
    {
      return Class.forName(x0);
    }
    catch (ClassNotFoundException x1)
    {
      throw new NoClassDefFoundError(x1.getMessage());
    }
  }
  
  private static void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
  
  private static boolean isInterface(JType t)
  {
    if (t.isPrimitive()) {
      return false;
    }
    return ((JClass)t).isInterface();
  }
  
  private static boolean isClass(Object exp)
  {
    return exp instanceof ClassItem;
  }
  
  private static boolean isSuperClass(Object exp)
  {
    return exp instanceof SuperClassItem;
  }
  
  private static boolean isInterface(Object exp)
  {
    return exp instanceof InterfaceItem;
  }
  
  private static boolean isField(Object exp)
  {
    return exp instanceof FieldItem;
  }
  
  private static boolean isPrimitive(Object exp)
  {
    return exp instanceof PrimitiveItem;
  }
  
  private static boolean isIgnore(Object exp)
  {
    return exp instanceof IgnoreItem;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\RelationNormalizer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */