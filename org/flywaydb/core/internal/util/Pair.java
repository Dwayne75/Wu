package org.flywaydb.core.internal.util;

public class Pair<L, R>
{
  private L left;
  private R right;
  
  public static <L, R> Pair<L, R> of(L left, R right)
  {
    Pair<L, R> pair = new Pair();
    pair.left = left;
    pair.right = right;
    return pair;
  }
  
  public L getLeft()
  {
    return (L)this.left;
  }
  
  public R getRight()
  {
    return (R)this.right;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\Pair.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */