package org.kohsuke.rngom.digested;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DGrammarPattern
  extends DPattern
  implements Iterable<DDefine>
{
  private final Map<String, DDefine> patterns = new HashMap();
  DPattern start;
  
  public DPattern getStart()
  {
    return this.start;
  }
  
  public DDefine get(String name)
  {
    return (DDefine)this.patterns.get(name);
  }
  
  DDefine getOrAdd(String name)
  {
    if (this.patterns.containsKey(name)) {
      return get(name);
    }
    DDefine d = new DDefine(name);
    this.patterns.put(name, d);
    return d;
  }
  
  public Iterator<DDefine> iterator()
  {
    return this.patterns.values().iterator();
  }
  
  public boolean isNullable()
  {
    return this.start.isNullable();
  }
  
  public <V> V accept(DPatternVisitor<V> visitor)
  {
    return (V)visitor.onGrammar(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DGrammarPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */