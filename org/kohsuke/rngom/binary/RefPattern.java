package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;
import org.kohsuke.rngom.util.Localizer;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class RefPattern
  extends Pattern
{
  private Pattern p;
  private Locator refLoc;
  private String name;
  private int checkRecursionDepth = -1;
  private boolean combineImplicit = false;
  private byte combineType = 0;
  private byte replacementStatus = 0;
  private boolean expanded = false;
  static final byte REPLACEMENT_KEEP = 0;
  static final byte REPLACEMENT_REQUIRE = 1;
  static final byte REPLACEMENT_IGNORE = 2;
  static final byte COMBINE_NONE = 0;
  static final byte COMBINE_CHOICE = 1;
  static final byte COMBINE_INTERLEAVE = 2;
  
  RefPattern(String name)
  {
    this.name = name;
  }
  
  Pattern getPattern()
  {
    return this.p;
  }
  
  void setPattern(Pattern p)
  {
    this.p = p;
  }
  
  Locator getRefLocator()
  {
    return this.refLoc;
  }
  
  void setRefLocator(Locator loc)
  {
    this.refLoc = loc;
  }
  
  void checkRecursion(int depth)
    throws SAXException
  {
    if (this.checkRecursionDepth == -1)
    {
      this.checkRecursionDepth = depth;
      this.p.checkRecursion(depth);
      this.checkRecursionDepth = -2;
    }
    else if (depth == this.checkRecursionDepth)
    {
      throw new SAXParseException(SchemaBuilderImpl.localizer.message("recursive_reference", this.name), this.refLoc);
    }
  }
  
  Pattern expand(SchemaPatternBuilder b)
  {
    if (!this.expanded)
    {
      this.p = this.p.expand(b);
      this.expanded = true;
    }
    return this.p;
  }
  
  boolean samePattern(Pattern other)
  {
    return false;
  }
  
  public void accept(PatternVisitor visitor)
  {
    this.p.accept(visitor);
  }
  
  public Object apply(PatternFunction f)
  {
    return f.caseRef(this);
  }
  
  byte getReplacementStatus()
  {
    return this.replacementStatus;
  }
  
  void setReplacementStatus(byte replacementStatus)
  {
    this.replacementStatus = replacementStatus;
  }
  
  boolean isCombineImplicit()
  {
    return this.combineImplicit;
  }
  
  void setCombineImplicit()
  {
    this.combineImplicit = true;
  }
  
  byte getCombineType()
  {
    return this.combineType;
  }
  
  void setCombineType(byte combineType)
  {
    this.combineType = combineType;
  }
  
  String getName()
  {
    return this.name;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\RefPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */