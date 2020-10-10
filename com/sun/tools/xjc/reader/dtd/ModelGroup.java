package com.sun.tools.xjc.reader.dtd;

import java.util.ArrayList;
import java.util.List;

final class ModelGroup
  extends Term
{
  Kind kind;
  
  static enum Kind
  {
    CHOICE,  SEQUENCE;
    
    private Kind() {}
  }
  
  private final List<Term> terms = new ArrayList();
  
  void normalize(List<Block> r, boolean optional)
  {
    switch (this.kind)
    {
    case SEQUENCE: 
      for (Term t : this.terms) {
        t.normalize(r, optional);
      }
      return;
    case CHOICE: 
      Block b = new Block((isOptional()) || (optional), isRepeated());
      addAllElements(b);
      r.add(b);
      return;
    }
  }
  
  void addAllElements(Block b)
  {
    for (Term t : this.terms) {
      t.addAllElements(b);
    }
  }
  
  boolean isOptional()
  {
    switch (this.kind)
    {
    case SEQUENCE: 
      for (Term t : this.terms) {
        if (!t.isOptional()) {
          return false;
        }
      }
      return true;
    case CHOICE: 
      for (Term t : this.terms) {
        if (t.isOptional()) {
          return true;
        }
      }
      return false;
    }
    throw new IllegalArgumentException();
  }
  
  boolean isRepeated()
  {
    switch (this.kind)
    {
    case SEQUENCE: 
      return true;
    case CHOICE: 
      for (Term t : this.terms) {
        if (t.isRepeated()) {
          return true;
        }
      }
      return false;
    }
    throw new IllegalArgumentException();
  }
  
  void setKind(short connectorType)
  {
    Kind k;
    switch (connectorType)
    {
    case 1: 
      k = Kind.SEQUENCE;
      break;
    case 0: 
      k = Kind.CHOICE;
      break;
    default: 
      throw new IllegalArgumentException();
    }
    assert ((this.kind == null) || (k == this.kind));
    this.kind = k;
  }
  
  void addTerm(Term t)
  {
    if ((t instanceof ModelGroup))
    {
      ModelGroup mg = (ModelGroup)t;
      if (mg.kind == this.kind)
      {
        this.terms.addAll(mg.terms);
        return;
      }
    }
    this.terms.add(t);
  }
  
  Term wrapUp()
  {
    switch (this.terms.size())
    {
    case 0: 
      return EMPTY;
    case 1: 
      assert (this.kind == null);
      return (Term)this.terms.get(0);
    }
    assert (this.kind != null);
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\ModelGroup.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */