package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class ChoicePattern
  extends BinaryPattern
{
  ChoicePattern(Pattern p1, Pattern p2)
  {
    super((p1.isNullable()) || (p2.isNullable()), combineHashCode(11, p1.hashCode(), p2.hashCode()), p1, p2);
  }
  
  Pattern expand(SchemaPatternBuilder b)
  {
    Pattern ep1 = this.p1.expand(b);
    Pattern ep2 = this.p2.expand(b);
    if ((ep1 != this.p1) || (ep2 != this.p2)) {
      return b.makeChoice(ep1, ep2);
    }
    return this;
  }
  
  boolean containsChoice(Pattern p)
  {
    return (this.p1.containsChoice(p)) || (this.p2.containsChoice(p));
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitChoice(this.p1, this.p2);
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseChoice(this);
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    if (dad != null) {
      dad.startChoice();
    }
    this.p1.checkRestrictions(context, dad, alpha);
    if (dad != null) {
      dad.alternative();
    }
    this.p2.checkRestrictions(context, dad, alpha);
    if (dad != null) {
      dad.endChoice();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\ChoicePattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */