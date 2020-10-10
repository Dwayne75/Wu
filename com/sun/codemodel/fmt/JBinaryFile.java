package com.sun.codemodel.fmt;

import com.sun.codemodel.JResourceFile;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class JBinaryFile
  extends JResourceFile
{
  private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
  
  public JBinaryFile(String name)
  {
    super(name);
  }
  
  public OutputStream getDataStore()
  {
    return this.baos;
  }
  
  public void build(OutputStream os)
    throws IOException
  {
    os.write(this.baos.toByteArray());
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\fmt\JBinaryFile.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */