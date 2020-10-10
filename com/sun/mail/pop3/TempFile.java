package com.sun.mail.pop3;

import java.io.File;
import java.io.IOException;

class TempFile
{
  private File file;
  private WritableSharedFile sf;
  
  public TempFile(File dir)
    throws IOException
  {
    this.file = File.createTempFile("pop3.", ".mbox", dir);
    
    this.file.deleteOnExit();
    this.sf = new WritableSharedFile(this.file);
  }
  
  public AppendStream getAppendStream()
    throws IOException
  {
    return this.sf.getAppendStream();
  }
  
  public void close()
  {
    try
    {
      this.sf.close();
    }
    catch (IOException ex) {}
    this.file.delete();
  }
  
  protected void finalize()
    throws Throwable
  {
    super.finalize();
    close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\pop3\TempFile.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */