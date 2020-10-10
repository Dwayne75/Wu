package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.util.StringPair;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.PackageContext;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.ClassItem;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UnmarshallerGenerator
{
  final Options options;
  final AnnotatedGrammar grammar;
  final JCodeModel codeModel;
  final GeneratorContext context;
  final boolean trace;
  static Class class$javax$xml$namespace$QName;
  
  public static Automaton[] generate(AnnotatedGrammar grammar, GeneratorContext context, Options opt)
  {
    return new UnmarshallerGenerator(grammar, context, opt)._generate();
  }
  
  private Automaton[] _generate()
  {
    ClassItem[] cis = this.grammar.getClasses();
    Automaton[] automata = new Automaton[cis.length];
    
    Map automataDic = new HashMap();
    for (int i = 0; i < cis.length; i++)
    {
      automata[i] = new Automaton(this.context.getClassContext(cis[i]));
      automataDic.put(cis[i], automata[i]);
    }
    for (int i = 0; i < automata.length; i++) {
      AutomatonBuilder.build(automata[i], this.context, automataDic);
    }
    if ((this.options.debugMode) && (this.options.verbose)) {
      for (int i = 0; i < cis.length; i++)
      {
        System.out.println(cis[i].getType().fullName());
        System.out.println("nullable: " + automata[i].isNullable());
        System.out.println();
      }
    }
    for (int i = 0; i < automata.length; i++) {
      new PerClassGenerator(this, automata[i]).generate();
    }
    generateGrammarInfoImpl();
    
    return automata;
  }
  
  private void generateGrammarInfoImpl()
  {
    PackageContext[] pcs = this.context.getAllPackageContexts();
    for (int i = 0; i < pcs.length; i++)
    {
      UnmarshallerGenerator.RootMapBuilder rmb = new UnmarshallerGenerator.RootMapBuilder(pcs[i].rootTagMap, pcs[i].objectFactory);
      
      Map roots = getRootMap(pcs[i]._package);
      
      ClassItem[] classes = (ClassItem[])roots.keySet().toArray(new ClassItem[roots.size()]);
      NameClass[] nameClasses = (NameClass[])roots.values().toArray(new NameClass[roots.size()]);
      
      UnmarshallerGenerator.ProbePointBuilder ppb = new UnmarshallerGenerator.ProbePointBuilder(null);
      for (int j = 0; j < nameClasses.length; j++) {
        nameClasses[j].visit(ppb);
      }
      StringPair[] probePoints = ppb.getResult();
      for (int j = 0; j < probePoints.length; j++)
      {
        for (int k = 0; k < nameClasses.length; k++) {
          if (nameClasses[k].accepts(probePoints[j]))
          {
            rmb.add(probePoints[j], classes[k]);
            break;
          }
        }
        if (k == nameClasses.length) {
          rmb.add(probePoints[j], null);
        }
      }
    }
  }
  
  private Map getRootMap(JPackage currentPackage)
  {
    Map roots = new HashMap();
    
    this.grammar.getTopLevel().visit(new UnmarshallerGenerator.1(this, currentPackage, roots));
    
    return roots;
  }
  
  UnmarshallerGenerator(AnnotatedGrammar _grammar, GeneratorContext _context, Options _opt)
  {
    this.options = _opt;
    this.trace = this.options.traceUnmarshaller;
    this.grammar = _grammar;
    this.codeModel = this.grammar.codeModel;
    this.context = _context;
  }
  
  protected JExpression generateNameClassTest(NameClass nc, JVar $uri, JVar $local)
  {
    return (JExpression)nc.visit(new UnmarshallerGenerator.2(this, $local, $uri));
  }
  
  static Class class$(String x0)
  {
    try
    {
      return Class.forName(x0);
    }
    catch (ClassNotFoundException x1)
    {
      throw new NoClassDefFoundError(x1.getMessage());
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\UnmarshallerGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */