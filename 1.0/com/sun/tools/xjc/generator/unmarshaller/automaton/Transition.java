package com.sun.tools.xjc.generator.unmarshaller.automaton;

import java.util.HashSet;
import java.util.Set;

public final class Transition
{
  public static final Transition REVERT_TO_PARENT = new Transition(null, null);
  public final Alphabet alphabet;
  public final State to;
  
  public Transition(Alphabet _alphabet, State _to)
  {
    this.alphabet = _alphabet;
    this.to = _to;
  }
  
  public Set head(State sourceState)
  {
    HashSet s = new HashSet();
    
    HashSet visited = new HashSet();
    visited.add(sourceState);
    
    head(s, visited, true);
    return s;
  }
  
  void head(Set result, Set visitedStates, boolean includeEE)
  {
    result.add(this.alphabet);
    if (!(this.alphabet instanceof Alphabet.Reference)) {
      return;
    }
    Alphabet.Reference ref = this.alphabet.asReference();
    if (ref.isNullable()) {
      this.to.head(result, visitedStates, includeEE);
    }
  }
  
  public void accept(TransitionVisitor visitor)
  {
    this.alphabet.accept(visitor, this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\Transition.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */