package com.sun.tools.xjc.generator;

import com.sun.msv.grammar.Expression;
import com.sun.msv.grammar.SimpleNameClass;

public class LookupTableUse
{
  public final LookupTable table;
  public final Expression anomaly;
  public final SimpleNameClass switchAttName;
  
  LookupTableUse(LookupTable _table, Expression _anomaly, SimpleNameClass _switchAttName)
  {
    this.table = _table;
    this.anomaly = _anomaly;
    this.switchAttName = _switchAttName;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\LookupTableUse.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */