package com.wurmonline.server;

import com.wurmonline.shared.exceptions.WurmServerException;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import javax.annotation.concurrent.GuardedBy;

public final class Factions
{
  private static Factions instance = null;
  @GuardedBy("FACTIONS_RW_LOCK")
  private static Map<String, Faction> factions;
  private static final ReentrantReadWriteLock FACTIONS_RW_LOCK = new ReentrantReadWriteLock();
  
  public static Factions getInstance()
  {
    if (instance == null) {
      instance = new Factions();
    }
    return instance;
  }
  
  /* Error */
  private Factions()
  {
    // Byte code:
    //   0: aload_0
    //   1: invokespecial 4	java/lang/Object:<init>	()V
    //   4: getstatic 5	com/wurmonline/server/Factions:FACTIONS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   7: invokevirtual 6	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   10: invokevirtual 7	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   13: new 8	java/util/HashMap
    //   16: dup
    //   17: invokespecial 9	java/util/HashMap:<init>	()V
    //   20: putstatic 10	com/wurmonline/server/Factions:factions	Ljava/util/Map;
    //   23: getstatic 5	com/wurmonline/server/Factions:FACTIONS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   26: invokevirtual 6	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   29: invokevirtual 11	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   32: goto +15 -> 47
    //   35: astore_1
    //   36: getstatic 5	com/wurmonline/server/Factions:FACTIONS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   39: invokevirtual 6	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   42: invokevirtual 11	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   45: aload_1
    //   46: athrow
    //   47: return
    // Line number table:
    //   Java source line #52	-> byte code offset #0
    //   Java source line #53	-> byte code offset #4
    //   Java source line #56	-> byte code offset #13
    //   Java source line #60	-> byte code offset #23
    //   Java source line #61	-> byte code offset #32
    //   Java source line #60	-> byte code offset #35
    //   Java source line #61	-> byte code offset #45
    //   Java source line #62	-> byte code offset #47
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	48	0	this	Factions
    //   35	11	1	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   13	23	35	finally
  }
  
  /* Error */
  public static void addFaction(Faction faction)
  {
    // Byte code:
    //   0: getstatic 5	com/wurmonline/server/Factions:FACTIONS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 6	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 7	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 10	com/wurmonline/server/Factions:factions	Ljava/util/Map;
    //   12: aload_0
    //   13: invokevirtual 12	com/wurmonline/server/Faction:getName	()Ljava/lang/String;
    //   16: aload_0
    //   17: invokeinterface 13 3 0
    //   22: pop
    //   23: getstatic 5	com/wurmonline/server/Factions:FACTIONS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   26: invokevirtual 6	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   29: invokevirtual 11	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   32: goto +15 -> 47
    //   35: astore_1
    //   36: getstatic 5	com/wurmonline/server/Factions:FACTIONS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   39: invokevirtual 6	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   42: invokevirtual 11	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   45: aload_1
    //   46: athrow
    //   47: return
    // Line number table:
    //   Java source line #66	-> byte code offset #0
    //   Java source line #69	-> byte code offset #9
    //   Java source line #73	-> byte code offset #23
    //   Java source line #74	-> byte code offset #32
    //   Java source line #73	-> byte code offset #35
    //   Java source line #74	-> byte code offset #45
    //   Java source line #75	-> byte code offset #47
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	48	0	faction	Faction
    //   35	11	1	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	23	35	finally
  }
  
  public static Faction getFaction(String name)
    throws Exception
  {
    FACTIONS_RW_LOCK.readLock().lock();
    try
    {
      Faction toReturn = (Faction)factions.get(name);
      if (toReturn == null) {
        throw new WurmServerException("No faction with name " + name);
      }
      return toReturn;
    }
    finally
    {
      FACTIONS_RW_LOCK.readLock().unlock();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\Factions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */