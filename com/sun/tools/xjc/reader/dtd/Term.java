package com.sun.tools.xjc.reader.dtd;

import java.util.List;

abstract class Term
{
  static final Term EMPTY = new Term()
  {
    void normalize(List<Block> r, boolean optional) {}
    
    void addAllElements(Block b) {}
    
    boolean isOptional()
    {
      return false;
    }
    
    boolean isRepeated()
    {
      return false;
    }
  };
  
  abstract void normalize(List<Block> paramList, boolean paramBoolean);
  
  abstract void addAllElements(Block paramBlock);
  
  abstract boolean isOptional();
  
  abstract boolean isRepeated();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\Term.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */