package com.sun.tools.xjc.generator.unmarshaller.automaton;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Iterator;

public class AutomatonToGraphViz
{
  private static final PrintStream debug = null;
  
  private static String getStateName(Automaton a, State s)
  {
    return "\"s" + a.getStateNumber(s) + (s.isListState ? "*" : "") + '"';
  }
  
  private static String getColor(Alphabet a)
  {
    if ((a instanceof Alphabet.EnterElement)) {
      return "0";
    }
    if ((a instanceof Alphabet.EnterAttribute)) {
      return "0.125";
    }
    if ((a instanceof Alphabet.LeaveAttribute)) {
      return "0.25";
    }
    if ((a instanceof Alphabet.LeaveElement)) {
      return "0.375";
    }
    if ((a instanceof Alphabet.Child)) {
      return "0.5";
    }
    if ((a instanceof Alphabet.SuperClass)) {
      return "0.625";
    }
    if ((a instanceof Alphabet.External)) {
      return "0.625";
    }
    if ((a instanceof Alphabet.Dispatch)) {
      return "0.625";
    }
    if ((a instanceof Alphabet.EverythingElse)) {
      return "0.625";
    }
    if ((a instanceof Alphabet.Text)) {
      return "0.75";
    }
    if ((a instanceof Alphabet.Interleave)) {
      return "0.875";
    }
    throw new InternalError(a.getClass().getName());
  }
  
  public static void convert(Automaton a, File target)
    throws IOException, InterruptedException
  {
    System.err.println("generating a graph to " + target.getPath());
    
    Process proc = Runtime.getRuntime().exec(new String[] { "dot", "-Tgif", "-o", target.getPath() });
    
    PrintWriter out = new PrintWriter(new BufferedOutputStream(proc.getOutputStream()));
    
    out.println("digraph G {");
    out.println("node [shape=\"circle\"];");
    
    Iterator itr = a.states();
    while (itr.hasNext())
    {
      State s = (State)itr.next();
      if (s.isFinalState()) {
        out.println(getStateName(a, s) + " [shape=\"doublecircle\"];");
      }
      if (s.getDelegatedState() != null) {
        out.println(MessageFormat.format("{0} -> {1} [style=dotted];", new Object[] { getStateName(a, s), getStateName(a, s.getDelegatedState()) }));
      }
      Iterator jtr = s.transitions();
      while (jtr.hasNext())
      {
        Transition t = (Transition)jtr.next();
        
        String str = MessageFormat.format("{0} -> {1} [ label=\"{2}\",color=\"{3} 1 .5\",fontcolor=\"{3} 1 .3\" ];", new Object[] { getStateName(a, s), getStateName(a, t.to), getAlphabetName(a, t.alphabet), getColor(t.alphabet) });
        
        out.println(str);
        if (debug != null) {
          debug.println(str);
        }
      }
    }
    out.println("}");
    out.flush();
    out.close();
    
    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    for (;;)
    {
      String s = in.readLine();
      if (s == null) {
        break;
      }
      System.out.println(s);
    }
    in.close();
    
    proc.waitFor();
  }
  
  private static String getAlphabetName(Automaton a, Alphabet alphabet)
  {
    String s = alphabet.toString();
    if ((alphabet instanceof Alphabet.Interleave))
    {
      s = s + " ->";
      Alphabet.Interleave ia = (Alphabet.Interleave)alphabet;
      for (int i = 0; i < ia.branches.length; i++) {
        s = s + " " + a.getStateNumber(ia.branches[i].initialState);
      }
    }
    return s;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\automaton\AutomatonToGraphViz.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */