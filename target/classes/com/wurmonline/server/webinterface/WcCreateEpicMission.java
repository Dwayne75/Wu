package com.wurmonline.server.webinterface;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WcCreateEpicMission
  extends WebCommand
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(WcCreateEpicMission.class.getName());
  private int collectiblesToWin = 5;
  private int collectiblesForWurmToWin = 8;
  private boolean spawnPointRequiredToWin = true;
  private int hexNumRequiredToWin = 0;
  private int scenarioNumber = 0;
  private int reasonPlusEffect = 0;
  private String scenarioName = "";
  private String scenarioQuest = "";
  public long entityNumber = 0L;
  private String entityName = "unknown";
  private int difficulty = 0;
  private long maxTimeSeconds = 0L;
  private boolean destroyPreviousMissions = false;
  
  public WcCreateEpicMission(long a_id, String scenName, int scenNumber, int reasonEff, int collReq, int collReqWurm, boolean spawnP, int hexNumReq, String questString, long epicEntity, int diff, String epicEntityName, long maxTimeSecs, boolean destroyPrevMissions)
  {
    super(a_id, (short)11);
    this.scenarioName = scenName;
    this.scenarioNumber = scenNumber;
    this.reasonPlusEffect = reasonEff;
    this.collectiblesToWin = collReq;
    this.collectiblesForWurmToWin = collReqWurm;
    this.spawnPointRequiredToWin = spawnP;
    this.hexNumRequiredToWin = hexNumReq;
    this.scenarioQuest = questString;
    this.entityNumber = epicEntity;
    this.difficulty = diff;
    this.entityName = epicEntityName;
    this.maxTimeSeconds = maxTimeSecs;
    this.destroyPreviousMissions = destroyPrevMissions;
    this.isRestrictedEpic = true;
  }
  
  public WcCreateEpicMission(long aId, byte[] _data)
  {
    super(aId, (short)11, _data);
    this.isRestrictedEpic = true;
  }
  
  public boolean autoForward()
  {
    return true;
  }
  
  byte[] encode()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] barr = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeUTF(this.scenarioName);
      dos.writeInt(this.scenarioNumber);
      dos.writeInt(this.reasonPlusEffect);
      dos.writeInt(this.collectiblesToWin);
      dos.writeInt(this.collectiblesForWurmToWin);
      dos.writeBoolean(this.spawnPointRequiredToWin);
      dos.writeInt(this.hexNumRequiredToWin);
      dos.writeUTF(this.scenarioQuest);
      dos.writeLong(this.entityNumber);
      dos.writeInt(this.difficulty);
      dos.writeUTF(this.entityName);
      dos.writeLong(this.maxTimeSeconds);
      dos.writeBoolean(this.destroyPreviousMissions);
      dos.flush();
      dos.close();
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      barr = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(barr);
    }
    return barr;
  }
  
  public void execute()
  {
    new WcCreateEpicMission.1(this).start();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcCreateEpicMission.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */