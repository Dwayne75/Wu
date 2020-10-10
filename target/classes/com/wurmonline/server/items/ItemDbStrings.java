package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;

public final class ItemDbStrings
  implements DbStrings
{
  private static ItemDbStrings instance;
  
  public String createItem()
  {
    return "insert into ITEMS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,CREATIONDATE,RARITY,CREATOR,ONBRIDGE,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  }
  
  public String transferItem()
  {
    return "insert into ITEMS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID,SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,BLESS,ENCHANT,TEMPERATURE, PRICE,BANKED,AUXDATA,CREATIONDATE,CREATIONSTATE,REALTEMPLATE,WORNARMOUR,COLOR,COLOR2,PLACE,POSX,POSY,POSZ,CREATOR,FEMALE,MAILED,MAILTIMES,RARITY,ONBRIDGE,LASTOWNERID,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  }
  
  public String loadItem()
  {
    return "select * from ITEMS where WURMID=?";
  }
  
  public String loadEffects()
  {
    return "select * from EFFECTS where OWNER=?";
  }
  
  public String getLock()
  {
    return "select * from LOCKS where WURMID=?";
  }
  
  public String getKeys()
  {
    return "select KEYID from ITEMKEYS where LOCKID=?";
  }
  
  public String addKey()
  {
    return "INSERT INTO ITEMKEYS (LOCKID,KEYID) VALUES(?,?)";
  }
  
  public String removeKey()
  {
    return "DELETE FROM ITEMKEYS WHERE KEYID=? AND LOCKID=?";
  }
  
  public String createLock()
  {
    return "insert into LOCKS ( WURMID, LOCKED) values(?,?)";
  }
  
  public String setZoneId()
  {
    return "UPDATE ITEMS SET ZONEID=? WHERE WURMID=?";
  }
  
  public String getZoneId()
  {
    return "SELECT ZONEID FROM ITEMS WHERE WURMID=?";
  }
  
  public String setParentId()
  {
    return "UPDATE ITEMS SET PARENTID=? WHERE WURMID=?";
  }
  
  public String getParentId()
  {
    return "SELECT PARENTID FROM ITEMS WHERE WURMID=?";
  }
  
  public String setTemplateId()
  {
    return "UPDATE ITEMS SET TEMPLATEID=? WHERE WURMID=?";
  }
  
  public String getTemplateId()
  {
    return "SELECT TEMPLATEID FROM ITEMS WHERE WURMID=?";
  }
  
  public String setInscription()
  {
    return "UPDATE INSCRIPTIONS SET INSCRIPTION=? WHERE WURMID=?";
  }
  
  public String getInscription()
  {
    return "SELECT INSCRIPTION FROM INSCRIPTIONS WHERE WURMID=?";
  }
  
  public String createInscription()
  {
    return "INSERT INTO INSCRIPTIONS (WURMID, INSCRIPTION, INSCRIBER, PENCOLOR) VALUES (?,?,?,?)";
  }
  
  public String setName()
  {
    return "UPDATE ITEMS SET NAME=? WHERE WURMID=?";
  }
  
  public String getName()
  {
    return "SELECT NAME FROM ITEMS WHERE WURMID=?";
  }
  
  public String setRarity()
  {
    return "UPDATE ITEMS SET RARITY=? WHERE WURMID=?";
  }
  
  public String setDescription()
  {
    return "UPDATE ITEMS SET DESCRIPTION=? WHERE WURMID=?";
  }
  
  public String getDescription()
  {
    return "SELECT DESCRIPTION FROM ITEMS WHERE WURMID=?";
  }
  
  public String setPlace()
  {
    return "UPDATE ITEMS SET PLACE=? WHERE WURMID=?";
  }
  
  public String getPlace()
  {
    return "SELECT PLACE FROM ITEMS WHERE WURMID=?";
  }
  
  public String setQualityLevel()
  {
    return "UPDATE ITEMS SET QUALITYLEVEL=? WHERE WURMID=?";
  }
  
  public String getQualityLevel()
  {
    return "SELECT QUALITYLEVEL FROM ITEMS WHERE WURMID=?";
  }
  
  public String setOriginalQualityLevel()
  {
    return "UPDATE ITEMS SET ORIGINALQUALITYLEVEL=? WHERE WURMID=?";
  }
  
  public String getOriginalQualityLevel()
  {
    return "SELECT ORIGINALQUALITYLEVEL FROM ITEMS WHERE WURMID=?";
  }
  
  public String setLastMaintained()
  {
    return "UPDATE ITEMS SET LASTMAINTAINED=? WHERE WURMID=?";
  }
  
  public String getLastMaintained()
  {
    return "SELECT LASTMAINTAINED FROM ITEMS WHERE WURMID=?";
  }
  
  public String setOwnerId()
  {
    return "UPDATE ITEMS SET OWNERID=? WHERE WURMID=?";
  }
  
  public String setLastOwnerId()
  {
    return "UPDATE ITEMS SET LASTOWNERID=? WHERE WURMID=?";
  }
  
  public String getOwnerId()
  {
    return "SELECT OWNERID FROM ITEMS WHERE WURMID=?";
  }
  
  public String setPosXYZRotation()
  {
    return "UPDATE ITEMS SET POSX=?, POSY=?, POSZ=?, ROTATION=? WHERE WURMID=?";
  }
  
  public String getPosXYZRotation()
  {
    return "SELECT POSX, POSY, POSZ, ROTATION FROM ITEMS WHERE WURMID=?";
  }
  
  public String setPosXYZ()
  {
    return "UPDATE ITEMS SET POSX=?, POSY=?, POSZ=? WHERE WURMID=?";
  }
  
  public String getPosXYZ()
  {
    return "SELECT POSX, POSY, POSZ FROM ITEMS WHERE WURMID=?";
  }
  
  public String setPosXY()
  {
    return "UPDATE ITEMS SET POSX=?, POSY=? WHERE WURMID=?";
  }
  
  public String getPosXY()
  {
    return "SELECT POSX, POSY FROM ITEMS WHERE WURMID=?";
  }
  
  public String setPosX()
  {
    return "UPDATE ITEMS SET POSX=? WHERE WURMID=?";
  }
  
  public String getPosX()
  {
    return "SELECT POSX FROM ITEMS WHERE WURMID=?";
  }
  
  public String setWeight()
  {
    return "UPDATE ITEMS SET WEIGHT=? WHERE WURMID=?";
  }
  
  public String getWeight()
  {
    return "SELECT WEIGHT FROM ITEMS WHERE WURMID=?";
  }
  
  public String setPosY()
  {
    return "UPDATE ITEMS SET POSY=? WHERE WURMID=?";
  }
  
  public String getPosY()
  {
    return "SELECT POSY FROM ITEMS WHERE WURMID=?";
  }
  
  public String setPosZ()
  {
    return "UPDATE ITEMS SET POSZ=? WHERE WURMID=?";
  }
  
  public String getPosZ()
  {
    return "SELECT POSZ FROM ITEMS WHERE WURMID=?";
  }
  
  public String setRotation()
  {
    return "UPDATE ITEMS SET ROTATION=? WHERE WURMID=?";
  }
  
  public String getRotation()
  {
    return "SELECT ROTATION FROM ITEMS WHERE WURMID=?";
  }
  
  public String savePos()
  {
    return "UPDATE ITEMS SET POSX=?,POSY=?,POSZ=?,ROTATION=?,ONBRIDGE=? WHERE WURMID=?";
  }
  
  public String clearItem()
  {
    return "UPDATE ITEMS SET NAME=?,DESCRIPTION=?,QUALITYLEVEL=?,ORIGINALQUALITYLEVEL=?,LASTMAINTAINED=?,ENCHANT=?,BANKED=?,SIZEX=?,SIZEY=?,SIZEZ=?,ZONEID=?,DAMAGE=?,PARENTID=?, ROTATION=?,WEIGHT=?,POSX=?,POSY=?,POSZ=?,CREATOR=?,AUXDATA=?,COLOR=?,COLOR2=?,TEMPERATURE=?,CREATIONDATE=?,CREATIONSTATE=0,MATERIAL=?, BLESS=?, MAILED=0, MAILTIMES=0,RARITY=?,CREATIONSTATE=?, OWNERID=-10, LASTOWNERID=-10 WHERE WURMID=?";
  }
  
  public String setDamage()
  {
    return "UPDATE ITEMS SET DAMAGE=?, LASTMAINTAINED=? WHERE WURMID=?";
  }
  
  public String getDamage()
  {
    return "SELECT DAMAGE FROM ITEMS WHERE WURMID=?";
  }
  
  public String setLocked()
  {
    return "UPDATE LOCKS SET LOCKED=? WHERE WURMID=?";
  }
  
  public String getLocked()
  {
    return "SELECT LOCKED FROM LOCKS WHERE WURMID=?";
  }
  
  public String setTransferred()
  {
    return "UPDATE ITEMS SET TRANSFERRED=? WHERE WURMID=?";
  }
  
  public String getAllItems()
  {
    return "SELECT * from ITEMS where PARENTID=?";
  }
  
  public String getItem()
  {
    return "SELECT * from ITEMS where WURMID=?";
  }
  
  public String setBless()
  {
    return "UPDATE ITEMS SET BLESS=? WHERE WURMID=?";
  }
  
  public String setSizeX()
  {
    return "UPDATE ITEMS SET SIZEX=? WHERE WURMID=?";
  }
  
  public String getSizeX()
  {
    return "SELECT SIZEX FROM ITEMS WHERE WURMID=?";
  }
  
  public String setSizeY()
  {
    return "UPDATE ITEMS SET SIZEY=? WHERE WURMID=?";
  }
  
  public String getSizeY()
  {
    return "SELECT SIZEY FROM ITEMS WHERE WURMID=?";
  }
  
  public String setSizeZ()
  {
    return "UPDATE ITEMS SET SIZEZ=? WHERE WURMID=?";
  }
  
  public String getSizeZ()
  {
    return "SELECT SIZEZ FROM ITEMS WHERE WURMID=?";
  }
  
  public String setLockId()
  {
    return "UPDATE ITEMS SET LOCKID=? WHERE WURMID=?";
  }
  
  public String setPrice()
  {
    return "UPDATE ITEMS SET PRICE=? WHERE WURMID=?";
  }
  
  public String setAuxData()
  {
    return "UPDATE ITEMS SET AUXDATA=? WHERE WURMID=?";
  }
  
  public String setCreationState()
  {
    return "UPDATE ITEMS SET CREATIONSTATE=? WHERE WURMID=?";
  }
  
  public String setRealTemplate()
  {
    return "UPDATE ITEMS SET REALTEMPLATE=? WHERE WURMID=?";
  }
  
  public String setColor()
  {
    return "UPDATE ITEMS SET COLOR=?,COLOR2=? WHERE WURMID=?";
  }
  
  public String setEnchant()
  {
    return "UPDATE ITEMS SET ENCHANT=? WHERE WURMID=?";
  }
  
  public String setBanked()
  {
    return "UPDATE ITEMS SET BANKED=? WHERE WURMID=?";
  }
  
  public String getData()
  {
    return "select * from ITEMDATA where WURMID=?";
  }
  
  public String createData()
  {
    if (DbConnector.isUseSqlite()) {
      return "insert OR IGNORE into ITEMDATA ( DATA1, DATA2, EXTRA1, EXTRA2, WURMID) values(?,?,?,?,?)";
    }
    return "insert IGNORE into ITEMDATA ( DATA1, DATA2, EXTRA1, EXTRA2, WURMID) values(?,?,?,?,?)";
  }
  
  public String updateData1()
  {
    return "update ITEMDATA set DATA1=? where WURMID=?";
  }
  
  public String updateData2()
  {
    return "update ITEMDATA set DATA2=? where WURMID=?";
  }
  
  public String updateExtra1()
  {
    return "update ITEMDATA set EXTRA1=? where WURMID=?";
  }
  
  public String updateExtra2()
  {
    return "update ITEMDATA set EXTRA2=? where WURMID=?";
  }
  
  public String updateAllData()
  {
    return "update ITEMDATA set DATA1=?, DATA2=?, EXTRA1=?, EXTRA2=? where WURMID=?";
  }
  
  public String setTemperature()
  {
    return "UPDATE ITEMS SET TEMPERATURE=? WHERE WURMID=?";
  }
  
  public String getTemperature()
  {
    return "SELECT TEMPERATURE FROM ITEMS WHERE WURMID=?";
  }
  
  public String setMaterial()
  {
    return "UPDATE ITEMS SET MATERIAL=? WHERE WURMID=?";
  }
  
  public String setWornAsArmour()
  {
    return "UPDATE ITEMS SET WORNARMOUR=? WHERE WURMID=?";
  }
  
  public String setFemale()
  {
    return "UPDATE ITEMS SET FEMALE=? WHERE WURMID=?";
  }
  
  public String setMailed()
  {
    return "UPDATE ITEMS SET MAILED=? WHERE WURMID=?";
  }
  
  public String setCreator()
  {
    return "UPDATE ITEMS SET CREATOR=? WHERE WURMID=?";
  }
  
  public String getZoneItems()
  {
    return "SELECT * FROM ITEMS WHERE OWNERID=-10";
  }
  
  public String getCreatureItems()
  {
    return "SELECT * FROM ITEMS WHERE OWNERID=?";
  }
  
  public String getPreloadedItems()
  {
    return "SELECT * FROM ITEMS WHERE TEMPLATEID=?";
  }
  
  public String getCreatureItemsNonTransferred()
  {
    return "SELECT WURMID FROM ITEMS WHERE OWNERID=? AND TRANSFERRED=0";
  }
  
  public String updateLastMaintainedBankItem()
  {
    return "UPDATE ITEMS SET LASTMAINTAINED=? WHERE BANKED=1";
  }
  
  public String getItemWeights()
  {
    return "SELECT WURMID, WEIGHT,SIZEX,SIZEY,SIZEZ, TEMPLATEID FROM ITEMS";
  }
  
  public String getOwnedItems()
  {
    return "SELECT OWNERID FROM ITEMS WHERE OWNERID>0 GROUP BY OWNERID";
  }
  
  public String deleteByOwnerId()
  {
    return "DELETE FROM ITEMS WHERE OWNERID=?";
  }
  
  public String deleteTransferedItem()
  {
    return "DELETE FROM ITEMS WHERE WURMID=? AND TRANSFERRED=0";
  }
  
  public String deleteItem()
  {
    return "delete from ITEMS where WURMID=?";
  }
  
  public String getRecycledItems()
  {
    return "SELECT * FROM ITEMS WHERE TEMPLATEID=? AND BANKED=1";
  }
  
  public String getItemsForZone()
  {
    return "Select WURMID from ITEMS where ZONEID=? AND BANKED=0";
  }
  
  public String setHidden()
  {
    return "UPDATE ITEMS SET HIDDEN=? WHERE WURMID=?";
  }
  
  public String setSettings()
  {
    return "UPDATE ITEMS SET SETTINGS=? WHERE WURMID=?";
  }
  
  public String setMailTimes()
  {
    return "UPDATE ITEMS SET MAILTIMES=? WHERE WURMID=?";
  }
  
  public String freeze()
  {
    return "INSERT INTO FROZENITEMS SELECT * FROM ITEMS WHERE WURMID=?";
  }
  
  public String thaw()
  {
    return "INSERT INTO ITEMS SELECT * FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public static ItemDbStrings getInstance()
  {
    if (instance == null) {
      instance = new ItemDbStrings();
    }
    return instance;
  }
  
  public final String getDbStringsType()
  {
    return "ItemDbStrings";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\ItemDbStrings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */