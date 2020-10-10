package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class TextPattern
  extends Pattern
{
  TextPattern()
  {
    super(true, 2, 1);
  }
  
  boolean samePattern(Pattern other)
  {
    return other instanceof TextPattern;
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitText();
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseText(this);
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    switch (context)
    {
    case 7: 
      throw new RestrictionViolationException("data_except_contains_text");
    case 0: 
      throw new RestrictionViolationException("start_contains_text");
    case 6: 
      throw new RestrictionViolationException("list_contains_text");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\TextPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */