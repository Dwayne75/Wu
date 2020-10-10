package javax.mail.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.mail.internet.SharedInputStream;

public class SharedByteArrayInputStream
  extends ByteArrayInputStream
  implements SharedInputStream
{
  protected int start = 0;
  
  public SharedByteArrayInputStream(byte[] buf)
  {
    super(buf);
  }
  
  public SharedByteArrayInputStream(byte[] buf, int offset, int length)
  {
    super(buf, offset, length);
    this.start = offset;
  }
  
  public long getPosition()
  {
    return this.pos - this.start;
  }
  
  public InputStream newStream(long start, long end)
  {
    if (start < 0L) {
      throw new IllegalArgumentException("start < 0");
    }
    if (end == -1L) {
      end = this.count - this.start;
    }
    return new SharedByteArrayInputStream(this.buf, this.start + (int)start, (int)(end - start));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\javax\mail\util\SharedByteArrayInputStream.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */