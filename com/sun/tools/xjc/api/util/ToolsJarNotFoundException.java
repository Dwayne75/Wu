package com.sun.tools.xjc.api.util;

import java.io.File;

public final class ToolsJarNotFoundException
  extends Exception
{
  public final File toolsJar;
  
  public ToolsJarNotFoundException(File toolsJar)
  {
    super(calcMessage(toolsJar));
    this.toolsJar = toolsJar;
  }
  
  private static String calcMessage(File toolsJar)
  {
    return Messages.TOOLS_JAR_NOT_FOUND.format(new Object[] { toolsJar.getPath() });
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\api\util\ToolsJarNotFoundException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */