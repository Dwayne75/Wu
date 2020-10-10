package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JPackage;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.Grammar;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.tools.xjc.grammar.id.SymbolSpace;
import com.sun.tools.xjc.grammar.xducer.DatabindableXducer;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.xml.bind.JAXBAssertionError;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.xml.sax.Locator;

public final class AnnotatedGrammar
  extends ReferenceExp
  implements Grammar
{
  private final ExpressionPool pool;
  public final JCodeModel codeModel;
  public final SymbolSpace defaultSymbolSpace;
  private final Map symbolSpaces = new HashMap();
  private final Map classes = new HashMap();
  private final Map interfaces = new HashMap();
  private final Set primitives = new HashSet();
  public JClass rootClass;
  public Long serialVersionUID = null;
  
  public AnnotatedGrammar(ExpressionPool pool)
  {
    this(null, pool, new JCodeModel());
  }
  
  public AnnotatedGrammar(Grammar source, JCodeModel _codeModel)
  {
    this(source.getTopLevel(), source.getPool(), _codeModel);
  }
  
  public AnnotatedGrammar(Expression topLevel, ExpressionPool pool, JCodeModel _codeModel)
  {
    super("");
    this.exp = topLevel;
    this.pool = pool;
    this.codeModel = _codeModel;
    this.defaultSymbolSpace = new SymbolSpace(this.codeModel);
    this.defaultSymbolSpace.setType(this.codeModel.ref(Object.class));
  }
  
  public Expression getTopLevel()
  {
    return this.exp;
  }
  
  public ExpressionPool getPool()
  {
    return this.pool;
  }
  
  public PrimitiveItem[] getPrimitives()
  {
    return (PrimitiveItem[])this.primitives.toArray(new PrimitiveItem[this.primitives.size()]);
  }
  
  public ClassItem[] getClasses()
  {
    return (ClassItem[])this.classes.values().toArray(new ClassItem[this.classes.size()]);
  }
  
  public Iterator iterateClasses()
  {
    return this.classes.values().iterator();
  }
  
  public InterfaceItem[] getInterfaces()
  {
    return (InterfaceItem[])this.interfaces.values().toArray(new InterfaceItem[this.interfaces.size()]);
  }
  
  public Iterator iterateInterfaces()
  {
    return this.interfaces.values().iterator();
  }
  
  public SymbolSpace getSymbolSpace(String name)
  {
    SymbolSpace ss = (SymbolSpace)this.symbolSpaces.get(name);
    if (ss == null) {
      this.symbolSpaces.put(name, ss = new SymbolSpace(this.codeModel));
    }
    return ss;
  }
  
  public PrimitiveItem createPrimitiveItem(Transducer _xducer, DatabindableDatatype _guard, Expression _exp, Locator loc)
  {
    PrimitiveItem pi = new PrimitiveItem(_xducer, _guard, _exp, loc);
    this.primitives.add(pi);
    return pi;
  }
  
  public PrimitiveItem createPrimitiveItem(JCodeModel writer, DatabindableDatatype dt, Expression exp, Locator loc)
  {
    return new PrimitiveItem(new DatabindableXducer(writer, dt), dt, exp, loc);
  }
  
  public ClassItem getClassItem(JDefinedClass type)
  {
    return (ClassItem)this.classes.get(type);
  }
  
  public ClassItem createClassItem(JDefinedClass type, Expression body, Locator loc)
  {
    if (this.classes.containsKey(type))
    {
      System.err.println("class name " + type.fullName() + " is already defined");
      Iterator itr = this.classes.keySet().iterator();
      while (itr.hasNext())
      {
        JDefinedClass cls = (JDefinedClass)itr.next();
        System.err.println(cls.fullName());
      }
      _assert(false);
    }
    ClassItem o = new ClassItem(this, type, body, loc);
    this.classes.put(type, o);
    return o;
  }
  
  public InterfaceItem getInterfaceItem(JDefinedClass type)
  {
    return (InterfaceItem)this.interfaces.get(type);
  }
  
  public InterfaceItem createInterfaceItem(JClass type, Expression body, Locator loc)
  {
    _assert(!this.interfaces.containsKey(type));
    
    InterfaceItem o = new InterfaceItem(type, body, loc);
    this.interfaces.put(type, o);
    return o;
  }
  
  public JPackage[] getUsedPackages()
  {
    Set s = new TreeSet(packageComparator);
    
    Iterator itr = iterateClasses();
    while (itr.hasNext()) {
      s.add(((ClassItem)itr.next()).getTypeAsDefined()._package());
    }
    itr = iterateInterfaces();
    while (itr.hasNext()) {
      s.add(((InterfaceItem)itr.next()).getTypeAsClass()._package());
    }
    return (JPackage[])s.toArray(new JPackage[s.size()]);
  }
  
  private static final void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
  
  private static final Comparator packageComparator = new AnnotatedGrammar.1();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\AnnotatedGrammar.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */