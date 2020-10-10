package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.NameClassAndExpression;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.generator.field.FieldRenderer;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.FieldUse;
import com.sun.tools.xjc.grammar.PrimitiveItem;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import com.sun.tools.xjc.grammar.xducer.TypeAdaptedTransducer;

class BodyPass
  extends AbstractPassImpl
{
  BodyPass(Context _context, String name)
  {
    super(_context, name);
  }
  
  public void onElement(ElementExp exp)
  {
    _onElement(exp);
  }
  
  private void _onElement(NameClassAndExpression exp)
  {
    Expression contentModel = exp.getContentModel();
    
    JBlock block = getBlock(true);
    
    XmlNameStoreAlgorithm algorithm = XmlNameStoreAlgorithm.get(exp.getNameClass());
    block.invoke(this.context.$serializer, "startElement").arg(algorithm.getNamespaceURI()).arg(algorithm.getLocalPart());
    
    BodyPass.FieldCloner fc = new BodyPass.FieldCloner(this, contentModel, true);
    fc.push();
    this.context.uriPass.build(contentModel);
    block.invoke(this.context.$serializer, "endNamespaceDecls");
    fc.pop();
    
    fc = new BodyPass.FieldCloner(this, contentModel, false);
    fc.push();
    this.context.attPass.build(contentModel);
    block.invoke(this.context.$serializer, "endAttributes");
    fc.pop();
    
    this.context.bodyPass.build(contentModel);
    
    block.invoke(this.context.$serializer, "endElement");
  }
  
  public void onExternal(ExternalItem item)
  {
    item.generateMarshaller(this.context.genContext, getBlock(true), this.context.getCurrentFieldMarshaller(), this.context.$serializer);
  }
  
  public void onAttribute(AttributeExp exp) {}
  
  public void onPrimitive(PrimitiveItem exp)
  {
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    
    Transducer xducer = TypeAdaptedTransducer.adapt(exp.xducer, fmg.owner().getFieldUse().type);
    
    getBlock(true).invoke(this.context.$serializer, "text").arg(xducer.generateSerializer(JExpr.cast(xducer.getReturnType(), fmg.peek(true)), this.context)).arg(JExpr.lit(fmg.owner().getFieldUse().name));
  }
  
  public void onValue(ValueExp exp)
  {
    marshalValue(exp);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\BodyPass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */