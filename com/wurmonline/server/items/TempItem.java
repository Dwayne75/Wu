package com.wurmonline.server.items;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.combat.ArmourTemplate;
import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.MovementScheme;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.modifiers.DoubleValueModifier;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class TempItem
  extends Item
{
  private static final Logger logger = Logger.getLogger(TempItem.class.getName());
  
  TempItem() {}
  
  public void bless(int blesser)
  {
    if (this.bless == 0) {
      this.bless = ((byte)blesser);
    }
  }
  
  public void setOwnerStuff(ItemTemplate templ) {}
  
  public void enchant(byte ench)
  {
    if (this.enchantment != ench) {
      this.enchantment = ench;
    }
  }
  
  public void setColor(int _color)
  {
    this.color = _color;
  }
  
  public void setColor2(int _color)
  {
    this.color2 = _color;
  }
  
  public void setLastOwnerId(long oid)
  {
    this.lastOwner = oid;
  }
  
  public TempItem(String aName, ItemTemplate aTemplate, float qLevel, @Nullable String aCreator)
    throws IOException
  {
    super(-10L, aName, aTemplate, qLevel, (byte)0, (byte)0, -10L, aCreator);
  }
  
  public TempItem(long wurmId, short aPlace, String aName, ItemTemplate aTemplate, float qLevel, @Nullable String aCreator)
    throws IOException
  {
    super(wurmId, aName, aTemplate, qLevel, (byte)1, (byte)0, -10L, aCreator);
    setPlace(aPlace);
  }
  
  public TempItem(String aName, short aPlace, ItemTemplate aTemplate, float aQualityLevel, String aCreator)
    throws IOException
  {
    super(aName, aPlace, aTemplate, aQualityLevel, (byte)0, (byte)0, -10L, aCreator);
  }
  
  public TempItem(String aName, ItemTemplate aTemplate, float aQualityLevel, float aPosX, float aPosY, float aPosZ, float aRotation, long bridgeId, String aCreator)
    throws IOException
  {
    super(aName, aTemplate, aQualityLevel, aPosX, aPosY, aPosZ, aRotation, (byte)0, (byte)0, bridgeId, aCreator);
  }
  
  void create(float aQualityLevel, long aCreationDate)
    throws IOException
  {
    this.qualityLevel = aQualityLevel;
    this.lastMaintained = aCreationDate;
  }
  
  void load()
    throws Exception
  {}
  
  public void loadEffects() {}
  
  void setPlace(short aPlace)
  {
    this.place = aPlace;
  }
  
  public short getPlace()
  {
    return this.place;
  }
  
  public void setLastMaintained(long time)
  {
    this.lastMaintained = time;
  }
  
  public long getLastMaintained()
  {
    return this.lastMaintained;
  }
  
  public boolean setQualityLevel(float newLevel)
  {
    this.qualityLevel = newLevel;
    return false;
  }
  
  public long getOwnerId()
  {
    return this.ownerId;
  }
  
  public boolean setOwnerId(long aOwnerId)
  {
    this.ownerId = aOwnerId;
    return true;
  }
  
  public boolean getLocked()
  {
    return this.locked;
  }
  
  public void setLocked(boolean aLocked)
  {
    this.locked = aLocked;
  }
  
  public int getTemplateId()
  {
    return this.template.getTemplateId();
  }
  
  public void setTemplateId(int aId)
  {
    try
    {
      this.template = ItemTemplateFactory.getInstance().getTemplate(aId);
    }
    catch (NoSuchTemplateException nst)
    {
      logger.log(Level.WARNING, nst.getMessage(), nst);
    }
  }
  
  public void setZoneId(int aId, boolean isOnSurface)
  {
    this.surfaced = isOnSurface;
    this.zoneId = aId;
  }
  
  public int getZoneId()
  {
    if (this.parentId != -10L) {
      if (Items.isItemLoaded(this.parentId)) {
        try
        {
          Item parent = Items.getItem(this.parentId);
          return parent.getZoneId();
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, "This REALLY shouldn't happen! parentId: " + this.parentId, nsi);
        }
      }
    }
    return this.zoneId;
  }
  
  public boolean setDescription(String desc)
  {
    this.description = desc;
    return false;
  }
  
  public String getDescription()
  {
    return this.description;
  }
  
  public void setName(String newname)
  {
    this.name = newname;
  }
  
  public void setName(String newname, boolean sendUpdate)
  {
    setName(newname);
  }
  
  public boolean setInscription(String aInscription, String aInscriber)
  {
    return setInscription(aInscription, aInscriber, 0);
  }
  
  public boolean setInscription(String aInscription, String aInscriber, int penColour)
  {
    this.inscription.setInscription(aInscription);
    this.inscription.setInscriber(aInscriber);
    this.inscription.setPenColour(penColour);
    return true;
  }
  
  public float getRotation()
  {
    return this.rotation;
  }
  
  public void setPos(float aPosX, float aPosY, float aPosZ, float aRotation, long bridgeId)
  {
    this.posX = aPosX;
    this.posY = aPosY;
    this.posZ = aPosZ;
    this.rotation = aRotation;
    this.onBridge = bridgeId;
  }
  
  public void setPosXYZRotation(float _posX, float _posY, float _posZ, float _rot)
  {
    this.posX = _posX;
    this.posY = _posY;
    this.posZ = _posZ;
    this.rotation = _rot;
  }
  
  public void setPosXYZ(float _posX, float _posY, float _posZ)
  {
    this.posX = _posX;
    this.posY = _posY;
    this.posZ = _posZ;
  }
  
  public void setPosXY(float _posX, float _posY)
  {
    this.posX = _posX;
    this.posY = _posY;
  }
  
  public void setPosX(float aPosX)
  {
    this.posX = aPosX;
  }
  
  public void setPosY(float aPosY)
  {
    this.posY = aPosY;
  }
  
  public void setPosZ(float aPosZ)
  {
    this.posZ = aPosZ;
  }
  
  public void setRotation(float aRotation)
  {
    this.rotation = aRotation;
  }
  
  public float getQualityLevel()
  {
    return this.qualityLevel;
  }
  
  public float getDamage()
  {
    return this.damage;
  }
  
  public Set<Item> getItems()
  {
    if (this.items == null) {
      this.items = new HashSet();
    }
    return this.items;
  }
  
  public Item[] getItemsAsArray()
  {
    if (this.items == null) {
      return new Item[0];
    }
    return (Item[])this.items.toArray(new Item[this.items.size()]);
  }
  
  public void setParentId(long pid, boolean isOnSurface)
  {
    this.surfaced = isOnSurface;
    if (this.parentId != pid)
    {
      if (pid == -10L)
      {
        if (this.watchers != null) {
          for (Creature watcher : this.watchers) {
            watcher.getCommunicator().sendRemoveFromInventory(this);
          }
        }
        this.watchers = null;
      }
      else
      {
        try
        {
          Item parent = Items.getItem(pid);
          if (this.ownerId != parent.getOwnerId()) {
            if ((parent.getPosX() != getPosX()) || (parent.getPosY() != getPosY())) {
              setPosXYZ(getPosX(), getPosY(), getPosZ());
            }
          }
        }
        catch (NoSuchItemException nsi)
        {
          logger.log(Level.WARNING, nsi.getMessage(), nsi);
        }
      }
      this.parentId = pid;
    }
  }
  
  public long getParentId()
  {
    return this.parentId;
  }
  
  void setSizeX(int sizex)
  {
    this.sizeX = sizex;
  }
  
  void setSizeY(int sizey)
  {
    this.sizeY = sizey;
  }
  
  void setSizeZ(int sizez)
  {
    this.sizeZ = sizez;
  }
  
  public int getSizeX()
  {
    if (this.sizeX > 0) {
      return this.sizeX;
    }
    return this.template.getSizeX();
  }
  
  public int getSizeY()
  {
    if (this.sizeY > 0) {
      return this.sizeY;
    }
    return this.template.getSizeY();
  }
  
  public int getSizeZ()
  {
    if (this.sizeZ > 0) {
      return this.sizeZ;
    }
    return this.template.getSizeZ();
  }
  
  public void setOriginalQualityLevel(float qlevel) {}
  
  public float getOriginalQualityLevel()
  {
    return this.originalQualityLevel;
  }
  
  public boolean setDamage(float dam)
  {
    float modifier = 1.0F;
    float difference = dam - this.damage;
    if (difference > 0.0F) {
      if (getSpellEffects() != null)
      {
        modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_DAMAGETAKEN);
        difference *= modifier;
      }
    }
    return setDamage(this.damage + difference, false);
  }
  
  public boolean setDamage(float dam, boolean override)
  {
    this.damage = dam;
    if (dam >= 100.0F)
    {
      setQualityLevel(0.0F);
      checkDecay();
      return true;
    }
    return false;
  }
  
  public void setData1(int data1)
  {
    if (this.data == null) {
      this.data = new ItemData(this.id, data1, -1, -1, -1);
    }
    this.data.data1 = data1;
  }
  
  public void setData2(int data2)
  {
    if (this.data == null) {
      this.data = new ItemData(this.id, -1, data2, -1, -1);
    }
    this.data.data2 = data2;
  }
  
  public void setData(int data1, int data2)
  {
    if (this.data == null) {
      this.data = new ItemData(this.id, data1, data2, -1, -1);
    }
    this.data.data1 = data1;
    this.data.data2 = data2;
  }
  
  public int getData1()
  {
    if (this.data != null) {
      return this.data.data1;
    }
    return -1;
  }
  
  public int getData2()
  {
    if (this.data != null) {
      return this.data.data2;
    }
    return -1;
  }
  
  public int getWeightGrams()
  {
    if (getSpellEffects() == null) {
      return this.weight;
    }
    float modifier = getSpellEffects().getRuneEffect(RuneUtilities.ModifierEffect.ENCH_WEIGHT);
    return (int)(this.weight * modifier);
  }
  
  public boolean setWeight(int w, boolean destroyOnWeightZero)
  {
    return setWeight(w, destroyOnWeightZero, false);
  }
  
  public boolean setWeight(int w, boolean destroyOnWeightZero, boolean sameOwner)
  {
    if (this.weight <= 0)
    {
      Items.destroyItem(this.id);
      return true;
    }
    this.weight = w;
    if (this.parentId != -10L) {
      updateParents();
    }
    return false;
  }
  
  public byte getMaterial()
  {
    return this.material;
  }
  
  public void setMaterial(byte mat)
  {
    this.material = mat;
  }
  
  public long getLockId()
  {
    return this.lockid;
  }
  
  public void setLockId(long lid)
  {
    this.lockid = lid;
  }
  
  void addItem(@Nullable Item item, boolean loading)
  {
    if (item != null)
    {
      getItems().add(item);
      if (this.parentId != -10L) {
        updateParents();
      }
    }
    else
    {
      logger.warning("Ignored attempt to add a null item to " + this);
    }
  }
  
  void removeItem(Item item)
  {
    if (this.items != null) {
      this.items.remove(item);
    }
    if (item.wornAsArmour) {
      item.setWornAsArmour(false, getOwnerId());
    }
    if (this.parentId != -10L) {
      updateParents();
    }
  }
  
  public void setPrice(int newPrice)
  {
    this.price = newPrice;
  }
  
  public void setTemperature(short temp)
  {
    this.temperature = temp;
  }
  
  public void setBanked(boolean bank)
  {
    this.banked = bank;
  }
  
  public void setAuxData(byte auxdata)
  {
    this.auxbyte = auxdata;
  }
  
  public void setCreationState(byte newState)
  {
    this.creationState = newState;
  }
  
  public void setRealTemplate(int rTemplate)
  {
    this.realTemplate = rTemplate;
  }
  
  void setWornAsArmour(boolean wornArmour, long newOwner)
  {
    if (this.wornAsArmour != wornArmour)
    {
      this.wornAsArmour = wornArmour;
      if (this.wornAsArmour) {
        try
        {
          Creature creature = Server.getInstance().getCreature(newOwner);
          ArmourTemplate armour = ArmourTemplate.getArmourTemplate(this.template.templateId);
          if (armour != null)
          {
            float moveModChange = armour.getMoveModifier();
            if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
              moveModChange *= ArmourTemplate.getMaterialMovementModifier(getMaterial());
            } else if (Servers.localServer.isChallengeOrEpicServer()) {
              if ((getMaterial() == 57) || (getMaterial() == 67)) {
                moveModChange *= 0.9F;
              } else if (getMaterial() == 56) {
                moveModChange *= 0.95F;
              }
            }
            creature.getMovementScheme().armourMod.setModifier(creature.getMovementScheme().armourMod.getModifier() - moveModChange);
          }
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, "Worn armour on unknown player: ", nsp);
        }
        catch (NoSuchCreatureException cnf)
        {
          logger.log(Level.WARNING, "Worn armour on unknown creature: ", cnf);
        }
      } else {
        try
        {
          Creature creature = Server.getInstance().getCreature(getOwnerId());
          ArmourTemplate armour = ArmourTemplate.getArmourTemplate(this.template.templateId);
          if (armour != null)
          {
            float moveModChange = armour.getMoveModifier();
            if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
              moveModChange *= ArmourTemplate.getMaterialMovementModifier(getMaterial());
            } else if (Servers.localServer.isChallengeOrEpicServer()) {
              if ((getMaterial() == 57) || (getMaterial() == 67)) {
                moveModChange *= 0.9F;
              } else if (getMaterial() == 56) {
                moveModChange *= 0.95F;
              }
            }
            creature.getMovementScheme().armourMod.setModifier(creature.getMovementScheme().armourMod.getModifier() + moveModChange);
          }
        }
        catch (NoSuchPlayerException nsp)
        {
          logger.log(Level.WARNING, "Worn armour on unknown player: ", nsp);
        }
        catch (NoSuchCreatureException cnf)
        {
          logger.log(Level.WARNING, "Worn armour on unknown creature: ", cnf);
        }
      }
    }
  }
  
  public void savePosition() {}
  
  public void setFemale(boolean _female)
  {
    this.female = _female;
  }
  
  public void setTransferred(boolean trans)
  {
    this.transferred = trans;
  }
  
  void addNewKey(long keyId) {}
  
  void removeNewKey(long keyId) {}
  
  public void setMailed(boolean _mailed)
  {
    this.mailed = _mailed;
  }
  
  public void setCreator(String _creator)
  {
    this.creator = _creator;
  }
  
  public void setHidden(boolean _hidden)
  {
    this.hidden = _hidden;
  }
  
  public void setDbStrings(DbStrings dbStrings) {}
  
  public DbStrings getDbStrings()
  {
    return null;
  }
  
  void clear(long wurmId, String _creator, float posx, float posy, float posz, float _rot, String _desc, String _name, float _qualitylevel, byte _material, byte aRarity, long bridgeId)
  {
    this.id = wurmId;
    this.creator = _creator;
    this.posX = posx;
    this.posY = posy;
    this.posZ = posz;
    this.description = _desc;
    this.name = _name;
    this.qualityLevel = _qualitylevel;
    this.originalQualityLevel = this.qualityLevel;
    this.rotation = _rot;
    this.zoneId = -10;
    this.parentId = -10L;
    this.sizeX = this.template.getSizeX();
    this.sizeY = this.template.getSizeY();
    this.sizeZ = this.template.getSizeZ();
    this.weight = this.template.getWeightGrams();
    this.lastMaintained = WurmCalendar.currentTime;
    this.creationDate = WurmCalendar.currentTime;
    this.creationState = 0;
    this.banked = false;
    this.damage = 0.0F;
    this.enchantment = 0;
    this.material = _material;
    this.rarity = aRarity;
    this.onBridge = bridgeId;
    this.creationState = 0;
  }
  
  public void setMailTimes(byte times) {}
  
  public void returnFromFreezer() {}
  
  public void moveToFreezer() {}
  
  public void deleteInDatabase() {}
  
  public boolean setRarity(byte newRarity)
  {
    if (newRarity != this.rarity)
    {
      this.rarity = newRarity;
      return true;
    }
    return false;
  }
  
  public void savePermissions() {}
  
  boolean saveInscription()
  {
    return false;
  }
  
  public void setExtra1(int extra1)
  {
    if (this.data == null) {
      this.data = new ItemData(this.id, -1, -1, -1, -1);
    }
    this.data.extra1 = extra1;
  }
  
  public void setExtra2(int extra2)
  {
    if (this.data == null) {
      this.data = new ItemData(this.id, -1, -1, -1, -1);
    }
    this.data.extra2 = extra2;
  }
  
  public void setExtra(int extra1, int extra2)
  {
    if (this.data == null) {
      this.data = new ItemData(this.id, -1, -1, -1, -1);
    }
    this.data.extra1 = extra1;
    this.data.extra2 = extra2;
  }
  
  public int getExtra1()
  {
    if (this.data != null) {
      return this.data.extra1;
    }
    return -1;
  }
  
  public int getExtra2()
  {
    if (this.data != null) {
      return this.data.extra2;
    }
    return -1;
  }
  
  public void setAllData(int data1, int data2, int extra1, int extra2)
  {
    if (this.data == null) {
      this.data = new ItemData(this.id, -1, -1, -1, -1);
    }
    this.data.data1 = data1;
    this.data.data2 = data2;
    this.data.extra1 = extra1;
    this.data.extra2 = extra2;
  }
  
  public void setPlacedOnParent(boolean onParent)
  {
    this.placedOnParent = onParent;
  }
  
  public boolean isItem()
  {
    return true;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\TempItem.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */