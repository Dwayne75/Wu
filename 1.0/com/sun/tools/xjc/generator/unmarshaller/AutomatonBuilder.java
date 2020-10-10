package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionVisitor;
import com.sun.msv.grammar.util.ExpressionFinder;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.grammar.ClassItem;
import java.util.Map;

public class AutomatonBuilder
{
  private final ClassItem classItem;
  private final GeneratorContext context;
  private final Map otherAutomata;
  private State tail;
  
  public static void build(Automaton a, GeneratorContext context, Map automata)
  {
    ClassItem ci = a.getOwner().target;
    a.setInitialState((State)ci.exp.visit(new AutomatonBuilder(ci, context, automata).normal));
  }
  
  private Automaton getAutomaton(ClassItem ci)
  {
    return (Automaton)this.otherAutomata.get(ci);
  }
  
  private AutomatonBuilder(ClassItem ci, GeneratorContext _context, Map _automata)
  {
    this.tail = new State();
    this.tail.markAsFinalState();
    this.classItem = ci;
    this.context = _context;
    this.otherAutomata = _automata;
  }
  
  private int idGen = 0;
  private final ExpressionVisitor normal = new AutomatonBuilder.Normal(this, null);
  private final ExpressionVisitor inIgnoredItem = new AutomatonBuilder.Ignored(this, null);
  private static final ExpressionFinder textFinder = new AutomatonBuilder.1();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\AutomatonBuilder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */