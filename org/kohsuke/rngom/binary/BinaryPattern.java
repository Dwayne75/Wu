package org.kohsuke.rngom.binary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.xml.sax.SAXException;

public abstract class BinaryPattern
  extends Pattern
{
  protected final Pattern p1;
  protected final Pattern p2;
  
  BinaryPattern(boolean nullable, int hc, Pattern p1, Pattern p2)
  {
    super(nullable, Math.max(p1.getContentType(), p2.getContentType()), hc);
    this.p1 = p1;
    this.p2 = p2;
  }
  
  void checkRecursion(int depth)
    throws SAXException
  {
    this.p1.checkRecursion(depth);
    this.p2.checkRecursion(depth);
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {
    this.p1.checkRestrictions(context, dad, alpha);
    this.p2.checkRestrictions(context, dad, alpha);
  }
  
  boolean samePattern(Pattern other)
  {
    if (getClass() != other.getClass()) {
      return false;
    }
    BinaryPattern b = (BinaryPattern)other;
    return (this.p1 == b.p1) && (this.p2 == b.p2);
  }
  
  public final Pattern getOperand1()
  {
    return this.p1;
  }
  
  public final Pattern getOperand2()
  {
    return this.p2;
  }
  
  public final void fillChildren(Collection col)
  {
    fillChildren(getClass(), this.p1, col);
    fillChildren(getClass(), this.p2, col);
  }
  
  public final Pattern[] getChildren()
  {
    List lst = new ArrayList();
    fillChildren(lst);
    return (Pattern[])lst.toArray(new Pattern[lst.size()]);
  }
  
  private void fillChildren(Class c, Pattern p, Collection col)
  {
    if (p.getClass() == c)
    {
      BinaryPattern bp = (BinaryPattern)p;
      bp.fillChildren(c, bp.p1, col);
      bp.fillChildren(c, bp.p2, col);
    }
    else
    {
      col.add(p);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\BinaryPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */