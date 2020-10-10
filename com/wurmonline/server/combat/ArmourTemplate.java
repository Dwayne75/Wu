package com.wurmonline.server.combat;

import com.wurmonline.server.Features.Feature;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.NoArmourException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSpaceException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArmourTemplate
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(ArmourTemplate.class.getName());
  public static HashMap<Integer, ArmourTemplate> armourTemplates = new HashMap();
  public static ArrayList<ProtectionSlot> protectionSlots = new ArrayList();
  protected int templateId;
  protected ArmourType armourType;
  protected float moveModifier;
  
  public static ArmourTemplate getArmourTemplate(int templateId)
  {
    if (armourTemplates.containsKey(Integer.valueOf(templateId))) {
      return (ArmourTemplate)armourTemplates.get(Integer.valueOf(templateId));
    }
    logger.warning(String.format("Item template id %s has no ArmourTemplate, but one was requested.", new Object[] { Integer.valueOf(templateId) }));
    return null;
  }
  
  public static ArmourTemplate getArmourTemplate(Item item)
  {
    return getArmourTemplate(item.getTemplateId());
  }
  
  public static void setArmourMoveModifier(int templateId, float value)
  {
    ArmourTemplate template = getArmourTemplate(templateId);
    if (template != null) {
      template.setMoveModifier(value);
    } else {
      logger.warning(String.format("Item template id %s has no ArmourTemplate, but one was requested.", new Object[] { Integer.valueOf(templateId) }));
    }
  }
  
  public static class ProtectionSlot
  {
    byte armourPosition;
    ArrayList<Byte> bodySlots = new ArrayList();
    
    ProtectionSlot(byte armourPosition, byte[] bodyPositions)
    {
      this.armourPosition = armourPosition;
      for (byte bodyPos : bodyPositions) {
        this.bodySlots.add(Byte.valueOf(bodyPos));
      }
      ArmourTemplate.protectionSlots.add(this);
    }
  }
  
  public static void initializeProtectionSlots()
  {
    new ProtectionSlot((byte)1, new byte[] { 17 });
    new ProtectionSlot((byte)29, new byte[] { 18, 19, 20 });
    new ProtectionSlot((byte)2, new byte[] { 21, 27, 26, 32, 23, 24, 25, 22 });
    
    new ProtectionSlot((byte)3, new byte[] { 5, 9 });
    new ProtectionSlot((byte)4, new byte[] { 6, 10 });
    new ProtectionSlot((byte)34, new byte[] { 7, 11, 8, 12 });
    
    new ProtectionSlot((byte)15, new byte[0]);
    new ProtectionSlot((byte)16, new byte[0]);
  }
  
  public static class ArmourType
  {
    protected HashMap<Byte, Float> armourEffectiveness = new HashMap();
    protected HashMap<Byte, Float> glanceRates = new HashMap();
    protected String name;
    protected float baseDamageReduction;
    protected float limitFactor;
    protected float arrowGlance;
    protected float creatureGlanceRate = 0.0F;
    protected byte creatureGlanceBonusWoundType = -1;
    protected float creatureBonusWoundIncrease = 0.0F;
    
    public ArmourType(String name, float baseDamageReduction, float limitFactor, float arrowGlance)
    {
      this.name = name;
      this.baseDamageReduction = baseDamageReduction;
      this.limitFactor = limitFactor;
      this.arrowGlance = arrowGlance;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public float getBaseDR()
    {
      return this.baseDamageReduction;
    }
    
    public void setBaseDR(float newBaseDR)
    {
      this.baseDamageReduction = newBaseDR;
    }
    
    public float getLimitFactor()
    {
      return this.limitFactor;
    }
    
    public float getArrowGlance()
    {
      return this.arrowGlance;
    }
    
    public float getEffectiveness(byte woundType)
    {
      if (this.armourEffectiveness.containsKey(Byte.valueOf(woundType))) {
        return ((Float)this.armourEffectiveness.get(Byte.valueOf(woundType))).floatValue();
      }
      ArmourTemplate.logger.warning(String.format("No armour effectiveness set for wound type %s against %s.", new Object[] { Byte.valueOf(woundType), getName() }));
      
      return 1.0F;
    }
    
    public void setEffectiveness(byte woundType, float effectiveness)
    {
      this.armourEffectiveness.put(Byte.valueOf(woundType), Float.valueOf(effectiveness));
    }
    
    public float getGlanceRate(byte woundType, byte armourMaterial)
    {
      float materialMod = ArmourTemplate.getArmourMatGlanceBonus(armourMaterial);
      if (this.glanceRates.containsKey(Byte.valueOf(woundType))) {
        return ((Float)this.glanceRates.get(Byte.valueOf(woundType))).floatValue() * materialMod;
      }
      ArmourTemplate.logger.warning(String.format("No glance rate set for wound type %s against %s.", new Object[] { Byte.valueOf(woundType), getName() }));
      
      return 0.0F;
    }
    
    public void setGlanceRate(byte woundType, float glanceRate)
    {
      this.glanceRates.put(Byte.valueOf(woundType), Float.valueOf(glanceRate));
    }
    
    public float getCreatureGlance(byte woundType, @Nonnull Item armour)
    {
      float toReturn = this.creatureGlanceRate;
      if (woundType == this.creatureGlanceBonusWoundType) {
        toReturn += this.creatureBonusWoundIncrease;
      }
      toReturn = 0.05F + toReturn * (float)Server.getBuffedQualityEffect(armour.getCurrentQualityLevel() / 100.0F);
      return toReturn;
    }
    
    public void setCreatureGlance(float baseGlance, byte bonusWoundType, float woundBonus)
    {
      this.creatureGlanceRate = baseGlance;
      this.creatureGlanceBonusWoundType = bonusWoundType;
      this.creatureBonusWoundIncrease = woundBonus;
    }
  }
  
  public ArmourTemplate(int templateId, ArmourType armourType, float moveModifier)
  {
    this.templateId = templateId;
    this.armourType = armourType;
    this.moveModifier = moveModifier;
    armourTemplates.put(Integer.valueOf(templateId), this);
  }
  
  public float getBaseDR()
  {
    return this.armourType.getBaseDR();
  }
  
  public float getEffectiveness(byte woundType)
  {
    return this.armourType.getEffectiveness(woundType);
  }
  
  public float getMoveModifier()
  {
    return this.moveModifier;
  }
  
  public ArmourType getArmourType()
  {
    return this.armourType;
  }
  
  public float getLimitFactor()
  {
    return this.armourType.getLimitFactor();
  }
  
  public void setMoveModifier(float newMoveModifier)
  {
    this.moveModifier = newMoveModifier;
  }
  
  public static float calculateDR(@Nonnull Item armour, byte woundType)
  {
    ArmourTemplate armourTemplate = getArmourTemplate(armour);
    if (armourTemplate != null)
    {
      float toReturn = armourTemplate.getBaseDR();
      toReturn += getArmourMatBonus(armour.getMaterial());
      toReturn *= armourTemplate.getEffectiveness(woundType);
      toReturn *= (1.0F + getRarityArmourBonus(armour.getRarity()));
      
      toReturn = 0.05F + (float)(toReturn * Server.getBuffedQualityEffect(armour.getCurrentQualityLevel() / 100.0F));
      return 1.0F - toReturn;
    }
    return 1.0F;
  }
  
  public static float calculateGlanceRate(@Nullable ArmourType armourType, @Nullable Item armour, byte woundType, float armourRating)
  {
    float toReturn = 0.0F;
    
    float baseArmour = 0.0F;
    if (armour != null)
    {
      armourType = armour.getArmourType();
    }
    else if (armourType != null)
    {
      if (woundType == 5) {
        return 0.0F;
      }
      return (1.0F - armourRating) / 2.0F;
    }
    if (armourType != null)
    {
      baseArmour = 0.05F;
      toReturn = armourType.getGlanceRate(woundType, armour.getMaterial());
    }
    if (armour != null)
    {
      toReturn += getRarityArmourBonus(armour.getRarity());
      toReturn = baseArmour + toReturn * (float)Server.getBuffedQualityEffect(armour.getCurrentQualityLevel() / 100.0F);
    }
    else
    {
      toReturn *= 0.5F;
    }
    return toReturn;
  }
  
  public static float calculateCreatureGlanceRate(byte woundType, @Nonnull Item armour)
  {
    ArmourType armourType = armour.getArmourType();
    if (armourType != null) {
      return armourType.getCreatureGlance(woundType, armour);
    }
    return 0.0F;
  }
  
  public static float calculateArrowGlance(@Nonnull Item armour, Item arrow)
  {
    ArmourType armourType = armour.getArmourType();
    if (armourType != null)
    {
      float toReturn = armourType.getArrowGlance();
      if (arrow.getTemplateId() == 454) {
        toReturn += 0.1F;
      } else if (arrow.getTemplateId() == 456) {
        toReturn -= 0.05F;
      }
      toReturn *= (1.0F + getRarityArmourBonus(armour.getRarity()));
      toReturn = (float)(toReturn + toReturn * Server.getBuffedQualityEffect(armour.getCurrentQualityLevel() / 100.0F));
      return toReturn;
    }
    return 0.0F;
  }
  
  public static float getRarityArmourBonus(byte rarity)
  {
    return rarity * 0.03F;
  }
  
  public static float getArmourMatBonus(byte armourMaterial)
  {
    if ((Servers.localServer.isChallengeOrEpicServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      if (armourMaterial == 9) {
        return 0.025F;
      }
      if ((armourMaterial == 57) || (armourMaterial == 56) || (armourMaterial == 67)) {
        return 0.05F;
      }
    }
    else
    {
      if (armourMaterial == 9) {
        return 0.02F;
      }
      if ((armourMaterial == 57) || (armourMaterial == 67)) {
        return 0.1F;
      }
      if (armourMaterial == 56) {
        return 0.05F;
      }
    }
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (armourMaterial)
      {
      case 56: 
        return 0.05F;
      case 30: 
        return 0.01F;
      case 31: 
        return 0.01F;
      case 10: 
        return -0.01F;
      case 57: 
        return 0.05F;
      case 7: 
        return -0.01F;
      case 12: 
        return -0.025F;
      case 67: 
        return 0.05F;
      case 8: 
        return -0.0075F;
      case 9: 
        return 0.025F;
      case 34: 
        return -0.0175F;
      case 13: 
        return -0.02F;
      case 96: 
        return 0.005F;
      }
    }
    return 0.0F;
  }
  
  public static float getArmourMatGlanceBonus(byte armourMaterial)
  {
    float materialMod = 1.0F;
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (armourMaterial)
      {
      case 10: 
        materialMod = 0.98F;
        break;
      case 7: 
        materialMod = 1.025F;
        break;
      case 8: 
        materialMod = 1.01F;
        break;
      case 96: 
        materialMod = 1.02F;
      }
    }
    return materialMod;
  }
  
  public static float getArmourDamageModFor(Item armour, byte woundType)
  {
    float toReturn = 1.0F;
    ArmourType armourType = armour.getArmourType();
    if (woundType == 0)
    {
      if ((armourType == ARMOUR_TYPE_CLOTH) || (armourType == ARMOUR_TYPE_PLATE) || (armourType == ARMOUR_TYPE_SCALE_DRAGON)) {
        toReturn = 4.0F;
      }
    }
    else if (woundType == 2)
    {
      if (armourType == ARMOUR_TYPE_PLATE) {
        toReturn = 4.0F;
      } else if ((armourType == ARMOUR_TYPE_CLOTH) || (armourType == ARMOUR_TYPE_CHAIN)) {
        toReturn = 2.0F;
      }
    }
    else if (woundType == 1)
    {
      if ((armourType == ARMOUR_TYPE_CLOTH) || (armourType == ARMOUR_TYPE_LEATHER) || (armourType == ARMOUR_TYPE_LEATHER_DRAGON) || (armourType == ARMOUR_TYPE_PLATE)) {
        toReturn = 4.0F;
      } else if ((armourType == ARMOUR_TYPE_STUDDED) || (armourType == ARMOUR_TYPE_CHAIN)) {
        toReturn = 2.0F;
      }
    }
    else if (woundType == 3) {
      toReturn = 4.0F;
    } else if (woundType == 4) {
      if (armourType == ARMOUR_TYPE_CLOTH) {
        toReturn = 4.0F;
      } else if ((armourType == ARMOUR_TYPE_LEATHER) || (armourType == ARMOUR_TYPE_LEATHER_DRAGON)) {
        toReturn = 0.5F;
      }
    }
    return toReturn;
  }
  
  public static float getMaterialMovementModifier(byte armourMaterial)
  {
    if (Features.Feature.METALLIC_ITEMS.isEnabled()) {
      switch (armourMaterial)
      {
      case 56: 
        return 0.95F;
      case 30: 
        return 1.0F;
      case 31: 
        return 1.0F;
      case 10: 
        return 0.99F;
      case 57: 
        return 0.9F;
      case 7: 
        return 1.05F;
      case 11: 
        return 1.0F;
      case 12: 
        return 1.025F;
      case 67: 
        return 0.9F;
      case 8: 
        return 1.02F;
      case 9: 
        return 1.0F;
      case 34: 
        return 0.98F;
      case 13: 
        return 0.975F;
      case 96: 
        return 1.01F;
      }
    }
    return 1.0F;
  }
  
  public static byte getArmourPosition(byte bodyPosition)
  {
    for (ProtectionSlot slot : protectionSlots) {
      if (slot.bodySlots.contains(Byte.valueOf(bodyPosition))) {
        return slot.armourPosition;
      }
    }
    return bodyPosition;
  }
  
  public static float getArmourModForLocation(Creature creature, byte location, byte woundType)
    throws NoArmourException
  {
    byte bodyPosition = getArmourPosition(location);
    try
    {
      Item armour = creature.getArmour(bodyPosition);
      return calculateDR(armour, woundType);
    }
    catch (NoSpaceException e)
    {
      logger.warning(creature.getName() + " no armour space on loc " + location);
    }
    catch (NoArmourException e)
    {
      throw new NoArmourException(String.format("Armour not found for %s at location %s.", new Object[] { creature.getName(), Byte.valueOf(location) }));
    }
    return 1.0F;
  }
  
  public static ArmourType ARMOUR_TYPE_NONE = new ArmourType("none", 0.0F, 0.3F, 0.0F);
  public static ArmourType ARMOUR_TYPE_LEATHER = new ArmourType("leather", 0.45F, 0.3F, 0.2F);
  public static ArmourType ARMOUR_TYPE_STUDDED = new ArmourType("studded", 0.5F, 0.0F, 0.2F);
  public static ArmourType ARMOUR_TYPE_CHAIN = new ArmourType("chain", 0.55F, -0.15F, 0.25F);
  public static ArmourType ARMOUR_TYPE_PLATE = new ArmourType("plate", 0.63F, -0.3F, 0.25F);
  public static ArmourType ARMOUR_TYPE_RING = new ArmourType("ring", 0.5F, 0.0F, 0.35F);
  public static ArmourType ARMOUR_TYPE_CLOTH = new ArmourType("cloth", 0.35F, 0.3F, 0.2F);
  public static ArmourType ARMOUR_TYPE_SCALE = new ArmourType("scale", 0.45F, 0.0F, 0.3F);
  public static ArmourType ARMOUR_TYPE_SPLINT = new ArmourType("splint", 0.55F, 0.0F, 0.3F);
  public static ArmourType ARMOUR_TYPE_LEATHER_DRAGON = new ArmourType("drake", 0.65F, -0.3F, 0.25F);
  public static ArmourType ARMOUR_TYPE_SCALE_DRAGON = new ArmourType("dragonscale", 0.7F, -0.3F, 0.35F);
  
  public static void initializeArmourTemplates()
  {
    new ArmourTemplate(116, ARMOUR_TYPE_STUDDED, 0.01F);
    new ArmourTemplate(117, ARMOUR_TYPE_STUDDED, 0.005F);
    new ArmourTemplate(119, ARMOUR_TYPE_STUDDED, 0.005F);
    new ArmourTemplate(118, ARMOUR_TYPE_STUDDED, 0.05F);
    new ArmourTemplate(120, ARMOUR_TYPE_STUDDED, 0.05F);
    new ArmourTemplate(115, ARMOUR_TYPE_STUDDED, 0.005F);
    
    new ArmourTemplate(959, ARMOUR_TYPE_STUDDED, 0.005F);
    new ArmourTemplate(1014, ARMOUR_TYPE_STUDDED, 0.005F);
    new ArmourTemplate(1015, ARMOUR_TYPE_STUDDED, 0.005F);
    
    new ArmourTemplate(105, ARMOUR_TYPE_LEATHER, 0.007F);
    new ArmourTemplate(107, ARMOUR_TYPE_LEATHER, 0.003F);
    new ArmourTemplate(103, ARMOUR_TYPE_LEATHER, 0.003F);
    new ArmourTemplate(108, ARMOUR_TYPE_LEATHER, 0.04F);
    new ArmourTemplate(104, ARMOUR_TYPE_LEATHER, 0.04F);
    new ArmourTemplate(106, ARMOUR_TYPE_LEATHER, 0.003F);
    
    new ArmourTemplate(702, ARMOUR_TYPE_LEATHER, 0.13F);
    
    new ArmourTemplate(274, ARMOUR_TYPE_CHAIN, 0.015F);
    new ArmourTemplate(279, ARMOUR_TYPE_CHAIN, 0.01F);
    new ArmourTemplate(275, ARMOUR_TYPE_CHAIN, 0.07F);
    new ArmourTemplate(276, ARMOUR_TYPE_CHAIN, 0.07F);
    new ArmourTemplate(277, ARMOUR_TYPE_CHAIN, 0.008F);
    new ArmourTemplate(278, ARMOUR_TYPE_CHAIN, 0.01F);
    
    new ArmourTemplate(703, ARMOUR_TYPE_CHAIN, 0.17F);
    
    new ArmourTemplate(474, ARMOUR_TYPE_SCALE_DRAGON, 0.001F);
    new ArmourTemplate(475, ARMOUR_TYPE_SCALE_DRAGON, 0.02F);
    new ArmourTemplate(476, ARMOUR_TYPE_SCALE_DRAGON, 0.02F);
    new ArmourTemplate(477, ARMOUR_TYPE_SCALE_DRAGON, 0.005F);
    new ArmourTemplate(478, ARMOUR_TYPE_SCALE_DRAGON, 0.001F);
    
    new ArmourTemplate(280, ARMOUR_TYPE_PLATE, 0.02F);
    new ArmourTemplate(284, ARMOUR_TYPE_PLATE, 0.02F);
    new ArmourTemplate(281, ARMOUR_TYPE_PLATE, 0.09F);
    new ArmourTemplate(282, ARMOUR_TYPE_PLATE, 0.09F);
    new ArmourTemplate(283, ARMOUR_TYPE_PLATE, 0.01F);
    new ArmourTemplate(273, ARMOUR_TYPE_PLATE, 0.02F);
    
    new ArmourTemplate(285, ARMOUR_TYPE_PLATE, 0.025F);
    new ArmourTemplate(286, ARMOUR_TYPE_PLATE, 0.025F);
    new ArmourTemplate(287, ARMOUR_TYPE_PLATE, 0.025F);
    if ((Servers.localServer.isChallengeOrEpicServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      setArmourMoveModifier(280, 0.0175F);
      setArmourMoveModifier(284, 0.015F);
      setArmourMoveModifier(281, 0.08F);
      setArmourMoveModifier(282, 0.08F);
      setArmourMoveModifier(273, 0.015F);
    }
    new ArmourTemplate(109, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(113, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1107, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1070, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1071, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1072, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1073, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(112, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(110, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1067, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1068, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1069, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(114, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(111, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1075, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1105, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1074, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1106, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(779, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1425, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1427, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(1426, ARMOUR_TYPE_CLOTH, 0.0F);
    
    new ArmourTemplate(704, ARMOUR_TYPE_CLOTH, 0.1F);
    new ArmourTemplate(791, ARMOUR_TYPE_CLOTH, 0.0F);
    
    new ArmourTemplate(943, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(944, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(947, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(945, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(946, ARMOUR_TYPE_CLOTH, 0.0F);
    
    new ArmourTemplate(948, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(949, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(950, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(951, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(953, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(952, ARMOUR_TYPE_CLOTH, 0.0F);
    
    new ArmourTemplate(954, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(957, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(956, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(955, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(958, ARMOUR_TYPE_CLOTH, 0.0F);
    
    new ArmourTemplate(961, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(964, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(963, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(962, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(965, ARMOUR_TYPE_CLOTH, 0.0F);
    new ArmourTemplate(966, ARMOUR_TYPE_CLOTH, 0.0F);
    
    new ArmourTemplate(960, ARMOUR_TYPE_CLOTH, 0.0F);
    
    new ArmourTemplate(979, ARMOUR_TYPE_PLATE, 0.025F);
    new ArmourTemplate(980, ARMOUR_TYPE_PLATE, 0.025F);
    new ArmourTemplate(998, ARMOUR_TYPE_PLATE, 0.025F);
    
    new ArmourTemplate(472, ARMOUR_TYPE_LEATHER_DRAGON, 0.001F);
    new ArmourTemplate(471, ARMOUR_TYPE_LEATHER_DRAGON, 0.04F);
    new ArmourTemplate(473, ARMOUR_TYPE_LEATHER_DRAGON, 0.04F);
    new ArmourTemplate(470, ARMOUR_TYPE_LEATHER_DRAGON, 0.004F);
    new ArmourTemplate(469, ARMOUR_TYPE_LEATHER_DRAGON, 0.004F);
    new ArmourTemplate(468, ARMOUR_TYPE_LEATHER_DRAGON, 0.001F);
    
    new ArmourTemplate(537, ARMOUR_TYPE_SCALE_DRAGON, 0.04F);
    new ArmourTemplate(531, ARMOUR_TYPE_SCALE_DRAGON, 0.04F);
    new ArmourTemplate(534, ARMOUR_TYPE_SCALE_DRAGON, 0.04F);
    new ArmourTemplate(536, ARMOUR_TYPE_SCALE_DRAGON, 0.001F);
    new ArmourTemplate(530, ARMOUR_TYPE_SCALE_DRAGON, 0.001F);
    new ArmourTemplate(533, ARMOUR_TYPE_SCALE_DRAGON, 0.001F);
    new ArmourTemplate(515, ARMOUR_TYPE_SCALE_DRAGON, 0.001F);
    new ArmourTemplate(330, ARMOUR_TYPE_SCALE_DRAGON, 0.009F);
    
    new ArmourTemplate(600, ARMOUR_TYPE_LEATHER_DRAGON, 0.001F);
  }
  
  public static void initializeCreatureArmour()
  {
    ARMOUR_TYPE_NONE.setCreatureGlance(0.0F, (byte)0, 0.0F);
    ARMOUR_TYPE_LEATHER.setCreatureGlance(0.25F, (byte)0, 0.05F);
    ARMOUR_TYPE_STUDDED.setCreatureGlance(0.25F, (byte)1, 0.05F);
    ARMOUR_TYPE_CHAIN.setCreatureGlance(0.3F, (byte)2, 0.1F);
    ARMOUR_TYPE_PLATE.setCreatureGlance(0.3F, (byte)1, 0.05F);
    ARMOUR_TYPE_RING.setCreatureGlance(0.3F, (byte)2, 0.05F);
    ARMOUR_TYPE_CLOTH.setCreatureGlance(0.25F, (byte)0, 0.05F);
    ARMOUR_TYPE_SCALE.setCreatureGlance(0.3F, (byte)1, 0.1F);
    ARMOUR_TYPE_SPLINT.setCreatureGlance(0.3F, (byte)0, 0.1F);
    ARMOUR_TYPE_LEATHER_DRAGON.setCreatureGlance(0.35F, (byte)0, 0.05F);
    ARMOUR_TYPE_SCALE_DRAGON.setCreatureGlance(0.35F, (byte)1, 0.05F);
  }
  
  public static void initializeArmourEffectiveness()
  {
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)3, 0.95F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)3, 1.05F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)3, 1.05F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)3, 1.07F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)3, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)3, 0.9F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)3, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)3, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)3, 1.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)3, 1.0F);
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      ARMOUR_TYPE_LEATHER.setEffectiveness((byte)3, 0.9F);
      ARMOUR_TYPE_CHAIN.setEffectiveness((byte)3, 1.075F);
      ARMOUR_TYPE_PLATE.setEffectiveness((byte)3, 1.05F);
      ARMOUR_TYPE_CLOTH.setEffectiveness((byte)3, 0.8F);
    }
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)0, 1.0F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)0, 1.0F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)0, 1.1F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)0, 0.9F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)0, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)0, 1.15F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)0, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)0, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)0, 1.1F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)0, 0.95F);
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      ARMOUR_TYPE_CHAIN.setEffectiveness((byte)0, 1.075F);
      ARMOUR_TYPE_PLATE.setEffectiveness((byte)0, 0.95F);
      ARMOUR_TYPE_CLOTH.setEffectiveness((byte)0, 1.2F);
      ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)0, 1.05F);
    }
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)2, 0.9F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)2, 1.1F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)2, 0.9F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)2, 1.0F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)2, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)2, 1.0F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)2, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)2, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)2, 1.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)2, 1.1F);
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      ARMOUR_TYPE_CHAIN.setEffectiveness((byte)2, 0.925F);
      ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)2, 1.05F);
    }
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)1, 1.1F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)1, 0.9F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)1, 1.0F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)1, 1.05F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)1, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)1, 0.8F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)1, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)1, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)1, 0.9F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)1, 1.0F);
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled())) {
      ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)1, 0.95F);
    }
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)4, 1.15F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)4, 1.0F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)4, 1.05F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)4, 0.95F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)4, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)4, 0.9F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)4, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)4, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)4, 1.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)4, 1.1F);
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      ARMOUR_TYPE_LEATHER.setEffectiveness((byte)4, 1.1F);
      ARMOUR_TYPE_CHAIN.setEffectiveness((byte)4, 1.075F);
      ARMOUR_TYPE_CLOTH.setEffectiveness((byte)4, 0.8F);
      ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)4, 1.05F);
    }
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)8, 1.0F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)8, 0.9F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)8, 0.9F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)8, 1.0F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)8, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)8, 1.25F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)8, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)8, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)8, 1.05F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)8, 0.95F);
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      ARMOUR_TYPE_STUDDED.setEffectiveness((byte)8, 1.1F);
      ARMOUR_TYPE_CHAIN.setEffectiveness((byte)8, 0.925F);
      ARMOUR_TYPE_CLOTH.setEffectiveness((byte)8, 1.2F);
    }
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)10, 0.9F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)10, 1.05F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)10, 1.0F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)10, 1.07F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)10, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)10, 1.0F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)10, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)10, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)10, 0.95F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)10, 1.0F);
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      ARMOUR_TYPE_STUDDED.setEffectiveness((byte)10, 0.9F);
      ARMOUR_TYPE_PLATE.setEffectiveness((byte)10, 1.05F);
    }
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)6, 1.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)6, 1.0F);
    
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)9, 1.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)9, 1.0F);
    
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)5, 1.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)5, 1.0F);
    
    ARMOUR_TYPE_LEATHER.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_STUDDED.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_CHAIN.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_PLATE.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_RING.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_CLOTH.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_SCALE.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_SPLINT.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setEffectiveness((byte)7, 1.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setEffectiveness((byte)7, 1.0F);
    
    byte woundType = 0;
    while (woundType <= 10)
    {
      ARMOUR_TYPE_NONE.setEffectiveness(woundType, 1.0F);
      woundType = (byte)(woundType + 1);
    }
  }
  
  public static void initializeArmourGlanceRates()
  {
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)3, 0.3F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)3, 0.45F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)3, 0.6F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)3, 0.45F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)3, 0.5F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)3, 0.3F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)3, 0.6F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)3, 0.3F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)3, 0.5F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)3, 0.4F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)0, 0.5F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)0, 0.6F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)0, 0.25F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)0, 0.25F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)0, 0.3F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)0, 0.5F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)0, 0.3F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)0, 0.4F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)0, 0.5F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)0, 0.5F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)2, 0.3F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)2, 0.25F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)2, 0.25F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)2, 0.6F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)2, 0.3F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)2, 0.35F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)2, 0.5F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)2, 0.4F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)2, 0.2F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)2, 0.6F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)1, 0.3F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)1, 0.25F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)1, 0.6F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)1, 0.25F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)1, 0.5F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)1, 0.35F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)1, 0.3F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)1, 0.4F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)1, 0.5F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)1, 0.2F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)4, 0.1F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)4, 0.1F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)4, 0.6F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)4, 0.3F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)4, 0.2F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)4, 0.1F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)4, 0.3F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)4, 0.2F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)4, 0.3F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)4, 0.5F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)8, 0.6F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)8, 0.6F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)8, 0.1F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)8, 0.3F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)8, 0.2F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)8, 0.6F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)8, 0.3F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)8, 0.2F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)8, 0.5F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)8, 0.2F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)10, 0.2F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)10, 0.2F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)10, 0.2F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)10, 0.3F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)10, 0.5F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)10, 0.6F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)10, 0.3F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)10, 0.6F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)10, 0.3F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)10, 0.2F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)6, 0.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)6, 0.0F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)9, 0.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)9, 0.0F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)5, 0.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)5, 0.0F);
    
    ARMOUR_TYPE_LEATHER.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_STUDDED.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_CHAIN.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_PLATE.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_RING.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_CLOTH.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_SCALE.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_SPLINT.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_LEATHER_DRAGON.setGlanceRate((byte)7, 0.0F);
    ARMOUR_TYPE_SCALE_DRAGON.setGlanceRate((byte)7, 0.0F);
    
    byte woundType = 0;
    while (woundType <= 10)
    {
      ARMOUR_TYPE_NONE.setGlanceRate(woundType, 0.0F);
      woundType = (byte)(woundType + 1);
    }
  }
  
  public static void initialize()
  {
    if ((Servers.isThisAnEpicOrChallengeServer()) || (Features.Feature.NEW_ARMOUR_VALUES.isEnabled()))
    {
      ARMOUR_TYPE_LEATHER.setBaseDR(0.6F);
      ARMOUR_TYPE_STUDDED.setBaseDR(0.625F);
      ARMOUR_TYPE_CHAIN.setBaseDR(0.625F);
      ARMOUR_TYPE_PLATE.setBaseDR(0.65F);
      ARMOUR_TYPE_CLOTH.setBaseDR(0.4F);
    }
    initializeProtectionSlots();
    initializeCreatureArmour();
    initializeArmourEffectiveness();
    initializeArmourGlanceRates();
    
    initializeArmourTemplates();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\combat\ArmourTemplate.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */