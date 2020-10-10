package com.sun.mail.iap;

import java.io.IOException;
import java.io.OutputStream;

public abstract interface Literal
{
  public abstract int size();
  
  public abstract void writeTo(OutputStream paramOutputStream)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\iap\Literal.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */