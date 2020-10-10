package org.seamless.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomToken
{
  protected final Random random;
  
  public RandomToken()
  {
    try
    {
      this.random = SecureRandom.getInstance("SHA1PRNG", "SUN");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    this.random.nextBytes(new byte[1]);
  }
  
  public String generate()
  {
    String token = null;
    while ((token == null) || (token.length() == 0))
    {
      long r0 = this.random.nextLong();
      if (r0 < 0L) {
        r0 = -r0;
      }
      long r1 = this.random.nextLong();
      if (r1 < 0L) {
        r1 = -r1;
      }
      token = Long.toString(r0, 36) + Long.toString(r1, 36);
    }
    return token;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\RandomToken.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */