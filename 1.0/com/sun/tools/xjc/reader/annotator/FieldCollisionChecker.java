package com.sun.tools.xjc.reader.annotator;

import com.sun.msv.grammar.BinaryExp;
import com.sun.msv.grammar.ChoiceExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.InterleaveExp;
import com.sun.msv.grammar.OtherExp;
import com.sun.msv.grammar.SequenceExp;
import com.sun.msv.grammar.xmlschema.OccurrenceExp;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.BGMWalker;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.SuperClassItem;
import com.sun.tools.xjc.util.SubList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xml.sax.Locator;

public class FieldCollisionChecker
  extends BGMWalker
{
  private final AnnotatorController controller;
  
  public static void check(AnnotatedGrammar grammar, AnnotatorController controller)
  {
    FieldCollisionChecker checker = new FieldCollisionChecker(controller);
    
    Set baseClasses = new HashSet();
    ClassItem[] cls = grammar.getClasses();
    for (int i = 0; i < cls.length; i++) {
      baseClasses.add(cls[i].getSuperClass());
    }
    for (int i = 0; i < cls.length; i++) {
      if (!baseClasses.contains(cls[i]))
      {
        checker.reset();
        cls[i].visit(checker);
      }
    }
  }
  
  private FieldCollisionChecker(AnnotatorController _controller)
  {
    this.controller = _controller;
  }
  
  private List fields = new ArrayList();
  private final Map class2fields = new HashMap();
  private int sl;
  private int sr;
  
  private void reset()
  {
    this.fields = new ArrayList();
    this.sl = (this.sr = -1);
  }
  
  public void onInterleave(InterleaveExp exp)
  {
    check(exp);
  }
  
  public void onSequence(SequenceExp exp)
  {
    check(exp);
  }
  
  private void check(BinaryExp exp)
  {
    int l = this.fields.size();
    exp.exp1.visit(this);
    int r = this.fields.size();
    exp.exp2.visit(this);
    
    compare(l, r, r, this.fields.size());
  }
  
  public void onChoice(ChoiceExp exp)
  {
    int l = this.fields.size();
    exp.exp1.visit(this);
    int r = this.fields.size();
    exp.exp2.visit(this);
    if ((l <= this.sl) && (this.sr <= r)) {
      compare(this.sl, this.sr, r, this.fields.size());
    } else if ((r <= this.sl) && (this.sr <= this.fields.size())) {
      compare(l, r, this.sl, this.sr);
    }
  }
  
  public Object onSuper(SuperClassItem sci)
  {
    this.sl = this.fields.size();
    
    sci.definition.visit(this);
    
    this.sr = this.fields.size();
    
    return null;
  }
  
  public Object onField(FieldItem item)
  {
    this.fields.add(item);
    if (item.name.equals("Class")) {
      error(item.locator, "FieldCollisionChecker.ReservedWordCollision", item.name);
    }
    return null;
  }
  
  public Object onClass(ClassItem item)
  {
    List subList = (List)this.class2fields.get(item);
    if (subList == null)
    {
      int s = this.fields.size();
      super.onClass(item);
      int e = this.fields.size();
      
      this.class2fields.put(item, new SubList(this.fields, s, e));
    }
    else
    {
      this.fields.addAll(subList);
    }
    return null;
  }
  
  public void onOther(OtherExp exp)
  {
    if ((exp instanceof OccurrenceExp)) {
      ((OccurrenceExp)exp).itemExp.visit(this);
    } else {
      super.onOther(exp);
    }
  }
  
  private void compare(int ls, int le, int rs, int re)
  {
    for (int l = ls; l < le; l++)
    {
      FieldItem left = (FieldItem)this.fields.get(l);
      for (int r = rs; r < re; r++)
      {
        FieldItem right = (FieldItem)this.fields.get(r);
        if ((left.name.equals(right.name)) && ((!left.collisionExpected) || (!right.collisionExpected)))
        {
          Locator locator;
          Locator locator;
          if (left.locator != null) {
            locator = left.locator;
          } else {
            locator = right.locator;
          }
          error(locator, "FieldCollisionChecker.PropertyNameCollision", left.name);
          if ((left.locator != null) && (right.locator != null)) {
            error(right.locator, "FieldCollisionChecker.PropertyNameCollision.Source", left.name);
          }
        }
      }
    }
  }
  
  private void error(Locator loc, String prop, Object arg)
  {
    this.controller.getErrorReceiver().error(loc, Messages.format(prop, arg));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\annotator\FieldCollisionChecker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */