package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JInvocation;
import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.generator.XmlNameStoreAlgorithm;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;

class AttributePass
  extends AbstractPassImpl
{
  AttributePass(Context _context)
  {
    super(_context, "Attributes");
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
    JBlock block = getBlock(true);
    
    XmlNameStoreAlgorithm algorithm = XmlNameStoreAlgorithm.get(exp);
    block.invoke(this.context.$serializer, "startAttribute").arg(algorithm.getNamespaceURI()).arg(algorithm.getLocalPart());
    
    this.context.bodyPass.build(exp.exp);
    
    block.invoke(this.context.$serializer, "endAttribute");
  }
  
  public void onPrimitive(PrimitiveItem exp)
  {
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    fmg.increment(this.context.getCurrentBlock());
  }
  
  public void onValue(ValueExp exp) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\AttributePass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */