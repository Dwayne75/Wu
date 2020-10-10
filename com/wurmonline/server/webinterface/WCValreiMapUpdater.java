package com.wurmonline.server.webinterface;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.epic.CollectedValreiItem;
import com.wurmonline.server.epic.EpicEntity;
import com.wurmonline.server.epic.EpicServerStatus;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.epic.MapHex;
import com.wurmonline.server.epic.ValreiFightHistory;
import com.wurmonline.server.epic.ValreiFightHistory.ValreiFighter;
import com.wurmonline.server.epic.ValreiFightHistoryManager;
import com.wurmonline.server.epic.ValreiMapData;
import com.wurmonline.shared.constants.ValreiConstants.ValreiFightAction;
import com.wurmonline.shared.util.StreamUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WCValreiMapUpdater
  extends WebCommand
{
  private static final Logger logger = Logger.getLogger(WCValreiMapUpdater.class.getName());
  public static final byte INITIAL_REQUEST = 0;
  public static final byte INITIAL_REQUEST_RESPONCE = 1;
  public static final byte UPDATE = 2;
  public static final byte REQUEST_TIME_UPDATE = 3;
  public static final byte SEND_TIME = 4;
  public static final byte NEW_FIGHT = 5;
  private byte messageType;
  private ValreiMapData toUpdate = null;
  private final List<EpicEntity> dataList = new ArrayList();
  
  public WCValreiMapUpdater(long aid, byte _messageType)
  {
    super(aid, (short)27);
    this.messageType = _messageType;
  }
  
  public WCValreiMapUpdater(long aid, byte[] data)
  {
    super(aid, (short)27, data);
  }
  
  public void collectData()
  {
    if ((this.dataList != null) && (this.dataList.size() > 0)) {
      this.dataList.clear();
    }
    for (EpicEntity ent : EpicServerStatus.getValrei().getAllEntities()) {
      this.dataList.add(ent);
    }
  }
  
  public void setEntityToUpdate(EpicEntity entity)
  {
    long eId = entity.getId();
    int hexId = entity.getMapHex() != null ? entity.getMapHex().getId() : -1;
    int type = entity.getType();
    int targetHex = entity.getTargetHex();
    String name = entity.getName();
    long now = System.currentTimeMillis();
    long remaining = entity.getTimeToNextHex() - now;
    float attack = entity.getAttack();
    float vitality = entity.getVitality();
    List<EpicEntity> list = entity.getAllCollectedItems();
    List<CollectedValreiItem> valList = CollectedValreiItem.fromList(list);
    
    float bodyStr = entity.getCurrentSkill(102);
    float bodySta = entity.getCurrentSkill(103);
    float bodyCon = entity.getCurrentSkill(104);
    float mindLog = entity.getCurrentSkill(100);
    float mindSpe = entity.getCurrentSkill(101);
    float soulStr = entity.getCurrentSkill(105);
    float soulDep = entity.getCurrentSkill(106);
    
    this.toUpdate = new ValreiMapData(eId, hexId, type, targetHex, name, remaining, bodyStr, bodySta, bodyCon, mindLog, mindSpe, soulStr, soulDep, valList);
  }
  
  public boolean autoForward()
  {
    return false;
  }
  
  byte[] encode()
  {
    if (this.messageType == 2)
    {
      if (this.toUpdate != null) {
        return createUpdateMessage();
      }
    }
    else
    {
      if (this.messageType == 0) {
        return createInitialRequestMessage();
      }
      if (this.messageType == 1)
      {
        collectData();
        return createInitialResponceMessage();
      }
      if (this.messageType == 3) {
        return createTimeUpdateRequest();
      }
      if (this.messageType == 4)
      {
        collectData();
        return createTimeUpdateMessage();
      }
      if (this.messageType == 5) {
        return createFightDetails();
      }
    }
    return new byte[0];
  }
  
  private final byte[] createTimeUpdateRequest()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteData = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeByte(3);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      byteData = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(byteData);
    }
    return byteData;
  }
  
  private final byte[] createTimeUpdateMessage()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteData = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeByte(4);
      int count = 0;
      for (Iterator<EpicEntity> it = this.dataList.iterator(); it.hasNext();)
      {
        EpicEntity entity = (EpicEntity)it.next();
        if ((!entity.isCollectable()) && (!entity.isSource())) {
          count++;
        }
      }
      dos.writeInt(count);
      for (it = this.dataList.iterator(); it.hasNext();)
      {
        EpicEntity entity = (EpicEntity)it.next();
        if ((!entity.isCollectable()) && (!entity.isSource()))
        {
          dos.writeLong(entity.getId());
          long now = System.currentTimeMillis();
          long remaining = entity.getTimeUntilLeave() - now;
          dos.writeLong(remaining);
        }
      }
    }
    catch (Exception ex)
    {
      Iterator<EpicEntity> it;
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      byteData = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(byteData);
    }
    return byteData;
  }
  
  private final byte[] createUpdateMessage()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteData = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeByte(2);
      ValreiMapData entity = this.toUpdate;
      
      dos.writeLong(entity.getEntityId());
      dos.writeInt(entity.getType());
      dos.writeInt(entity.getHexId());
      dos.writeInt(entity.getTargetHexId());
      dos.writeUTF(entity.getName());
      
      dos.writeLong(entity.getTimeRemaining());
      if ((!entity.isCollectable()) && (!entity.isSourceItem()))
      {
        dos.writeFloat(entity.getBodyStr());
        dos.writeFloat(entity.getBodySta());
        dos.writeFloat(entity.getBodyCon());
        dos.writeFloat(entity.getMindLog());
        dos.writeFloat(entity.getMindSpe());
        dos.writeFloat(entity.getSoulStr());
        dos.writeFloat(entity.getSoulDep());
        
        List<CollectedValreiItem> list = entity.getCarried();
        int collected = list.size();
        dos.writeInt(collected);
        for (int i = 0; i < collected; i++)
        {
          dos.writeUTF(((CollectedValreiItem)list.get(i)).getName());
          dos.writeInt(((CollectedValreiItem)list.get(i)).getType());
          dos.writeLong(((CollectedValreiItem)list.get(i)).getId());
        }
      }
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      byteData = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(byteData);
    }
    return byteData;
  }
  
  private final byte[] createFightDetails()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteData = null;
    ValreiFightHistory vf = ValreiFightHistoryManager.getInstance().getLatestFight();
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeByte(5);
      writeFightDetails(dos, vf);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      byteData = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(byteData);
    }
    return byteData;
  }
  
  private final byte[] createInitialRequestMessage()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteData = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeByte(0);
    }
    catch (Exception ex)
    {
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      byteData = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(byteData);
    }
    return byteData;
  }
  
  private final byte[] createInitialResponceMessage()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream dos = null;
    byte[] byteData = null;
    try
    {
      dos = new DataOutputStream(bos);
      dos.writeByte(1);
      dos.writeInt(this.dataList.size());
      for (Iterator localIterator = this.dataList.iterator(); localIterator.hasNext();)
      {
        entity = (EpicEntity)localIterator.next();
        
        dos.writeLong(entity.getId());
        dos.writeInt(entity.getType());
        dos.writeInt(entity.getMapHex() == null ? -1 : entity.getMapHex().getId());
        dos.writeInt(entity.getTargetHex());
        dos.writeUTF(entity.getName());
        
        long now = System.currentTimeMillis();
        long remaining = entity.getTimeUntilLeave() - now;
        dos.writeLong(remaining);
        if ((!entity.isCollectable()) && (!entity.isSource()))
        {
          dos.writeFloat(entity.getCurrentSkill(102));
          dos.writeFloat(entity.getCurrentSkill(103));
          dos.writeFloat(entity.getCurrentSkill(104));
          dos.writeFloat(entity.getCurrentSkill(100));
          dos.writeFloat(entity.getCurrentSkill(101));
          dos.writeFloat(entity.getCurrentSkill(105));
          dos.writeFloat(entity.getCurrentSkill(106));
          
          List<EpicEntity> list = entity.getAllCollectedItems();
          int collected = list.size();
          dos.writeInt(collected);
          for (int i = 0; i < collected; i++)
          {
            dos.writeUTF(((EpicEntity)list.get(i)).getName());
            dos.writeInt(((EpicEntity)list.get(i)).getType());
            dos.writeLong(((EpicEntity)list.get(i)).getId());
          }
        }
      }
      Object allFights = ValreiFightHistoryManager.getInstance().getAllFights();
      dos.writeInt(((ArrayList)allFights).size());
      for (ValreiFightHistory vf : (ArrayList)allFights) {
        writeFightDetails(dos, vf);
      }
    }
    catch (Exception ex)
    {
      EpicEntity entity;
      logger.log(Level.WARNING, ex.getMessage(), ex);
    }
    finally
    {
      StreamUtilities.closeOutputStreamIgnoreExceptions(dos);
      byteData = bos.toByteArray();
      StreamUtilities.closeOutputStreamIgnoreExceptions(bos);
      setData(byteData);
    }
    return byteData;
  }
  
  public void writeFightDetails(DataOutputStream dos, ValreiFightHistory vf)
    throws IOException
  {
    dos.writeLong(vf.getFightId());
    dos.writeInt(vf.getMapHexId());
    dos.writeUTF(vf.getMapHexName());
    dos.writeLong(vf.getFightTime());
    dos.writeInt(vf.getFighters().size());
    for (ValreiFightHistory.ValreiFighter v : vf.getFighters().values())
    {
      dos.writeLong(v.getFighterId());
      dos.writeUTF(v.getName());
    }
    dos.writeInt(vf.getTotalActions());
    for (int i = 0; i <= vf.getTotalActions(); i++)
    {
      ValreiConstants.ValreiFightAction act = vf.getFightAction(i);
      dos.writeInt(act.getActionNum());
      dos.writeShort(act.getActionId());
      dos.writeInt(act.getActionData().length);
      dos.write(act.getActionData());
    }
  }
  
  public void execute()
  {
    DataInputStream dis = null;
    byte type = -1;
    try
    {
      dis = new DataInputStream(new ByteArrayInputStream(getData()));
      
      type = dis.readByte();
      if (type == 2) {
        readUpdateRequest(dis);
      } else if (type == 0) {
        handleInitialRequest();
      } else if (type == 1) {
        readFullRequestResponce(dis);
      } else if (type == 3) {
        handleTimeUpdateRequest();
      } else if (type == 4) {
        readTimeUpdateMessage(dis);
      } else if (type == 5) {
        readFightDetails(dis);
      }
    }
    catch (IOException ex)
    {
      logger.log(Level.WARNING, "Unpack exception " + ex.getMessage() + " messageType " + type, ex);
    }
    finally
    {
      StreamUtilities.closeInputStreamIgnoreExceptions(dis);
    }
  }
  
  private void readTimeUpdateMessage(DataInputStream dis)
    throws IOException
  {
    int count = dis.readInt();
    for (int i = 0; i < count; i++)
    {
      long id = dis.readLong();
      long remaining = dis.readLong();
      
      ValreiMapData.updateEntityTime(id, remaining);
    }
  }
  
  private void handleTimeUpdateRequest()
  {
    WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), (byte)4);
    updater.sendFromLoginServer();
  }
  
  private void handleInitialRequest()
  {
    WCValreiMapUpdater updater = new WCValreiMapUpdater(WurmId.getNextWCCommandId(), (byte)1);
    updater.sendFromLoginServer();
  }
  
  public void testDataEncoding()
  {
    collectData();
    byte[] responce = createInitialResponceMessage();
    DataInputStream dis = null;
    try
    {
      dis = new DataInputStream(new ByteArrayInputStream(responce));
      byte type = dis.readByte();
      if (type == 1) {
        readFullRequestResponce(dis);
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
  
  private void readUpdateRequest(DataInputStream dis)
    throws IOException
  {
    long id = dis.readLong();
    int type = dis.readInt();
    int hexId = dis.readInt();
    int targetHex = dis.readInt();
    String name = dis.readUTF();
    long remainingTime = dis.readLong();
    float attack = 0.0F;
    float vitality = 0.0F;
    float bodyStr = 0.0F;
    float bodySta = 0.0F;
    float bodyCon = 0.0F;
    float mindLog = 0.0F;
    float mindSpe = 0.0F;
    float soulStr = 0.0F;
    float soulDep = 0.0F;
    
    List<CollectedValreiItem> list = new ArrayList();
    if ((type != 2) && (type != 1))
    {
      bodyStr = dis.readFloat();
      bodySta = dis.readFloat();
      bodyCon = dis.readFloat();
      mindLog = dis.readFloat();
      mindSpe = dis.readFloat();
      soulStr = dis.readFloat();
      soulDep = dis.readFloat();
      
      int count = dis.readInt();
      for (int j = 0; j < count; j++)
      {
        String carriedName = dis.readUTF();
        int collType = dis.readInt();
        long collId = dis.readLong();
        list.add(new CollectedValreiItem(collId, carriedName, collType));
      }
    }
    ValreiMapData.updateEntity(id, hexId, type, targetHex, name, remainingTime, bodyStr, bodySta, bodyCon, mindLog, mindSpe, soulStr, soulDep, list);
  }
  
  private void readFullRequestResponce(DataInputStream dis)
    throws IOException
  {
    int size = dis.readInt();
    for (int i = 0; i < size; i++)
    {
      long id = dis.readLong();
      int type = dis.readInt();
      int hexId = dis.readInt();
      int targetHex = dis.readInt();
      String name = dis.readUTF();
      long remainingTime = dis.readLong();
      float attack = 0.0F;
      float vitality = 0.0F;
      float bodyStr = 0.0F;
      float bodySta = 0.0F;
      float bodyCon = 0.0F;
      float mindLog = 0.0F;
      float mindSpe = 0.0F;
      float soulStr = 0.0F;
      float soulDep = 0.0F;
      List<CollectedValreiItem> list = new ArrayList();
      if ((type != 2) && (type != 1))
      {
        bodyStr = dis.readFloat();
        bodySta = dis.readFloat();
        bodyCon = dis.readFloat();
        mindLog = dis.readFloat();
        mindSpe = dis.readFloat();
        soulStr = dis.readFloat();
        soulDep = dis.readFloat();
        
        int count = dis.readInt();
        for (int j = 0; j < count; j++)
        {
          String carriedName = dis.readUTF();
          int carriedType = dis.readInt();
          long collId = dis.readLong();
          list.add(new CollectedValreiItem(collId, carriedName, carriedType));
        }
      }
      ValreiMapData.updateEntity(id, hexId, type, targetHex, name, remainingTime, bodyStr, bodySta, bodyCon, mindLog, mindSpe, soulStr, soulDep, list);
    }
    int fightsSize = dis.readInt();
    for (int i = 0; i < fightsSize; i++) {
      readFightDetails(dis);
    }
    ValreiMapData.setHasInitialData();
  }
  
  private void readFightDetails(DataInputStream dis)
    throws IOException
  {
    long fightId = dis.readLong();
    int mapHexId = dis.readInt();
    String mapHexName = dis.readUTF();
    long fightTime = dis.readLong();
    
    ValreiFightHistory vf = new ValreiFightHistory(fightId, mapHexId, mapHexName, fightTime);
    
    int fightersSize = dis.readInt();
    for (int j = 0; j < fightersSize; j++)
    {
      long fighterId = dis.readLong();
      String fighterName = dis.readUTF();
      vf.addFighter(fighterId, fighterName);
    }
    int actionsSize = dis.readInt();
    for (int j = 0; j <= actionsSize; j++)
    {
      int actionNum = dis.readInt();
      short actionId = dis.readShort();
      int dataLen = dis.readInt();
      byte[] actionData = new byte[dataLen];
      dis.read(actionData);
      
      vf.addAction(actionId, actionData);
    }
    vf.setFightCompleted(true);
    ValreiFightHistoryManager.getInstance().addFight(fightId, vf, false);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\webinterface\WCValreiMapUpdater.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */