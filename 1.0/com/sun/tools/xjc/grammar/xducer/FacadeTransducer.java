package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;

public class FacadeTransducer
  implements Transducer
{
  private final Transducer marshaller;
  private final Transducer unmarshaller;
  
  public FacadeTransducer(Transducer _marshaller, Transducer _unmarshaller)
  {
    this.marshaller = _marshaller;
    this.unmarshaller = _unmarshaller;
  }
  
  public JType getReturnType()
  {
    return this.marshaller.getReturnType();
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
  
  public void populate(AnnotatedGrammar grammar, GeneratorContext context)
  {
    this.marshaller.populate(grammar, context);
    this.unmarshaller.populate(grammar, context);
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return this.marshaller.generateSerializer(value, context);
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context)
  {
    this.marshaller.declareNamespace(body, value, context);
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return this.unmarshaller.generateDeserializer(literal, context);
  }
  
  public boolean needsDelayedDeserialization()
  {
    return this.unmarshaller.needsDelayedDeserialization();
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    return this.unmarshaller.generateConstant(exp);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\FacadeTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */