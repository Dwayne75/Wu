package com.sun.tools.xjc.reader;

import com.sun.codemodel.JPackage;
import com.sun.msv.grammar.ReferenceExp;

public abstract interface PackageTracker
{
  public abstract JPackage get(ReferenceExp paramReferenceExp);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\PackageTracker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */