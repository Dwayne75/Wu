package com.wurmonline.server.items;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Server;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.spells.Spell;
import com.wurmonline.server.spells.SpellEffect;
import com.wurmonline.server.villages.DeadVillage;
import com.wurmonline.shared.util.MaterialUtilities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class FragmentUtilities
{
  private static final int DIFFRANGE_TRASH = 0;
  private static final int DIFFRANGE_0_15 = 1;
  private static final int DIFFRANGE_15_30 = 2;
  private static final int DIFFRANGE_30_40 = 3;
  private static final int DIFFRANGE_40_50 = 4;
  private static final int DIFFRANGE_50_60 = 5;
  private static final int DIFFRANGE_60_70 = 6;
  private static final int DIFFRANGE_70_80 = 7;
  private static final int DIFFRANGE_80_90 = 8;
  private static final int DIFFRANGE_90_100 = 9;
  private static int[] diffTrash = { 776, 786, 1122, 1121, 1123, 132, 38, 39, 43, 41, 40, 207, 42, 785, 785, 146, 688, 23, 454, 561, 551 };
  private static int[] diff0_15 = { 46, 47, 49, 220, 48, 453 };
  private static int[] diff15_30 = { 1011, 685, 687, 690, 45, 44, 223, 205, 221, 1411, 784, 778, 217, 218, 188, 451, 1408, 1407, 1416 };
  private static int[] diff30_40 = { 77, 813, 1161, 76, 78, 1020, 523, 127, 154, 389, 125, 126, 124, 123, 395, 270, 121, 269, 494, 452, 1406, 1421, 1418 };
  private static int[] diff40_50 = { 1022, 1172, 1169, 1165, 1252, 1323, 1324, 1405, 708, 88, 91, 89, 293, 295, 294, 148, 147, 149, 1417, 1420, 1419, 1430 };
  private static int[] diff50_60 = { 62, 20, 97, 388, 93, 8, 25, 7, 27, 24, 493, 394, 268, 267, 1325, 1330, 1415 };
  private static int[] diff60_70 = { 21, 80, 81, 87, 90, 3, 706, 290, 292, 291, 274, 279, 278, 275, 276, 277, 1328, 1327, 1329, 1326, 710 };
  private static int[] diff70_80 = { 976, 973, 978, 975, 974, 280, 284, 281, 282, 283, 83, 86, 287, 286 };
  private static int[] anniversaryGifts = { 791, 738, 967, 1306, 1321, 1100, 1297, 972, 1032, 844, 700, 1334, 997 };
  private static int[] justStatues = { 1408, 1407, 1416, 1406, 1421, 1418, 1323, 1324, 1405, 1417, 1420, 1419, 1325, 1330, 1415, 1328, 1327, 1329, 1326, 1430 };
  private static HashMap<Integer, ArrayList<Integer>> fragmentLists = new HashMap();
  static final byte CLASS_WEAPON = 1;
  static final byte CLASS_ARMOUR = 2;
  static final byte CLASS_TOOL = 3;
  static final byte CLASS_CONTAINER = 4;
  static final byte CLASS_VEHICLE = 5;
  static final byte CLASS_ALL = 6;
  
  static
  {
    for (int fragment : diffTrash) {
      addFragment(fragment, 0);
    }
    for (int fragment : diff0_15) {
      addFragment(fragment, 1);
    }
    for (int fragment : diff15_30) {
      addFragment(fragment, 2);
    }
    for (int fragment : diff30_40) {
      addFragment(fragment, 3);
    }
    for (int fragment : diff40_50) {
      addFragment(fragment, 4);
    }
    for (int fragment : diff50_60) {
      addFragment(fragment, 5);
    }
    for (int fragment : diff60_70) {
      addFragment(fragment, 6);
    }
    for (int fragment : diff70_80) {
      addFragment(fragment, 7);
    }
  }
  
  public static FragmentUtilities.Fragment getRandomFragmentForSkill(double skill, boolean trashPossible)
  {
    if (skill < 0.0D) {
      return null;
    }
    int maxRange = 1;
    if ((skill >= 15.0D) && (skill < 30.0D)) {
      maxRange = 2;
    } else if (skill >= 30.0D) {
      maxRange = (int)Math.min(9.0D, Math.floor(skill / 10.0D) - 1.0D);
    }
    int thisRange = Server.rand.nextInt(maxRange + 1);
    if ((trashPossible) && (Server.rand.nextInt(3) != 0)) {
      thisRange = Math.max(0, thisRange - 3);
    }
    boolean bumpMaterial = false;
    if (thisRange == 8)
    {
      thisRange = 5;
      bumpMaterial = true;
    }
    else if (thisRange == 9)
    {
      thisRange = 6;
      bumpMaterial = true;
    }
    int itemId = -1;
    byte materialId = -1;
    ArrayList<Integer> possibleItems = (ArrayList)fragmentLists.get(Integer.valueOf(thisRange));
    if (possibleItems != null) {
      itemId = ((Integer)possibleItems.get(Server.rand.nextInt(possibleItems.size()))).intValue();
    }
    if (itemId == -1) {
      return null;
    }
    ItemTemplate item = ItemTemplateFactory.getInstance().getTemplateOrNull(itemId);
    if (item == null) {
      return null;
    }
    materialId = item.getMaterial();
    if ((item.isMetal()) && (!item.isOre) && (!item.isMetalLump())) {
      materialId = 93;
    }
    if ((item.isMetal()) && (!MaterialUtilities.isMetal(materialId))) {
      materialId = 93;
    } else if ((item.isWood()) && (!MaterialUtilities.isWood(materialId))) {
      materialId = 14;
    }
    if ((bumpMaterial) && (item.isMetal()) && (materialId == 93)) {
      materialId = 94;
    }
    return new FragmentUtilities.Fragment(itemId, materialId);
  }
  
  public static Item createVillageCache(Player performer, Item archReport, DeadVillage vill, Skill archSkill)
  {
    if ((!archReport.getAuxBit(0)) || (!archReport.getAuxBit(1)) || (!archReport.getAuxBit(2)) || (!archReport.getAuxBit(3))) {
      return null;
    }
    try
    {
      Item cache = ItemFactory.createItem(1422, archReport.getCurrentQualityLevel(), vill.getFounderName());
      cache.setName(vill.getDeedName());
      
      int statueCount = (int)Math.min(6.0D, (archSkill.getKnowledge(0.0D) + archReport.getCurrentQualityLevel()) / 28.0D);
      int goodCount = (int)Math.min(6.0D, (archSkill.getKnowledge(0.0D) + archReport.getCurrentQualityLevel()) / 28.0D);
      float dvModifier = Math.min(2.0F, 0.25F + vill.getTimeSinceDisband() / 120.0F + vill.getTotalAge() / 60.0F);
      int totalGiven = 0;
      for (int i = 0; i < statueCount * dvModifier; i++)
      {
        double power = archSkill.skillCheck(i * 5, archReport, 0.0D, false, 1.0F);
        Item statueFrag = ItemFactory.createItem(1307, (float)Math.min(100.0D, Math.max(1.0D, power)), vill.getFounderName());
        statueFrag.setRealTemplate(justStatues[Server.rand.nextInt(justStatues.length)]);
        statueFrag.setLastOwnerId(performer.getWurmId());
        if (statueFrag.isMetal()) {
          if (Server.rand.nextInt(500) == 0) {
            statueFrag.setMaterial((byte)95);
          } else if (Server.rand.nextInt(50) == 0) {
            statueFrag.setMaterial((byte)94);
          } else {
            statueFrag.setMaterial((byte)93);
          }
        }
        cache.insertItem(statueFrag, true);
        totalGiven++;
      }
      if (archSkill.getKnowledge(0.0D) > 50.0D) {
        for (int i = 0; i < goodCount * dvModifier; i++)
        {
          double power = archSkill.skillCheck(i * 10, archReport, 0.0D, false, 1.0F);
          Item randomFrag = ItemFactory.createItem(1307, (float)Math.min(100.0D, Math.max(1.0D, power)), vill.getFounderName());
          int[] list = diff50_60;
          if (power > 50.0D) {
            list = diff70_80;
          } else if (power > 30.0D) {
            list = diff60_70;
          }
          randomFrag.setRealTemplate(list[Server.rand.nextInt(list.length)]);
          randomFrag.setLastOwnerId(performer.getWurmId());
          randomFrag.setMaterial(randomFrag.getRealTemplate().getMaterial());
          if ((randomFrag.isMetal()) && (!randomFrag.getTemplate().isOre) && (!randomFrag.getTemplate().isMetalLump())) {
            if (Server.rand.nextInt(500) == 0) {
              randomFrag.setMaterial((byte)95);
            } else if (Server.rand.nextInt(50) == 0) {
              randomFrag.setMaterial((byte)94);
            } else {
              randomFrag.setMaterial((byte)93);
            }
          }
          cache.insertItem(randomFrag, true);
          totalGiven++;
        }
      }
      for (int i = totalGiven; i < 10; i++)
      {
        double power = archSkill.skillCheck(i * 5, archReport, 0.0D, false, 1.0F);
        Item randomFrag = ItemFactory.createItem(1307, (float)Math.min(100.0D, Math.max(1.0D, power)), vill.getFounderName());
        int[] list = diff15_30;
        if (power > 50.0D) {
          list = diff40_50;
        } else if (power > 20.0D) {
          list = diff30_40;
        }
        randomFrag.setRealTemplate(list[Server.rand.nextInt(list.length)]);
        randomFrag.setLastOwnerId(performer.getWurmId());
        randomFrag.setMaterial(randomFrag.getRealTemplate().getMaterial());
        if ((randomFrag.isMetal()) && (!randomFrag.getTemplate().isOre) && (!randomFrag.getTemplate().isMetalLump())) {
          if (Server.rand.nextInt(500) == 0) {
            randomFrag.setMaterial((byte)95);
          } else if (Server.rand.nextInt(50) == 0) {
            randomFrag.setMaterial((byte)94);
          } else {
            randomFrag.setMaterial((byte)93);
          }
        }
        cache.insertItem(randomFrag, true);
      }
      Item tokenMini = ItemFactory.createItem(1423, (float)((archSkill.getKnowledge(0.0D) + archReport.getCurrentQualityLevel()) / 2.0D), vill
        .getFounderName());
      double tokenPower = archSkill.skillCheck(50.0D, archReport, 0.0D, false, 1.0F);
      if (tokenPower > 80.0D) {
        tokenMini.setMaterial(getMetalMoonMaterial(100));
      } else if (tokenPower > 60.0D) {
        tokenMini.setMaterial(getMetalAlloyMaterial(100));
      } else if (tokenPower > 30.0D) {
        tokenMini.setMaterial(getMetalBaseMaterial((int)tokenPower));
      }
      tokenMini.setName(vill.getDeedName());
      tokenMini.setData(vill.getDeedId());
      tokenMini.setAuxData((byte)((archReport.getAuxData() & 0xFF) >>> 4));
      tokenMini.setAuxBit(7, true);
      tokenMini.setLastOwnerId(performer.getWurmId());
      
      cache.insertItem(tokenMini, true);
      
      return cache;
    }
    catch (FailedException|NoSuchTemplateException localFailedException) {}
    return null;
  }
  
  public static int getDifficultyForItem(int itemId, int materialId)
  {
    for (int fragment : diff0_15) {
      if (fragment == itemId) {
        return 5;
      }
    }
    for (int fragment : diff15_30) {
      if (fragment == itemId) {
        return 15;
      }
    }
    for (int fragment : diff30_40) {
      if (fragment == itemId) {
        return 25;
      }
    }
    for (int fragment : diff40_50) {
      if (fragment == itemId) {
        return 35;
      }
    }
    for (int fragment : diff50_60) {
      if (fragment == itemId)
      {
        if ((materialId == 94) || (materialId == 9)) {
          return 75;
        }
        return 45;
      }
    }
    for (int fragment : diff60_70) {
      if (fragment == itemId)
      {
        if ((materialId == 94) || (materialId == 9)) {
          return 85;
        }
        return 55;
      }
    }
    for (int fragment : diff70_80) {
      if (fragment == itemId) {
        return 65;
      }
    }
    return 10;
  }
  
  public static byte getMetalBaseMaterial(int identifyLevel)
  {
    switch (Server.rand.nextInt(Math.max(6, 75 - identifyLevel)))
    {
    case 0: 
      return 7;
    case 1: 
      return 8;
    case 2: 
      return 10;
    case 3: 
      return 13;
    case 4: 
      return 34;
    case 5: 
      return 12;
    }
    return 11;
  }
  
  public static byte getMetalAlloyMaterial(int identifyLevel)
  {
    switch (Server.rand.nextInt(Math.max(4, 75 - identifyLevel)))
    {
    case 0: 
      return 30;
    case 1: 
      return 31;
    case 2: 
      return 96;
    }
    return 9;
  }
  
  public static byte getMetalMoonMaterial(int identifyLevel)
  {
    switch (Server.rand.nextInt(Math.max(10, 90 - identifyLevel)))
    {
    case 0: 
      return 67;
    case 1: 
    case 2: 
      return 56;
    }
    return 57;
  }
  
  public static byte getRandomWoodMaterial(int identifyLevel)
  {
    switch (Server.rand.nextInt(Math.max(25, 75 - identifyLevel)))
    {
    case 0: 
      return 42;
    case 1: 
      return 14;
    case 2: 
      return 91;
    case 3: 
      return 50;
    case 4: 
      return 39;
    case 5: 
      return 45;
    case 6: 
      return 63;
    case 7: 
      return 65;
    case 8: 
      return 49;
    case 9: 
      return 71;
    case 10: 
      return 46;
    case 11: 
      return 43;
    case 12: 
      return 66;
    case 13: 
      return 92;
    case 14: 
      return 41;
    case 15: 
      return 38;
    case 16: 
      return 51;
    case 17: 
      return 44;
    case 18: 
      return 88;
    case 19: 
      return 37;
    case 20: 
      return 90;
    case 21: 
      return 47;
    case 22: 
      return 48;
    case 23: 
      return 64;
    case 24: 
      return 40;
    }
    return 14;
  }
  
  public static int getRandomAnniversaryGift()
  {
    return anniversaryGifts[Server.rand.nextInt(anniversaryGifts.length)];
  }
  
  public static int getRandomEnchantNumber(int weight)
  {
    if (weight < 50) {
      return 0;
    }
    int[] vals = new int[8];
    for (int i = 0; i < 8; i++) {
      vals[i] = Server.rand.nextInt(1000);
    }
    int closest = vals[0];
    int weightedVal = (weight - 50) * 20;
    for (int i = 0; i < 8; i++) {
      if (Math.abs(weightedVal - vals[i]) < Math.abs(weightedVal - closest)) {
        closest = vals[i];
      }
    }
    return Math.min(5, Math.max(1, Math.round(closest / 200.0F)));
  }
  
  public static void addRandomEnchantment(Item toEnchant, int enchLevel, float power)
  {
    byte itemClass = 6;
    if (toEnchant.isWeapon()) {
      itemClass = 1;
    } else if (toEnchant.isArmour()) {
      itemClass = 2;
    } else if (toEnchant.isTool()) {
      itemClass = 3;
    } else if (toEnchant.isHollow()) {
      itemClass = 4;
    } else if (toEnchant.isVehicle()) {
      itemClass = 5;
    }
    FragmentUtilities.FragmentEnchantment f = FragmentUtilities.FragmentEnchantment.getRandomEnchantment(itemClass, enchLevel);
    if (f == null) {
      return;
    }
    byte enchantment = f.getEnchantment();
    if (enchantment <= -51)
    {
      if (RuneUtilities.canApplyRuneTo(enchantment, toEnchant)) {}
    }
    else
    {
      if ((toEnchant.isWeapon()) && (!Spell.mayWeaponBeEnchanted(toEnchant, null, enchantment))) {
        return;
      }
      if ((toEnchant.isArmour()) && (!Spell.mayArmourBeEnchanted(toEnchant, null, enchantment))) {
        return;
      }
      if ((toEnchant.isTool()) && (!Spell.mayReceiveSkillgainBuff(toEnchant, null, enchantment))) {
        return;
      }
      if (!Spell.mayBeEnchanted(toEnchant)) {
        return;
      }
    }
    ItemSpellEffects effs = toEnchant.getSpellEffects();
    if (effs == null) {
      effs = new ItemSpellEffects(toEnchant.getWurmId());
    }
    SpellEffect e = effs.getSpellEffect(enchantment);
    if (e == null)
    {
      e = new SpellEffect(toEnchant.getWurmId(), enchantment, power, 20000000);
      effs.addSpellEffect(e);
    }
    else
    {
      if (power > e.getPower() + power / 5.0F) {
        e.setPower(power);
      } else {
        e.setPower(e.getPower() + power / 5.0F);
      }
      if ((enchantment != 45) && (e.getPower() > 104.0F)) {
        e.setPower(104.0F);
      }
    }
  }
  
  private static void addFragment(int itemId, int range)
  {
    ArrayList<Integer> fragments = (ArrayList)fragmentLists.get(Integer.valueOf(range));
    if (fragments == null)
    {
      fragments = new ArrayList();
      fragmentLists.put(Integer.valueOf(range), fragments);
    }
    fragments.add(Integer.valueOf(itemId));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\FragmentUtilities.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */