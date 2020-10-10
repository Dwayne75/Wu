package org.fourthline.cling.support.shared;

import java.io.PrintStream;

public class AWTExceptionHandler
{
  public void handle(Throwable ex)
  {
    System.err.println("============= The application encountered an unrecoverable error, exiting... =============");
    ex.printStackTrace(System.err);
    System.err.println("==========================================================================================");
    System.exit(1);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\shared\AWTExceptionHandler.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */