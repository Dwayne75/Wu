package com.sun.tools.xjc.generator.cls;

import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.generator.ClassContext;
import com.sun.tools.xjc.generator.MethodWriter;
import com.sun.tools.xjc.grammar.ClassItem;

public abstract interface ImplStructureStrategy
{
  public abstract JDefinedClass createImplClass(ClassItem paramClassItem);
  
  public abstract MethodWriter createMethodWriter(ClassContext paramClassContext);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\generator\cls\ImplStructureStrategy.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */