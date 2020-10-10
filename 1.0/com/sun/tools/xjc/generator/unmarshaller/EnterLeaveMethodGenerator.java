package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.NameClass;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Named;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.Reference;
import com.sun.tools.xjc.generator.unmarshaller.automaton.State;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;

class EnterLeaveMethodGenerator
  extends HandlerMethodGenerator
{
  protected JVar $uri;
  protected JVar $local;
  protected JVar $qname;
  
  EnterLeaveMethodGenerator(PerClassGenerator parent, String _methodName, Class _alphabetType)
  {
    super(parent, _methodName, _alphabetType);
  }
  
  protected void generateAction(Alphabet alpha, Transition tr, JBlock $body) {}
  
  protected boolean performTransition(State state, Alphabet alphabet, Transition action)
  {
    JBlock $body = getCase(state);
    if (alphabet.isNamed()) {
      $body = $body._if(generateNameClassTest(alphabet.asNamed().name))._then();
    }
    generateAction(alphabet, action, $body);
    if (action == Transition.REVERT_TO_PARENT)
    {
      generateRevertToParent($body);
    }
    else if ((action.alphabet instanceof Alphabet.Reference))
    {
      generateSpawnChild($body, action);
    }
    else
    {
      generateGoto($body, action.to);
      $body._return();
    }
    return alphabet.isNamed();
  }
  
  protected JSwitch makeSwitch(JMethod method, JBlock body)
  {
    declareParameters(method);
    if (this.trace) {
      body.invoke(this.$tracer, "on" + capitalize()).arg(this.$uri).arg(this.$local);
    }
    JSwitch s = super.makeSwitch(method, body);
    
    addParametersToContextSwitch(body.invoke(JExpr.ref("super"), method));
    
    return s;
  }
  
  protected void declareParameters(JMethod method)
  {
    this.$uri = method.param(String.class, "___uri");
    this.$local = method.param(String.class, "___local");
    this.$qname = method.param(String.class, "___qname");
  }
  
  protected void addParametersToContextSwitch(JInvocation inv)
  {
    inv.arg(this.$uri).arg(this.$local).arg(this.$qname);
  }
  
  private JExpression generateNameClassTest(NameClass nc)
  {
    getSwitch();
    
    return this.parent.parent.generateNameClassTest(nc, this.$uri, this.$local);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\EnterLeaveMethodGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */