package com.sun.tools.xjc.generator;

import com.sun.msv.grammar.ChoiceExp;
import java.util.ArrayList;
import java.util.List;

class LookupTableInterner
  implements LookupTableBuilder
{
  private final List liveTable = new ArrayList();
  private final LookupTableBuilder core;
  
  public LookupTableInterner(LookupTableBuilder _core)
  {
    this.core = _core;
  }
  
  public LookupTableUse buildTable(ChoiceExp exp)
  {
    LookupTableUse t = this.core.buildTable(exp);
    if (t == null) {
      return null;
    }
    return new LookupTableUse(intern(t.table), t.anomaly, t.switchAttName);
  }
  
  private LookupTable intern(LookupTable t)
  {
    for (int i = 0; i < this.liveTable.size(); i++)
    {
      LookupTable a = (LookupTable)this.liveTable.get(i);
      if (a.isConsistentWith(t))
      {
        a.absorb(t);
        return a;
      }
    }
    this.liveTable.add(t);
    return t;
  }
  
  public LookupTable[] listTables()
  {
    return (LookupTable[])this.liveTable.toArray(new LookupTable[this.liveTable.size()]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\LookupTableInterner.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */