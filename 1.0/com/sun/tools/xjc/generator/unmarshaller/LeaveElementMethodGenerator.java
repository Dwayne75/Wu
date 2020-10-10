package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.LeaveElement;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;

class LeaveElementMethodGenerator
  extends EnterLeaveMethodGenerator
{
  LeaveElementMethodGenerator(PerClassGenerator parent)
  {
    super(parent, "leaveElement", Alphabet.LeaveElement.class);
  }
  
  protected void generateAction(Alphabet alpha, Transition tr, JBlock body)
  {
    if (tr.alphabet == alpha) {
      body.invoke(this.parent.$context, "popAttributes");
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\LeaveElementMethodGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */