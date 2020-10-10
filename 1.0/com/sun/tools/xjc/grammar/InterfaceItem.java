package com.sun.tools.xjc.grammar;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import com.sun.msv.grammar.Expression;
import org.xml.sax.Locator;

public class InterfaceItem
  extends TypeItem
{
  private final JClass type;
  
  protected InterfaceItem(JClass _type, Expression body, Locator loc)
  {
    super(_type.name(), loc);
    this.type = _type;
    this.exp = body;
  }
  
  public JType getType()
  {
    return this.type;
  }
  
  public JClass getTypeAsClass()
  {
    return this.type;
  }
  
  public ClassItem getSuperType()
  {
    return null;
  }
  
  public Object visitJI(JavaItemVisitor visitor)
  {
    return visitor.onInterface(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\grammar\InterfaceItem.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */