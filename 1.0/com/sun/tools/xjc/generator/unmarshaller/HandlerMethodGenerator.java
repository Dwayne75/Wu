package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCase;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForLoop;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JLabel;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Child;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Dispatch;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EnterAttribute;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EverythingElse;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.External;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Interleave;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.LeaveAttribute;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Reference;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.StaticReference;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.SuperClass;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.xml.bind.JAXBAssertionError;
import java.text.MessageFormat;
import java.util.Iterator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

abstract class HandlerMethodGenerator
{
  protected final PerClassGenerator parent;
  protected final JCodeModel codeModel;
  protected final boolean trace;
  protected final JVar $tracer;
  protected final String methodName;
  private final Class alphabetType;
  protected final TransitionTable table;
  private JBlock $case;
  private JSwitch $switch;
  private JVar $attIdx;
  private JLabel outerLabel;
  
  protected HandlerMethodGenerator(PerClassGenerator _parent, String _mname, Class _alphabetType)
  {
    this.parent = _parent;
    this.methodName = _mname;
    this.alphabetType = _alphabetType;
    this.codeModel = this.parent.parent.codeModel;
    this.trace = this.parent.parent.trace;
    this.$tracer = this.parent.$tracer;
    this.table = this.parent.transitionTable;
  }
  
  protected JBlock getCase(State source)
  {
    if (this.$case != null) {
      return this.$case;
    }
    this.$case = getSwitch()._case(JExpr.lit(this.parent.automaton.getStateNumber(source))).body();
    
    return this.$case;
  }
  
  protected boolean hasCase(State source)
  {
    return this.$case != null;
  }
  
  protected String getNameOfMethodDecl()
  {
    return this.methodName;
  }
  
  protected final JSwitch getSwitch()
  {
    if (this.$switch != null) {
      return this.$switch;
    }
    JMethod method = this.parent.unmarshaller.method(1, this.codeModel.VOID, getNameOfMethodDecl());
    
    method._throws(SAXException.class);
    
    this.$attIdx = method.body().decl(this.codeModel.INT, "attIdx");
    
    this.outerLabel = method.body().label("outer");
    JWhileLoop w = method.body()._while(JExpr.TRUE);
    
    this.$switch = makeSwitch(method, w.body());
    
    w.body()._break();
    
    return this.$switch;
  }
  
  protected JSwitch makeSwitch(JMethod method, JBlock parentBody)
  {
    return parentBody._switch(this.parent.$state);
  }
  
  private void onState(State state, TransitionTable table)
  {
    TransitionTable.Entry[] row = table.list(state);
    
    boolean canFallThrough = true;
    TransitionTable.Entry catchAll = null;
    for (int i = 0; (i < row.length) && (canFallThrough); i++)
    {
      Alphabet a = row[i].alphabet;
      if (this.alphabetType.isInstance(a)) {
        canFallThrough = performTransition(state, a, row[i].transition);
      } else if (a.isEnterAttribute()) {
        buildAttributeCheckClause(getCase(state), state, (Alphabet.EnterAttribute)a, row[i]);
      } else if ((a.isDispatch()) && (this.alphabetType != Alphabet.EnterAttribute.class) && (this.alphabetType != Alphabet.LeaveAttribute.class)) {
        generateDispatch(getCase(state), a.asDispatch(), row[i]);
      } else if (a == Alphabet.EverythingElse.theInstance) {
        catchAll = row[i];
      }
    }
    if ((canFallThrough) && (catchAll != null)) {
      canFallThrough = performTransition(state, catchAll.alphabet, catchAll.transition);
    }
    if (canFallThrough) {
      if (state.getDelegatedState() != null)
      {
        generateGoto(getCase(state), state.getDelegatedState());
        
        getCase(state)._continue(this.outerLabel);
      }
      else if (hasCase(state))
      {
        getCase(state)._break();
      }
    }
  }
  
  protected abstract boolean performTransition(State paramState, Alphabet paramAlphabet, Transition paramTransition);
  
  protected final void generate()
  {
    Iterator itr = this.parent.automaton.states();
    while (itr.hasNext())
    {
      this.$case = null;
      onState((State)itr.next(), this.table);
    }
  }
  
  protected abstract void addParametersToContextSwitch(JInvocation paramJInvocation);
  
  protected final String capitalize()
  {
    return Character.toUpperCase(this.methodName.charAt(0)) + this.methodName.substring(1);
  }
  
  protected final void generateRevertToParent(JBlock $body)
  {
    if (this.trace) {
      $body.invoke(this.$tracer, "onRevertToParent");
    }
    JInvocation inv = $body.invoke("revertToParentFrom" + capitalize());
    
    addParametersToContextSwitch(inv);
    
    $body._return();
  }
  
  protected void generateSpawnChildFromExternal(JBlock $body, Transition tr, JExpression memento)
  {
    _assert(false);
  }
  
  protected final void generateSpawnChild(JBlock $body, Transition tr)
  {
    _assert(tr.alphabet instanceof Alphabet.Reference);
    
    JExpression memento = JExpr.lit(this.parent.automaton.getStateNumber(tr.to));
    if ((tr.alphabet instanceof Alphabet.External))
    {
      generateSpawnChildFromExternal($body, tr, memento);
    }
    else if ((tr.alphabet instanceof Alphabet.Interleave))
    {
      Alphabet.Interleave ia = (Alphabet.Interleave)tr.alphabet;
      
      JInvocation $inv = $body.invoke("spawnHandlerFrom" + capitalize()).arg(JExpr._new(this.parent.getInterleaveDispatcher(ia))).arg(memento);
      
      addParametersToContextSwitch($inv);
      $body._return();
    }
    else if (!tr.alphabet.isDispatch())
    {
      Alphabet.StaticReference sr = (Alphabet.StaticReference)tr.alphabet;
      
      JClass childType = sr.target.getOwner().implRef;
      if ((tr.alphabet instanceof Alphabet.SuperClass))
      {
        if (this.trace)
        {
          $body.invoke(this.$tracer, "onSpawnSuper").arg(JExpr.lit(childType.name()));
          
          $body.invoke(this.$tracer, "suspend");
        }
        JInvocation $inv = $body.invoke("spawnHandlerFrom" + capitalize()).arg(JExpr.direct(MessageFormat.format("(({0}){1}.this).new Unmarshaller(context)", new Object[] { childType.fullName(), this.parent.context.implClass.fullName() }))).arg(memento);
        
        addParametersToContextSwitch($inv);
        $body._return();
      }
      else
      {
        Alphabet.Child c = (Alphabet.Child)tr.alphabet;
        if (this.trace)
        {
          $body.invoke(this.$tracer, "onSpawnChild").arg(JExpr.lit(childType.name())).arg(JExpr.lit(c.field.getFieldUse().name));
          
          $body.invoke(this.$tracer, "suspend");
        }
        JInvocation $childObj = JExpr.invoke("spawnChildFrom" + capitalize()).arg(JExpr.dotclass(childType)).arg(memento);
        
        addParametersToContextSwitch($childObj);
        
        c.field.setter($body, JExpr.cast(childType, $childObj));
        
        $body._return();
      }
    }
  }
  
  protected final void generateGoto(JBlock $body, State target)
  {
    this.parent.generateGoto($body, target);
  }
  
  private void buildAttributeCheckClause(JBlock body, State current, Alphabet.EnterAttribute alphabet, TransitionTable.Entry tte)
  {
    NameClass nc = alphabet.name;
    
    JExpression $context = JExpr.ref("context");
    if ((nc instanceof SimpleNameClass))
    {
      SimpleNameClass snc = (SimpleNameClass)nc;
      
      body.assign(this.$attIdx, $context.invoke("getAttribute").arg(JExpr.lit(snc.namespaceURI)).arg(JExpr.lit(snc.localName)));
    }
    else
    {
      JBlock b = body.block();
      
      JVar $a = b.decl(this.codeModel.ref(Attributes.class), "a", $context.invoke("getUnconsumedAttributes"));
      
      JForLoop loop = b._for();
      loop.init(this.$attIdx, JExpr.invoke($a, "getLength").minus(JExpr.lit(1)));
      
      loop.test(this.$attIdx.gte(JExpr.lit(0)));
      loop.update(this.$attIdx.decr());
      
      JType str = this.codeModel.ref(String.class);
      JVar $uri = loop.body().decl(str, "uri", $a.invoke("getURI").arg(this.$attIdx));
      JVar $local = loop.body().decl(str, "local", $a.invoke("getLocalName").arg(this.$attIdx));
      
      loop.body()._if(this.parent.parent.generateNameClassTest(nc, $uri, $local))._then()._break();
    }
    JBlock _then = body._if(this.$attIdx.gte(JExpr.lit(0)))._then();
    
    HandlerMethodGenerator.AttOptimizeInfo aoi = calcOptimizableAttribute(tte);
    if (aoi == null)
    {
      _then.invoke($context, "consumeAttribute").arg(this.$attIdx);
      
      addParametersToContextSwitch(_then.invoke($context.invoke("getCurrentHandler"), this.methodName));
      
      _then._return();
    }
    else
    {
      JVar $v = _then.decl(8, this.codeModel.ref(String.class), "v", $context.invoke("eatAttribute").arg(this.$attIdx));
      
      this.parent.eatText(_then, aoi.valueHandler, $v);
      generateGoto(_then, aoi.nextState);
      if ((aoi.nextState.isListState ^ current.isListState))
      {
        addParametersToContextSwitch(_then.invoke($context.invoke("getCurrentHandler"), this.methodName));
        
        _then._return();
      }
      else
      {
        _then._continue(this.outerLabel);
      }
    }
  }
  
  private void generateDispatch(JBlock $body, Alphabet.Dispatch da, TransitionTable.Entry tte)
  {
    JBlock block = $body.block();
    
    JVar $childType = block.decl(this.codeModel.ref(Class.class), "child", this.parent.invokeLookup(da, tte));
    
    block = block._if($childType.ne(JExpr._null()))._then();
    if (this.trace)
    {
      block.invoke(this.$tracer, "onSpawnChild").arg(JExpr.lit('{' + da.attName.namespaceURI + '}' + da.attName.localName)).arg(JExpr.lit(da.field.getFieldUse().name));
      
      block.invoke(this.$tracer, "suspend");
    }
    JInvocation $childObj = JExpr.invoke("spawnChildFrom" + capitalize()).arg($childType).arg(JExpr.lit(this.parent.automaton.getStateNumber(tte.transition.to)));
    
    addParametersToContextSwitch($childObj);
    
    da.field.setter(block, JExpr.cast(da.field.getFieldUse().type, $childObj));
    
    block._return();
  }
  
  private HandlerMethodGenerator.AttOptimizeInfo calcOptimizableAttribute(TransitionTable.Entry tte)
  {
    if (!tte.transition.alphabet.isEnterAttribute()) {
      return null;
    }
    Transition[] hop1 = tte.transition.to.listTransitions();
    if (hop1.length != 1) {
      return null;
    }
    Transition t1 = hop1[0];
    if (!t1.alphabet.isBoundText()) {
      return null;
    }
    Transition[] hop2 = t1.to.listTransitions();
    if (hop2.length != 1) {
      return null;
    }
    Transition t2 = hop2[0];
    if (!t2.alphabet.isLeaveAttribute()) {
      return null;
    }
    return new HandlerMethodGenerator.AttOptimizeInfo(t1.alphabet.asBoundText(), t2.to);
  }
  
  protected static final void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\HandlerMethodGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */