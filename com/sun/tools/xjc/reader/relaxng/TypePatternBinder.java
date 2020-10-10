package com.sun.tools.xjc.reader.relaxng;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import org.kohsuke.rngom.digested.DAttributePattern;
import org.kohsuke.rngom.digested.DChoicePattern;
import org.kohsuke.rngom.digested.DDefine;
import org.kohsuke.rngom.digested.DListPattern;
import org.kohsuke.rngom.digested.DMixedPattern;
import org.kohsuke.rngom.digested.DOneOrMorePattern;
import org.kohsuke.rngom.digested.DOptionalPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DRefPattern;
import org.kohsuke.rngom.digested.DZeroOrMorePattern;

final class TypePatternBinder
  extends DPatternWalker
{
  private boolean canInherit;
  private final Stack<Boolean> stack = new Stack();
  private final Set<DDefine> cannotBeInherited = new HashSet();
  
  void reset()
  {
    this.canInherit = true;
    this.stack.clear();
  }
  
  public Void onRef(DRefPattern p)
  {
    if (!this.canInherit) {
      this.cannotBeInherited.add(p.getTarget());
    } else {
      this.canInherit = false;
    }
    return null;
  }
  
  public Void onChoice(DChoicePattern p)
  {
    push(false);
    super.onChoice(p);
    pop();
    return null;
  }
  
  public Void onAttribute(DAttributePattern p)
  {
    push(false);
    super.onAttribute(p);
    pop();
    return null;
  }
  
  public Void onList(DListPattern p)
  {
    push(false);
    super.onList(p);
    pop();
    return null;
  }
  
  public Void onMixed(DMixedPattern p)
  {
    push(false);
    super.onMixed(p);
    pop();
    return null;
  }
  
  public Void onOneOrMore(DOneOrMorePattern p)
  {
    push(false);
    super.onOneOrMore(p);
    pop();
    return null;
  }
  
  public Void onZeroOrMore(DZeroOrMorePattern p)
  {
    push(false);
    super.onZeroOrMore(p);
    pop();
    return null;
  }
  
  public Void onOptional(DOptionalPattern p)
  {
    push(false);
    super.onOptional(p);
    pop();
    return null;
  }
  
  private void push(boolean v)
  {
    this.stack.push(Boolean.valueOf(this.canInherit));
    this.canInherit = v;
  }
  
  private void pop()
  {
    this.canInherit = ((Boolean)this.stack.pop()).booleanValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\TypePatternBinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */