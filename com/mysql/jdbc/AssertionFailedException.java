package com.mysql.jdbc;

public class AssertionFailedException
  extends RuntimeException
{
  public static void shouldNotHappen(Exception ex)
    throws AssertionFailedException
  {
    throw new AssertionFailedException(ex);
  }
  
  public AssertionFailedException(Exception ex)
  {
    super(Messages.getString("AssertionFailedException.0") + ex.toString() + Messages.getString("AssertionFailedException.1"));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\AssertionFailedException.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */