package com.sun.tools.xjc.grammar.xducer;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JType;
import com.sun.msv.datatype.xsd.QnameValueType;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.generator.util.WhitespaceNormalizer;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;

public class QNameTransducer
  extends TransducerImpl
{
  private final JCodeModel codeModel;
  
  public QNameTransducer(JCodeModel cm)
  {
    this.codeModel = cm;
  }
  
  public void declareNamespace(BlockReference body, JExpression value, SerializerContext context)
  {
    context.declareNamespace(body.get(true), value.invoke("getNamespaceURI"), value.invoke("getPrefix"), JExpr.FALSE);
  }
  
  public JExpression generateSerializer(JExpression value, SerializerContext context)
  {
    return this.codeModel.ref(DatatypeConverter.class).staticInvoke("printQName").arg(value).arg(context.getNamespaceContext());
  }
  
  public JExpression generateDeserializer(JExpression lexical, DeserializerContext context)
  {
    return this.codeModel.ref(DatatypeConverter.class).staticInvoke("parseQName").arg(WhitespaceNormalizer.COLLAPSE.generate(this.codeModel, lexical)).arg(context.getNamespaceContext());
  }
  
  public JType getReturnType()
  {
    return this.codeModel.ref(QName.class);
  }
  
  public JExpression generateConstant(ValueExp exp)
  {
    QnameValueType data = (QnameValueType)exp.value;
    
    return JExpr._new(this.codeModel.ref(QName.class)).arg(JExpr.lit(data.namespaceURI)).arg(JExpr.lit(data.localPart));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\xducer\QNameTransducer.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */