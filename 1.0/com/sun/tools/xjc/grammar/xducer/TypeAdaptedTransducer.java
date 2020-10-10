package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.FieldUse;

public final class TypeAdaptedTransducer
  extends TransducerDecorator
{
  private final JType expectedType;
  private final boolean boxing;
  
  public static Transducer adapt(Transducer xducer, FieldRenderer fieldRenderer)
  {
    return adapt(xducer, fieldRenderer.getFieldUse().type);
  }
  
  public static Transducer adapt(Transducer xducer, JType expectedType)
  {
    JType t = xducer.getReturnType();
    if (((t instanceof JPrimitiveType)) && ((expectedType instanceof JClass)))
    {
      expectedType = ((JPrimitiveType)t).getWrapperClass();
      return new TypeAdaptedTransducer(xducer, expectedType);
    }
    if (((t instanceof JClass)) && ((expectedType instanceof JPrimitiveType))) {
      return new TypeAdaptedTransducer(xducer, expectedType);
    }
    return xducer;
  }
  
  private TypeAdaptedTransducer(Transducer _xducer, JType _expectedType)
  {
    super(_xducer);
    this.expectedType = _expectedType;
    this.boxing = (this.expectedType instanceof JClass);
  }
  
  public JType getReturnType()
  {
    return this.expectedType;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    if (this.boxing) {
      return super.generateSerializer(((JPrimitiveType)super.getReturnType()).unwrap(value), context);
    }
    return super.generateSerializer(((JPrimitiveType)this.expectedType).wrap(value), context);
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    if (this.boxing) {
      return ((JPrimitiveType)super.getReturnType()).wrap(super.generateDeserializer(literal, context));
    }
    return ((JPrimitiveType)this.expectedType).unwrap(super.generateDeserializer(literal, context));
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    if (this.boxing) {
      return ((JPrimitiveType)super.getReturnType()).wrap(super.generateConstant(exp));
    }
    return ((JPrimitiveType)this.expectedType).unwrap(super.generateConstant(exp));
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context)
  {
    if (this.boxing) {
      super.declareNamespace(body, ((JPrimitiveType)super.getReturnType()).unwrap(value), context);
    } else {
      super.declareNamespace(body, ((JPrimitiveType)this.expectedType).wrap(value), context);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\TypeAdaptedTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */