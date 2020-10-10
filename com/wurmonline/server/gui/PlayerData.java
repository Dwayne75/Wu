package com.wurmonline.server.gui;

import com.wurmonline.server.MiscConstants;
import java.util.logging.Logger;

public class PlayerData
  implements MiscConstants
{
  private static final String saveData = "UPDATE PLAYERS SET CURRENTSERVER=?, POWER=?, REIMBURSED=?, UNDEADTYPE=? WHERE WURMID=?";
  private static final String savePosition = "UPDATE POSITION SET POSX=?,POSY=? WHERE WURMID=?";
  private String name;
  private long wurmid;
  private int power;
  private float posx;
  private float posy;
  private int server;
  private boolean reimbursed;
  private byte undeadType = 0;
  private static final Logger logger = Logger.getLogger(PlayerData.class.getName());
  
  /* Error */
  public final void save()
    throws java.sql.SQLException
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_1
    //   2: aconst_null
    //   3: astore_2
    //   4: invokestatic 4	com/wurmonline/server/DbConnector:getPlayerDbCon	()Ljava/sql/Connection;
    //   7: astore_1
    //   8: aload_1
    //   9: ldc 6
    //   11: invokeinterface 7 2 0
    //   16: astore_2
    //   17: aload_2
    //   18: iconst_1
    //   19: aload_0
    //   20: invokevirtual 8	com/wurmonline/server/gui/PlayerData:getServer	()I
    //   23: invokeinterface 9 3 0
    //   28: aload_2
    //   29: iconst_2
    //   30: aload_0
    //   31: invokevirtual 10	com/wurmonline/server/gui/PlayerData:getPower	()I
    //   34: i2b
    //   35: invokeinterface 11 3 0
    //   40: aload_2
    //   41: iconst_3
    //   42: aload_0
    //   43: invokevirtual 12	com/wurmonline/server/gui/PlayerData:isReimbursed	()Z
    //   46: invokeinterface 13 3 0
    //   51: aload_2
    //   52: iconst_4
    //   53: aload_0
    //   54: getfield 3	com/wurmonline/server/gui/PlayerData:undeadType	B
    //   57: invokeinterface 11 3 0
    //   62: aload_2
    //   63: iconst_5
    //   64: aload_0
    //   65: invokevirtual 14	com/wurmonline/server/gui/PlayerData:getWurmid	()J
    //   68: invokeinterface 15 4 0
    //   73: aload_2
    //   74: invokeinterface 16 1 0
    //   79: pop
    //   80: aload_2
    //   81: aconst_null
    //   82: invokestatic 17	com/wurmonline/server/utils/DbUtilities:closeDatabaseObjects	(Ljava/sql/Statement;Ljava/sql/ResultSet;)V
    //   85: aload_1
    //   86: invokestatic 18	com/wurmonline/server/DbConnector:returnConnection	(Ljava/sql/Connection;)V
    //   89: goto +15 -> 104
    //   92: astore_3
    //   93: aload_2
    //   94: aconst_null
    //   95: invokestatic 17	com/wurmonline/server/utils/DbUtilities:closeDatabaseObjects	(Ljava/sql/Statement;Ljava/sql/ResultSet;)V
    //   98: aload_1
    //   99: invokestatic 18	com/wurmonline/server/DbConnector:returnConnection	(Ljava/sql/Connection;)V
    //   102: aload_3
    //   103: athrow
    //   104: aload_0
    //   105: invokevirtual 19	com/wurmonline/server/gui/PlayerData:savePosition	()V
    //   108: return
    // Line number table:
    //   Java source line #57	-> byte code offset #0
    //   Java source line #58	-> byte code offset #2
    //   Java source line #61	-> byte code offset #4
    //   Java source line #62	-> byte code offset #8
    //   Java source line #63	-> byte code offset #17
    //   Java source line #64	-> byte code offset #28
    //   Java source line #65	-> byte code offset #40
    //   Java source line #66	-> byte code offset #51
    //   Java source line #67	-> byte code offset #62
    //   Java source line #69	-> byte code offset #73
    //   Java source line #73	-> byte code offset #80
    //   Java source line #74	-> byte code offset #85
    //   Java source line #75	-> byte code offset #89
    //   Java source line #73	-> byte code offset #92
    //   Java source line #74	-> byte code offset #98
    //   Java source line #75	-> byte code offset #102
    //   Java source line #76	-> byte code offset #104
    //   Java source line #77	-> byte code offset #108
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	109	0	this	PlayerData
    //   1	98	1	dbcon	java.sql.Connection
    //   3	91	2	ps	java.sql.PreparedStatement
    //   92	11	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   4	80	92	finally
  }
  
  /* Error */
  public final void savePosition()
    throws java.sql.SQLException
  {
    // Byte code:
    //   0: aconst_null
    //   1: astore_1
    //   2: aconst_null
    //   3: astore_2
    //   4: invokestatic 4	com/wurmonline/server/DbConnector:getPlayerDbCon	()Ljava/sql/Connection;
    //   7: astore_1
    //   8: aload_1
    //   9: ldc 20
    //   11: invokeinterface 7 2 0
    //   16: astore_2
    //   17: aload_2
    //   18: iconst_1
    //   19: aload_0
    //   20: invokevirtual 21	com/wurmonline/server/gui/PlayerData:getPosx	()F
    //   23: invokeinterface 22 3 0
    //   28: aload_2
    //   29: iconst_2
    //   30: aload_0
    //   31: invokevirtual 23	com/wurmonline/server/gui/PlayerData:getPosy	()F
    //   34: invokeinterface 22 3 0
    //   39: aload_2
    //   40: iconst_3
    //   41: aload_0
    //   42: invokevirtual 14	com/wurmonline/server/gui/PlayerData:getWurmid	()J
    //   45: invokeinterface 15 4 0
    //   50: aload_2
    //   51: invokeinterface 16 1 0
    //   56: pop
    //   57: aload_2
    //   58: aconst_null
    //   59: invokestatic 17	com/wurmonline/server/utils/DbUtilities:closeDatabaseObjects	(Ljava/sql/Statement;Ljava/sql/ResultSet;)V
    //   62: aload_1
    //   63: invokestatic 18	com/wurmonline/server/DbConnector:returnConnection	(Ljava/sql/Connection;)V
    //   66: goto +15 -> 81
    //   69: astore_3
    //   70: aload_2
    //   71: aconst_null
    //   72: invokestatic 17	com/wurmonline/server/utils/DbUtilities:closeDatabaseObjects	(Ljava/sql/Statement;Ljava/sql/ResultSet;)V
    //   75: aload_1
    //   76: invokestatic 18	com/wurmonline/server/DbConnector:returnConnection	(Ljava/sql/Connection;)V
    //   79: aload_3
    //   80: athrow
    //   81: return
    // Line number table:
    //   Java source line #81	-> byte code offset #0
    //   Java source line #82	-> byte code offset #2
    //   Java source line #85	-> byte code offset #4
    //   Java source line #86	-> byte code offset #8
    //   Java source line #87	-> byte code offset #17
    //   Java source line #88	-> byte code offset #28
    //   Java source line #89	-> byte code offset #39
    //   Java source line #90	-> byte code offset #50
    //   Java source line #94	-> byte code offset #57
    //   Java source line #95	-> byte code offset #62
    //   Java source line #96	-> byte code offset #66
    //   Java source line #94	-> byte code offset #69
    //   Java source line #95	-> byte code offset #75
    //   Java source line #96	-> byte code offset #79
    //   Java source line #97	-> byte code offset #81
    // Local variable table:
    //   start	length	slot	name	signature
    //   0	82	0	this	PlayerData
    //   1	75	1	dbcon	java.sql.Connection
    //   3	68	2	ps	java.sql.PreparedStatement
    //   69	11	3	localObject	Object
    // Exception table:
    //   from	to	target	type
    //   4	57	69	finally
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }
  
  public long getWurmid()
  {
    return this.wurmid;
  }
  
  public void setWurmid(long wurmid)
  {
    this.wurmid = wurmid;
  }
  
  public int getPower()
  {
    return this.power;
  }
  
  public void setPower(int power)
  {
    if (power < 0) {
      power = 0;
    }
    if (power > 5) {
      power = 5;
    }
    if (power > this.power) {
      setReimbursed(false);
    }
    this.power = power;
  }
  
  public float getPosx()
  {
    return this.posx;
  }
  
  public void setPosx(float posx)
  {
    this.posx = posx;
  }
  
  public float getPosy()
  {
    return this.posy;
  }
  
  public void setPosy(float posy)
  {
    this.posy = posy;
  }
  
  public int getServer()
  {
    return this.server;
  }
  
  public void setServer(int server)
  {
    this.server = server;
  }
  
  public boolean isReimbursed()
  {
    return this.reimbursed;
  }
  
  public void setReimbursed(boolean reimbursed)
  {
    this.reimbursed = reimbursed;
  }
  
  public byte getUndeadType()
  {
    return this.undeadType;
  }
  
  public void setUndeadType(byte undeadType)
  {
    this.undeadType = undeadType;
  }
  
  public boolean isUndead()
  {
    return this.undeadType != 0;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\gui\PlayerData.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */