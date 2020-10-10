package org.kohsuke.rngom.binary;

public class PatternBuilder
{
  private final EmptyPattern empty;
  protected final NotAllowedPattern notAllowed;
  protected final PatternInterner interner;
  
  public PatternBuilder()
  {
    this.empty = new EmptyPattern();
    this.notAllowed = new NotAllowedPattern();
    this.interner = new PatternInterner();
  }
  
  public PatternBuilder(PatternBuilder parent)
  {
    this.empty = parent.empty;
    this.notAllowed = parent.notAllowed;
    this.interner = new PatternInterner(parent.interner);
  }
  
  Pattern makeEmpty()
  {
    return this.empty;
  }
  
  Pattern makeNotAllowed()
  {
    return this.notAllowed;
  }
  
  Pattern makeGroup(Pattern p1, Pattern p2)
  {
    if (p1 == this.empty) {
      return p2;
    }
    if (p2 == this.empty) {
      return p1;
    }
    if ((p1 == this.notAllowed) || (p2 == this.notAllowed)) {
      return this.notAllowed;
    }
    Pattern p = new GroupPattern(p1, p2);
    return this.interner.intern(p);
  }
  
  Pattern makeInterleave(Pattern p1, Pattern p2)
  {
    if (p1 == this.empty) {
      return p2;
    }
    if (p2 == this.empty) {
      return p1;
    }
    if ((p1 == this.notAllowed) || (p2 == this.notAllowed)) {
      return this.notAllowed;
    }
    Pattern p = new InterleavePattern(p1, p2);
    return this.interner.intern(p);
  }
  
  Pattern makeChoice(Pattern p1, Pattern p2)
  {
    if ((p1 == this.empty) && (p2.isNullable())) {
      return p2;
    }
    if ((p2 == this.empty) && (p1.isNullable())) {
      return p1;
    }
    Pattern p = new ChoicePattern(p1, p2);
    return this.interner.intern(p);
  }
  
  Pattern makeOneOrMore(Pattern p)
  {
    if ((p == this.empty) || (p == this.notAllowed) || ((p instanceof OneOrMorePattern))) {
      return p;
    }
    Pattern p1 = new OneOrMorePattern(p);
    return this.interner.intern(p1);
  }
  
  Pattern makeOptional(Pattern p)
  {
    return makeChoice(p, this.empty);
  }
  
  Pattern makeZeroOrMore(Pattern p)
  {
    return makeOptional(makeOneOrMore(p));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\PatternBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */