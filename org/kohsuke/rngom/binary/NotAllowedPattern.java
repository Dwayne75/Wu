package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class NotAllowedPattern
  extends Pattern
{
  NotAllowedPattern()
  {
    super(false, 0, 7);
  }
  
  boolean isNotAllowed()
  {
    return true;
  }
  
  boolean samePattern(Pattern other)
  {
    return other.getClass() == getClass();
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitNotAllowed();
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseNotAllowed(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\NotAllowedPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */