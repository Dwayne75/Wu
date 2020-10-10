package com.sun.tools.xjc.reader.dtd;

import java.util.LinkedHashSet;
import java.util.Set;

final class Block
{
  final boolean isOptional;
  final boolean isRepeated;
  final Set<Element> elements = new LinkedHashSet();
  
  Block(boolean optional, boolean repeated)
  {
    this.isOptional = optional;
    this.isRepeated = repeated;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\dtd\Block.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */