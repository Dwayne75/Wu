package com.sun.tools.xjc.generator.unmarshaller.automaton;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class State
{
  public boolean isListState = false;
  private boolean isFinalState = false;
  private final Set transitions = new HashSet();
  private State delegatedState;
  
  public State getDelegatedState()
  {
    return this.delegatedState;
  }
  
  public void setDelegatedState(State _delegatedState)
  {
    State s = _delegatedState;
    while (s != null)
    {
      if (s == this)
      {
        absorb(_delegatedState);
        return;
      }
      s = s.delegatedState;
    }
    if ((this.isFinalState) && (!_delegatedState.isFinalState))
    {
      absorb(_delegatedState);
      return;
    }
    if (this.delegatedState == null)
    {
      this.delegatedState = _delegatedState;
      this.isListState |= this.delegatedState.isListState;
    }
    else
    {
      absorb(_delegatedState);
    }
  }
  
  public void addTransition(Transition t)
  {
    this.transitions.add(t);
  }
  
  public Iterator transitions()
  {
    return this.transitions.iterator();
  }
  
  public Transition[] listTransitions()
  {
    return (Transition[])this.transitions.toArray(new Transition[this.transitions.size()]);
  }
  
  public void acceptForEachTransition(TransitionVisitor visitor)
  {
    for (Iterator itr = this.transitions.iterator(); itr.hasNext();) {
      ((Transition)itr.next()).accept(visitor);
    }
  }
  
  public void absorb(State rhs)
  {
    this.transitions.addAll(rhs.transitions);
    this.isListState |= rhs.isListState;
    if (rhs.isFinalState) {
      markAsFinalState();
    }
    if (rhs.delegatedState != null) {
      setDelegatedState(rhs.delegatedState);
    }
  }
  
  public Set head()
  {
    HashSet s = new HashSet();
    head(s, new HashSet(), true);
    return s;
  }
  
  void head(Set result, Set visitedStates, boolean includeEE)
  {
    if (!visitedStates.add(this)) {
      return;
    }
    if ((this.isFinalState) && (includeEE)) {
      result.add(Alphabet.EverythingElse.theInstance);
    }
    for (Iterator itr = this.transitions.iterator(); itr.hasNext();)
    {
      Transition t = (Transition)itr.next();
      t.head(result, visitedStates, includeEE);
    }
    if (this.delegatedState != null) {
      this.delegatedState.head(result, visitedStates, includeEE);
    }
  }
  
  public boolean hasTransition()
  {
    return !this.transitions.isEmpty();
  }
  
  public boolean isFinalState()
  {
    return this.isFinalState;
  }
  
  public void markAsFinalState()
  {
    this.isFinalState = true;
    if ((this.delegatedState != null) && (!this.delegatedState.isFinalState))
    {
      State p = this.delegatedState;
      this.delegatedState = null;
      absorb(p);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\State.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */