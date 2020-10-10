package com.sun.codemodel.fmt;

import com.sun.codemodel.JResourceFile;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class JSerializedObject
  extends JResourceFile
{
  private final Object obj;
  
  public JSerializedObject(String name, Object obj)
    throws IOException
  {
    super(name);
    this.obj = obj;
  }
  
  protected void build(OutputStream os)
    throws IOException
  {
    ObjectOutputStream oos = new ObjectOutputStream(os);
    oos.writeObject(this.obj);
    oos.close();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\fmt\JSerializedObject.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */