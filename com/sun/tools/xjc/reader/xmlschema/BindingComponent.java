package com.sun.tools.xjc.reader.xmlschema;

import com.sun.tools.xjc.reader.Ring;

public abstract class BindingComponent
{
  protected BindingComponent()
  {
    Ring.add(this);
  }
  
  protected final ErrorReporter getErrorReporter()
  {
    return (ErrorReporter)Ring.get(ErrorReporter.class);
  }
  
  protected final ClassSelector getClassSelector()
  {
    return (ClassSelector)Ring.get(ClassSelector.class);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\BindingComponent.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */