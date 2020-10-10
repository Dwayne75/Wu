package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JStringLiteral;
import com.sun.codemodel.JVar;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;

class URIPass
  extends AbstractPassImpl
{
  URIPass(Context _context)
  {
    super(_context, "URIs");
  }
  
  public void onElement(ElementExp exp)
  {
    if (this.context.isInside()) {
      this.context.skipPass.build(exp.contentModel);
    }
  }
  
  public void onExternal(ExternalItem item)
  {
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    fmg.increment(this.context.getCurrentBlock());
  }
  
  public void onAttribute(AttributeExp exp)
  {
    XmlNameStoreAlgorithm algorithm = XmlNameStoreAlgorithm.get(exp);
    JExpression namespaceURI = algorithm.getNamespaceURI();
    if ((!(namespaceURI instanceof JStringLiteral)) || (!((JStringLiteral)namespaceURI).str.equals(""))) {
      getBlock(true).invoke(this.context.$serializer.invoke("getNamespaceContext"), "declareNamespace").arg(namespaceURI).arg(JExpr._null()).arg(JExpr.TRUE);
    }
    this.context.uriPass.build(exp.exp);
  }
  
  public void onPrimitive(PrimitiveItem exp)
  {
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    
    Transducer xducer = TypeAdaptedTransducer.adapt(exp.xducer, fmg.owner().getFieldUse().type);
    
    xducer.declareNamespace(this.context.getCurrentBlock(), JExpr.cast(xducer.getReturnType(), fmg.peek(false)), this.context);
    
    fmg.increment(this.context.getCurrentBlock());
  }
  
  public void onValue(ValueExp exp) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\URIPass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */