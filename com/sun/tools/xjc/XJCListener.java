package com.sun.tools.xjc;

import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.outline.Outline;

public abstract class XJCListener
  implements ErrorListener
{
  /**
   * @deprecated
   */
  public void generatedFile(String fileName) {}
  
  public void generatedFile(String fileName, int current, int total)
  {
    generatedFile(fileName);
  }
  
  public void message(String msg) {}
  
  public void compiled(Outline outline) {}
  
  public boolean isCanceled()
  {
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\XJCListener.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */