package org.seamless.util;

public class Exceptions
{
  public static Throwable unwrap(Throwable throwable)
    throws IllegalArgumentException
  {
    if (throwable == null) {
      throw new IllegalArgumentException("Cannot unwrap null throwable");
    }
    for (Throwable current = throwable; current != null; current = current.getCause()) {
      throwable = current;
    }
    return throwable;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\util\Exceptions.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */