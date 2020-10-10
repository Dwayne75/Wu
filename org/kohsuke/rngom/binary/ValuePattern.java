package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;
import org.relaxng.datatype.Datatype;

public class ValuePattern
  extends StringPattern
{
  Object obj;
  Datatype dt;
  
  ValuePattern(Datatype dt, Object obj)
  {
    super(combineHashCode(27, obj.hashCode()));
    this.dt = dt;
    this.obj = obj;
  }
  
  boolean samePattern(Pattern other)
  {
    if (getClass() != other.getClass()) {
      return false;
    }
    if (!(other instanceof ValuePattern)) {
      return false;
    }
    return (this.dt.equals(((ValuePattern)other).dt)) && (this.dt.sameValue(this.obj, ((ValuePattern)other).obj));
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitValue(this.dt, this.obj);
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseValue(this);
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    switch (context)
    {
    case 0: 
      throw new RestrictionViolationException("start_contains_value");
    }
  }
  
  Datatype getDatatype()
  {
    return this.dt;
  }
  
  Object getValue()
  {
    return this.obj;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\ValuePattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */