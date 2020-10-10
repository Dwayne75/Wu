package com.wurmonline.server;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;
import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;

public final class Twit
{
  private static final Logger logger = Logger.getLogger(Twit.class.getName());
  private final String sender;
  private final String twit;
  private final String consumerKey;
  private final String consumerSecret;
  private final String oauthToken;
  private final String oauthTokenSecret;
  private final boolean isVillage;
  private static final Twit[] emptyTwits = new Twit[0];
  @GuardedBy("TWITS_RW_LOCK")
  private static final List<Twit> twits = new LinkedList();
  private static final ReentrantReadWriteLock TWITS_RW_LOCK = new ReentrantReadWriteLock();
  private static final TwitterThread twitterThread = new TwitterThread();
  
  public Twit(String senderName, String toTwit, String consumerKeyToUse, String consumerSecretToUse, String applicationToken, String applicationSecret, boolean _isVillage)
  {
    this.sender = senderName;
    this.twit = toTwit.substring(0, Math.min(toTwit.length(), 279));
    this.consumerKey = consumerKeyToUse;
    this.consumerSecret = consumerSecretToUse;
    this.oauthToken = applicationToken;
    this.oauthTokenSecret = applicationSecret;
    this.isVillage = _isVillage;
  }
  
  /* Error */
  private static final Twit[] getTwitsArray()
  {
    // Byte code:
    //   0: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 16	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 17	com/wurmonline/server/Twit:twits	Ljava/util/List;
    //   12: invokeinterface 18 1 0
    //   17: ifle +66 -> 83
    //   20: getstatic 17	com/wurmonline/server/Twit:twits	Ljava/util/List;
    //   23: invokeinterface 18 1 0
    //   28: anewarray 19	com/wurmonline/server/Twit
    //   31: astore_0
    //   32: iconst_0
    //   33: istore_1
    //   34: getstatic 17	com/wurmonline/server/Twit:twits	Ljava/util/List;
    //   37: invokeinterface 20 1 0
    //   42: astore_2
    //   43: aload_2
    //   44: invokeinterface 21 1 0
    //   49: ifeq +21 -> 70
    //   52: aload_0
    //   53: iload_1
    //   54: aload_2
    //   55: invokeinterface 22 1 0
    //   60: checkcast 19	com/wurmonline/server/Twit
    //   63: aastore
    //   64: iinc 1 1
    //   67: goto -24 -> 43
    //   70: aload_0
    //   71: astore_2
    //   72: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   75: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   78: invokevirtual 23	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   81: aload_2
    //   82: areturn
    //   83: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   86: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   89: invokevirtual 23	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   92: goto +15 -> 107
    //   95: astore_3
    //   96: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   99: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   102: invokevirtual 23	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   105: aload_3
    //   106: athrow
    //   107: getstatic 24	com/wurmonline/server/Twit:emptyTwits	[Lcom/wurmonline/server/Twit;
    //   110: areturn
    // Line number table:
    //   Java source line #67	-> byte code offset #0
    //   Java source line #68	-> byte code offset #9
    //   Java source line #70	-> byte code offset #20
    //   Java source line #71	-> byte code offset #32
    //   Java source line #72	-> byte code offset #34
    //   Java source line #74	-> byte code offset #52
    //   Java source line #75	-> byte code offset #64
    //   Java source line #77	-> byte code offset #70
    //   Java source line #82	-> byte code offset #72
    //   Java source line #77	-> byte code offset #81
    //   Java source line #82	-> byte code offset #83
    //   Java source line #83	-> byte code offset #92
    //   Java source line #82	-> byte code offset #95
    //   Java source line #83	-> byte code offset #105
    //   Java source line #84	-> byte code offset #107
    // Local variable table:
    //   start	length	slot	name	signature
    //   31	40	0	toReturn	Twit[]
    //   33	32	1	x	int
    //   42	40	2	it	java.util.ListIterator<Twit>
    //   95	11	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	72	95	finally
  }
  
  /* Error */
  private static final void removeTwit(Twit twit)
  {
    // Byte code:
    //   0: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 16	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 17	com/wurmonline/server/Twit:twits	Ljava/util/List;
    //   12: aload_0
    //   13: invokeinterface 25 2 0
    //   18: pop
    //   19: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   22: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   25: invokevirtual 23	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   28: goto +15 -> 43
    //   31: astore_1
    //   32: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   35: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   38: invokevirtual 23	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   41: aload_1
    //   42: athrow
    //   43: return
    // Line number table:
    //   Java source line #91	-> byte code offset #0
    //   Java source line #92	-> byte code offset #9
    //   Java source line #96	-> byte code offset #19
    //   Java source line #97	-> byte code offset #28
    //   Java source line #96	-> byte code offset #31
    //   Java source line #97	-> byte code offset #41
    //   Java source line #98	-> byte code offset #43
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	44	0	twit	Twit
    //   31	11	1	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   0	19	31	finally
  }
  
  private static void pollTwits()
  {
    Twit[] twitarr = getTwitsArray();
    if (twitarr.length > 0) {
      for (int y = 0; y < twitarr.length; y++) {
        try
        {
          twitJTwitter(twitarr[y]);
          removeTwit(twitarr[y]);
        }
        catch (Exception ex)
        {
          if ((ex.getMessage().startsWith("Already tweeted!")) || (ex.getMessage().startsWith("Forbidden")) || 
            (ex.getMessage().startsWith("Unauthorized")) || (ex.getMessage().startsWith("Invalid")))
          {
            logger.log(Level.INFO, "Removed duplicate or unauthorized " + twitarr[y].twit);
            removeTwit(twitarr[y]);
          }
          else if (twitarr[y].isVillage)
          {
            logger.log(Level.INFO, "Twitting failed for village " + ex.getMessage() + " Removing.");
            removeTwit(twitarr[y]);
          }
          else
          {
            if ((twitarr[y].twit == null) || (twitarr[y].twit.length() == 0)) {
              removeTwit(twitarr[y]);
            }
            logger.log(Level.INFO, "Twitting failed for server " + ex.getMessage() + ". Trying later.");
          }
        }
      }
    }
  }
  
  /* Error */
  public static final void twit(Twit twit)
  {
    // Byte code:
    //   0: aload_0
    //   1: ifnull +46 -> 47
    //   4: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   7: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   10: invokevirtual 16	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   13: getstatic 17	com/wurmonline/server/Twit:twits	Ljava/util/List;
    //   16: aload_0
    //   17: invokeinterface 47 2 0
    //   22: pop
    //   23: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   26: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   29: invokevirtual 23	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   32: goto +15 -> 47
    //   35: astore_1
    //   36: getstatic 14	com/wurmonline/server/Twit:TWITS_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   39: invokevirtual 15	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   42: invokevirtual 23	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   45: aload_1
    //   46: athrow
    //   47: return
    // Line number table:
    //   Java source line #181	-> byte code offset #0
    //   Java source line #185	-> byte code offset #4
    //   Java source line #186	-> byte code offset #13
    //   Java source line #190	-> byte code offset #23
    //   Java source line #191	-> byte code offset #32
    //   Java source line #190	-> byte code offset #35
    //   Java source line #191	-> byte code offset #45
    //   Java source line #193	-> byte code offset #47
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	48	0	twit	Twit
    //   35	11	1	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   4	23	35	finally
  }
  
  private static void twitJTwitter(Twit twit)
  {
    logger.log(Level.INFO, "creating oauthClient for " + twit.twit);
    
    OAuthSignpostClient oauthClient = new OAuthSignpostClient(twit.consumerKey, twit.consumerSecret, twit.oauthToken, twit.oauthTokenSecret);
    
    logger.log(Level.INFO, "creating twitter for " + twit.twit);
    Twitter twitter = new Twitter(twit.sender, oauthClient);
    
    twitter.setStatus(twit.twit);
    logger.log(Level.INFO, "done sending twit " + twit.twit);
  }
  
  public static final TwitterThread getTwitterThread()
  {
    return twitterThread;
  }
  
  private static class TwitterThread
    implements Runnable
  {
    public void run()
    {
      try
      {
        long start = System.nanoTime();
        
        Twit.access$000();
        
        float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
        if (lElapsedTime > (float)Constants.lagThreshold) {
          Twit.logger.info("Finished calling Twit.pollTwits(), which took " + lElapsedTime + " millis.");
        }
      }
      catch (RuntimeException e)
      {
        Twit.logger.log(Level.WARNING, "Caught exception in ScheduledExecutorService while calling Twit.pollTwits()", e);
        throw e;
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\Twit.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */