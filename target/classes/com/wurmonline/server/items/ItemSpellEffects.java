package com.wurmonline.server.items;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.utils.DbUtilities;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ItemSpellEffects
{
  private static final String GET_ALL_ITEMSPELLEFFECTS = "SELECT * FROM SPELLEFFECTS";
  private final Map<Byte, SpellEffect> spellEffects;
  private static final Logger logger = Logger.getLogger(ItemSpellEffects.class.getName());
  private static final Map<Long, ItemSpellEffects> itemSpellEffects = new HashMap();
  
  public ItemSpellEffects(long _itemId)
  {
    this.spellEffects = new HashMap();
    itemSpellEffects.put(new Long(_itemId), this);
  }
  
  public void addSpellEffect(SpellEffect effect)
  {
    SpellEffect old = getSpellEffect(effect.type);
    if ((old != null) && (old.power > effect.power))
    {
      effect.delete();
      return;
    }
    if (old != null) {
      old.delete();
    }
    this.spellEffects.put(Byte.valueOf(effect.type), effect);
  }
  
  public byte getRandomRuneEffect()
  {
    for (int i = -128; i <= -51; i++) {
      if (this.spellEffects.containsKey(Byte.valueOf((byte)i))) {
        return (byte)i;
      }
    }
    return -10;
  }
  
  public float getRuneEffect(RuneUtilities.ModifierEffect effect)
  {
    float toReturn = 1.0F;
    for (int i = -128; i <= -51; i++) {
      if (this.spellEffects.containsKey(Byte.valueOf((byte)i))) {
        toReturn += RuneUtilities.getModifier((byte)i, effect);
      }
    }
    return toReturn;
  }
  
  public int getNumberOfRuneEffects()
  {
    int toReturn = 0;
    for (int i = -128; i <= -51; i++) {
      if (this.spellEffects.containsKey(Byte.valueOf((byte)i))) {
        toReturn++;
      }
    }
    return toReturn;
  }
  
  public SpellEffect getSpellEffect(byte type)
  {
    if (this.spellEffects.containsKey(Byte.valueOf(type))) {
      return (SpellEffect)this.spellEffects.get(Byte.valueOf(type));
    }
    return null;
  }
  
  public SpellEffect[] getEffects()
  {
    return (SpellEffect[])this.spellEffects.values().toArray(new SpellEffect[this.spellEffects.size()]);
  }
  
  public SpellEffect removeSpellEffect(byte number)
  {
    SpellEffect old = getSpellEffect(number);
    if (old != null)
    {
      old.delete();
      this.spellEffects.remove(Byte.valueOf(number));
    }
    return old;
  }
  
  public void destroy()
  {
    SpellEffect[] effects = getEffects();
    for (int x = 0; x < effects.length; x++) {
      effects[x].delete();
    }
    this.spellEffects.clear();
  }
  
  public void clear()
  {
    this.spellEffects.clear();
  }
  
  public static ItemSpellEffects getSpellEffects(long itemid)
  {
    return (ItemSpellEffects)itemSpellEffects.get(new Long(itemid));
  }
  
  public static void loadSpellEffectsForItems()
  {
    long start = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getItemDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM SPELLEFFECTS");
      rs = ps.executeQuery();
      int numEffects = 0;
      while (rs.next())
      {
        SpellEffect sp = new SpellEffect(rs.getLong("WURMID"), rs.getLong("ITEMID"), rs.getByte("TYPE"), rs.getFloat("POWER"), rs.getInt("TIMELEFT"), (byte)9, (byte)0);
        
        Long id = new Long(sp.owner);
        ItemSpellEffects eff = (ItemSpellEffects)itemSpellEffects.get(id);
        if (eff == null) {
          eff = new ItemSpellEffects(sp.owner);
        }
        eff.addSpellEffect(sp);
        numEffects++;
      }
      logger.log(Level.INFO, "Loaded " + numEffects + " Spell Effects For Items, that took " + 
        (float)(System.nanoTime() - start) / 1000000.0F + " ms");
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\ItemSpellEffects.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */