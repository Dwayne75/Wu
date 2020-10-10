package com.sun.codemodel.fmt;

import com.sun.codemodel.JResourceFile;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class JTextFile
  extends JResourceFile
{
  public JTextFile(String name)
  {
    super(name);
  }
  
  private String contents = null;
  
  public void setContents(String _contents)
  {
    this.contents = _contents;
  }
  
  public void build(OutputStream out)
    throws IOException
  {
    Writer w = new OutputStreamWriter(out);
    w.write(this.contents);
    w.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\fmt\JTextFile.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */