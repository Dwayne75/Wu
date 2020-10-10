package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.XSDatatype;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;

public abstract class TransducerImpl
  implements Transducer
{
  public void populate(AnnotatedGrammar grammar, GeneratorContext context) {}
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context) {}
  
  public boolean needsDelayedDeserialization()
  {
    return false;
  }
  
  public boolean isID()
  {
    return false;
  }
  
  public SymbolSpace getIDSymbolSpace()
  {
    return null;
  }
  
  public boolean isBuiltin()
  {
    return false;
  }
  
  public String toString()
  {
    String className = getClass().getName();
    int idx = className.lastIndexOf('.');
    if (idx >= 0) {
      className = className.substring(idx + 1);
    }
    return className + ":" + getReturnType().name();
  }
  
  protected final String obtainString(ValueExp exp)
  {
    return ((XSDatatype)exp.dt).convertToLexicalValue(exp.value, null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\TransducerImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */