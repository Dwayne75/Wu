package com.sun.tools.xjc.generator.unmarshaller;

import com.sun.codemodel.JBlock;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Alphabet.EnterAttribute;
import com.sun.tools.xjc.generator.unmarshaller.automaton.Transition;

class EnterAttributeMethodGenerator
  extends EnterLeaveMethodGenerator
{
  public EnterAttributeMethodGenerator(PerClassGenerator parent)
  {
    super(parent, "enterAttribute", Alphabet.EnterAttribute.class);
  }
  
  protected void generateAction(Alphabet alpha, Transition tr, JBlock $body)
  {
    if (tr.alphabet == alpha)
    {
      Alphabet.EnterAttribute ea = (Alphabet.EnterAttribute)alpha;
      
      XmlNameStoreAlgorithm.get(ea.name).onNameUnmarshalled(this.codeModel, $body, this.$uri, this.$local);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\unmarshaller\EnterAttributeMethodGenerator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */