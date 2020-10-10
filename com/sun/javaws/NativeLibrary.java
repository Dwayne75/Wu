package com.sun.javaws;

public abstract class NativeLibrary
{
  private static NativeLibrary nativeLibrary;
  
  public static synchronized NativeLibrary getInstance()
  {
    if (nativeLibrary == null) {
      nativeLibrary = NativeLibraryFactory.newInstance();
    }
    return nativeLibrary;
  }
  
  public void load() {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\javaws\NativeLibrary.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */