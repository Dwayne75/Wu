package com.google.common.jimfs;

import java.io.IOException;

public abstract interface FileLookup
{
  public abstract File lookup()
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\FileLookup.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */