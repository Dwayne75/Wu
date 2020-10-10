package javax.jnlp;

import java.io.File;
import java.io.IOException;

public abstract interface ExtendedService
{
  public abstract FileContents openFile(File paramFile)
    throws IOException;
  
  public abstract FileContents[] openFiles(File[] paramArrayOfFile)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\ExtendedService.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */