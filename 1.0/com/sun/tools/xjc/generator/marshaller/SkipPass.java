package com.sun.tools.xjc.generator.marshaller;

import com.sun.msv.grammar.AttributeExp;
import com.sun.msv.grammar.ElementExp;
import com.sun.msv.grammar.ValueExp;
import com.sun.tools.xjc.grammar.ExternalItem;
import com.sun.tools.xjc.grammar.PrimitiveItem;

public class SkipPass
  extends AbstractPassImpl
{
  SkipPass(Context _context)
  {
    super(_context, "Skip");
  }
  
  public void onElement(ElementExp exp)
  {
    this.context.skipPass.build(exp.contentModel);
  }
  
  public void onExternal(ExternalItem item)
  {
    increment();
  }
  
  public void onAttribute(AttributeExp exp)
  {
    this.context.skipPass.build(exp.exp);
  }
  
  public void onPrimitive(PrimitiveItem exp)
  {
    increment();
  }
  
  public void onValue(ValueExp exp) {}
  
  private void increment()
  {
    FieldMarshallerGenerator fmg = this.context.getCurrentFieldMarshaller();
    fmg.increment(this.context.getCurrentBlock());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\SkipPass.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */