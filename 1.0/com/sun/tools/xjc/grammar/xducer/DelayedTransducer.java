package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;

public abstract class DelayedTransducer
  implements Transducer
{
  private Transducer core = null;
  
  protected abstract Transducer create();
  
  private void update()
  {
    if (this.core == null) {
      this.core = create();
    }
  }
  
  public JType getReturnType()
  {
    update();
    return this.core.getReturnType();
  }
  
  public boolean isBuiltin()
  {
    return false;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    update();
    return this.core.generateSerializer(value, context);
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    update();
    return this.core.generateDeserializer(literal, context);
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    update();
    return this.core.generateConstant(exp);
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context)
  {
    update();
    this.core.declareNamespace(body, value, context);
  }
  
  public boolean needsDelayedDeserialization()
  {
    update();
    return this.core.needsDelayedDeserialization();
  }
  
  public void populate(AnnotatedGrammar grammar, GeneratorContext context)
  {
    update();
    this.core.populate(grammar, context);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\DelayedTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */