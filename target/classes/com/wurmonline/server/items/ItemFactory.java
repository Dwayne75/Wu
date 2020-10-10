package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.FailedException;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.Server;
import com.wurmonline.server.WurmId;
import com.wurmonline.server.bodys.Body;
import com.wurmonline.server.effects.Effect;
import com.wurmonline.server.effects.EffectFactory;
import com.wurmonline.server.epic.HexMap;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.ItemMaterials;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ItemFactory
  implements MiscConstants, ItemTypes, ItemMaterials
{
  private static final Logger logger = Logger.getLogger(ItemFactory.class.getName());
  private static final String deleteItemData = "delete from ITEMDATA where WURMID=?";
  private static DbStrings dbstrings;
  
  @Nonnull
  public static Item createItem(int templateId, float qualityLevel, byte material, byte aRarity, @Nullable String creator)
    throws FailedException, NoSuchTemplateException
  {
    return createItem(templateId, qualityLevel, material, aRarity, -10L, creator);
  }
  
  public static Optional<Item> createItemOptional(int templateId, float qualityLevel, byte material, byte aRarity, @Nullable String creator)
  {
    try
    {
      return Optional.of(createItem(templateId, qualityLevel, material, aRarity, creator));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return Optional.empty();
  }
  
  public static void createContainerRestrictions(Item item)
  {
    ItemTemplate template = item.getTemplate();
    if ((template.getContainerRestrictions() != null) && (!template.isNoPut())) {
      for (ContainerRestriction cRest : template.getContainerRestrictions())
      {
        boolean skipAdd = false;
        for (Item i : item.getItems()) {
          if ((i.getTemplateId() == 1392) && (cRest.contains(i.getRealTemplateId()))) {
            skipAdd = true;
          } else if (cRest.contains(i.getTemplateId())) {
            skipAdd = true;
          }
        }
        if (!skipAdd) {
          try
          {
            Item tempSlotItem = createItem(1392, 100.0F, item.getCreatorName());
            tempSlotItem.setRealTemplate(cRest.getEmptySlotTemplateId());
            tempSlotItem.setName(cRest.getEmptySlotName());
            
            item.insertItem(tempSlotItem, true);
          }
          catch (FailedException|NoSuchTemplateException localFailedException1) {}
        }
      }
    }
  }
  
  @Nonnull
  public static Item createItem(int templateId, float qualityLevel, byte material, byte aRarity, long bridgeId, @Nullable String creator)
    throws FailedException, NoSuchTemplateException
  {
    ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
    if (material == 0) {
      material = template.getMaterial();
    }
    String name = generateName(template, material);
    if (template.isTemporary()) {
      try
      {
        Item toReturn = new TempItem(name, template, qualityLevel, creator);
        if (logger.isLoggable(Level.FINEST)) {
          logger.finest("Creating tempitem: " + toReturn);
        }
      }
      catch (IOException ex)
      {
        throw new FailedException(ex);
      }
    } else {
      try
      {
        if (template.isRecycled)
        {
          Item toReturn = Itempool.getRecycledItem(templateId, qualityLevel);
          if (toReturn != null)
          {
            if (toReturn.isTemporary()) {
              toReturn.clear(WurmId.getNextTempItemId(), creator, 0.0F, 0.0F, 0.0F, 1.0F, "", name, qualityLevel, material, aRarity, bridgeId);
            } else {
              toReturn.clear(toReturn.id, creator, 0.0F, 0.0F, 0.0F, 1.0F, "", name, qualityLevel, material, aRarity, bridgeId);
            }
            return toReturn;
          }
        }
        Item toReturn = new DbItem(-10L, name, template, qualityLevel, material, aRarity, bridgeId, creator);
        if (template.isCoin()) {
          Server.getInstance().transaction(toReturn.getWurmId(), -10L, bridgeId, "new " + toReturn.getName(), template
            .getValue());
        }
      }
      catch (IOException iox)
      {
        throw new FailedException(iox);
      }
    }
    Item toReturn;
    if (template.getInitialContainers() != null) {
      for (InitialContainer ic : template.getInitialContainers())
      {
        byte icMaterial = ic.getMaterial() == 0 ? material : ic.getMaterial();
        Item subItem = createItem(ic.getTemplateId(), Math.max(1.0F, qualityLevel), icMaterial, aRarity, creator);
        
        subItem.setName(ic.getName());
        toReturn.insertItem(subItem, true);
      }
    }
    if (toReturn != null) {
      createContainerRestrictions(toReturn);
    }
    return toReturn;
  }
  
  public static Item createItem(int templateId, float qualityLevel, byte aRarity, @Nullable String creator)
    throws FailedException, NoSuchTemplateException
  {
    return createItem(templateId, qualityLevel, (byte)0, aRarity, creator);
  }
  
  public static Optional<Item> createItemOptional(int templateId, float qualityLevel, byte aRarity, @Nullable String creator)
  {
    return createItemOptional(templateId, qualityLevel, (byte)0, aRarity, creator);
  }
  
  @Nonnull
  public static Item createItem(int templateId, float qualityLevel, @Nullable String creator)
    throws FailedException, NoSuchTemplateException
  {
    return createItem(templateId, qualityLevel, (byte)0, (byte)0, creator);
  }
  
  public static String generateName(ItemTemplate template, byte material)
  {
    String name = template.sizeString + template.getName();
    if (template.getTemplateId() == 683) {
      name = HexMap.generateFirstName() + " " + HexMap.generateSecondName();
    }
    if (template.unique) {
      name = template.getName();
    }
    return name;
  }
  
  public static Item createBodyPart(Body body, short place, int templateId, String name, float qualityLevel)
    throws FailedException, NoSuchTemplateException
  {
    ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
    Item toReturn = null;
    try
    {
      long wurmId = WurmId.getNextBodyPartId(body.getOwnerId(), (byte)place, 
        WurmId.getType(body.getOwnerId()) == 0);
      if (template.isRecycled)
      {
        toReturn = Itempool.getRecycledItem(templateId, qualityLevel);
        if (toReturn != null)
        {
          toReturn.clear(-10L, "", 0.0F, 0.0F, 0.0F, 0.0F, "", name, qualityLevel, template.getMaterial(), (byte)0, -10L);
          
          toReturn.setPlace(place);
        }
      }
      if (toReturn == null) {
        toReturn = new TempItem(wurmId, place, name, template, qualityLevel, "");
      }
    }
    catch (IOException ex)
    {
      throw new FailedException(ex);
    }
    return toReturn;
  }
  
  @Nullable
  public static Item createInventory(long ownerId, short place, float qualityLevel)
    throws FailedException, NoSuchTemplateException
  {
    ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(0);
    Item toReturn = null;
    try
    {
      long wurmId = WurmId.getNextBodyPartId(ownerId, (byte)place, 
        WurmId.getType(ownerId) == 0);
      if (template.isRecycled)
      {
        toReturn = Itempool.getRecycledItem(0, qualityLevel);
        if (toReturn != null) {
          toReturn.clear(wurmId, "", 0.0F, 0.0F, 0.0F, 0.0F, "", "inventory", qualityLevel, template
            .getMaterial(), (byte)0, -10L);
        }
      }
      if (toReturn == null) {
        toReturn = new TempItem(wurmId, place, "inventory", template, qualityLevel, "");
      }
    }
    catch (IOException ex)
    {
      throw new FailedException(ex);
    }
    return toReturn;
  }
  
  public static Item loadItem(long id)
    throws NoSuchItemException, Exception
  {
    Item item = null;
    if ((WurmId.getType(id) == 2) || (WurmId.getType(id) == 19) || 
      (WurmId.getType(id) == 20)) {
      item = new DbItem(id);
    } else {
      throw new NoSuchItemException("Temporary item.");
    }
    return item;
  }
  
  public static void decay(long id, @Nullable DbStrings dbStrings)
  {
    dbstrings = dbStrings;
    if (dbstrings == null) {
      dbstrings = Item.getDbStringsByWurmId(id);
    }
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement(dbstrings.deleteItem());
      ps.setLong(1, id);
      ps.executeUpdate();
      DbUtilities.closeDatabaseObjects(ps, null);
      
      ps = dbcon.prepareStatement("delete from ITEMDATA where WURMID=?");
      ps.setLong(1, id);
      ps.executeUpdate();
      DbUtilities.closeDatabaseObjects(ps, null);
      
      ps = dbcon.prepareStatement("DELETE FROM ITEMKEYS WHERE LOCKID=?");
      ps.setLong(1, id);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to decay item with id " + id, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static void clearData(long id)
  {
    Connection dbcon = null;
    PreparedStatement ps = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement("delete from ITEMDATA where WURMID=?");
      ps.setLong(1, id);
      ps.executeUpdate();
      DbUtilities.closeDatabaseObjects(ps, null);
      
      ps = dbcon.prepareStatement("DELETE FROM ITEMKEYS WHERE LOCKID=?");
      ps.setLong(1, id);
      ps.executeUpdate();
    }
    catch (SQLException ex)
    {
      logger.log(Level.WARNING, "Failed to decay item with id " + id, ex);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, null);
      DbConnector.returnConnection(dbcon);
    }
  }
  
  public static Item createItem(int templateId, float qualityLevel, float posX, float posY, float rot, boolean onSurface, byte rarity, long bridgeId, @Nullable String creator)
    throws NoSuchTemplateException, FailedException
  {
    return createItem(templateId, qualityLevel, posX, posY, rot, onSurface, (byte)0, rarity, bridgeId, creator);
  }
  
  public static Item createItem(int templateId, float qualityLevel, float posX, float posY, float rot, boolean onSurface, byte material, byte aRarity, long bridgeId, @Nullable String creator)
    throws NoSuchTemplateException, FailedException
  {
    return createItem(templateId, qualityLevel, posX, posY, rot, onSurface, material, aRarity, bridgeId, creator, (byte)0);
  }
  
  public static Item createItem(int templateId, float qualityLevel, float posX, float posY, float rot, boolean onSurface, byte material, byte aRarity, long bridgeId, @Nullable String creator, byte initialAuxData)
    throws NoSuchTemplateException, FailedException
  {
    float height = 0.0F;
    try
    {
      height = Zones.calculateHeight(posX, posY, onSurface);
    }
    catch (NoSuchZoneException nsz)
    {
      logger.log(Level.WARNING, "Could not calculate height for position: " + posX + ", " + posY + ", surfaced: " + onSurface + " due to " + nsz
        .getMessage(), nsz);
    }
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("Factory trying to create item with id " + templateId + " at " + posX + ", " + posY + ", " + height + ".");
    }
    ItemTemplate template = ItemTemplateFactory.getInstance().getTemplate(templateId);
    if (material == 0) {
      material = template.getMaterial();
    }
    String name = generateName(template, material);
    Item toReturn = null;
    try
    {
      if (template.isRecycled)
      {
        toReturn = Itempool.getRecycledItem(templateId, qualityLevel);
        if (toReturn != null) {
          if (toReturn.isTemporary()) {
            toReturn.clear(WurmId.getNextTempItemId(), creator, posX, posY, height, rot, "", name, qualityLevel, material, aRarity, bridgeId);
          } else {
            toReturn.clear(toReturn.id, creator, posX, posY, height, rot, "", name, qualityLevel, material, aRarity, bridgeId);
          }
        }
      }
      if (toReturn == null) {
        if (template.isTemporary()) {
          toReturn = new TempItem(name, template, qualityLevel, posX, posY, height, rot, bridgeId, creator);
        } else {
          toReturn = new DbItem(name, template, qualityLevel, posX, posY, height, rot, material, aRarity, bridgeId, creator);
        }
      }
      try
      {
        if ((toReturn.getTemplateId() == 385) || (toReturn.getTemplateId() == 731)) {
          toReturn.setAuxData((byte)(100 + initialAuxData));
        }
        Zone zone = Zones.getZone((int)posX >> 2, (int)posY >> 2, onSurface);
        zone.addItem(toReturn);
        if ((toReturn.getTemplateId() == 385) || (toReturn.getTemplateId() == 731)) {
          toReturn.setAuxData(initialAuxData);
        }
      }
      catch (NoSuchZoneException sex)
      {
        logger.log(Level.WARNING, "Could not get Zone for position: " + posX + ", " + posY + ", surfaced: " + onSurface + " due to " + sex
          .getMessage(), sex);
      }
    }
    catch (IOException ex)
    {
      throw new FailedException(ex);
    }
    toReturn.setOwner(-10L, true);
    if (toReturn.isFire())
    {
      toReturn.setTemperature((short)20000);
      Effect effect = EffectFactory.getInstance().createFire(toReturn.getWurmId(), toReturn.getPosX(), toReturn
        .getPosY(), toReturn.getPosZ(), toReturn.isOnSurface());
      toReturn.addEffect(effect);
    }
    return toReturn;
  }
  
  public static int[] metalLumpList = { 46, 221, 223, 205, 47, 220, 49, 44, 45, 48, 837, 698, 694, 1411 };
  
  public static boolean isMetalLump(int itemTemplateId)
  {
    for (int lumpId : metalLumpList) {
      if (lumpId == itemTemplateId) {
        return true;
      }
    }
    return false;
  }
  
  public static Optional<Item> createItemOptional(int itemTemplateId, float qualityLevel, String creator)
  {
    try
    {
      return Optional.of(createItem(itemTemplateId, qualityLevel, creator));
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return Optional.empty();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\ItemFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */