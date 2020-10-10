package com.sun.tools.xjc.reader.dtd.bindinfo;

import com.sun.tools.xjc.grammar.xducer.Transducer;

public abstract interface BIConversion
{
  public abstract String name();
  
  public abstract Transducer getTransducer();
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\dtd\bindinfo\BIConversion.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */