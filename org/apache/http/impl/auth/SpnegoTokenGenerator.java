package org.apache.http.impl.auth;

import java.io.IOException;

@Deprecated
public abstract interface SpnegoTokenGenerator
{
  public abstract byte[] generateSpnegoDERObject(byte[] paramArrayOfByte)
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\impl\auth\SpnegoTokenGenerator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */