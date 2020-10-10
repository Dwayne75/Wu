package org.fourthline.cling.binding.staging;

import java.net.URI;
import org.fourthline.cling.model.meta.Icon;

public class MutableIcon
{
  public String mimeType;
  public int width;
  public int height;
  public int depth;
  public URI uri;
  
  public Icon build()
  {
    return new Icon(this.mimeType, this.width, this.height, this.depth, this.uri);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\binding\staging\MutableIcon.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */