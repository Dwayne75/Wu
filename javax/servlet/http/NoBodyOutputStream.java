package javax.servlet.http;

import java.io.IOException;
import java.util.ResourceBundle;
import javax.servlet.ServletOutputStream;

class NoBodyOutputStream
  extends ServletOutputStream
{
  private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
  private static ResourceBundle lStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings");
  private int contentLength = 0;
  
  int getContentLength()
  {
    return this.contentLength;
  }
  
  public void write(int b)
  {
    this.contentLength += 1;
  }
  
  public void write(byte[] buf, int offset, int len)
    throws IOException
  {
    if (len >= 0) {
      this.contentLength += len;
    } else {
      throw new IOException(lStrings.getString("err.io.negativelength"));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\servlet\http\NoBodyOutputStream.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */