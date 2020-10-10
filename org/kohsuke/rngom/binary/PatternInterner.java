package org.kohsuke.rngom.binary;

final class PatternInterner
{
  private static final int INIT_SIZE = 256;
  private static final float LOAD_FACTOR = 0.3F;
  private Pattern[] table;
  private int used;
  private int usedLimit;
  
  PatternInterner()
  {
    this.table = null;
    this.used = 0;
    this.usedLimit = 0;
  }
  
  PatternInterner(PatternInterner parent)
  {
    this.table = parent.table;
    if (this.table != null) {
      this.table = ((Pattern[])this.table.clone());
    }
    this.used = parent.used;
    this.usedLimit = parent.usedLimit;
  }
  
  Pattern intern(Pattern p)
  {
    int h;
    int h;
    if (this.table == null)
    {
      this.table = new Pattern['Ā'];
      this.usedLimit = 76;
      h = firstIndex(p);
    }
    else
    {
      for (h = firstIndex(p); this.table[h] != null; h = nextIndex(h)) {
        if (p.samePattern(this.table[h])) {
          return this.table[h];
        }
      }
    }
    if (this.used >= this.usedLimit)
    {
      Pattern[] oldTable = this.table;
      this.table = new Pattern[this.table.length << 1];
      for (int i = oldTable.length; i > 0;)
      {
        i--;
        if (oldTable[i] != null)
        {
          int j = firstIndex(oldTable[i]);
          while (this.table[j] != null) {
            j = nextIndex(j);
          }
          this.table[j] = oldTable[i];
        }
      }
      for (h = firstIndex(p); this.table[h] != null; h = nextIndex(h)) {}
      this.usedLimit = ((int)(this.table.length * 0.3F));
    }
    this.used += 1;
    this.table[h] = p;
    return p;
  }
  
  private int firstIndex(Pattern p)
  {
    return p.patternHashCode() & this.table.length - 1;
  }
  
  private int nextIndex(int i)
  {
    return i == 0 ? this.table.length - 1 : i - 1;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\binary\PatternInterner.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */