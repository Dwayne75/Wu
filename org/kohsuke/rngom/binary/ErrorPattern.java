package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;

public class ErrorPattern
  extends Pattern
{
  ErrorPattern()
  {
    super(false, 0, 3);
  }
  
  boolean samePattern(Pattern other)
  {
    return other instanceof ErrorPattern;
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitError();
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseError(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\ErrorPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */