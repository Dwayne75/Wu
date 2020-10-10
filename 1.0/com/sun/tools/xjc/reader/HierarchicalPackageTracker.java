package com.sun.tools.xjc.reader;

import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.ReferenceExp;
import com.sun.msv.grammar.util.ExpressionWalker;
import java.util.HashMap;
import java.util.Map;

public final class HierarchicalPackageTracker
  implements PackageTracker
{
  private final Map dic = new HashMap();
  private JPackage pkg;
  
  public final JPackage get(ReferenceExp exp)
  {
    return (JPackage)this.dic.get(exp);
  }
  
  private final ExpressionWalker visitor = new HierarchicalPackageTracker.1(this);
  
  public final void associate(Expression exp, JPackage _pkg)
  {
    this.pkg = _pkg;
    exp.visit(this.visitor);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\HierarchicalPackageTracker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */