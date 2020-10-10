package org.apache.http.client.utils;

import org.apache.http.annotation.Immutable;

@Immutable
public class Punycode
{
  private static final Idn impl;
  
  static
  {
    Idn _impl;
    try
    {
      _impl = new JdkIdn();
    }
    catch (Exception e)
    {
      _impl = new Rfc3492Idn();
    }
    impl = _impl;
  }
  
  public static String toUnicode(String punycode)
  {
    return impl.toUnicode(punycode);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\apache\http\client\utils\Punycode.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */