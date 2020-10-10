package com.sun.tools.xjc.generator.unmarshaller.automaton;

public abstract class Alphabet
{
  public final int order;
  
  public Alphabet(int _order)
  {
    this.order = _order;
  }
  
  public final Alphabet.Named asNamed()
  {
    return (Alphabet.Named)this;
  }
  
  public final Alphabet.Reference asReference()
  {
    return (Alphabet.Reference)this;
  }
  
  public final Alphabet.StaticReference asStaticReference()
  {
    return (Alphabet.StaticReference)this;
  }
  
  public final Alphabet.Text asText()
  {
    return (Alphabet.Text)this;
  }
  
  public final Alphabet.BoundText asBoundText()
  {
    return (Alphabet.BoundText)this;
  }
  
  public final Alphabet.Dispatch asDispatch()
  {
    return (Alphabet.Dispatch)this;
  }
  
  public final boolean isReference()
  {
    return this instanceof Alphabet.Reference;
  }
  
  public final boolean isEnterAttribute()
  {
    return this instanceof Alphabet.EnterAttribute;
  }
  
  public final boolean isLeaveAttribute()
  {
    return this instanceof Alphabet.LeaveAttribute;
  }
  
  public final boolean isText()
  {
    return this instanceof Alphabet.Text;
  }
  
  public final boolean isNamed()
  {
    return this instanceof Alphabet.Named;
  }
  
  public final boolean isBoundText()
  {
    return this instanceof Alphabet.BoundText;
  }
  
  public final boolean isDispatch()
  {
    return this instanceof Alphabet.Dispatch;
  }
  
  public abstract void accept(AlphabetVisitor paramAlphabetVisitor);
  
  protected abstract void accept(TransitionVisitor paramTransitionVisitor, Transition paramTransition);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\Alphabet.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */