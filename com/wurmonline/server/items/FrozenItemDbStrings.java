package com.wurmonline.server.items;

public final class FrozenItemDbStrings
  implements DbStrings
{
  private static FrozenItemDbStrings instance;
  
  public String createItem()
  {
    return "insert into FROZENITEMS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,CREATIONDATE,RARITY,CREATOR,ONBRIDGE,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  }
  
  public String transferItem()
  {
    return "insert into FROZENITEMS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,BLESS,ENCHANT,TEMPERATURE, PRICE,BANKED,AUXDATA,CREATIONDATE,CREATIONSTATE,REALTEMPLATE,WORNARMOUR,COLOR,COLOR2,PLACE,POSX,POSY,POSZ,CREATOR,FEMALE,MAILED,MAILTIMES,RARITY,ONBRIDGE,LASTOWNERID,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  }
  
  public String loadItem()
  {
    return "select * from FROZENITEMS where WURMID=?";
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
    return "UPDATE FROZENITEMS SET ZONEID=? WHERE WURMID=?";
  }
  
  public String getZoneId()
  {
    return "SELECT ZONEID FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setParentId()
  {
    return "UPDATE FROZENITEMS SET PARENTID=? WHERE WURMID=?";
  }
  
  public String getParentId()
  {
    return "SELECT PARENTID FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setTemplateId()
  {
    return "UPDATE FROZENITEMS SET TEMPLATEID=? WHERE WURMID=?";
  }
  
  public String getTemplateId()
  {
    return "SELECT TEMPLATEID FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setName()
  {
    return "UPDATE FROZENITEMS SET NAME=? WHERE WURMID=?";
  }
  
  public String getName()
  {
    return "SELECT NAME FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String getInscription()
  {
    return "SELECT INSCRIPTION FROM INSCRIPTIONS WHERE WURMID=?";
  }
  
  public String setInscription()
  {
    return "UPDATE INSCRIPTIONS SET INSCRIPTION = ? WHERE WURMID=?";
  }
  
  public String setRarity()
  {
    return "UPDATE FROZENITEMS SET RARITY=? WHERE WURMID=?";
  }
  
  public String setDescription()
  {
    return "UPDATE FROZENITEMS SET DESCRIPTION=? WHERE WURMID=?";
  }
  
  public String createInscription()
  {
    return "insert into INSCRIPTIONS (WURMID, INSCRIPTION, INSCRIBER) VALUES (?,?,?)";
  }
  
  public String getDescription()
  {
    return "SELECT DESCRIPTION FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setPlace()
  {
    return "UPDATE FROZENITEMS SET PLACE=? WHERE WURMID=?";
  }
  
  public String getPlace()
  {
    return "SELECT PLACE FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setQualityLevel()
  {
    return "UPDATE FROZENITEMS SET QUALITYLEVEL=? WHERE WURMID=?";
  }
  
  public String getQualityLevel()
  {
    return "SELECT QUALITYLEVEL FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setOriginalQualityLevel()
  {
    return "UPDATE FROZENITEMS SET ORIGINALQUALITYLEVEL=? WHERE WURMID=?";
  }
  
  public String getOriginalQualityLevel()
  {
    return "SELECT ORIGINALQUALITYLEVEL FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setLastMaintained()
  {
    return "UPDATE FROZENITEMS SET LASTMAINTAINED=? WHERE WURMID=?";
  }
  
  public String getLastMaintained()
  {
    return "SELECT LASTMAINTAINED FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setOwnerId()
  {
    return "UPDATE FROZENITEMS SET OWNERID=? WHERE WURMID=?";
  }
  
  public String setLastOwnerId()
  {
    return "UPDATE FROZENITEMS SET LASTOWNERID=? WHERE WURMID=?";
  }
  
  public String getOwnerId()
  {
    return "SELECT OWNERID FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setPosXYZRotation()
  {
    return "UPDATE FROZENITEMS SET POSX=?, POSY=?, POSZ=?, ROTATION=? WHERE WURMID=?";
  }
  
  public String getPosXYZRotation()
  {
    return "SELECT POSX, POSY, POSZ, ROTATION FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setPosXYZ()
  {
    return "UPDATE FROZENITEMS SET POSX=?, POSY=?, POSZ=? WHERE WURMID=?";
  }
  
  public String getPosXYZ()
  {
    return "SELECT POSX, POSY, POSZ FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setPosXY()
  {
    return "UPDATE FROZENITEMS SET POSX=?, POSY=? WHERE WURMID=?";
  }
  
  public String getPosXY()
  {
    return "SELECT POSX, POSY FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setPosX()
  {
    return "UPDATE FROZENITEMS SET POSX=? WHERE WURMID=?";
  }
  
  public String getPosX()
  {
    return "SELECT POSX FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setWeight()
  {
    return "UPDATE FROZENITEMS SET WEIGHT=? WHERE WURMID=?";
  }
  
  public String getWeight()
  {
    return "SELECT WEIGHT FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setPosY()
  {
    return "UPDATE FROZENITEMS SET POSY=? WHERE WURMID=?";
  }
  
  public String getPosY()
  {
    return "SELECT POSY FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setPosZ()
  {
    return "UPDATE FROZENITEMS SET POSZ=? WHERE WURMID=?";
  }
  
  public String getPosZ()
  {
    return "SELECT POSZ FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setRotation()
  {
    return "UPDATE FROZENITEMS SET ROTATION=? WHERE WURMID=?";
  }
  
  public String getRotation()
  {
    return "SELECT ROTATION FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String savePos()
  {
    return "UPDATE FROZENITEMS SET POSX=?,POSY=?,POSZ=?,ROTATION=?,ONBRIDGE=? WHERE WURMID=?";
  }
  
  public String clearItem()
  {
    return "UPDATE FROZENITEMS SET NAME=?,DESCRIPTION=?,QUALITYLEVEL=?,ORIGINALQUALITYLEVEL=?,LASTMAINTAINED=?,ENCHANT=?,BANKED=?,SIZEX=?,SIZEY=?,SIZEZ=?,ZONEID=?,DAMAGE=?,PARENTID=?, ROTATION=?,WEIGHT=?,POSX=?,POSY=?,POSZ=?,CREATOR=?,AUXDATA=?,COLOR=?,COLOR2=?,TEMPERATURE=?,CREATIONDATE=?,CREATIONSTATE=0,MATERIAL=?, BLESS=?, MAILED=0, MAILTIMES=0,RARITY=?, OWNERID=-10, LASTOWNERID=-10 WHERE WURMID=?";
  }
  
  public String setDamage()
  {
    return "UPDATE FROZENITEMS SET DAMAGE=?,LASTMAINTAINED=? WHERE WURMID=?";
  }
  
  public String getDamage()
  {
    return "SELECT DAMAGE FROM FROZENITEMS WHERE WURMID=?";
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
    return "UPDATE FROZENITEMS SET TRANSFERRED=? WHERE WURMID=?";
  }
  
  public String getAllItems()
  {
    return "SELECT * from FROZENITEMS where PARENTID=?";
  }
  
  public String getItem()
  {
    return "SELECT * from FROZENITEMS where WURMID=?";
  }
  
  public String setBless()
  {
    return "UPDATE FROZENITEMS SET BLESS=? WHERE WURMID=?";
  }
  
  public String setSizeX()
  {
    return "UPDATE FROZENITEMS SET SIZEX=? WHERE WURMID=?";
  }
  
  public String getSizeX()
  {
    return "SELECT SIZEX FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setSizeY()
  {
    return "UPDATE FROZENITEMS SET SIZEY=? WHERE WURMID=?";
  }
  
  public String getSizeY()
  {
    return "SELECT SIZEY FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setSizeZ()
  {
    return "UPDATE FROZENITEMS SET SIZEZ=? WHERE WURMID=?";
  }
  
  public String getSizeZ()
  {
    return "SELECT SIZEZ FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setLockId()
  {
    return "UPDATE FROZENITEMS SET LOCKID=? WHERE WURMID=?";
  }
  
  public String setPrice()
  {
    return "UPDATE FROZENITEMS SET PRICE=? WHERE WURMID=?";
  }
  
  public String setAuxData()
  {
    return "UPDATE FROZENITEMS SET AUXDATA=? WHERE WURMID=?";
  }
  
  public String setCreationState()
  {
    return "UPDATE FROZENITEMS SET CREATIONSTATE=? WHERE WURMID=?";
  }
  
  public String setRealTemplate()
  {
    return "UPDATE FROZENITEMS SET REALTEMPLATE=? WHERE WURMID=?";
  }
  
  public String setColor()
  {
    return "UPDATE FROZENITEMS SET COLOR=?,COLOR2=? WHERE WURMID=?";
  }
  
  public String setEnchant()
  {
    return "UPDATE FROZENITEMS SET ENCHANT=? WHERE WURMID=?";
  }
  
  public String setBanked()
  {
    return "UPDATE FROZENITEMS SET BANKED=? WHERE WURMID=?";
  }
  
  public String getData()
  {
    return "select * from ITEMDATA where WURMID=?";
  }
  
  public String createData()
  {
    return "insert into ITEMDATA ( DATA1, DATA2, WURMID) values(?,?,?)";
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
    return "UPDATE FROZENITEMS SET TEMPERATURE=? WHERE WURMID=?";
  }
  
  public String getTemperature()
  {
    return "SELECT TEMPERATURE FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public String setMaterial()
  {
    return "UPDATE FROZENITEMS SET MATERIAL=? WHERE WURMID=?";
  }
  
  public String setWornAsArmour()
  {
    return "UPDATE FROZENITEMS SET WORNARMOUR=? WHERE WURMID=?";
  }
  
  public String setFemale()
  {
    return "UPDATE FROZENITEMS SET FEMALE=? WHERE WURMID=?";
  }
  
  public String setMailed()
  {
    return "UPDATE FROZENITEMS SET MAILED=? WHERE WURMID=?";
  }
  
  public String setCreator()
  {
    return "UPDATE FROZENITEMS SET CREATOR=? WHERE WURMID=?";
  }
  
  public String getZoneItems()
  {
    return "SELECT * FROM FROZENITEMS WHERE OWNERID=-10";
  }
  
  public String getCreatureItems()
  {
    return "SELECT * FROM FROZENITEMS WHERE OWNERID=?";
  }
  
  public String getPreloadedItems()
  {
    return "SELECT * FROM FROZENITEMS WHERE TEMPLATEID=?";
  }
  
  public String getCreatureItemsNonTransferred()
  {
    return "SELECT WURMID FROM FROZENITEMS WHERE OWNERID=? AND TRANSFERRED=0";
  }
  
  public String updateLastMaintainedBankItem()
  {
    return "UPDATE FROZENITEMS SET LASTMAINTAINED=? WHERE BANKED=1";
  }
  
  public String getItemWeights()
  {
    return "SELECT WURMID, WEIGHT,SIZEX,SIZEY,SIZEZ, TEMPLATEID FROM FROZENITEMS";
  }
  
  public String getOwnedItems()
  {
    return "SELECT OWNERID FROM FROZENITEMS WHERE OWNERID>0 GROUP BY OWNERID";
  }
  
  public String deleteByOwnerId()
  {
    return "UPDATE FROZENITEMS SET OWNERID=-10 WHERE OWNERID=?";
  }
  
  public String deleteTransferedItem()
  {
    return "DELETE FROM FROZENITEMS WHERE WURMID=? AND TRANSFERRED=0";
  }
  
  public String deleteItem()
  {
    return "delete from FROZENITEMS where WURMID=?";
  }
  
  public String getRecycledItems()
  {
    return "SELECT * FROM FROZENITEMS WHERE TEMPLATEID=? AND BANKED=1";
  }
  
  public String getItemsForZone()
  {
    return "Select WURMID from FROZENITEMS where ZONEID=? AND BANKED=0";
  }
  
  public String setHidden()
  {
    return "UPDATE FROZENITEMS SET HIDDEN=? WHERE WURMID=?";
  }
  
  public String setSettings()
  {
    return "UPDATE FROZENITEMS SET SETTINGS=? WHERE WURMID=?";
  }
  
  public String setMailTimes()
  {
    return "UPDATE FROZENITEMS SET MAILTIMES=? WHERE WURMID=?";
  }
  
  public String freeze()
  {
    return "INSERT INTO FROZENITEMS SELECT * FROM ITEMS WHERE WURMID=?";
  }
  
  public String thaw()
  {
    return "INSERT INTO ITEMS SELECT * FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public static FrozenItemDbStrings getInstance()
  {
    if (instance == null) {
      instance = new FrozenItemDbStrings();
    }
    return instance;
  }
  
  public final String getDbStringsType()
  {
    return "FrozenItemDbStrings";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\FrozenItemDbStrings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */