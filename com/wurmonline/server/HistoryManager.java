package com.wurmonline.server;

import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

public final class HistoryManager
{
  private static final Logger logger = Logger.getLogger(HistoryManager.class.getName());
  private static final String ADD_HISTORY = "INSERT INTO HISTORY(EVENTDATE,SERVER,PERFORMER,EVENT) VALUES (?,?,?,?)";
  private static final String GET_HISTORY = "SELECT EVENTDATE, SERVER, PERFORMER, EVENT FROM HISTORY WHERE SERVER=? ORDER BY EVENTDATE DESC";
  @GuardedBy("HISTORY_RW_LOCK")
  private static final LinkedList<HistoryEvent> HISTORY = new LinkedList();
  private static final ReentrantReadWriteLock HISTORY_RW_LOCK = new ReentrantReadWriteLock();
  
  static HistoryEvent[] getHistoryEvents()
  {
    HISTORY_RW_LOCK.readLock().lock();
    try
    {
      return (HistoryEvent[])HISTORY.toArray(new HistoryEvent[HISTORY.size()]);
    }
    finally
    {
      HISTORY_RW_LOCK.readLock().unlock();
    }
  }
  
  /* Error */
  public static String[] getHistory(int numevents)
  {
    // Byte code:
    //   0: iconst_0
    //   1: anewarray 12	java/lang/String
    //   4: astore_1
    //   5: getstatic 3	com/wurmonline/server/HistoryManager:HISTORY_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   8: invokevirtual 4	java/util/concurrent/locks/ReentrantReadWriteLock:readLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$ReadLock;
    //   11: invokevirtual 5	java/util/concurrent/locks/ReentrantReadWriteLock$ReadLock:lock	()V
    //   14: getstatic 6	com/wurmonline/server/HistoryManager:HISTORY	Ljava/util/LinkedList;
    //   17: invokevirtual 7	java/util/LinkedList:size	()I
    //   20: istore_2
    //   21: getstatic 3	com/wurmonline/server/HistoryManager:HISTORY_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   24: invokevirtual 4	java/util/concurrent/locks/ReentrantReadWriteLock:readLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$ReadLock;
    //   27: invokevirtual 11	java/util/concurrent/locks/ReentrantReadWriteLock$ReadLock:unlock	()V
    //   30: goto +15 -> 45
    //   33: astore_3
    //   34: getstatic 3	com/wurmonline/server/HistoryManager:HISTORY_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   37: invokevirtual 4	java/util/concurrent/locks/ReentrantReadWriteLock:readLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$ReadLock;
    //   40: invokevirtual 11	java/util/concurrent/locks/ReentrantReadWriteLock$ReadLock:unlock	()V
    //   43: aload_3
    //   44: athrow
    //   45: iload_2
    //   46: ifle +46 -> 92
    //   49: iload_0
    //   50: iload_2
    //   51: invokestatic 13	java/lang/Math:min	(II)I
    //   54: istore_3
    //   55: iload_3
    //   56: anewarray 12	java/lang/String
    //   59: astore_1
    //   60: invokestatic 14	com/wurmonline/server/HistoryManager:getHistoryEvents	()[Lcom/wurmonline/server/HistoryEvent;
    //   63: astore 4
    //   65: iconst_0
    //   66: istore 5
    //   68: iload 5
    //   70: iload_3
    //   71: if_icmpge +21 -> 92
    //   74: aload_1
    //   75: iload 5
    //   77: aload 4
    //   79: iload 5
    //   81: aaload
    //   82: invokevirtual 15	com/wurmonline/server/HistoryEvent:getLongDesc	()Ljava/lang/String;
    //   85: aastore
    //   86: iinc 5 1
    //   89: goto -21 -> 68
    //   92: aload_1
    //   93: areturn
    // Line number table:
    //   Java source line #86	-> byte code offset #0
    //   Java source line #88	-> byte code offset #5
    //   Java source line #91	-> byte code offset #14
    //   Java source line #95	-> byte code offset #21
    //   Java source line #96	-> byte code offset #30
    //   Java source line #95	-> byte code offset #33
    //   Java source line #96	-> byte code offset #43
    //   Java source line #97	-> byte code offset #45
    //   Java source line #99	-> byte code offset #49
    //   Java source line #101	-> byte code offset #55
    //   Java source line #102	-> byte code offset #60
    //   Java source line #103	-> byte code offset #65
    //   Java source line #105	-> byte code offset #74
    //   Java source line #103	-> byte code offset #86
    //   Java source line #108	-> byte code offset #92
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	94	0	numevents	int
    //   4	89	1	hist	String[]
    //   20	2	2	lHistorySize	int
    //   45	6	2	lHistorySize	int
    //   33	11	3	localObject	Object
    //   54	18	3	numbersToFetch	int
    //   63	15	4	events	HistoryEvent[]
    //   66	21	5	x	int
    // Exception table:
    //   from	to	target	type
    //   14	21	33	finally
  }
  
  static void loadHistory()
  {
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    HISTORY_RW_LOCK.writeLock().lock();
    try
    {
      dbcon = DbConnector.getLoginDbCon();
      ps = dbcon.prepareStatement("SELECT EVENTDATE, SERVER, PERFORMER, EVENT FROM HISTORY WHERE SERVER=? ORDER BY EVENTDATE DESC");
      ps.setInt(1, Servers.localServer.id);
      rs = ps.executeQuery();
      while (rs.next()) {
        HISTORY.add(new HistoryEvent(rs.getLong("EVENTDATE"), rs.getString("PERFORMER"), rs.getString("EVENT"), rs
          .getInt("SERVER")));
      }
    }
    catch (SQLException sqx)
    {
      float lElapsedTime;
      logger.log(Level.WARNING, "Problem loading History for loacl server id: " + Servers.localServer.id + " due to " + sqx
        .getMessage(), sqx);
    }
    finally
    {
      float lElapsedTime;
      HISTORY_RW_LOCK.writeLock().unlock();
      
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      
      float lElapsedTime = (float)(System.nanoTime() - start) / 1000000.0F;
      logger.info("Loaded " + HISTORY.size() + " HISTORY events from the database took " + lElapsedTime + " ms");
    }
  }
  
  public static void addHistory(String performerName, String event)
  {
    addHistory(performerName, event, true);
  }
  
  /* Error */
  public static void addHistory(String performerName, String event, boolean twit)
  {
    // Byte code:
    //   0: getstatic 3	com/wurmonline/server/HistoryManager:HISTORY_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 17	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 18	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 6	com/wurmonline/server/HistoryManager:HISTORY	Ljava/util/LinkedList;
    //   12: new 8	com/wurmonline/server/HistoryEvent
    //   15: dup
    //   16: invokestatic 59	java/lang/System:currentTimeMillis	()J
    //   19: aload_0
    //   20: aload_1
    //   21: getstatic 23	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   24: getfield 24	com/wurmonline/server/ServerEntry:id	I
    //   27: invokespecial 35	com/wurmonline/server/HistoryEvent:<init>	(JLjava/lang/String;Ljava/lang/String;I)V
    //   30: invokevirtual 60	java/util/LinkedList:addFirst	(Ljava/lang/Object;)V
    //   33: getstatic 3	com/wurmonline/server/HistoryManager:HISTORY_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   36: invokevirtual 17	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   39: invokevirtual 37	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   42: goto +15 -> 57
    //   45: astore_3
    //   46: getstatic 3	com/wurmonline/server/HistoryManager:HISTORY_RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   49: invokevirtual 17	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   52: invokevirtual 37	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   55: aload_3
    //   56: athrow
    //   57: aconst_null
    //   58: astore_3
    //   59: aconst_null
    //   60: astore 4
    //   62: invokestatic 19	com/wurmonline/server/DbConnector:getLoginDbCon	()Ljava/sql/Connection;
    //   65: astore_3
    //   66: aload_3
    //   67: ldc 61
    //   69: invokeinterface 22 2 0
    //   74: astore 4
    //   76: aload 4
    //   78: iconst_1
    //   79: invokestatic 59	java/lang/System:currentTimeMillis	()J
    //   82: invokeinterface 62 4 0
    //   87: aload 4
    //   89: iconst_2
    //   90: getstatic 23	com/wurmonline/server/Servers:localServer	Lcom/wurmonline/server/ServerEntry;
    //   93: getfield 24	com/wurmonline/server/ServerEntry:id	I
    //   96: invokeinterface 25 3 0
    //   101: aload 4
    //   103: iconst_3
    //   104: aload_0
    //   105: invokeinterface 63 3 0
    //   110: aload 4
    //   112: iconst_4
    //   113: aload_1
    //   114: invokeinterface 63 3 0
    //   119: aload 4
    //   121: invokeinterface 64 1 0
    //   126: pop
    //   127: aload 4
    //   129: aconst_null
    //   130: invokestatic 38	com/wurmonline/server/utils/DbUtilities:closeDatabaseObjects	(Ljava/sql/Statement;Ljava/sql/ResultSet;)V
    //   133: aload_3
    //   134: invokestatic 39	com/wurmonline/server/DbConnector:returnConnection	(Ljava/sql/Connection;)V
    //   137: goto +49 -> 186
    //   140: astore 5
    //   142: getstatic 41	com/wurmonline/server/HistoryManager:logger	Ljava/util/logging/Logger;
    //   145: getstatic 53	java/util/logging/Level:WARNING	Ljava/util/logging/Level;
    //   148: aload 5
    //   150: invokevirtual 56	java/sql/SQLException:getMessage	()Ljava/lang/String;
    //   153: aload 5
    //   155: invokevirtual 57	java/util/logging/Logger:log	(Ljava/util/logging/Level;Ljava/lang/String;Ljava/lang/Throwable;)V
    //   158: aload 4
    //   160: aconst_null
    //   161: invokestatic 38	com/wurmonline/server/utils/DbUtilities:closeDatabaseObjects	(Ljava/sql/Statement;Ljava/sql/ResultSet;)V
    //   164: aload_3
    //   165: invokestatic 39	com/wurmonline/server/DbConnector:returnConnection	(Ljava/sql/Connection;)V
    //   168: goto +18 -> 186
    //   171: astore 6
    //   173: aload 4
    //   175: aconst_null
    //   176: invokestatic 38	com/wurmonline/server/utils/DbUtilities:closeDatabaseObjects	(Ljava/sql/Statement;Ljava/sql/ResultSet;)V
    //   179: aload_3
    //   180: invokestatic 39	com/wurmonline/server/DbConnector:returnConnection	(Ljava/sql/Connection;)V
    //   183: aload 6
    //   185: athrow
    //   186: iload_2
    //   187: ifeq +32 -> 219
    //   190: invokestatic 65	com/wurmonline/server/Server:getInstance	()Lcom/wurmonline/server/Server;
    //   193: new 42	java/lang/StringBuilder
    //   196: dup
    //   197: invokespecial 43	java/lang/StringBuilder:<init>	()V
    //   200: aload_0
    //   201: invokevirtual 45	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   204: ldc 66
    //   206: invokevirtual 45	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   209: aload_1
    //   210: invokevirtual 45	java/lang/StringBuilder:append	(Ljava/lang/String;)Ljava/lang/StringBuilder;
    //   213: invokevirtual 50	java/lang/StringBuilder:toString	()Ljava/lang/String;
    //   216: invokevirtual 67	com/wurmonline/server/Server:twitLocalServer	(Ljava/lang/String;)V
    //   219: return
    // Line number table:
    //   Java source line #154	-> byte code offset #0
    //   Java source line #157	-> byte code offset #9
    //   Java source line #161	-> byte code offset #33
    //   Java source line #162	-> byte code offset #42
    //   Java source line #161	-> byte code offset #45
    //   Java source line #162	-> byte code offset #55
    //   Java source line #163	-> byte code offset #57
    //   Java source line #164	-> byte code offset #59
    //   Java source line #167	-> byte code offset #62
    //   Java source line #168	-> byte code offset #66
    //   Java source line #169	-> byte code offset #76
    //   Java source line #170	-> byte code offset #87
    //   Java source line #171	-> byte code offset #101
    //   Java source line #172	-> byte code offset #110
    //   Java source line #173	-> byte code offset #119
    //   Java source line #181	-> byte code offset #127
    //   Java source line #182	-> byte code offset #133
    //   Java source line #183	-> byte code offset #137
    //   Java source line #175	-> byte code offset #140
    //   Java source line #177	-> byte code offset #142
    //   Java source line #181	-> byte code offset #158
    //   Java source line #182	-> byte code offset #164
    //   Java source line #183	-> byte code offset #168
    //   Java source line #181	-> byte code offset #171
    //   Java source line #182	-> byte code offset #179
    //   Java source line #183	-> byte code offset #183
    //   Java source line #184	-> byte code offset #186
    //   Java source line #185	-> byte code offset #190
    //   Java source line #186	-> byte code offset #219
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	220	0	performerName	String
    //   0	220	1	event	String
    //   0	220	2	twit	boolean
    //   45	11	3	localObject1	Object
    //   58	122	3	dbcon	Connection
    //   60	114	4	ps	PreparedStatement
    //   140	14	5	sqx	SQLException
    //   171	13	6	localObject2	Object
    // Exception table:
    //   from	to	target	type
    //   9	33	45	finally
    //   62	127	140	java/sql/SQLException
    //   62	127	171	finally
    //   140	158	171	finally
    //   171	173	171	finally
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\HistoryManager.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */