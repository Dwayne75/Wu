package org.apache.http.impl.entity;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpMessage;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolException;
import org.apache.http.ProtocolVersion;
import org.apache.http.annotation.Immutable;
import org.apache.http.entity.ContentLengthStrategy;

@Immutable
public class StrictContentLengthStrategy
  implements ContentLengthStrategy
{
  private final int implicitLen;
  
  public StrictContentLengthStrategy(int implicitLen)
  {
    this.implicitLen = implicitLen;
  }
  
  public StrictContentLengthStrategy()
  {
    this(-1);
  }
  
  public long determineLength(HttpMessage message)
    throws HttpException
  {
    if (message == null) {
      throw new IllegalArgumentException("HTTP message may not be null");
    }
    Header transferEncodingHeader = message.getFirstHeader("Transfer-Encoding");
    if (transferEncodingHeader != null)
    {
      String s = transferEncodingHeader.getValue();
      if ("chunked".equalsIgnoreCase(s))
      {
        if (message.getProtocolVersion().lessEquals(HttpVersion.HTTP_1_0)) {
          throw new ProtocolException("Chunked transfer encoding not allowed for " + message.getProtocolVersion());
        }
        return -2L;
      }
      if ("identity".equalsIgnoreCase(s)) {
        return -1L;
      }
      throw new ProtocolException("Unsupported transfer encoding: " + s);
    }
    Header contentLengthHeader = message.getFirstHeader("Content-Length");
    if (contentLengthHeader != null)
    {
      String s = contentLengthHeader.getValue();
      try
      {
        long len = Long.parseLong(s);
        if (len < 0L) {
          throw new ProtocolException("Negative content length: " + s);
        }
        return len;
      }
      catch (NumberFormatException e)
      {
        throw new ProtocolException("Invalid content length: " + s);
      }
    }
    return this.implicitLen;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\entity\StrictContentLengthStrategy.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */