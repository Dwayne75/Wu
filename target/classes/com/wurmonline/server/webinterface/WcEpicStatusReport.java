package com.wurmonline.server.webinterface;

import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.epic.Effectuator;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.EpicMission;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class WcEpicStatusReport
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WcEpicStatusReport.class.getName());
  private boolean success = false;
  private boolean entityStatusMessage = false;
  private long entityId = 0L;
  private byte missionType = -1;
  private int missionDifficulty = -1;
  private final Map<String, Integer> entityStatuses = new HashMap();
  private final Map<Integer, Integer> entityHexes = new HashMap();
  private final Map<Integer, String> entityMap = new HashMap();
  
  public WcEpicStatusReport(long aId, boolean wasSuccess, int epicEntityId, byte type, int difficulty)
  {
    super(aId, (short)10);
    this.success = wasSuccess;
    this.entityId = epicEntityId;
    this.missionType = type;
    this.missionDifficulty = difficulty;
    this.isRestrictedEpic = true;
  }
  
  public WcEpicStatusReport(long aId, byte[] _data)
  {
    super(aId, (short)10, _data);
    this.isRestrictedEpic = true;
  }
  
  public final void addEntityStatus(String status, int statusEntityId)
  {
    this.entityStatuses.put(status, Integer.valueOf(statusEntityId));
    this.entityStatusMessage = true;
  }
  
  public final void addEntityHex(int entity, int hexId)
  {
    this.entityHexes.put(Integer.valueOf(entity), Integer.valueOf(hexId));
  }
  
  public final void fillStatusReport(HexMap map)
  {
    EpicEntity[] entities = map.getAllEntities();
    for (EpicEntity entity : entities)
    {
      if (entity.isDeity()) {
        this.entityMap.put(Integer.valueOf((int)entity.getId()), entity.getName());
      }
      addEntityStatus(entity.getLocationStatus(), (int)entity.getId());
      addEntityStatus(entity.getEnemyStatus(), (int)entity.getId());
      int collsCarried = entity.countCollectables();
      if (collsCarried > 0) {
        addEntityStatus(entity.getName() + " is carrying " + collsCarried + " of the " + entity.getCollectibleName() + ".", 
          (int)entity.getId());
      }
      if (entity.isDeity()) {
        if (entity.getMapHex() != null) {
          addEntityHex((int)entity.getId(), entity.getMapHex().getId());
        }
      }
    }
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
      dos.writeBoolean(this.entityStatusMessage);
      if (this.entityStatusMessage)
      {
        dos.writeInt(this.entityMap.size());
        for (Map.Entry<Integer, String> entry : this.entityMap.entrySet())
        {
          dos.writeInt(((Integer)entry.getKey()).intValue());
          dos.writeUTF((String)entry.getValue());
        }
        dos.writeInt(this.entityStatuses.size());
        if (this.entityStatuses.size() > 0) {
          for (Map.Entry<String, Integer> entry : this.entityStatuses.entrySet())
          {
            dos.writeUTF((String)entry.getKey());
            dos.writeInt(((Integer)entry.getValue()).intValue());
          }
        }
        dos.writeInt(this.entityHexes.size());
        if (this.entityHexes.size() > 0) {
          for (Map.Entry<Integer, Integer> entry : this.entityHexes.entrySet())
          {
            dos.writeInt(((Integer)entry.getKey()).intValue());
            dos.writeInt(((Integer)entry.getValue()).intValue());
          }
        }
      }
      else
      {
        dos.writeBoolean(this.success);
        dos.writeLong(this.entityId);
        dos.writeByte(this.missionType);
        dos.writeInt(this.missionDifficulty);
      }
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
    DataInputStream dis = null;
    try
    {
      dis = new DataInputStream(new ByteArrayInputStream(getData()));
      this.entityStatusMessage = dis.readBoolean();
      if (this.entityStatusMessage)
      {
        Deities.clearValreiStatuses();
        int numsx = dis.readInt();
        for (int x = 0; x < numsx; x++)
        {
          int entity = dis.readInt();
          String name = dis.readUTF();
          Deities.addEntity(entity, name);
        }
        int nums = dis.readInt();
        for (int x = 0; x < nums; x++)
        {
          String status = dis.readUTF();
          int entity = dis.readInt();
          Deities.addStatus(status, entity);
        }
        int numsPos = dis.readInt();
        for (int x = 0; x < numsPos; x++)
        {
          int entity = dis.readInt();
          int hexPos = dis.readInt();
          Deities.addPosition(entity, hexPos);
        }
      }
      else
      {
        this.success = dis.readBoolean();
        this.entityId = dis.readLong();
        this.missionType = dis.readByte();
        this.missionDifficulty = dis.readInt();
        ServerEntry entry = Servers.getServerWithId(WurmId.getOrigin(getWurmId()));
        if (entry != null) {
          if (Server.getEpicMap() != null) {
            if (entry.EPIC)
            {
              EpicMission mission = EpicServerStatus.getEpicMissionForEntity((int)this.entityId);
              if (mission != null)
              {
                if ((!Servers.localServer.EPIC) && (this.success))
                {
                  float oldStatus = mission.getMissionProgress();
                  mission.updateProgress(oldStatus + 1.0F);
                }
                EpicEntity entity = Server.getEpicMap().getEntity(this.entityId);
                if (entity != null)
                {
                  Date now = new Date();
                  DateFormat format = DateFormat.getDateInstance(3);
                  if (this.success)
                  {
                    Server.getEpicMap().broadCast(entity
                      .getName() + " received help from " + entry.name + ". " + format
                      .format(now) + " " + Server.rand.nextInt(1000));
                    Server.getEpicMap().setEntityHelped(this.entityId, this.missionType, this.missionDifficulty);
                  }
                  else
                  {
                    Server.getEpicMap().broadCast(entity
                      .getName() + " never received help from " + entry.name + ". " + format
                      .format(now) + " " + Server.rand.nextInt(1000));
                    if (entity.isDeity()) {
                      entity.setShouldCreateMission(true, false);
                    }
                  }
                }
              }
              else
              {
                EpicEntity entity = Server.getEpicMap().getEntity(this.entityId);
                Date now = new Date();
                DateFormat format = DateFormat.getDateInstance(3);
                Server.getEpicMap().broadCast(entity
                  .getName() + " did not have an active mission when receiving help from " + entry.name + ". " + format
                  
                  .format(now) + " " + Server.rand.nextInt(1000));
                entity.setShouldCreateMission(true, false);
              }
            }
            else if (this.success)
            {
              EpicEntity entity = Server.getEpicMap().getEntity(this.entityId);
              if (entity != null)
              {
                int effect = Server.rand.nextInt(4) + 1;
                
                WcEpicEvent wce = new WcEpicEvent(WurmId.getNextWCCommandId(), 0, this.entityId, 0, effect, entity.getName() + "s followers now have the attention of the " + Effectuator.getSpiritType(effect) + " spirits.", false);
                wce.sendToServer(entry.id);
              }
            }
          }
        }
      }
    }
    catch (IOException ex)
    {
      logger.log(Level.WARNING, "Unpack exception " + ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\webinterface\WcEpicStatusReport.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */