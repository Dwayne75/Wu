package com.sun.mail.util;

import java.io.IOException;
import java.io.InputStream;

public class QDecoderStream
  extends QPDecoderStream
{
  public QDecoderStream(InputStream in)
  {
    super(in);
  }
  
  public int read()
    throws IOException
  {
    int c = this.in.read();
    if (c == 95) {
      return 32;
    }
    if (c == 61)
    {
      this.ba[0] = ((byte)this.in.read());
      this.ba[1] = ((byte)this.in.read());
      try
      {
        return ASCIIUtility.parseInt(this.ba, 0, 2, 16);
      }
      catch (NumberFormatException nex)
      {
        throw new DecodingException("QDecoder: Error in QP stream " + nex.getMessage());
      }
    }
    return c;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\util\QDecoderStream.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */