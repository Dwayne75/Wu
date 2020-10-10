package com.sun.jnlp;

import com.sun.deploy.resources.ResourceManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

final class MeteredFileOutputStream
  extends FileOutputStream
{
  static String _message = null;
  private FileContentsImpl _contents;
  private long _written = 0L;
  
  MeteredFileOutputStream(File paramFile, boolean paramBoolean, FileContentsImpl paramFileContentsImpl)
    throws FileNotFoundException
  {
    super(paramFile.getAbsolutePath(), paramBoolean);
    this._contents = paramFileContentsImpl;
    this._written = paramFile.length();
    if (_message == null) {
      _message = ResourceManager.getString("APIImpl.persistence.filesizemessage");
    }
  }
  
  public void write(int paramInt)
    throws IOException
  {
    checkWrite(1);
    super.write(paramInt);
    this._written += 1L;
  }
  
  public void write(byte[] paramArrayOfByte, int paramInt1, int paramInt2)
    throws IOException
  {
    checkWrite(paramInt2);
    super.write(paramArrayOfByte, paramInt1, paramInt2);
    this._written += paramInt2;
  }
  
  public void write(byte[] paramArrayOfByte)
    throws IOException
  {
    write(paramArrayOfByte, 0, paramArrayOfByte.length);
  }
  
  private void checkWrite(int paramInt)
    throws IOException
  {
    if (this._written + paramInt > this._contents.getMaxLength()) {
      throw new IOException(_message);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\jnlp\MeteredFileOutputStream.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */