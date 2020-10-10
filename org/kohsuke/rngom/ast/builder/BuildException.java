package org.kohsuke.rngom.ast.builder;

public class BuildException
  extends RuntimeException
{
  private final Throwable cause;
  
  public BuildException(Throwable cause)
  {
    if (cause == null) {
      throw new NullPointerException("null cause");
    }
    this.cause = cause;
  }
  
  public Throwable getCause()
  {
    return this.cause;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\builder\BuildException.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */