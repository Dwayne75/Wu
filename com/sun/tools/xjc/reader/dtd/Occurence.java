package com.sun.tools.xjc.reader.dtd;

import java.util.List;

final class Occurence
  extends Term
{
  final Term term;
  final boolean isOptional;
  final boolean isRepeated;
  
  Occurence(Term term, boolean optional, boolean repeated)
  {
    this.term = term;
    this.isOptional = optional;
    this.isRepeated = repeated;
  }
  
  static Term wrap(Term t, int occurence)
  {
    switch (occurence)
    {
    case 3: 
      return t;
    case 1: 
      return new Occurence(t, false, true);
    case 0: 
      return new Occurence(t, true, true);
    case 2: 
      return new Occurence(t, true, false);
    }
    throw new IllegalArgumentException();
  }
  
  void normalize(List<Block> r, boolean optional)
  {
    if (this.isRepeated)
    {
      Block b = new Block((this.isOptional) || (optional), true);
      addAllElements(b);
      r.add(b);
    }
    else
    {
      this.term.normalize(r, (optional) || (this.isOptional));
    }
  }
  
  void addAllElements(Block b)
  {
    this.term.addAllElements(b);
  }
  
  boolean isOptional()
  {
    return (this.isOptional) || (this.term.isOptional());
  }
  
  boolean isRepeated()
  {
    return (this.isRepeated) || (this.term.isRepeated());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\Occurence.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */