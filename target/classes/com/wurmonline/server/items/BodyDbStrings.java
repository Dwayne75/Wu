package com.wurmonline.server.items;

public final class BodyDbStrings
  implements DbStrings
{
  private static BodyDbStrings instance;
  
  public String createItem()
  {
    return "insert into BODYPARTS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,CREATIONDATE,RARITY,CREATOR,ONBRIDGE,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  }
  
  public String transferItem()
  {
    return "insert into BODYPARTS (WURMID, TEMPLATEID, NAME,QUALITYLEVEL,ORIGINALQUALITYLEVEL, LASTMAINTAINED, OWNERID, SIZEX, SIZEY, SIZEZ, ZONEID, DAMAGE, ROTATION, PARENTID, WEIGHT, MATERIAL, LOCKID,DESCRIPTION,BLESS,ENCHANT,TEMPERATURE, PRICE,BANKED,AUXDATA,CREATIONDATE,CREATIONSTATE,REALTEMPLATE,WORNARMOUR,COLOR,COLOR2,PLACE,POSX,POSY,POSZ,CREATOR,FEMALE,MAILED,MAILTIMES,RARITY,ONBRIDGE,LASTOWNERID,SETTINGS) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
  }
  
  public String loadItem()
  {
    return "select * from BODYPARTS where WURMID=?";
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
    return "UPDATE BODYPARTS SET ZONEID=? WHERE WURMID=?";
  }
  
  public String getZoneId()
  {
    return "SELECT ZONEID FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setParentId()
  {
    return "UPDATE BODYPARTS SET PARENTID=? WHERE WURMID=?";
  }
  
  public String getParentId()
  {
    return "SELECT PARENTID FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setTemplateId()
  {
    return "UPDATE BODYPARTS SET TEMPLATEID=? WHERE WURMID=?";
  }
  
  public String getTemplateId()
  {
    return "SELECT TEMPLATEID FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setName()
  {
    return "UPDATE BODYPARTS SET NAME=? WHERE WURMID=?";
  }
  
  public String getInscription()
  {
    return "SELECT INSCRIPTION FROM INSCRIPTIONS WHERE WURMID=?";
  }
  
  public String setInscription()
  {
    return "UPDATE INSCRIPTIONS SET INSCRIPTION=? WHERE WURMID=?";
  }
  
  public String createInscription()
  {
    return "insert into INSCRIPTIONS (WURMID, INSCRIPTION, INSCRIBER) VALUES (?,?,?)";
  }
  
  public String setRarity()
  {
    return "UPDATE BODYPARTS SET RARITY=? WHERE WURMID=?";
  }
  
  public String getName()
  {
    return "SELECT NAME FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setDescription()
  {
    return "UPDATE BODYPARTS SET DESCRIPTION=? WHERE WURMID=?";
  }
  
  public String getDescription()
  {
    return "SELECT DESCRIPTION FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setPlace()
  {
    return "UPDATE BODYPARTS SET PLACE=? WHERE WURMID=?";
  }
  
  public String getPlace()
  {
    return "SELECT PLACE FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setQualityLevel()
  {
    return "UPDATE BODYPARTS SET QUALITYLEVEL=? WHERE WURMID=?";
  }
  
  public String getQualityLevel()
  {
    return "SELECT QUALITYLEVEL FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setOriginalQualityLevel()
  {
    return "UPDATE BODYPARTS SET ORIGINALQUALITYLEVEL=? WHERE WURMID=?";
  }
  
  public String getOriginalQualityLevel()
  {
    return "SELECT ORIGINALQUALITYLEVEL FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setLastMaintained()
  {
    return "UPDATE BODYPARTS SET LASTMAINTAINED=? WHERE WURMID=?";
  }
  
  public String getLastMaintained()
  {
    return "SELECT LASTMAINTAINED FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setOwnerId()
  {
    return "UPDATE BODYPARTS SET OWNERID=? WHERE WURMID=?";
  }
  
  public String setLastOwnerId()
  {
    return "UPDATE BODYPARTS SET LASTOWNERID=? WHERE WURMID=?";
  }
  
  public String getOwnerId()
  {
    return "SELECT OWNERID FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setPosXYZRotation()
  {
    return "UPDATE BODYPARTS SET POSX=?, POSY=?, POSZ=?, ROTATION=? WHERE WURMID=?";
  }
  
  public String getPosXYZRotation()
  {
    return "SELECT POSX, POSY, POSZ, ROTATION FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setPosXYZ()
  {
    return "UPDATE BODYPARTS SET POSX=?, POSY=?, POSZ=? WHERE WURMID=?";
  }
  
  public String getPosXYZ()
  {
    return "SELECT POSX, POSY, POSZ FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setPosXY()
  {
    return "UPDATE BODYPARTS SET POSX=?, POSY=? WHERE WURMID=?";
  }
  
  public String getPosXY()
  {
    return "SELECT POSX, POSY FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setPosX()
  {
    return "UPDATE BODYPARTS SET POSX=? WHERE WURMID=?";
  }
  
  public String getPosX()
  {
    return "SELECT POSX FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setWeight()
  {
    return "UPDATE BODYPARTS SET WEIGHT=? WHERE WURMID=?";
  }
  
  public String getWeight()
  {
    return "SELECT WEIGHT FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setPosY()
  {
    return "UPDATE BODYPARTS SET POSY=? WHERE WURMID=?";
  }
  
  public String getPosY()
  {
    return "SELECT POSY FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setPosZ()
  {
    return "UPDATE BODYPARTS SET POSZ=? WHERE WURMID=?";
  }
  
  public String getPosZ()
  {
    return "SELECT POSZ FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setRotation()
  {
    return "UPDATE BODYPARTS SET ROTATION=? WHERE WURMID=?";
  }
  
  public String getRotation()
  {
    return "SELECT ROTATION FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String savePos()
  {
    return "UPDATE BODYPARTS SET POSX=?,POSY=?,POSZ=?,ROTATION=?,ONBRIDGE=? WHERE WURMID=?";
  }
  
  public String clearItem()
  {
    return "UPDATE BODYPARTS SET NAME=?,DESCRIPTION=?,QUALITYLEVEL=?,ORIGINALQUALITYLEVEL=?,LASTMAINTAINED=?,ENCHANT=?,BANKED=?,SIZEX=?,SIZEY=?,SIZEZ=?,ZONEID=?,DAMAGE=?,PARENTID=?, ROTATION=?,WEIGHT=?,POSX=?,POSY=?,POSZ=?,CREATOR=?,AUXDATA=?,COLOR=?,COLOR2=?,TEMPERATURE=?,CREATIONDATE=?,CREATIONSTATE=0,MATERIAL=?, BLESS=?,RARITY=?,CREATIONSTATE=?, OWNERID=-10, LASTOWNERID=-10 WHERE WURMID=?";
  }
  
  public String setDamage()
  {
    return "UPDATE BODYPARTS SET DAMAGE=?, LASTMAINTAINED=? WHERE WURMID=?";
  }
  
  public String getDamage()
  {
    return "SELECT DAMAGE FROM BODYPARTS WHERE WURMID=?";
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
    return "UPDATE BODYPARTS SET TRANSFERRED=? WHERE WURMID=?";
  }
  
  public String getAllItems()
  {
    return "SELECT * from BODYPARTS where PARENTID=?";
  }
  
  public String getItem()
  {
    return "SELECT * from BODYPARTS where WURMID=?";
  }
  
  public String setBless()
  {
    return "UPDATE BODYPARTS SET BLESS=? WHERE WURMID=?";
  }
  
  public String setSizeX()
  {
    return "UPDATE BODYPARTS SET SIZEX=? WHERE WURMID=?";
  }
  
  public String getSizeX()
  {
    return "SELECT SIZEX FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setSizeY()
  {
    return "UPDATE BODYPARTS SET SIZEY=? WHERE WURMID=?";
  }
  
  public String getSizeY()
  {
    return "SELECT SIZEY FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setSizeZ()
  {
    return "UPDATE BODYPARTS SET SIZEZ=? WHERE WURMID=?";
  }
  
  public String getSizeZ()
  {
    return "SELECT SIZEZ FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setLockId()
  {
    return "UPDATE BODYPARTS SET LOCKID=? WHERE WURMID=?";
  }
  
  public String setPrice()
  {
    return "UPDATE BODYPARTS SET PRICE=? WHERE WURMID=?";
  }
  
  public String setAuxData()
  {
    return "UPDATE BODYPARTS SET AUXDATA=? WHERE WURMID=?";
  }
  
  public String setCreationState()
  {
    return "UPDATE BODYPARTS SET CREATIONSTATE=? WHERE WURMID=?";
  }
  
  public String setRealTemplate()
  {
    return "UPDATE BODYPARTS SET REALTEMPLATE=? WHERE WURMID=?";
  }
  
  public String setColor()
  {
    return "UPDATE BODYPARTS SET COLOR=?,COLOR2=? WHERE WURMID=?";
  }
  
  public String setEnchant()
  {
    return "UPDATE BODYPARTS SET ENCHANT=? WHERE WURMID=?";
  }
  
  public String setBanked()
  {
    return "UPDATE BODYPARTS SET BANKED=? WHERE WURMID=?";
  }
  
  public String getData()
  {
    return "select * from ITEMDATA where WURMID=?";
  }
  
  public String createData()
  {
    return "insert into ITEMDATA ( DATA1, DATA2, EXTRA1, EXTRA2, WURMID) values(?,?,?,?,?)";
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
    return "UPDATE BODYPARTS SET TEMPERATURE=? WHERE WURMID=?";
  }
  
  public String getTemperature()
  {
    return "SELECT TEMPERATURE FROM BODYPARTS WHERE WURMID=?";
  }
  
  public String setMaterial()
  {
    return "UPDATE BODYPARTS SET MATERIAL=? WHERE WURMID=?";
  }
  
  public String setWornAsArmour()
  {
    return "UPDATE BODYPARTS SET WORNARMOUR=? WHERE WURMID=?";
  }
  
  public String setFemale()
  {
    return "UPDATE BODYPARTS SET FEMALE=? WHERE WURMID=?";
  }
  
  public String setMailed()
  {
    return "UPDATE BODYPARTS SET MAILED=? WHERE WURMID=?";
  }
  
  public String setCreator()
  {
    return "UPDATE BODYPARTS SET CREATOR=? WHERE WURMID=?";
  }
  
  public String getZoneItems()
  {
    return "SELECT * FROM BODYPARTS WHERE OWNERID=-10";
  }
  
  public String getCreatureItems()
  {
    return "SELECT * FROM BODYPARTS WHERE OWNERID=?";
  }
  
  public String getPreloadedItems()
  {
    return "SELECT * FROM BODYPARTS WHERE TEMPLATEID=?";
  }
  
  public String getCreatureItemsNonTransferred()
  {
    return "SELECT WURMID FROM BODYPARTS WHERE OWNERID=? AND TRANSFERRED=0";
  }
  
  public String updateLastMaintainedBankItem()
  {
    return "UPDATE BODYPARTS SET LASTMAINTAINED=? WHERE BANKED=1";
  }
  
  public String getItemWeights()
  {
    return "SELECT WURMID, WEIGHT,SIZEX,SIZEY,SIZEZ, TEMPLATEID FROM BODYPARTS";
  }
  
  public String getOwnedItems()
  {
    return "SELECT OWNERID FROM BODYPARTS WHERE OWNERID>0 GROUP BY OWNERID";
  }
  
  public String deleteByOwnerId()
  {
    return "DELETE FROM BODYPARTS WHERE OWNERID=?";
  }
  
  public String deleteTransferedItem()
  {
    return "DELETE FROM BODYPARTS WHERE WURMID=? AND TRANSFERRED=0";
  }
  
  public String deleteItem()
  {
    return "delete from BODYPARTS where WURMID=?";
  }
  
  public String getRecycledItems()
  {
    return "SELECT * FROM BODYPARTS WHERE TEMPLATEID=? AND BANKED=1";
  }
  
  public String getItemsForZone()
  {
    return "Select WURMID from BODYPARTS where ZONEID=? AND BANKED=0";
  }
  
  public String setHidden()
  {
    return "UPDATE BODYPARTS SET HIDDEN=? WHERE WURMID=?";
  }
  
  public String setSettings()
  {
    return "UPDATE BODYPARTS SET SETTINGS=? WHERE WURMID=?";
  }
  
  public String setMailTimes()
  {
    return "UPDATE BODYPARTS SET MAILTIMES=? WHERE WURMID=?";
  }
  
  public String freeze()
  {
    return "INSERT INTO FROZENITEMS SELECT * FROM ITEMS WHERE WURMID=?";
  }
  
  public String thaw()
  {
    return "INSERT INTO ITEMS SELECT * FROM FROZENITEMS WHERE WURMID=?";
  }
  
  public static BodyDbStrings getInstance()
  {
    if (instance == null) {
      instance = new BodyDbStrings();
    }
    return instance;
  }
  
  public final String getDbStringsType()
  {
    return "BodyDbStrings";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\BodyDbStrings.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */