package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class ListPattern
  extends Pattern
{
  Pattern p;
  Locator locator;
  
  ListPattern(Pattern p, Locator locator)
  {
    super(false, 3, combineHashCode(37, p.hashCode()));
    
    this.p = p;
    this.locator = locator;
  }
  
  Pattern expand(SchemaPatternBuilder b)
  {
    Pattern ep = this.p.expand(b);
    if (ep != this.p) {
      return b.makeList(ep, this.locator);
    }
    return this;
  }
  
  void checkRecursion(int depth)
    throws SAXException
  {
    this.p.checkRecursion(depth);
  }
  
  boolean samePattern(Pattern other)
  {
    return ((other instanceof ListPattern)) && (this.p == ((ListPattern)other).p);
  }
  
  public void accept(PatternVisitor visitor)
  {
    visitor.visitList(this.p);
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseList(this);
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    switch (context)
    {
    case 7: 
      throw new RestrictionViolationException("data_except_contains_list");
    case 0: 
      throw new RestrictionViolationException("start_contains_list");
    case 6: 
      throw new RestrictionViolationException("list_contains_list");
    }
    try
    {
      this.p.checkRestrictions(6, dad, null);
    }
    catch (RestrictionViolationException e)
    {
      e.maybeSetLocator(this.locator);
      throw e;
    }
  }
  
  Pattern getOperand()
  {
    return this.p;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\ListPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */