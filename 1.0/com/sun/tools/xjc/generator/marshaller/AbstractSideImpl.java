package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JPrimitiveType;
import com.sun.codemodel.JType;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.xml.bind.JAXBAssertionError;

abstract class AbstractSideImpl
  implements Side
{
  protected final Context context;
  
  protected AbstractSideImpl(Context _context)
  {
    this.context = _context;
  }
  
  protected final JBlock getBlock(boolean create)
  {
    return this.context.getCurrentBlock().get(create);
  }
  
  protected final BlockReference createWhileBlock(BlockReference parent, JExpression expr)
  {
    return new AbstractSideImpl.1(this, parent, expr);
  }
  
  protected final JExpression instanceOf(JExpression obj, JType type)
  {
    if (this.context.codeModel.NULL == type) {
      return obj.eq(JExpr._null());
    }
    if ((type instanceof JPrimitiveType)) {
      type = ((JPrimitiveType)type).getWrapperClass();
    }
    return obj._instanceof(type);
  }
  
  protected static Object _assert(boolean b)
  {
    if (!b) {
      throw new JAXBAssertionError();
    }
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\AbstractSideImpl.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */