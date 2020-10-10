package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.xml.bind.DatatypeConverterImpl;

public class UserTransducer
  extends TransducerImpl
{
  private final JType type;
  private final JCodeModel codeModel;
  private final String parseMethod;
  private final String printMethod;
  private final boolean enableNamespaceContext;
  private static final String ERR_EXTERNAL_PARSE_METHOD_REQUIRED = "UserTransducer.ExternalParseMethodRequired";
  private static final String ERR_EXTERNAL_PRINT_METHOD_REQUIRED = "UserTransducer.ExternalPrintMethodRequired";
  
  public UserTransducer(JType _type, String _parseMethod, String _printMethod, boolean _enableNamespaceContext)
  {
    this.type = _type;
    this.codeModel = _type.owner();
    
    this.parseMethod = _parseMethod;
    this.printMethod = _printMethod;
    
    this.enableNamespaceContext = _enableNamespaceContext;
    if (this.type.isPrimitive())
    {
      if (this.parseMethod.indexOf('.') == -1) {
        throw new IllegalArgumentException(Messages.format("UserTransducer.ExternalParseMethodRequired", _type.name()));
      }
      if (this.printMethod.indexOf('.') == -1) {
        throw new IllegalArgumentException(Messages.format("UserTransducer.ExternalPrintMethodRequired", _type.name()));
      }
    }
  }
  
  public UserTransducer(JType _type, String _parseMethod, String _printMethod)
  {
    this(_type, _parseMethod, _printMethod, false);
  }
  
  public JType getReturnType()
  {
    return this.type;
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return _generateSerializer(value, context);
  }
  
  private JInvocation _generateSerializer(JExpression value, SerializerContext context)
  {
    int idx = this.printMethod.lastIndexOf('.');
    JInvocation inv;
    JInvocation inv;
    if (idx < 0) {
      inv = value.invoke(this.printMethod);
    } else {
      try
      {
        inv = this.codeModel.ref(this.printMethod.substring(0, idx)).staticInvoke(this.printMethod.substring(idx + 1)).arg(value);
      }
      catch (ClassNotFoundException e)
      {
        throw new NoClassDefFoundError(e.getMessage());
      }
    }
    if (this.enableNamespaceContext) {
      inv.arg(context.getNamespaceContext());
    }
    return inv;
  }
  
  public JExpression generateDeserializer(JExpression literal, DeserializerContext context)
  {
    JInvocation inv;
    JInvocation inv;
    if (this.parseMethod.equals("new"))
    {
      inv = JExpr._new(this.type);
    }
    else
    {
      int idx = this.parseMethod.lastIndexOf('.');
      JInvocation inv;
      if (idx < 0) {
        inv = ((JClass)this.type).staticInvoke(this.parseMethod);
      } else {
        try
        {
          inv = this.codeModel.ref(this.parseMethod.substring(0, idx)).staticInvoke(this.parseMethod.substring(idx + 1));
        }
        catch (ClassNotFoundException e)
        {
          throw new NoClassDefFoundError(e.getMessage());
        }
      }
    }
    inv.arg(literal);
    if (this.enableNamespaceContext) {
      inv.arg(context.getNamespaceContext());
    }
    return inv;
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context)
  {
    if (this.enableNamespaceContext) {
      body.get(true).add(_generateSerializer(value, context));
    }
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    return generateDeserializer(this.codeModel.ref(DatatypeConverterImpl.class).staticInvoke("installHook").arg(JExpr.lit(obtainString(exp))), null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\UserTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */