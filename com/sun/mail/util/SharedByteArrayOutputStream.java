package com.sun.mail.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.mail.util.SharedByteArrayInputStream;

public class SharedByteArrayOutputStream
  extends ByteArrayOutputStream
{
  public SharedByteArrayOutputStream(int size)
  {
    super(size);
  }
  
  public InputStream toStream()
  {
    return new SharedByteArrayInputStream(this.buf, 0, this.count);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\SharedByteArrayOutputStream.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */