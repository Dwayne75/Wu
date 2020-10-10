package com.wurmonline.server.skills;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

public final class SkillStat
  implements TimeConstants
{
  private static Logger logger = Logger.getLogger(SkillStat.class.getName());
  public final Map<Long, Double> stats = new HashMap();
  @GuardedBy("RW_LOCK")
  private static final Map<Integer, SkillStat> allStats = new HashMap();
  private static final ReentrantReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
  private final String skillName;
  private final int skillnum;
  private static final String loadAllPlayerSkills = "select NUMBER,OWNER,VALUE from SKILLS sk INNER JOIN PLAYERS p ON p.WURMID=sk.OWNER AND p.CURRENTSERVER=? WHERE sk.VALUE>25 ";
  
  private SkillStat(int num, String name)
  {
    this.skillName = name;
    this.skillnum = num;
  }
  
  private static int loadAllStats()
  {
    Connection dbcon = null;
    int numberSkillsLoaded = 0;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getPlayerDbCon();
      ps = dbcon.prepareStatement("select NUMBER,OWNER,VALUE from SKILLS sk INNER JOIN PLAYERS p ON p.WURMID=sk.OWNER AND p.CURRENTSERVER=? WHERE sk.VALUE>25 ");
      ps.setInt(1, Servers.localServer.id);
      rs = ps.executeQuery();
      while (rs.next())
      {
        SkillStat sk = getSkillStatForSkill(rs.getInt("NUMBER"));
        if (sk != null) {
          sk.stats.put(new Long(rs.getLong("OWNER")), new Double(rs.getDouble("VALUE")));
        }
        numberSkillsLoaded++;
      }
    }
    catch (SQLException sqx)
    {
      logger.log(Level.WARNING, "Problem loading the Skill stats due to " + sqx.getMessage(), sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
    }
    return numberSkillsLoaded;
  }
  
  /* Error */
  static final void addSkill(int skillNum, String name)
  {
    // Byte code:
    //   0: getstatic 41	com/wurmonline/server/skills/SkillStat:RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   3: invokevirtual 42	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   6: invokevirtual 43	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:lock	()V
    //   9: getstatic 44	com/wurmonline/server/skills/SkillStat:allStats	Ljava/util/Map;
    //   12: iload_0
    //   13: invokestatic 45	java/lang/Integer:valueOf	(I)Ljava/lang/Integer;
    //   16: new 10	com/wurmonline/server/skills/SkillStat
    //   19: dup
    //   20: iload_0
    //   21: aload_1
    //   22: invokespecial 46	com/wurmonline/server/skills/SkillStat:<init>	(ILjava/lang/String;)V
    //   25: invokeinterface 29 3 0
    //   30: pop
    //   31: getstatic 41	com/wurmonline/server/skills/SkillStat:RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   34: invokevirtual 42	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   37: invokevirtual 47	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   40: goto +15 -> 55
    //   43: astore_2
    //   44: getstatic 41	com/wurmonline/server/skills/SkillStat:RW_LOCK	Ljava/util/concurrent/locks/ReentrantReadWriteLock;
    //   47: invokevirtual 42	java/util/concurrent/locks/ReentrantReadWriteLock:writeLock	()Ljava/util/concurrent/locks/ReentrantReadWriteLock$WriteLock;
    //   50: invokevirtual 47	java/util/concurrent/locks/ReentrantReadWriteLock$WriteLock:unlock	()V
    //   53: aload_2
    //   54: athrow
    //   55: return
    // Line number table:
    //   Java source line #120	-> byte code offset #0
    //   Java source line #123	-> byte code offset #9
    //   Java source line #127	-> byte code offset #31
    //   Java source line #128	-> byte code offset #40
    //   Java source line #127	-> byte code offset #43
    //   Java source line #128	-> byte code offset #53
    //   Java source line #129	-> byte code offset #55
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	56	0	skillNum	int
    //   0	56	1	name	String
    //   43	11	2	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   9	31	43	finally
  }
  
  public static final void pollSkills()
  {
    Thread statsPoller = new SkillStat.1("StatsPoller");
    
    statsPoller.start();
  }
  
  public static final SkillStat getSkillStatForSkill(int num)
  {
    RW_LOCK.readLock().lock();
    try
    {
      return (SkillStat)allStats.get(Integer.valueOf(num));
    }
    finally
    {
      RW_LOCK.readLock().unlock();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\skills\SkillStat.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */