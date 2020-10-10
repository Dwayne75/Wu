package com.wurmonline.server.bodys;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.combat.CombatConstants;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.CreatureStatus;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemFactory;
import com.wurmonline.server.items.NoSpaceException;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.shared.constants.CounterTypes;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Body
  implements CounterTypes, CombatConstants, MiscConstants
{
  private Wounds wounds;
  private final Creature owner;
  private short centimetersHigh;
  private short centimetersLong;
  private short centimetersWide;
  private final BodyTemplate template;
  private static final Logger logger = Logger.getLogger(Body.class.getName());
  private static final String GET_WOUNDS = "SELECT * FROM WOUNDS WHERE OWNER=?";
  private final Item[] spaces;
  private boolean initialized = false;
  
  Body(BodyTemplate aTemplate, Creature aOwner, short aCentimetersHigh, short aCentimetersLong, short aCentimetersWide)
  {
    this.template = aTemplate;
    this.owner = aOwner;
    this.centimetersHigh = aCentimetersHigh;
    this.centimetersLong = aCentimetersLong;
    this.centimetersWide = aCentimetersWide;
    this.spaces = new Item[48];
  }
  
  public byte getType()
  {
    return this.template.type;
  }
  
  public final void loadWounds()
  {
    if ((this.owner instanceof Player))
    {
      Connection dbcon = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      try
      {
        dbcon = DbConnector.getPlayerDbCon();
        ps = dbcon.prepareStatement("SELECT * FROM WOUNDS WHERE OWNER=?");
        ps.setLong(1, this.owner.getWurmId());
        rs = ps.executeQuery();
        while (rs.next())
        {
          DbWound wound = new DbWound(rs.getLong("ID"), rs.getByte("TYPE"), rs.getByte("LOCATION"), rs.getFloat("SEVERITY"), this.owner.getWurmId(), rs.getFloat("POISONSEVERITY"), rs.getFloat("INFECTIONSEVERITY"), rs.getLong("LASTPOLLED"), rs.getBoolean("BANDAGED"), rs.getByte("HEALEFF"));
          addWound(wound);
        }
      }
      catch (SQLException sqx)
      {
        logger.log(Level.WARNING, sqx.getMessage(), sqx);
      }
      finally
      {
        DbUtilities.closeDatabaseObjects(ps, rs);
        DbConnector.returnConnection(dbcon);
      }
    }
  }
  
  public void sendWounds()
  {
    if ((this.owner instanceof Player)) {
      if (this.wounds != null)
      {
        Wound[] w = this.wounds.getWounds();
        for (int x = 0; x < w.length; x++) {
          try
          {
            this.owner.getCommunicator().sendAddWound(w[x], getBodyPartForWound(w[x]));
          }
          catch (NoSpaceException nsp)
          {
            logger.log(Level.INFO, nsp.getMessage(), nsp);
          }
        }
      }
    }
  }
  
  public Wounds getWounds()
  {
    return this.wounds;
  }
  
  public boolean addWound(Wound wound)
  {
    if ((this.owner.isInvulnerable()) && (!this.owner.isPlayer()))
    {
      if (wound.getType() != 7) {
        logger.log(Level.INFO, "Invulnerable " + this.owner.getName() + " receiving wound. Ignoring.", new Exception());
      }
      wound.delete();
      return false;
    }
    if (this.wounds == null) {
      this.wounds = new Wounds();
    }
    this.wounds.addWound(wound);
    this.owner.setWounded();
    return this.owner.getStatus().modifyWounds((int)wound.getSeverity());
  }
  
  public boolean isWounded()
  {
    if (this.wounds == null) {
      return false;
    }
    return this.wounds.hasWounds();
  }
  
  public void removeWound(Wound wound)
  {
    if (this.wounds != null) {
      this.wounds.remove(wound);
    }
  }
  
  public long getId()
  {
    if (this.spaces[0] != null) {
      return this.spaces[0].getWurmId();
    }
    logger.log(Level.INFO, "This should be looked into:", new Exception());
    return -10L;
  }
  
  public long getOwnerId()
  {
    if (this.owner == null) {
      return -10L;
    }
    return this.owner.getWurmId();
  }
  
  public String getWoundLocationString(int location)
  {
    return this.template.typeString[location];
  }
  
  public Item getBodyPartForWound(Wound wound)
    throws NoSpaceException
  {
    int pos = wound.getLocation();
    if ((this.spaces == null) || (pos < 0)) {
      throw new NoSpaceException(String.valueOf(pos));
    }
    Item toReturn = null;
    if ((pos == 1) || (pos == 17)) {
      toReturn = this.spaces[1];
    } else if ((pos == 29) || (pos == 18) || (pos == 19) || (pos == 20)) {
      toReturn = this.spaces[29];
    } else if (pos == 36) {
      toReturn = this.spaces[1];
    } else if ((pos == 2) || (pos == 21) || (pos == 27) || (pos == 26) || (pos == 32) || (pos == 23) || (pos == 24) || (pos == 25) || (pos == 22) || (pos == 46) || (pos == 47) || (pos == 45) || (pos == 42) || (pos == 35) || (pos == 0)) {
      toReturn = this.spaces[2];
    } else if ((pos == 3) || (pos == 5) || (pos == 9) || (pos == 44)) {
      toReturn = this.spaces[3];
    } else if ((pos == 4) || (pos == 6) || (pos == 10)) {
      toReturn = this.spaces[4];
    } else if ((pos == 30) || (pos == 7) || (pos == 11) || (pos == 31) || (pos == 8) || (pos == 12) || (pos == 43) || (pos == 41)) {
      toReturn = this.spaces[34];
    } else if ((pos == 37) || (pos == 39)) {
      toReturn = this.spaces[13];
    } else if ((pos == 38) || (pos == 40)) {
      toReturn = this.spaces[14];
    } else if (pos < this.spaces.length) {
      toReturn = this.spaces[pos];
    }
    if (toReturn == null) {
      throw new NoSpaceException("No space for " + getWoundLocationString(pos));
    }
    return toReturn;
  }
  
  public void healFully()
  {
    if (this.wounds != null)
    {
      Wound[] w = this.wounds.getWounds();
      for (int x = 0; x < w.length; x++) {
        this.wounds.remove(w[x]);
      }
    }
    this.owner.setDisease((byte)0);
    this.owner.getStatus().removeWounds();
  }
  
  public short getCentimetersLong()
  {
    return this.centimetersLong;
  }
  
  public short getCentimetersHigh()
  {
    return this.centimetersHigh;
  }
  
  public short getCentimetersWide()
  {
    return this.centimetersWide;
  }
  
  public float getWeight(byte weightLevel)
  {
    float modifier = 1.0F;
    if (weightLevel >= 50) {
      modifier = 1.0F + (weightLevel - 50) / 100.0F;
    } else {
      modifier = 0.5F * (1.0F + Math.max(1, weightLevel) / 50.0F);
    }
    return this.centimetersHigh * this.centimetersLong * this.centimetersWide / 1.4F * modifier;
  }
  
  public void setCentimetersLong(short aCentimetersLong)
  {
    this.centimetersLong = aCentimetersLong;
    this.owner.calculateSize();
  }
  
  public void setCentimetersHigh(short aCentimetersHigh)
  {
    this.centimetersHigh = aCentimetersHigh;
    this.owner.calculateSize();
  }
  
  public void setCentimetersWide(short aCentimetersWide)
  {
    this.centimetersWide = aCentimetersWide;
    this.owner.calculateSize();
  }
  
  public Item getBodyPart(int pos)
    throws NoSpaceException
  {
    if ((this.spaces == null) || (pos < 0)) {
      throw new NoSpaceException(String.valueOf(pos));
    }
    Item toReturn = null;
    if ((pos == 1) || (pos == 17)) {
      toReturn = this.spaces[1];
    } else if ((pos == 29) || (pos == 18) || (pos == 19) || (pos == 20)) {
      toReturn = this.spaces[29];
    } else if ((pos == 2) || (pos == 21) || (pos == 27) || (pos == 26) || (pos == 32) || (pos == 23) || (pos == 24) || (pos == 25) || (pos == 22)) {
      toReturn = this.spaces[2];
    } else if ((pos == 3) || (pos == 5) || (pos == 9)) {
      toReturn = this.spaces[3];
    } else if ((pos == 4) || (pos == 6) || (pos == 10)) {
      toReturn = this.spaces[4];
    } else if ((pos == 30) || (pos == 7) || (pos == 11) || (pos == 31) || (pos == 8) || (pos == 12)) {
      toReturn = this.spaces[34];
    } else if ((pos == 38) || (pos == 40))
    {
      if (this.template.type == 0)
      {
        toReturn = this.spaces[pos];
        if (toReturn == null) {
          toReturn = this.spaces[14];
        }
      }
      else
      {
        toReturn = this.spaces[14];
      }
    }
    else if ((pos == 37) || (pos == 39))
    {
      if (this.template.type == 0)
      {
        toReturn = this.spaces[pos];
        if (toReturn == null) {
          toReturn = this.spaces[13];
        }
      }
      else
      {
        toReturn = this.spaces[13];
      }
    }
    else if (pos == 44)
    {
      if (this.template.type == 0)
      {
        toReturn = this.spaces[pos];
        if (toReturn == null) {
          toReturn = this.spaces[3];
        }
      }
      else
      {
        toReturn = this.spaces[3];
      }
    }
    else if ((pos == 43) || (pos == 41))
    {
      if (this.template.type == 0)
      {
        toReturn = this.spaces[pos];
        if (toReturn == null) {
          toReturn = this.spaces[34];
        }
      }
      else
      {
        toReturn = this.spaces[34];
      }
    }
    else if (pos == 36)
    {
      if (this.template.type == 0)
      {
        toReturn = this.spaces[pos];
        if (toReturn == null) {
          toReturn = this.spaces[1];
        }
      }
      else
      {
        toReturn = this.spaces[1];
      }
    }
    else if ((pos == 35) || (pos == 42) || (pos == 45) || (pos == 46) || (pos == 47))
    {
      if (this.template.type == 0)
      {
        toReturn = this.spaces[pos];
        if (toReturn == null) {
          toReturn = this.spaces[2];
        }
      }
      else
      {
        toReturn = this.spaces[2];
      }
    }
    else if (pos < this.spaces.length) {
      toReturn = this.spaces[pos];
    }
    if (toReturn == null) {
      throw new NoSpaceException("No space for " + getWoundLocationString(pos));
    }
    return toReturn;
  }
  
  public Item[] getSpaces()
  {
    return this.spaces;
  }
  
  public Item[] getAllItems()
  {
    Set<Item> items = new HashSet();
    for (int x = 0; x < this.spaces.length; x++) {
      if (this.spaces[x] != null)
      {
        Item[] itemarr = this.spaces[x].getAllItems(false);
        for (int y = 0; y < itemarr.length; y++) {
          if (!itemarr[y].isBodyPart()) {
            items.add(itemarr[y]);
          }
        }
      }
    }
    Item[] toReturn = new Item[items.size()];
    toReturn = (Item[])items.toArray(toReturn);
    return toReturn;
  }
  
  public Item[] getContainersAndWornItems()
  {
    Set<Item> items = getContainersAndWornItems(getBodyItem());
    return (Item[])items.toArray(new Item[items.size()]);
  }
  
  public Set<Item> getContainersAndWornItems(Item item)
  {
    Set<Item> items = item.getItems();
    Set<Item> newItems = new HashSet();
    for (Iterator<Item> it = items.iterator(); it.hasNext();)
    {
      Item next = (Item)it.next();
      if (next.isBodyPart()) {
        newItems.addAll(getContainersAndWornItems(next));
      } else {
        newItems.add(next);
      }
    }
    return newItems;
  }
  
  public byte getRandomWoundPos()
    throws Exception
  {
    return this.template.getRandomWoundPos();
  }
  
  public byte getRandomWoundPos(byte attackerStance)
    throws Exception
  {
    if (attackerStance == 7) {
      return this.template.getHighWoundPos();
    }
    if (attackerStance == 10) {
      return this.template.getLowWoundPos();
    }
    if (attackerStance == 6) {
      return this.template.getUpperLeftWoundPos();
    }
    if (attackerStance == 1) {
      return this.template.getUpperRightWoundPos();
    }
    if (attackerStance == 5) {
      return this.template.getMidLeftWoundPos();
    }
    if (attackerStance == 2) {
      return this.template.getMidRightWoundPos();
    }
    if (attackerStance == 3) {
      return this.template.getLowerRightWoundPos();
    }
    if (attackerStance == 4) {
      return this.template.getLowerLeftWoundPos();
    }
    return this.template.getCenterWoundPos();
  }
  
  public byte getCenterWoundPos()
    throws Exception
  {
    return this.template.getCenterWoundPos();
  }
  
  private void createBodyPart(byte bodyConstant, int tempalteId, String partName, byte constData)
    throws FailedException, NoSuchTemplateException
  {
    this.spaces[bodyConstant] = ItemFactory.createBodyPart(this, (short)bodyConstant, tempalteId, partName, 50.0F);
    this.spaces[bodyConstant].setAuxData(constData);
  }
  
  public void createBodyParts()
    throws FailedException, NoSuchTemplateException
  {
    if (this.initialized) {
      return;
    }
    createBodyPart((byte)0, 16, this.template.bodyS, (byte)24);
    createBodyPart((byte)1, 12, this.template.headS, (byte)2);
    createBodyPart((byte)13, 14, this.template.leftHandS, (byte)7);
    
    createBodyPart((byte)14, 14, this.template.rightHandS, (byte)8);
    
    createBodyPart((byte)15, 15, this.template.leftFootS, (byte)9);
    
    createBodyPart((byte)16, 15, this.template.rightFootS, (byte)10);
    
    createBodyPart((byte)2, 13, this.template.torsoS, (byte)3);
    createBodyPart((byte)29, 17, this.template.faceS, (byte)25);
    createBodyPart((byte)3, 11, this.template.leftArmS, (byte)5);
    createBodyPart((byte)4, 11, this.template.rightArmS, (byte)6);
    
    createBodyPart((byte)34, 19, this.template.legsS, (byte)4);
    if (this.template.type == 4) {
      this.spaces[28] = ItemFactory.createBodyPart(this, 28, 12, this.template.secondHeadS, 50.0F);
    }
    if (this.template.type == 8)
    {
      this.spaces[31] = ItemFactory.createBodyPart(this, 31, 10, this.template.rightLegS, 50.0F);
      
      this.spaces[30] = ItemFactory.createBodyPart(this, 30, 10, this.template.leftLegS, 50.0F);
    }
    if ((this.template.type == 0) && (this.owner.isPlayer()))
    {
      this.spaces[40] = createEquipmentSlot(40, "right ring", 16);
      
      this.spaces[14].insertItem(this.spaces[40]);
      Item rHeld = createEquipmentSlot((byte)38, "right held item", (byte)1);
      
      rHeld.setDescription("main weapon");
      this.spaces[14].insertItem(rHeld);
      this.spaces[13].insertItem(createEquipmentSlot((byte)39, "left ring", (byte)17));
      
      Item lHeld = createEquipmentSlot((byte)37, "left held item", (byte)0);
      
      lHeld.setDescription("off-hand weapon");
      this.spaces[13].insertItem(lHeld);
      this.spaces[3].insertItem(createEquipmentSlot((byte)44, "shield slot", (byte)11));
      
      this.spaces[2].insertItem(createEquipmentSlot((byte)45, "cape", (byte)14));
      
      this.spaces[2].insertItem(createEquipmentSlot((byte)46, "left shoulder", (byte)18));
      
      this.spaces[2].insertItem(createEquipmentSlot((byte)47, "right shoulder", (byte)19));
      
      this.spaces[2].insertItem(createEquipmentSlot((byte)42, "back", (byte)20));
      
      this.spaces[1].insertItem(createEquipmentSlot((byte)36, "neck", (byte)21));
      
      this.spaces[34].insertItem(createEquipmentSlot((byte)43, "belt", (byte)22));
      
      this.spaces[34].insertItem(createEquipmentSlot((byte)41, "hip slot", (byte)23));
      
      this.spaces[2].insertItem(createEquipmentSlot((byte)35, "tabard", (byte)15));
    }
    buildBody();
    this.initialized = true;
  }
  
  private Item createEquipmentSlot(byte space, String name, byte slotConstant)
    throws FailedException, NoSuchTemplateException
  {
    Item item = ItemFactory.createBodyPart(this, (short)space, 823, name, 50.0F);
    item.setAuxData(slotConstant);
    item.setOwnerId(this.owner.getWurmId());
    this.spaces[space] = item;
    return item;
  }
  
  private void buildBody()
  {
    this.template.buildBody(this.spaces, this.owner);
  }
  
  public Item getBodyItem()
  {
    return this.spaces[0];
  }
  
  public void load()
    throws Exception
  {
    createBodyParts();
  }
  
  public void sleep(Creature sleeper, boolean epicServer)
    throws IOException
  {
    this.spaces[0].sleep(sleeper, epicServer);
  }
  
  public void poll()
  {
    if (this.wounds != null) {
      this.wounds.poll(this.owner);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\bodys\Body.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */