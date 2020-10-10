package com.sun.tools.xjc.generator;

import com.sun.msv.grammar.ChoiceExp;
import java.util.HashMap;
import java.util.Map;

class LookupTableCache
  implements LookupTableBuilder
{
  private final Map cache = new HashMap();
  private final LookupTableBuilder core;
  
  public LookupTableCache(LookupTableBuilder _core)
  {
    this.core = _core;
  }
  
  public LookupTableUse buildTable(ChoiceExp exp)
  {
    if (this.cache.containsKey(exp)) {
      return (LookupTableUse)this.cache.get(exp);
    }
    LookupTableUse t = this.core.buildTable(exp);
    this.cache.put(exp, t);
    return t;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\LookupTableCache.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */