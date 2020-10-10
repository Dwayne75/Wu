package com.sun.tools.xjc.reader.relaxng;

import java.util.HashSet;
import java.util.Set;
import org.kohsuke.rngom.digested.DDefine;
import org.kohsuke.rngom.digested.DGrammarPattern;
import org.kohsuke.rngom.digested.DPattern;
import org.kohsuke.rngom.digested.DPatternWalker;
import org.kohsuke.rngom.digested.DRefPattern;

final class DefineFinder
  extends DPatternWalker
{
  public final Set<DDefine> defs = new HashSet();
  
  public Void onGrammar(DGrammarPattern p)
  {
    for (DDefine def : p)
    {
      this.defs.add(def);
      def.getPattern().accept(this);
    }
    return (Void)p.getStart().accept(this);
  }
  
  public Void onRef(DRefPattern p)
  {
    return null;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\relaxng\DefineFinder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */