package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JType;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.xducer.Transducer;
import org.xml.sax.Locator;

public class PrimitiveItem
  extends TypeItem
{
  public final Transducer xducer;
  public final DatabindableDatatype guard;
  
  protected PrimitiveItem(Transducer _xducer, DatabindableDatatype _guard, Expression _exp, Locator loc)
  {
    super(_xducer.toString(), loc);
    
    this.xducer = _xducer;
    this.exp = _exp;
    this.guard = _guard;
  }
  
  public JType getType()
  {
    return this.xducer.getReturnType();
  }
  
  public Object visitJI(JavaItemVisitor visitor)
  {
    return visitor.onPrimitive(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\PrimitiveItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */