package javax.jnlp;

import java.awt.datatransfer.Transferable;

public abstract interface ClipboardService
{
  public abstract Transferable getContents();
  
  public abstract void setContents(Transferable paramTransferable);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\jnlp\ClipboardService.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */