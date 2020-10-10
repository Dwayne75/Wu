package javax.jnlp;

import java.io.IOException;

public abstract interface FileOpenService
{
  public abstract FileContents openFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException;
  
  public abstract FileContents[] openMultiFileDialog(String paramString, String[] paramArrayOfString)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\FileOpenService.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */