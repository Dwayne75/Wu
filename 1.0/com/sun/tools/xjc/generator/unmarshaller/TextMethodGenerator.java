package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.BoundText;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Child;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EverythingElse;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.IgnoredText;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Reference;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.SuperClass;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Text;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;
import com.sun.tools.xjc.generator.validator.StringOutputStream;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.runtime.ValidationContextAdaptor;
import com.sun.xml.bind.unmarshaller.DatatypeDeserializer;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import org.relaxng.datatype.Datatype;

class TextMethodGenerator
  extends HandlerMethodGenerator
{
  private JVar $value;
  
  TextMethodGenerator(PerClassGenerator parent)
  {
    super(parent, "text", Alphabet.Text.class);
  }
  
  private boolean needsGuard(State state)
  {
    int count = 0;
    TransitionTable.Entry[] e = this.table.list(state);
    for (int i = 0; i < e.length; i++) {
      if (e[i].alphabet.isText()) {
        count++;
      }
    }
    return count > 1;
  }
  
  private JExpression guardClause(Alphabet a)
  {
    if (((a instanceof Alphabet.IgnoredText)) || ((a instanceof Alphabet.SuperClass)) || ((a instanceof Alphabet.Child)) || ((a instanceof Alphabet.EverythingElse))) {
      return JExpr.TRUE;
    }
    _assert(a instanceof Alphabet.BoundText);
    DatabindableDatatype guard = ((Alphabet.BoundText)a).item.guard;
    
    StringWriter sw = new StringWriter();
    try
    {
      ObjectOutputStream oos = new ObjectOutputStream(new StringOutputStream(sw));
      oos.writeObject(guard);
      oos.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      throw new InternalError("unserializable datatype:" + guard);
    }
    JVar $dt = this.parent.context.implClass.field(28, class$org$relaxng$datatype$Datatype, "___dt" + this.datatypeId++, this.codeModel.ref(DatatypeDeserializer.class).staticInvoke("deserialize").arg(JExpr.lit(sw.getBuffer().toString())));
    JExpression con;
    JExpression con;
    if (guard.isContextDependent()) {
      con = JExpr._new(this.parent.parent.context.getRuntime(ValidationContextAdaptor.class)).arg(this.parent.$context);
    } else {
      con = JExpr._null();
    }
    return $dt.invoke("isValid").arg(this.$value).arg(con);
  }
  
  private int datatypeId = 0;
  
  protected boolean performTransition(State state, Alphabet alphabet, Transition action)
  {
    JBlock block = getCase(state);
    
    boolean needsGuard = needsGuard(state);
    if (needsGuard) {
      block = block._if(guardClause(alphabet))._then();
    }
    if (action == Transition.REVERT_TO_PARENT)
    {
      generateRevertToParent(block);
      return needsGuard;
    }
    if ((action.alphabet instanceof Alphabet.Reference))
    {
      generateSpawnChild(block, action);
      return needsGuard;
    }
    if ((action.alphabet instanceof Alphabet.BoundText)) {
      this.parent.eatText(block, action.alphabet.asBoundText(), this.$value);
    }
    generateGoto(block, action.to);
    block._return();
    return needsGuard;
  }
  
  protected String getNameOfMethodDecl()
  {
    return "handleText";
  }
  
  protected JSwitch makeSwitch(JMethod method, JBlock body)
  {
    this.$value = method.param(8, String.class, "value");
    if (this.trace) {
      body.invoke(this.$tracer, "onText").arg(this.$value);
    }
    JTryBlock tryBlock = body._try();
    JSwitch s = super.makeSwitch(method, tryBlock.body());
    
    JCatchBlock c = tryBlock._catch(this.codeModel.ref(RuntimeException.class));
    JVar $e = c.param("e");
    c.body().invoke("handleUnexpectedTextException").arg(this.$value).arg($e);
    
    return s;
  }
  
  protected void addParametersToContextSwitch(JInvocation inv)
  {
    inv.arg(this.$value);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\TextMethodGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */