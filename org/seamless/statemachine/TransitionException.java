package org.seamless.statemachine;

public class TransitionException
  extends RuntimeException
{
  public TransitionException(String s)
  {
    super(s);
  }
  
  public TransitionException(String s, Throwable throwable)
  {
    super(s, throwable);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\statemachine\TransitionException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */