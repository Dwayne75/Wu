package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.grammar.ValueExp;

public class DatabindableXducer
  extends TransducerImpl
{
  private final DatabindableDatatype dt;
  private final JClass returnType;
  
  public DatabindableXducer(JCodeModel writer, DatabindableDatatype _dt)
  {
    this.dt = _dt;
    
    String name = this.dt.getJavaObjectType().getName();
    int idx = name.lastIndexOf(".");
    if (idx < 0) {
      this.returnType = writer._package("").ref(name);
    } else {
      this.returnType = writer._package(name.substring(0, idx)).ref(name.substring(idx + 1));
    }
  }
  
  public JType getReturnType()
  {
    return this.returnType;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    throw new UnsupportedOperationException("TODO");
  }
  
  public JExpression generateDeserializer(JExpression value, DeserializerContext context)
  {
    throw new UnsupportedOperationException("TODO");
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    throw new UnsupportedOperationException("TODO");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\DatabindableXducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */