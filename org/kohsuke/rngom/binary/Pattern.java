package org.kohsuke.rngom.binary;

import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.binary.visitor.PatternFunction;
import org.kohsuke.rngom.binary.visitor.PatternVisitor;
import org.xml.sax.SAXException;

public abstract class Pattern
  implements ParsedPattern
{
  private boolean nullable;
  private int hc;
  private int contentType;
  static final int TEXT_HASH_CODE = 1;
  static final int ERROR_HASH_CODE = 3;
  static final int EMPTY_HASH_CODE = 5;
  static final int NOT_ALLOWED_HASH_CODE = 7;
  static final int CHOICE_HASH_CODE = 11;
  static final int GROUP_HASH_CODE = 13;
  static final int INTERLEAVE_HASH_CODE = 17;
  static final int ONE_OR_MORE_HASH_CODE = 19;
  static final int ELEMENT_HASH_CODE = 23;
  static final int VALUE_HASH_CODE = 27;
  static final int ATTRIBUTE_HASH_CODE = 29;
  static final int DATA_HASH_CODE = 31;
  static final int LIST_HASH_CODE = 37;
  static final int AFTER_HASH_CODE = 41;
  static final int EMPTY_CONTENT_TYPE = 0;
  static final int ELEMENT_CONTENT_TYPE = 1;
  static final int MIXED_CONTENT_TYPE = 2;
  static final int DATA_CONTENT_TYPE = 3;
  static final int START_CONTEXT = 0;
  static final int ELEMENT_CONTEXT = 1;
  static final int ELEMENT_REPEAT_CONTEXT = 2;
  static final int ELEMENT_REPEAT_GROUP_CONTEXT = 3;
  static final int ELEMENT_REPEAT_INTERLEAVE_CONTEXT = 4;
  static final int ATTRIBUTE_CONTEXT = 5;
  static final int LIST_CONTEXT = 6;
  static final int DATA_EXCEPT_CONTEXT = 7;
  
  static int combineHashCode(int hc1, int hc2, int hc3)
  {
    return hc1 * hc2 * hc3;
  }
  
  static int combineHashCode(int hc1, int hc2)
  {
    return hc1 * hc2;
  }
  
  Pattern(boolean nullable, int contentType, int hc)
  {
    this.nullable = nullable;
    this.contentType = contentType;
    this.hc = hc;
  }
  
  Pattern()
  {
    this.nullable = false;
    this.hc = hashCode();
    this.contentType = 0;
  }
  
  void checkRecursion(int depth)
    throws SAXException
  {}
  
  Pattern expand(SchemaPatternBuilder b)
  {
    return this;
  }
  
  public final boolean isNullable()
  {
    return this.nullable;
  }
  
  boolean isNotAllowed()
  {
    return false;
  }
  
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException
  {}
  
  abstract boolean samePattern(Pattern paramPattern);
  
  final int patternHashCode()
  {
    return this.hc;
  }
  
  final int getContentType()
  {
    return this.contentType;
  }
  
  boolean containsChoice(Pattern p)
  {
    return this == p;
  }
  
  public abstract void accept(PatternVisitor paramPatternVisitor);
  
  public abstract Object apply(PatternFunction paramPatternFunction);
  
  static boolean contentTypeGroupable(int ct1, int ct2)
  {
    if ((ct1 == 0) || (ct2 == 0)) {
      return true;
    }
    if ((ct1 == 3) || (ct2 == 3)) {
      return false;
    }
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\Pattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */