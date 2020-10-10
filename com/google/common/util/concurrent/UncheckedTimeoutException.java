package com.google.common.util.concurrent;

import javax.annotation.Nullable;

public class UncheckedTimeoutException
  extends RuntimeException
{
  private static final long serialVersionUID = 0L;
  
  public UncheckedTimeoutException() {}
  
  public UncheckedTimeoutException(@Nullable String message)
  {
    super(message);
  }
  
  public UncheckedTimeoutException(@Nullable Throwable cause)
  {
    super(cause);
  }
  
  public UncheckedTimeoutException(@Nullable String message, @Nullable Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\google\common\util\concurrent\UncheckedTimeoutException.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */