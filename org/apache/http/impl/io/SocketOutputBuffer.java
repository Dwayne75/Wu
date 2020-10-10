package org.apache.http.impl.io;

import java.io.IOException;
import java.net.Socket;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.params.HttpParams;

@NotThreadSafe
public class SocketOutputBuffer
  extends AbstractSessionOutputBuffer
{
  public SocketOutputBuffer(Socket socket, int buffersize, HttpParams params)
    throws IOException
  {
    if (socket == null) {
      throw new IllegalArgumentException("Socket may not be null");
    }
    if (buffersize < 0) {
      buffersize = socket.getSendBufferSize();
    }
    if (buffersize < 1024) {
      buffersize = 1024;
    }
    init(socket.getOutputStream(), buffersize, params);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\io\SocketOutputBuffer.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */