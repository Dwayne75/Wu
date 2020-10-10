package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.msv.grammar.SimpleNameClass;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.LookupTable;
import com.sun.tools.xjc.generator.XMLDeserializerContextImpl;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.BoundText;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Dispatch;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Interleave;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Interleave.Branch;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.LeaveAttribute;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Automaton;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.DeserializerContext;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;
import com.sun.tools.xjc.runtime.AbstractUnmarshallingEventHandlerImpl;
import com.sun.tools.xjc.runtime.InterleaveDispatcher;
import com.sun.tools.xjc.runtime.UnmarshallableObject;
import com.sun.tools.xjc.runtime.UnmarshallingContext;
import com.sun.tools.xjc.runtime.UnmarshallingEventHandler;
import com.sun.tools.xjc.util.CodeModelClassFactory;
import com.sun.xml.bind.JAXBAssertionError;
import com.sun.xml.bind.unmarshaller.Tracer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xml.sax.SAXException;

class PerClassGenerator
{
  final UnmarshallerGenerator parent;
  private final JCodeModel codeModel;
  final ClassContext context;
  final Automaton automaton;
  final JDefinedClass unmarshaller;
  final JFieldRef $state;
  final JFieldRef $context;
  private final DeserializerContext dc;
  final TransitionTable transitionTable;
  JVar $tracer;
  private int idGen = 0;
  
  public int createId()
  {
    return ++this.idGen;
  }
  
  private final Map interleaveDispatcherImpls = new HashMap();
  
  PerClassGenerator(UnmarshallerGenerator _parent, Automaton a)
  {
    this.parent = _parent;
    this.codeModel = this.parent.codeModel;
    this.context = a.getOwner();
    this.automaton = a;
    
    JDefinedClass impl = this.context.implClass;
    
    impl._implements(getRuntime(UnmarshallableObject.class));
    
    this.unmarshaller = this.parent.context.getClassFactory().createClass(impl, "Unmarshaller", null);
    this.unmarshaller._extends(getRuntime(AbstractUnmarshallingEventHandlerImpl.class));
    
    JMethod method = this.unmarshaller.method(1, Object.class, "owner");
    method.body()._return(impl.staticRef("this"));
    
    this.$state = JExpr.ref("state");
    this.$context = JExpr.ref("context");
    this.dc = new XMLDeserializerContextImpl(this.$context);
    
    JMethod method = impl.method(1, getRuntime(UnmarshallingEventHandler.class), "createUnmarshaller");
    
    JVar $context = method.param(getRuntime(UnmarshallingContext.class), "context");
    method.body()._return(JExpr._new(this.unmarshaller).arg($context));
    
    this.transitionTable = new TransitionTable(this.automaton);
  }
  
  protected void generate()
  {
    JMethod con = this.unmarshaller.constructor(1);
    JVar $context = con.param(getRuntime(UnmarshallingContext.class), "context");
    con.body().invoke("super").arg($context).arg(JExpr.lit(generateEncodedTextType()));
    if (this.parent.trace)
    {
      this.$tracer = this.unmarshaller.field(4, Tracer.class, "tracer");
      con.body().assign(this.$tracer, $context.invoke("getTracer"));
    }
    JMethod con = this.unmarshaller.constructor(2);
    JVar $context = con.param(getRuntime(UnmarshallingContext.class), "context");
    JVar $init = con.param(this.codeModel.INT, "startState");
    con.body().invoke("this").arg($context);
    con.body().assign(this.$state, $init);
    
    new EnterElementMethodGenerator(this).generate();
    new LeaveElementMethodGenerator(this).generate();
    new EnterAttributeMethodGenerator(this).generate();
    new EnterLeaveMethodGenerator(this, "leaveAttribute", Alphabet.LeaveAttribute.class).generate();
    new TextMethodGenerator(this).generate();
    
    generateLeaveChild();
  }
  
  private JClass getRuntime(Class clazz)
  {
    return this.parent.context.getRuntime(clazz);
  }
  
  public JClass getInterleaveDispatcher(Alphabet.Interleave a)
  {
    JClass cls = (JClass)this.interleaveDispatcherImpls.get(a);
    if (cls != null) {
      return cls;
    }
    JDefinedClass impl = null;
    try
    {
      impl = this.unmarshaller._class(4, "Interleave" + createId());
    }
    catch (JClassAlreadyExistsException e)
    {
      e.printStackTrace();
      _assert(false);
    }
    impl._extends(getRuntime(InterleaveDispatcher.class));
    
    JMethod cstr = impl.constructor(4);
    
    JInvocation arrayInit = JExpr._new(getRuntime(UnmarshallingEventHandler.class).array());
    for (int i = 0; i < a.branches.length; i++) {
      arrayInit.arg(JExpr._new(this.unmarshaller).arg(JExpr._super().ref("sites").component(JExpr.lit(i))).arg(getStateNumber(a.branches[i].initialState)));
    }
    cstr.body().invoke("super").arg(this.$context).arg(JExpr.lit(a.branches.length));
    cstr.body().invoke("init").arg(arrayInit);
    
    generateGetBranchForXXX(impl, a, "Element", 0);
    generateGetBranchForXXX(impl, a, "Attribute", 1);
    
    JMethod m = impl.method(2, this.codeModel.INT, "getBranchForText");
    m.body()._return(JExpr.lit(a.getTextBranchIndex()));
    
    this.interleaveDispatcherImpls.put(a, impl);
    return impl;
  }
  
  private void generateGetBranchForXXX(JDefinedClass clazz, Alphabet.Interleave a, String methodSuffix, int nameIdx)
  {
    JMethod method = clazz.method(2, this.codeModel.INT, "getBranchFor" + methodSuffix);
    JVar $uri = method.param(this.codeModel.ref(String.class), "uri");
    JVar $local = method.param(this.codeModel.ref(String.class), "local");
    for (int i = 0; i < a.branches.length; i++)
    {
      Alphabet.Interleave.Branch br = a.branches[i];
      NameClass nc = br.getName(nameIdx);
      if (!nc.isNull()) {
        method.body()._if(this.parent.generateNameClassTest(nc, $uri, $local))._then()._return(JExpr.lit(i));
      }
    }
    method.body()._return(JExpr.lit(-1));
  }
  
  protected final void generateGoto(JBlock $body, State target)
  {
    generateGoto($body, getStateNumber(target));
  }
  
  private JExpression getStateNumber(State state)
  {
    return JExpr.lit(this.automaton.getStateNumber(state));
  }
  
  private void generateGoto(JBlock $body, JExpression nextState)
  {
    if (this.parent.trace) {
      $body.invoke(this.$tracer, "nextState").arg(nextState);
    }
    $body.assign(this.$state, nextState);
  }
  
  protected void generateLeaveChild()
  {
    if (!this.parent.trace) {
      return;
    }
    JMethod method = this.unmarshaller.method(1, this.codeModel.VOID, "leaveChild");
    
    method._throws(SAXException.class);
    
    JVar $nextState = method.param(this.codeModel.INT, "nextState");
    
    method.body().invoke(this.$tracer, "nextState").arg($nextState);
    method.body().invoke(JExpr._super(), "leaveChild").arg($nextState);
  }
  
  private String generateEncodedTextType()
  {
    StringBuffer buf = new StringBuffer(this.automaton.getStateSize());
    for (int i = this.automaton.getStateSize() - 1; i >= 0; i--) {
      buf.append('-');
    }
    Iterator itr = this.automaton.states();
    while (itr.hasNext())
    {
      State s = (State)itr.next();
      
      buf.setCharAt(this.automaton.getStateNumber(s), s.isListState ? 'L' : '-');
    }
    return buf.toString();
  }
  
  private final Map eatTextFunctions = new HashMap();
  
  protected final void eatText(JBlock block, Alphabet.BoundText ta, JExpression $attValue)
  {
    JMethod method = (JMethod)this.eatTextFunctions.get(ta);
    if (method == null)
    {
      method = generateEatTextFunction(ta);
      this.eatTextFunctions.put(ta, method);
    }
    block.invoke(method).arg($attValue);
  }
  
  private JMethod generateEatTextFunction(Alphabet.BoundText ta)
  {
    JMethod method = this.unmarshaller.method(4, this.codeModel.VOID, "eatText" + createId());
    method._throws(SAXException.class);
    JVar $value = method.param(8, String.class, "value");
    
    JTryBlock $try = method.body()._try();
    JCatchBlock $catch = $try._catch(this.codeModel.ref(Exception.class));
    $catch.body().invoke("handleParseConversionException").arg($catch.param("e"));
    if (this.parent.trace) {
      $try.body().invoke(this.$tracer, "onConvertValue").arg($value).arg(JExpr.lit(ta.field.getFieldUse().name));
    }
    if (!ta.item.xducer.needsDelayedDeserialization())
    {
      ta.field.setter($try.body(), TypeAdaptedTransducer.adapt(ta.item.xducer, ta.field).generateDeserializer($value, this.dc));
    }
    else
    {
      JDefinedClass patcher = this.codeModel.newAnonymousClass(this.codeModel.ref(Runnable.class));
      
      JMethod run = patcher.method(1, this.codeModel.VOID, "run");
      
      ta.field.setter(run.body(), ta.item.xducer.generateDeserializer($value, this.dc));
      
      $try.body().invoke(this.$context, "addPatcher").arg(JExpr._new(patcher));
    }
    return method;
  }
  
  private final Map dispatchLookupFunctions = new HashMap();
  
  protected final JExpression invokeLookup(Alphabet.Dispatch da, TransitionTable.Entry tte)
  {
    JMethod lookup = (JMethod)this.dispatchLookupFunctions.get(da);
    if (lookup == null) {
      this.dispatchLookupFunctions.put(da, lookup = generateDispatchFunction(da, tte));
    }
    return JExpr.invoke(lookup);
  }
  
  protected final JMethod generateDispatchFunction(Alphabet.Dispatch da, TransitionTable.Entry tte)
  {
    JMethod lookup = this.unmarshaller.method(4, Class.class, "lookup" + createId());
    lookup._throws(SAXException.class);
    JBlock body = lookup.body();
    JExpression $context = JExpr.ref("context");
    
    JVar $idx = body.decl(this.codeModel.INT, "idx", $context.invoke("getAttribute").arg(JExpr.lit(da.attName.namespaceURI)).arg(JExpr.lit(da.attName.localName)));
    
    JConditional cond = body._if($idx.gte(JExpr.lit(0)));
    cond._then()._return(da.table.lookup(this.parent.context, $context.invoke("eatAttribute").arg($idx), $context));
    
    cond._else()._return(JExpr._null());
    
    return lookup;
  }
  
  private static final void _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\PerClassGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */