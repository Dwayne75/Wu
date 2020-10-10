package com.sun.tools.xjc.generator.unmarshaller.automaton;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.xml.bind.JAXBAssertionError;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Automaton
{
  private final ClassContext owner;
  private State initial;
  private Boolean nullable = null;
  private final Map states = new HashMap();
  private int iota = 0;
  
  public Automaton(ClassContext _owner)
  {
    this.owner = _owner;
  }
  
  public void setInitialState(State _initialState)
  {
    if (this.initial != null) {
      throw new JAXBAssertionError();
    }
    this.initial = _initialState;
    
    new Automaton.StateEnumerator(this, null).visit(this.initial);
  }
  
  public State getInitialState()
  {
    return this.initial;
  }
  
  public int getStateNumber(State s)
  {
    return ((Integer)this.states.get(s)).intValue();
  }
  
  public int getStateSize()
  {
    return this.states.size();
  }
  
  public Iterator states()
  {
    return this.states.keySet().iterator();
  }
  
  public ClassContext getOwner()
  {
    return this.owner;
  }
  
  public boolean isNullable()
  {
    if (this.nullable == null)
    {
      ExpressionPool pool = new ExpressionPool();
      if (this.owner.target.exp.getExpandedExp(pool).isEpsilonReducible()) {
        this.nullable = Boolean.TRUE;
      } else {
        this.nullable = Boolean.FALSE;
      }
    }
    return this.nullable.booleanValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\Automaton.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */