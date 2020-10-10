package com.google.common.jimfs;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.util.Iterator;

final class DowngradedDirectoryStream
  implements DirectoryStream<Path>
{
  private final SecureDirectoryStream<Path> secureDirectoryStream;
  
  DowngradedDirectoryStream(SecureDirectoryStream<Path> secureDirectoryStream)
  {
    this.secureDirectoryStream = ((SecureDirectoryStream)Preconditions.checkNotNull(secureDirectoryStream));
  }
  
  public Iterator<Path> iterator()
  {
    return this.secureDirectoryStream.iterator();
  }
  
  public void close()
    throws IOException
  {
    this.secureDirectoryStream.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\jimfs\DowngradedDirectoryStream.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */