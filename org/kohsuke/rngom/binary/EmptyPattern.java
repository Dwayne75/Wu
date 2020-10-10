package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class EmptyPattern
  extends Pattern
{
  EmptyPattern()
  {
    super(true, 0, 5);
  }
  
  boolean samePattern(Pattern other)
  {
    return other instanceof EmptyPattern;
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitEmpty();
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseEmpty(this);
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    switch (context)
    {
    case 7: 
      throw new RestrictionViolationException("data_except_contains_empty");
    case 0: 
      throw new RestrictionViolationException("start_contains_empty");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\EmptyPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */