package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.xml.bind.JAXBAssertionError;

public class CastTranducer
  extends TransducerDecorator
{
  private final JPrimitiveType type;
  
  public CastTranducer(JPrimitiveType _type, Transducer _core)
  {
    super(_core);
    this.type = _type;
    if (!super.getReturnType().isPrimitive()) {
      throw new JAXBAssertionError();
    }
  }
  
  public JType getReturnType()
  {
    return this.type;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return super.generateSerializer(JExpr.cast(super.getReturnType(), value), context);
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    return JExpr.cast(this.type, super.generateDeserializer(literal, context));
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context)
  {
    super.declareNamespace(body, JExpr.cast(super.getReturnType(), value), context);
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    return JExpr.cast(this.type, super.generateConstant(exp));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\CastTranducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */