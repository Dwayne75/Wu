package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.attribute.FileAttributeView;

abstract class AbstractAttributeView
  implements FileAttributeView
{
  private final FileLookup lookup;
  
  protected AbstractAttributeView(FileLookup lookup)
  {
    this.lookup = ((FileLookup)Preconditions.checkNotNull(lookup));
  }
  
  protected final File lookupFile()
    throws IOException
  {
    return this.lookup.lookup();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\AbstractAttributeView.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */