package com.google.common.io;

import java.io.IOException;

@Deprecated
public abstract interface InputSupplier<T>
{
  public abstract T getInput()
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\io\InputSupplier.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */