package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.GeneratorContext;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.grammar.AnnotatedGrammar;
import com.sun.tools.xjc.grammar.id.SymbolSpace;

public abstract interface Transducer
{
  public abstract JType getReturnType();
  
  public abstract void populate(AnnotatedGrammar paramAnnotatedGrammar, GeneratorContext paramGeneratorContext);
  
  public abstract JExpression generateSerializer(JExpression paramJExpression, SerializerContext paramSerializerContext);
  
  public abstract void declareNamespace(BlockReference paramBlockReference, JExpression paramJExpression, SerializerContext paramSerializerContext);
  
  public abstract JExpression generateDeserializer(JExpression paramJExpression, DeserializerContext paramDeserializerContext);
  
  public abstract boolean needsDelayedDeserialization();
  
  public abstract boolean isID();
  
  public abstract SymbolSpace getIDSymbolSpace();
  
  public abstract boolean isBuiltin();
  
  public abstract JExpression generateConstant(ValueExp paramValueExp);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\Transducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */