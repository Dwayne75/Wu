package com.sun.javaws.cache;

import java.io.IOException;
import java.io.OutputStream;

public abstract interface Patcher
{
  public abstract void applyPatch(PatchDelegate paramPatchDelegate, String paramString1, String paramString2, OutputStream paramOutputStream)
    throws IOException;
  
  public static abstract interface PatchDelegate
  {
    public abstract void patching(int paramInt);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\cache\Patcher.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */