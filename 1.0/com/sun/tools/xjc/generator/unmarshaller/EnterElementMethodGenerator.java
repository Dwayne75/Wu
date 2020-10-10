package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EnterElement;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.External;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;
import com.sun.tools.xjc.grammar.ExternalItem;
import org.xml.sax.Attributes;

class EnterElementMethodGenerator
  extends EnterLeaveMethodGenerator
{
  private JVar $atts;
  
  EnterElementMethodGenerator(PerClassGenerator parent)
  {
    super(parent, "enterElement", Alphabet.EnterElement.class);
  }
  
  protected void generateAction(Alphabet alpha, Transition tr, JBlock body)
  {
    if (tr.alphabet == alpha)
    {
      Alphabet.EnterElement ee = (Alphabet.EnterElement)alpha;
      
      XmlNameStoreAlgorithm.get(ee.name).onNameUnmarshalled(this.codeModel, body, this.$uri, this.$local);
      
      body.invoke(this.parent.$context, "pushAttributes").arg(this.$atts).arg(ee.isDataElement ? JExpr.TRUE : JExpr.FALSE);
    }
  }
  
  protected void declareParameters(JMethod method)
  {
    super.declareParameters(method);
    this.$atts = method.param(Attributes.class, "__atts");
  }
  
  protected void addParametersToContextSwitch(JInvocation inv)
  {
    super.addParametersToContextSwitch(inv);
    inv.arg(this.$atts);
  }
  
  protected void generateSpawnChildFromExternal(JBlock $body, Transition tr, JExpression memento)
  {
    if (this.trace)
    {
      $body.invoke(this.$tracer, "onSpawnWildcard");
      $body.invoke(this.$tracer, "suspend");
    }
    Alphabet.External ae = (Alphabet.External)tr.alphabet;
    
    JExpression co = ae.owner.generateUnmarshaller(this.parent.parent.context, this.parent.$context, $body, memento, this.$uri, this.$local, this.$qname, this.$atts);
    
    JBlock then = $body._if(co.ne(JExpr._null()))._then();
    
    ae.field.setter(then, co);
    
    $body._return();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\EnterElementMethodGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */