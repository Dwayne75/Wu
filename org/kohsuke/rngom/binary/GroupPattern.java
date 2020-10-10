package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class GroupPattern
  extends BinaryPattern
{
  GroupPattern(Pattern p1, Pattern p2)
  {
    super((p1.isNullable()) && (p2.isNullable()), combineHashCode(13, p1.hashCode(), p2.hashCode()), p1, p2);
  }
  
  Pattern expand(SchemaPatternBuilder b)
  {
    Pattern ep1 = this.p1.expand(b);
    Pattern ep2 = this.p2.expand(b);
    if ((ep1 != this.p1) || (ep2 != this.p2)) {
      return b.makeGroup(ep1, ep2);
    }
    return this;
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    switch (context)
    {
    case 0: 
      throw new RestrictionViolationException("start_contains_group");
    case 7: 
      throw new RestrictionViolationException("data_except_contains_group");
    }
    super.checkRestrictions(context == 2 ? 3 : context, dad, alpha);
    if ((context != 6) && (!contentTypeGroupable(this.p1.getContentType(), this.p2.getContentType()))) {
      throw new RestrictionViolationException("group_string");
    }
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitGroup(this.p1, this.p2);
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseGroup(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\GroupPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */