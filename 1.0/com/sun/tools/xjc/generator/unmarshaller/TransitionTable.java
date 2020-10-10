package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EverythingElse;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Reference;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.OrderComparator;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

class TransitionTable
{
  TransitionTable(Automaton a)
  {
    Iterator itr = a.states();
    while (itr.hasNext())
    {
      State state = (State)itr.next();
      
      TreeMap tm = new TreeMap(OrderComparator.theInstance);
      for (Iterator jtr = state.transitions(); jtr.hasNext();)
      {
        Transition t = (Transition)jtr.next();
        tm.put(t.alphabet, t);
      }
      ArrayList r = new ArrayList();
      for (Iterator jtr = tm.entrySet().iterator(); jtr.hasNext();)
      {
        Map.Entry e = (Map.Entry)jtr.next();
        buildList(r, (Alphabet)e.getKey(), (Transition)e.getValue());
      }
      if (state.isFinalState()) {
        r.add(new TransitionTable.Entry(Alphabet.EverythingElse.theInstance, Transition.REVERT_TO_PARENT, null));
      }
      Set alphabetsSeen = new HashSet();
      for (int i = 0; i < r.size();) {
        if (!alphabetsSeen.add(((TransitionTable.Entry)r.get(i)).alphabet)) {
          r.remove(i);
        } else {
          i++;
        }
      }
      this.table.put(state, (TransitionTable.Entry[])r.toArray(new TransitionTable.Entry[r.size()]));
    }
  }
  
  private void buildList(ArrayList r, Alphabet alphabet, Transition transition)
  {
    if (alphabet.isReference())
    {
      Iterator itr = alphabet.asReference().head(true).iterator();
      while (itr.hasNext()) {
        buildList(r, (Alphabet)itr.next(), transition);
      }
    }
    else
    {
      r.add(new TransitionTable.Entry(alphabet, transition, null));
    }
  }
  
  private final Map table = new HashMap();
  
  public TransitionTable.Entry[] list(State s)
  {
    TransitionTable.Entry[] r = (TransitionTable.Entry[])this.table.get(s);
    if (r == null) {
      return empty;
    }
    return r;
  }
  
  private static final TransitionTable.Entry[] empty = new TransitionTable.Entry[0];
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\TransitionTable.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */