package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;

public abstract class TransducerDecorator
  implements Transducer
{
  protected final Transducer core;
  
  protected TransducerDecorator(Transducer _core)
  {
    this.core = _core;
  }
  
  public JType getReturnType()
  {
    return this.core.getReturnType();
  }
  
  public boolean isBuiltin()
  {
    return false;
  }
  
  public void populate(AnnotatedGrammar grammar, GeneratorContext context)
  {
    this.core.populate(grammar, context);
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return this.core.generateSerializer(value, context);
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context)
  {
    this.core.declareNamespace(body, value, context);
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return this.core.generateDeserializer(literal, context);
  }
  
  public boolean needsDelayedDeserialization()
  {
    return this.core.needsDelayedDeserialization();
  }
  
  public boolean isID()
  {
    return this.core.isID();
  }
  
  public SymbolSpace getIDSymbolSpace()
  {
    return this.core.getIDSymbolSpace();
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    return this.core.generateConstant(exp);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\TransducerDecorator.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */