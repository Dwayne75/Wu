package com.sun.javaws;

public class NativeLibraryFactory
{
  public static NativeLibrary newInstance()
  {
    return new WinNativeLibrary();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\NativeLibraryFactory.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */