package com.sun.tools.xjc.generator.marshaller;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JTryBlock;
import com.sun.tools.xjc.generator.util.BlockReference;
import com.sun.tools.xjc.runtime.Util;

class PrintExceptionTryCatchBlockReference
  implements BlockReference
{
  private final BlockReference parent;
  private final Context context;
  private JTryBlock block = null;
  
  PrintExceptionTryCatchBlockReference(Context _context)
  {
    this.context = _context;
    this.parent = this.context.getCurrentBlock();
  }
  
  public JBlock get(boolean create)
  {
    if ((!create) && (this.block == null)) {
      return null;
    }
    if (this.block == null)
    {
      this.block = this.parent.get(true)._try();
      
      JCodeModel codeModel = this.context.codeModel;
      
      JCatchBlock $catch = this.block._catch(codeModel.ref(Exception.class));
      $catch.body().staticInvoke(this.context.getRuntime(Util.class), "handlePrintConversionException").arg(JExpr._this()).arg($catch.param("e")).arg(this.context.$serializer);
    }
    return this.block.body();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\marshaller\PrintExceptionTryCatchBlockReference.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */