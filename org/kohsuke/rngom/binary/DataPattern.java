package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;
import org.relaxng.datatype.Datatype;

public class DataPattern
  extends StringPattern
{
  private Datatype dt;
  
  DataPattern(Datatype dt)
  {
    super(combineHashCode(31, dt.hashCode()));
    this.dt = dt;
  }
  
  boolean samePattern(Pattern other)
  {
    if (other.getClass() != getClass()) {
      return false;
    }
    return this.dt.equals(((DataPattern)other).dt);
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitData(this.dt);
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseData(this);
  }
  
  Datatype getDatatype()
  {
    return this.dt;
  }
  
  boolean allowsAnyString()
  {
    return false;
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    switch (context)
    {
    case 0: 
      throw new RestrictionViolationException("start_contains_data");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\DataPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */