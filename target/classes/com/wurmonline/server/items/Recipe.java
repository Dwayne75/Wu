package com.wurmonline.server.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.Achievement;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.players.Achievements;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.util.StringUtilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Recipe
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(Recipe.class.getName());
  public static final byte TIME = 0;
  public static final byte HEAT = 1;
  public static final byte CREATE = 2;
  public static final short DEBUG_RECIPE = 0;
  private final String name;
  private final short recipeId;
  private boolean known = false;
  private boolean nameable = false;
  private String skillName = "";
  private int skillId = -1;
  private final Map<Short, String> cookers = new HashMap();
  private final Map<Short, Byte> cookersDif = new HashMap();
  private final Map<Short, String> containers = new HashMap();
  private final Map<Short, Byte> containersDif = new HashMap();
  private byte trigger = 2;
  private Ingredient activeItem = null;
  private Ingredient targetItem = null;
  private Ingredient resultItem = null;
  private final List<IngredientGroup> ingredientGroups = new ArrayList();
  private int achievementId = -1;
  private String achievementName = "";
  private final Map<Byte, Ingredient> allIngredients = new HashMap();
  private boolean lootable = false;
  private int lootableCreature = -10;
  private byte lootableRarity = 0;
  
  public Recipe(String name, short recipeId)
  {
    this.name = name;
    this.recipeId = recipeId;
  }
  
  public Recipe(short recipeId)
  {
    this.recipeId = recipeId;
    
    Recipe templateRecipe = Recipes.getRecipeById(this.recipeId);
    if (templateRecipe != null)
    {
      this.name = templateRecipe.name;
      setDefaults(templateRecipe);
    }
    else
    {
      this.name = ("Null Recipe " + this.recipeId);
      logger.warning("Null recipe with ID: " + this.recipeId);
    }
  }
  
  public Recipe(DataInputStream dis)
    throws IOException, NoSuchTemplateException
  {
    this.recipeId = dis.readShort();
    
    Recipe templateRecipe = Recipes.getRecipeById(this.recipeId);
    if (templateRecipe != null)
    {
      this.name = templateRecipe.name;
      setDefaults(templateRecipe);
    }
    else
    {
      this.name = ("Null Recipe " + this.recipeId);
    }
    byte cookerCount = dis.readByte();
    if (cookerCount > 0) {
      for (int ic = 0; ic < cookerCount; ic++)
      {
        short cookerid = dis.readShort();
        
        addToCookerList(cookerid);
      }
    }
    byte containerCount = dis.readByte();
    if (containerCount > 0) {
      for (int ic = 0; ic < containerCount; ic++)
      {
        short containerid = dis.readShort();
        
        addToContainerList(containerid);
      }
    }
    boolean hasActiveItem = dis.readBoolean();
    if (hasActiveItem) {
      setActiveItem(new Ingredient(dis));
    }
    boolean hasTargetItem = dis.readBoolean();
    if (hasTargetItem) {
      setTargetItem(new Ingredient(dis));
    }
    byte groupCount = dis.readByte();
    if (groupCount > 0) {
      for (int ic = 0; ic < groupCount; ic++)
      {
        IngredientGroup ig = new IngredientGroup(dis);
        if (ig.size() > 0) {
          addToIngredientGroupList(ig);
        } else {
          logger.warning("recipe contains empty IngredientGroup: [" + this.recipeId + "] " + this.name);
        }
        for (Ingredient i : ig.getIngredients()) {
          this.allIngredients.put(Byte.valueOf(i.getIngredientId()), i);
        }
      }
    }
  }
  
  public void pack(DataOutputStream dos)
    throws IOException
  {
    dos.writeShort(this.recipeId);
    
    dos.writeByte(this.cookers.size());
    for (Short cooker : this.cookers.keySet()) {
      dos.writeShort(cooker.shortValue());
    }
    dos.writeByte(this.containers.size());
    for (??? = this.containers.keySet().iterator(); ???.hasNext();)
    {
      container = (Short)???.next();
      
      dos.writeShort(container.shortValue());
    }
    Short container;
    dos.writeBoolean(hasActiveItem());
    if (hasActiveItem()) {
      this.activeItem.pack(dos);
    }
    dos.writeBoolean(hasTargetItem());
    if (hasTargetItem()) {
      this.targetItem.pack(dos);
    }
    Object toSend = new ArrayList();
    for (IngredientGroup ig : this.ingredientGroups) {
      if (ig.size() > 0) {
        ((ArrayList)toSend).add(ig);
      }
    }
    dos.writeByte(((ArrayList)toSend).size());
    for (IngredientGroup ig : (ArrayList)toSend) {
      ig.pack(dos);
    }
  }
  
  public String getRecipeName()
  {
    return this.name;
  }
  
  public String getName()
  {
    if (this.nameable)
    {
      String namer = Recipes.getRecipeNamer(this.recipeId);
      if ((namer != null) && (namer.length() > 0)) {
        return namer + "'s " + this.name;
      }
      return this.name + "+";
    }
    return this.name;
  }
  
  public short getRecipeId()
  {
    return this.recipeId;
  }
  
  public byte getRecipeColourCode(long playerId)
  {
    int colour = 0;
    if (this.lootable) {
      colour = this.lootableRarity;
    }
    if (isKnown()) {
      colour |= 0x4;
    }
    if (RecipesByPlayer.isFavourite(playerId, this.recipeId)) {
      colour |= 0x8;
    }
    if (!RecipesByPlayer.isKnownRecipe(playerId, this.recipeId)) {
      colour |= 0x10;
    }
    return (byte)colour;
  }
  
  public short getMenuId()
  {
    return (short)(this.recipeId + 8000);
  }
  
  byte getCurrentGroupId()
  {
    return (byte)(this.ingredientGroups.size() - 1);
  }
  
  public void setLootable(int creatureId, byte rarity)
  {
    if (creatureId != -10)
    {
      this.lootable = true;
      this.lootableCreature = creatureId;
      this.lootableRarity = rarity;
    }
    else
    {
      this.lootable = false;
    }
  }
  
  public boolean isLootable()
  {
    return this.lootable;
  }
  
  public int getLootableCreature()
  {
    return this.lootableCreature;
  }
  
  public byte getLootableRarity()
  {
    return this.lootableRarity;
  }
  
  public byte getIngredientCount()
  {
    return (byte)this.allIngredients.size();
  }
  
  public void addIngredient(Ingredient ingredient)
  {
    byte gId = ingredient.getGroupId();
    if (gId == -3)
    {
      setResultItem(ingredient);
    }
    else
    {
      Ingredient old = (Ingredient)this.allIngredients.put(Byte.valueOf(ingredient.getIngredientId()), ingredient);
      if (old != null) {
        logger.info("Recipe (" + this.recipeId + ") Overridden Ingredient (" + old.getIngredientId() + ") group (" + gId + ") old:" + old
          .getName(true) + " new:" + ingredient.getName(true) + ".");
      }
      if (gId == -2)
      {
        setActiveItem(ingredient);
      }
      else if (gId == -1)
      {
        setTargetItem(ingredient);
      }
      else
      {
        IngredientGroup ig = getGroupById(gId);
        if (ig != null) {
          ig.add(ingredient);
        } else {
          logger.log(Level.WARNING, "IngredientGroup is null for groupID: " + gId, new Exception());
        }
      }
    }
  }
  
  public Ingredient getIngredientById(byte ingredientId)
  {
    return (Ingredient)this.allIngredients.get(Byte.valueOf(ingredientId));
  }
  
  public String getSubMenuName(Item container)
  {
    StringBuilder buf = new StringBuilder();
    if (this.resultItem.hasCState())
    {
      buf.append(this.resultItem.getCStateName());
      if (this.resultItem.hasPState()) {
        buf.append(" " + this.resultItem.getPStateName());
      }
      buf.append(" ");
    }
    else if ((this.resultItem.hasPState()) && (this.resultItem.getPState() != 0))
    {
      buf.append(this.resultItem.getPStateName() + " ");
    }
    buf.append(getResultName(container));
    return buf.toString();
  }
  
  void setKnown(boolean known)
  {
    this.known = known;
  }
  
  public boolean isKnown()
  {
    return this.known;
  }
  
  void setNameable(boolean nameable)
  {
    this.nameable = nameable;
  }
  
  public boolean isNameable()
  {
    return this.nameable;
  }
  
  public void setSkill(int skillId, String skillName)
  {
    this.skillName = skillName;
    this.skillId = skillId;
  }
  
  public int getSkillId()
  {
    return this.skillId;
  }
  
  public String getSkillName()
  {
    return this.skillName;
  }
  
  public void setTrigger(byte trigger)
  {
    this.trigger = trigger;
  }
  
  public byte getTrigger()
  {
    return this.trigger;
  }
  
  public int getDifficulty(Item target)
  {
    int diff = this.resultItem.getDifficulty();
    if (diff == -100) {
      diff = (int)getResultTemplate(target).getDifficulty();
    }
    if (target.isFoodMaker()) {
      for (IngredientGroup ig : this.ingredientGroups) {
        diff += ig.getGroupDifficulty();
      }
    } else if (hasTargetItem()) {
      diff += this.targetItem.getDifficulty();
    }
    Item cooker = target.getTopParentOrNull();
    if (cooker != null)
    {
      Byte cookerDif = (Byte)this.cookersDif.get(Short.valueOf((short)cooker.getTemplateId()));
      if (cookerDif != null) {
        diff += cookerDif.byteValue();
      }
    }
    Byte containerDif = (Byte)this.containersDif.get(Short.valueOf((short)target.getTemplateId()));
    if (containerDif != null) {
      diff += containerDif.byteValue();
    }
    return diff;
  }
  
  public void addToCookerList(int cookerTemplateId, String cookerName, int cookerDif)
  {
    this.cookers.put(Short.valueOf((short)cookerTemplateId), cookerName);
    this.cookersDif.put(Short.valueOf((short)cookerTemplateId), Byte.valueOf((byte)cookerDif));
  }
  
  public void addToCookerList(int cookerTemplateId)
  {
    String name = "";
    try
    {
      ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplate(cookerTemplateId);
      name = cookerIT.getName();
    }
    catch (NoSuchTemplateException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    addToCookerList(cookerTemplateId, name, 0);
  }
  
  private boolean isCooker(int cookerTemplateId)
  {
    return this.cookers.containsKey(Short.valueOf((short)cookerTemplateId));
  }
  
  public Set<ItemTemplate> getCookerTemplates()
  {
    Set<ItemTemplate> cookerTemplates = new HashSet();
    for (Short sc : this.cookers.keySet()) {
      try
      {
        ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplate(sc.shortValue());
        cookerTemplates.add(cookerIT);
      }
      catch (NoSuchTemplateException localNoSuchTemplateException) {}
    }
    return cookerTemplates;
  }
  
  public void addToContainerList(int containerTemplateId, String containerName, int containerDif)
  {
    this.containers.put(Short.valueOf((short)containerTemplateId), containerName);
    this.containersDif.put(Short.valueOf((short)containerTemplateId), Byte.valueOf((byte)containerDif));
  }
  
  public void addToContainerList(int containerTemplateId)
  {
    String name = "";
    try
    {
      ItemTemplate containerIT = ItemTemplateFactory.getInstance().getTemplate(containerTemplateId);
      name = containerIT.getName();
    }
    catch (NoSuchTemplateException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    addToContainerList(containerTemplateId, name, 0);
  }
  
  public boolean isContainer(int containerTemplateId)
  {
    return this.containers.containsKey(Short.valueOf((short)containerTemplateId));
  }
  
  public Set<ItemTemplate> getContainerTemplates()
  {
    Set<ItemTemplate> containerTemplates = new HashSet();
    for (Short sc : this.containers.keySet()) {
      try
      {
        ItemTemplate cookerIT = ItemTemplateFactory.getInstance().getTemplate(sc.shortValue());
        containerTemplates.add(cookerIT);
      }
      catch (NoSuchTemplateException localNoSuchTemplateException) {}
    }
    return containerTemplates;
  }
  
  public Map<String, Ingredient> getAllIngredients(boolean incActiveAndTargetItems)
  {
    Map<String, Ingredient> knownIngredients = new HashMap();
    for (Ingredient ingredient : this.allIngredients.values()) {
      if ((ingredient.getGroupId() >= 0) || (incActiveAndTargetItems)) {
        if (!ingredient.getTemplate().isCookingTool()) {
          knownIngredients.put(ingredient.getName(true), ingredient);
        }
      }
    }
    return knownIngredients;
  }
  
  public void setActiveItem(Ingredient ingredient)
  {
    this.activeItem = ingredient;
  }
  
  @Nullable
  public Ingredient getActiveItem()
  {
    return this.activeItem;
  }
  
  public boolean hasActiveItem()
  {
    return this.activeItem != null;
  }
  
  private boolean isActiveItem(Item source)
  {
    if (this.activeItem.getTemplateId() == 14) {
      return true;
    }
    if (!this.activeItem.checkFoodGroup(source)) {
      return false;
    }
    if (!this.activeItem.checkCorpseData(source)) {
      return false;
    }
    if (!this.activeItem.checkState(source)) {
      return false;
    }
    if (!this.activeItem.checkMaterial(source)) {
      return false;
    }
    if (!this.activeItem.checkRealTemplate(source)) {
      return false;
    }
    return true;
  }
  
  public String getActiveItemName()
  {
    if (hasActiveItem()) {
      return this.activeItem.getName(false);
    }
    return "";
  }
  
  public void setTargetItem(Ingredient targetIngredient)
  {
    this.targetItem = targetIngredient;
    if (targetIngredient.getTemplateId() == 1173) {
      this.trigger = 2;
    }
  }
  
  @Nullable
  public Ingredient getTargetItem()
  {
    return this.targetItem;
  }
  
  public boolean hasTargetItem()
  {
    return this.targetItem != null;
  }
  
  private boolean isTargetItem(Item target, boolean checkLiquids)
  {
    if (target.isFoodMaker())
    {
      for (Short ii : this.containers.keySet()) {
        if (ii.intValue() == target.getTemplateId()) {
          return true;
        }
      }
      return false;
    }
    if (this.targetItem == null) {
      return false;
    }
    if (!this.targetItem.checkFoodGroup(target)) {
      return false;
    }
    if (!this.targetItem.checkCorpseData(target)) {
      return false;
    }
    if (!this.targetItem.checkState(target)) {
      return false;
    }
    if (!this.targetItem.checkMaterial(target)) {
      return false;
    }
    if (!this.targetItem.checkRealTemplate(target)) {
      return false;
    }
    if ((useResultTemplateWeight()) && (checkLiquids)) {
      if (getTargetLossWeight(target) > target.getWeightGrams()) {
        return false;
      }
    }
    return true;
  }
  
  public int getTargetLossWeight(Item target)
  {
    int loss = this.targetItem.getLoss();
    if (loss != 100)
    {
      int rWeight = (int)(this.resultItem.getTemplate().getWeightGrams() * (1.0F / ((100 - loss) / 100.0F)));
      
      return rWeight;
    }
    return target.getWeightGrams();
  }
  
  public String getTargetItemName()
  {
    if (hasTargetItem()) {
      return this.targetItem.getName(false);
    }
    return "";
  }
  
  public void setResultItem(Ingredient resultIngredient)
  {
    this.resultItem = resultIngredient;
  }
  
  public Ingredient getResultItem()
  {
    return this.resultItem;
  }
  
  public ItemTemplate getResultTemplate(Item container)
  {
    if (this.resultItem.isFoodGroup())
    {
      Item item = findIngredient(container, this.resultItem);
      if (item != null) {
        return item.getTemplate();
      }
    }
    return this.resultItem.getTemplate();
  }
  
  public boolean useResultTemplateWeight()
  {
    return this.resultItem.useResultTemplateWeight();
  }
  
  public String getResultName(Item container)
  {
    String resultName = this.resultItem.getResultName();
    if (resultName.length() > 0) {
      return doSubstituation(container, resultName);
    }
    StringBuilder buf = new StringBuilder();
    if (this.resultItem.isFoodGroup())
    {
      Item item = findIngredient(container, this.resultItem);
      if (item != null) {
        buf.append(item.getActualName());
      }
    }
    else
    {
      buf.append(this.resultItem.getTemplateName());
    }
    return buf.toString();
  }
  
  String doSubstituation(Item container, String name)
  {
    String newName = name;
    if (newName.indexOf('#') >= 0) {
      if ((this.resultItem.hasRealTemplateId()) && (this.resultItem.getRealItemTemplate() != null))
      {
        newName = newName.replace("#", this.resultItem.getRealItemTemplate().getName());
      }
      else if (this.resultItem.hasRealTemplateRef())
      {
        ItemTemplate realTemplate = getResultRealTemplate(container);
        if (realTemplate != null) {
          newName = newName.replace("#", realTemplate.getName());
        } else {
          newName = newName.replace("# ", "").replace(" #", "");
        }
      }
    }
    if (newName.indexOf('$') >= 0) {
      if (this.resultItem.hasMaterial())
      {
        newName = newName.replace("$", this.resultItem.getMaterialName());
      }
      else if (this.resultItem.hasMaterialRef())
      {
        byte material = getResultMaterial(container);
        newName = newName.replace("$", Materials.convertMaterialByteIntoString(material));
      }
    }
    return newName.trim();
  }
  
  String getResultName(Ingredient ingredient)
  {
    StringBuilder buf = new StringBuilder();
    String resultName = this.resultItem.getResultName();
    if (resultName.length() > 0)
    {
      if (this.resultItem.hasCState())
      {
        buf.append(this.resultItem.getCStateName());
        if ((this.resultItem.hasPState()) && (this.resultItem.getPStateName().length() > 0)) {
          buf.append(" " + this.resultItem.getPStateName());
        }
        buf.append(" ");
      }
      else if ((this.resultItem.hasPState()) && (this.resultItem.getPStateName().length() > 0))
      {
        buf.append(this.resultItem.getPStateName() + " ");
      }
      if (resultName.indexOf('#') >= 0) {
        if (ingredient.getRealItemTemplate() != null) {
          resultName = resultName.replace("#", ingredient.getRealItemTemplate().getName().replace("any ", ""));
        } else if (this.resultItem.hasRealTemplateRef()) {
          resultName = resultName.replace("# ", "").replace(" #", "");
        }
      }
      if (resultName.indexOf('$') >= 0) {
        if (ingredient.hasMaterial()) {
          resultName = resultName.replace("$", ingredient.getMaterialName());
        } else if (this.resultItem.hasMaterialRef()) {
          resultName = resultName.replace("$ ", "").replace(" $", "");
        }
      }
      buf.append(resultName.trim());
      return buf.toString();
    }
    buf.append(this.resultItem.getName(false));
    if ((!this.resultItem.hasMaterial()) && (ingredient.hasMaterial())) {
      buf.append(" (" + ingredient.getMaterialName() + ")");
    }
    return buf.toString();
  }
  
  public String getResultNameWithGenus(Item container)
  {
    return StringUtilities.addGenus(getSubMenuName(container), container.isNamePlural());
  }
  
  public boolean hasResultState()
  {
    return this.resultItem.hasXState();
  }
  
  public byte getResultState()
  {
    return this.resultItem.getXState();
  }
  
  public byte getResultMaterial(Item target)
  {
    if (this.resultItem.hasMaterialRef())
    {
      if ((this.targetItem != null) && (this.targetItem.getTemplateName().equalsIgnoreCase(this.resultItem.getMaterialRef()))) {
        return target.getMaterial();
      }
      IngredientGroup group = getGroupByType(1);
      if (group != null)
      {
        Ingredient ingredient = group.getIngredientByName(this.resultItem.getMaterialRef());
        if ((ingredient != null) && (ingredient.getMaterial() != 0))
        {
          Item item = findIngredient(target, ingredient);
          if (item != null) {
            return item.getMaterial();
          }
        }
      }
    }
    if (this.resultItem.hasMaterial()) {
      return this.resultItem.getMaterial();
    }
    return this.resultItem.getTemplate().getMaterial();
  }
  
  public boolean hasDescription()
  {
    return this.resultItem.hasResultDescription();
  }
  
  public String getResultDescription(Item container)
  {
    return doSubstituation(container, this.resultItem.getResultDescription());
  }
  
  public void addAchievements(Creature performer, Item newItem)
  {
    if (this.achievementId != -1)
    {
      AchievementTemplate at = Achievement.getTemplate(this.achievementId);
      if (at != null) {
        if (at.isInLiters()) {
          performer.achievement(this.achievementId, newItem.getWeightGrams() / 1000);
        } else {
          performer.achievement(this.achievementId);
        }
      }
    }
  }
  
  public void addAchievementsOffline(long wurmId, Item newItem)
  {
    if (this.achievementId != -1)
    {
      AchievementTemplate at = Achievement.getTemplate(this.achievementId);
      if (at != null) {
        if (at.isInLiters()) {
          Achievements.triggerAchievement(wurmId, this.achievementId, newItem.getWeightGrams() / 1000);
        } else {
          Achievements.triggerAchievement(wurmId, this.achievementId);
        }
      }
    }
  }
  
  @Nullable
  public ItemTemplate getResultRealTemplate(Item target)
  {
    if (this.resultItem.getRealTemplateRef().length() > 0)
    {
      if (hasOneContainer()) {
        for (Map.Entry<Short, String> container : this.containers.entrySet()) {
          if (((String)container.getValue()).equalsIgnoreCase(this.resultItem.getRealTemplateRef())) {
            return target.getRealTemplate();
          }
        }
      }
      if ((this.targetItem != null) && (this.targetItem.getTemplateName().equalsIgnoreCase(this.resultItem.getRealTemplateRef())))
      {
        ItemTemplate rit = target.getRealTemplate();
        if (rit != null) {
          return rit;
        }
        return target.getTemplate();
      }
      IngredientGroup group = getGroupByType(1);
      if (group != null)
      {
        Ingredient ingredient = group.getIngredientByName(this.resultItem.getRealTemplateRef());
        if (ingredient != null)
        {
          Item item = findIngredient(target, ingredient);
          if (item != null)
          {
            ItemTemplate rit = item.getRealTemplate();
            if (rit != null) {
              return rit;
            }
            return item.getTemplate();
          }
        }
      }
    }
    else if (this.resultItem.hasRealTemplate())
    {
      return this.resultItem.getRealItemTemplate();
    }
    return null;
  }
  
  @Nullable
  private Item findIngredient(Item container, Ingredient ingredient)
  {
    int foodGroup = ingredient.isFoodGroup() ? ingredient.getTemplateId() : 0;
    if ((container.isFoodMaker()) || (container.getTemplate().isCooker()) || (container.getTemplateId() == 1284))
    {
      for (Item item : container.getItemsAsArray()) {
        if (foodGroup > 0)
        {
          if (item.getTemplate().getFoodGroup() == foodGroup) {
            if ((!ingredient.hasRealTemplate()) || 
            
              (item.getRealTemplateId() == ingredient.getRealTemplateId())) {
              if ((!ingredient.hasMaterial()) || 
              
                (item.getMaterial() == ingredient.getMaterial())) {
                return item;
              }
            }
          }
        }
        else if (item.getTemplateId() == ingredient.getTemplateId()) {
          if ((!ingredient.hasRealTemplate()) || 
          
            (item.getRealTemplateId() == ingredient.getRealTemplateId()) || (
            (item.getRealTemplate() != null) && (item.getRealTemplate().getFoodGroup() == ingredient.getRealTemplateId()))) {
            if ((!ingredient.hasMaterial()) || 
            
              (item.getMaterial() == ingredient.getMaterial())) {
              return item;
            }
          }
        }
      }
    }
    else if (container.getTemplate().getFoodGroup() == foodGroup)
    {
      if (ingredient.hasRealTemplate()) {
        if (container.getRealTemplateId() != ingredient.getRealTemplateId()) {
          return null;
        }
      }
      if (ingredient.hasMaterial()) {
        if (container.getMaterial() != ingredient.getMaterial()) {
          return null;
        }
      }
      return container;
    }
    return null;
  }
  
  @Nullable
  public Ingredient findMatchingIngredient(Item item)
  {
    for (Ingredient ingredient : this.allIngredients.values()) {
      if (ingredient.matches(item)) {
        return ingredient;
      }
    }
    return null;
  }
  
  boolean isPartialMatch(Item container)
  {
    if (getRecipeId() == 0) {
      System.out.println("isPartialMatch:" + getRecipeId() + " " + getTriggerName());
    }
    if (hasTargetItem())
    {
      if (!isTargetItem(container, false)) {
        return false;
      }
    }
    else if (hasContainer()) {
      if (!isContainer(container.getTemplateId())) {
        return false;
      }
    }
    Item[] items = container.getItemsAsArray();
    boolean[] founds = new boolean[items.length];
    for (int x = 0; x < founds.length; x++) {
      founds[x] = false;
    }
    if (getRecipeId() == 0) {
      System.out.println("isPartialMatch2:" + getRecipeId() + " " + getTriggerName());
    }
    for (IngredientGroup ig : this.ingredientGroups)
    {
      ig.clearFound();
      for (int x = 0; x < items.length; x++) {
        if ((founds[x] == 0) && (ig.matches(items[x]))) {
          founds[x] = true;
        }
      }
    }
    if (getRecipeId() == 0) {
      System.out.println("isPartialMatch3:" + getRecipeId() + " " + getTriggerName());
    }
    for (int x = 0; x < items.length; x++) {
      if (founds[x] != 0)
      {
        Ingredient ingredient = findMatchingIngredient(items[x]);
        if ((ingredient != null) && (!ingredient.wasFound(true, false))) {
          return false;
        }
      }
      else
      {
        return false;
      }
    }
    for (IngredientGroup ig : this.ingredientGroups)
    {
      if ((ig.getGroupType() == 3) && (ig.getFound(false) > 1)) {
        return false;
      }
      if ((ig.getGroupType() == 2) && (ig.getFound(false) > 1)) {
        return false;
      }
      if ((ig.getGroupType() == 5) && (!ig.wasFound())) {
        return false;
      }
    }
    return true;
  }
  
  public Ingredient[] getWhatsMissing()
  {
    Set<Ingredient> ingredients = new HashSet();
    for (IngredientGroup ig : this.ingredientGroups) {
      if ((ig.getGroupType() == 1) || 
        (ig.getGroupType() == 3) || 
        (ig.getGroupType() == 4)) {
        if (!ig.wasFound()) {
          for (Ingredient ingredient : ig.getIngredients()) {
            if (!ingredient.wasFound(ig.getGroupType() == 4, false)) {
              ingredients.add(ingredient);
            }
          }
        }
      }
    }
    return (Ingredient[])ingredients.toArray(new Ingredient[ingredients.size()]);
  }
  
  public void addToIngredientGroupList(IngredientGroup ingredientGroup)
  {
    this.ingredientGroups.add(ingredientGroup);
  }
  
  public void setDefaults(Recipe templateRecipe)
  {
    for (IngredientGroup ig : templateRecipe.getGroups()) {
      if (ig.size() > 0) {
        addToIngredientGroupList(ig.clone());
      } else {
        logger.warning("recipe contains empty IngredientGroup: [" + templateRecipe.recipeId + "] " + templateRecipe.name);
      }
    }
    this.resultItem = templateRecipe.resultItem.clone(null);
    this.lootable = templateRecipe.lootable;
    this.nameable = templateRecipe.nameable;
    this.lootableCreature = templateRecipe.lootableCreature;
    this.lootableRarity = templateRecipe.lootableRarity;
    this.trigger = templateRecipe.trigger;
    this.skillId = templateRecipe.skillId;
    this.skillName = templateRecipe.skillName;
    this.achievementId = templateRecipe.achievementId;
    this.achievementName = templateRecipe.achievementName;
  }
  
  public void copyGroupsFrom(Recipe recipe)
  {
    for (IngredientGroup ig : recipe.getGroups()) {
      addToIngredientGroupList(ig.clone());
    }
  }
  
  @Nullable
  public IngredientGroup getGroupById(byte groupId)
  {
    try
    {
      return (IngredientGroup)this.ingredientGroups.get(groupId);
    }
    catch (IndexOutOfBoundsException e) {}
    return null;
  }
  
  @Nullable
  public IngredientGroup getGroupByType(int groupType)
  {
    for (IngredientGroup ig : this.ingredientGroups) {
      if (ig.getGroupType() == groupType) {
        return ig;
      }
    }
    return null;
  }
  
  public IngredientGroup[] getGroups()
  {
    return (IngredientGroup[])this.ingredientGroups.toArray(new IngredientGroup[this.ingredientGroups.size()]);
  }
  
  public boolean hasCooker()
  {
    return !this.cookers.isEmpty();
  }
  
  public boolean hasCooker(int cookerId)
  {
    return this.cookers.containsKey(Short.valueOf((short)cookerId));
  }
  
  public boolean hasOneCooker()
  {
    return this.cookers.size() == 1;
  }
  
  public short getCookerId()
  {
    Iterator localIterator = this.cookers.keySet().iterator();
    if (localIterator.hasNext())
    {
      Short ss = (Short)localIterator.next();
      
      return ss.shortValue();
    }
    return -10;
  }
  
  public boolean hasContainer()
  {
    return !this.containers.isEmpty();
  }
  
  public boolean hasOneContainer()
  {
    return this.containers.size() == 1;
  }
  
  public boolean hasContainer(int containerId)
  {
    return this.containers.containsKey(Short.valueOf((short)containerId));
  }
  
  public boolean hasContainer(String containerName)
  {
    for (Map.Entry<Short, String> container : this.containers.entrySet()) {
      if (((String)container.getValue()).equalsIgnoreCase(containerName)) {
        return true;
      }
    }
    return false;
  }
  
  public short getContainerId()
  {
    Iterator localIterator = this.containers.keySet().iterator();
    if (localIterator.hasNext())
    {
      Short ss = (Short)localIterator.next();
      
      return ss.shortValue();
    }
    return -10;
  }
  
  boolean checkIngredients(Item container)
  {
    Item[] items = container.getItemsAsArray();
    boolean[] founds = new boolean[items.length];
    for (int x = 0; x < founds.length; x++) {
      founds[x] = false;
    }
    if (getRecipeId() == 0) {
      System.out.println("checkIngredients:" + getRecipeId() + " " + getTriggerName());
    }
    for (IngredientGroup ig : this.ingredientGroups)
    {
      ig.clearFound();
      for (int x = 0; x < items.length; x++) {
        if (ig.matches(items[x])) {
          founds[x] = true;
        }
      }
    }
    if (getRecipeId() == 0) {
      System.out.println("checkIngredients2:" + getRecipeId() + " " + getTriggerName());
    }
    for (int x = 0; x < founds.length; x++) {
      if (founds[x] == 0) {
        return false;
      }
    }
    if (getRecipeId() == 0) {
      System.out.println("checkIngredients3:" + getRecipeId() + " " + getTriggerName());
    }
    for (IngredientGroup ig : this.ingredientGroups) {
      if (!ig.wasFound()) {
        return false;
      }
    }
    if (getRecipeId() == 0) {
      System.out.println("checkIngredients4:" + getRecipeId() + " " + getTriggerName());
    }
    return true;
  }
  
  public float getChanceFor(@Nullable Item activeItem, Item target, Creature performer)
  {
    Skills skills = performer.getSkills();
    Skill primSkill = null;
    Skill secondarySkill = null;
    double bonus = 0.0D;
    try
    {
      primSkill = skills.getSkill(getSkillId());
    }
    catch (Exception localException) {}
    try
    {
      if ((hasActiveItem()) && (activeItem != null) && (isActiveItem(activeItem))) {
        secondarySkill = skills.getSkill(activeItem.getPrimarySkill());
      }
    }
    catch (Exception localException1) {}
    if (secondarySkill != null) {
      bonus = Math.max(1.0D, secondarySkill.getKnowledge(activeItem, 0.0D) / 10.0D);
    }
    float chance = 0.0F;
    int diff = getDifficulty(target);
    if (primSkill != null) {
      chance = (float)primSkill.getChance(diff, activeItem, bonus);
    } else {
      chance = 1 / (1 + diff) * 100;
    }
    return chance;
  }
  
  void setAchievementTriggered(int achievementId, String achievementName)
  {
    this.achievementId = achievementId;
    this.achievementName = achievementName;
  }
  
  public String getTriggerName()
  {
    switch (this.trigger)
    {
    case 0: 
      return "Time";
    case 1: 
      return "Heat";
    case 2: 
      if (isTargetActionType()) {
        return "Target Action";
      }
      if (isContainerActionType()) {
        return "Container Action";
      }
      return "Create";
    }
    return "Unknown";
  }
  
  boolean isRecipeOk(long playerId, @Nullable Item activeItem, Item target, boolean checkActive, boolean checkLiquids)
  {
    if (getRecipeId() == 0) {
      System.out.println("isRecipeOk:" + getRecipeId() + " " + checkActive + " " + getTriggerName() + "(" + target.getName() + ")");
    }
    if ((playerId != -10L) && (isLootable()) && (!RecipesByPlayer.isKnownRecipe(playerId, this.recipeId))) {
      return false;
    }
    if ((checkActive) && (activeItem != null) && (getActiveItem() != null))
    {
      if (!isActiveItem(activeItem)) {
        return false;
      }
      if ((checkLiquids) && (activeItem.isLiquid()))
      {
        int weightNeeded = getUsedActiveItemWeightGrams(activeItem, target);
        if (activeItem.getWeightGrams() < weightNeeded) {
          return false;
        }
      }
    }
    if ((this.targetItem != null) && (!isTargetItem(target, checkLiquids))) {
      return false;
    }
    if ((this.trigger == 1) && (checkActive))
    {
      Item cooker = target.getTopParentOrNull();
      if (cooker == null) {
        return false;
      }
      if (!isCooker((short)cooker.getTemplateId())) {
        return false;
      }
    }
    Item parent;
    if (this.targetItem == null)
    {
      if (hasContainer())
      {
        if (!isContainer((short)target.getTemplateId())) {
          return false;
        }
      }
      else if (hasCooker()) {
        if (!isCooker((short)target.getTemplateId())) {
          return false;
        }
      }
    }
    else if ((this.trigger == 1) && (checkActive))
    {
      Item cooker = target.getTopParentOrNull();
      parent = target.getParentOrNull();
      if ((cooker == null) || (parent == null)) {
        return false;
      }
      if (cooker.getTemplateId() != parent.getTemplateId()) {
        return false;
      }
      if (hasContainer()) {
        if (!isContainer((short)parent.getTemplateId())) {
          return false;
        }
      }
    }
    if ((target.isFoodMaker()) || (target.getTemplate().isCooker()) || ((target.isRecipeItem()) && (target.isHollow())))
    {
      if (getRecipeId() == 0) {
        System.out.println("isRecipeOk2:" + getRecipeId() + " " + checkActive);
      }
      if (!checkIngredients(target)) {
        return false;
      }
      if (getRecipeId() == 0) {
        System.out.println("isRecipeOk3:" + getRecipeId() + " " + checkActive);
      }
      if ((checkLiquids) && (!getNewWeightGrams(target).isSuccess())) {
        return false;
      }
      return true;
    }
    int needed = getActiveItem() != null ? 2 : 1;
    if (this.allIngredients.size() != needed) {
      return false;
    }
    for (Ingredient ingredient : this.allIngredients.values()) {
      if (ingredient.matches(target)) {
        return true;
      }
    }
    return false;
  }
  
  public int getUsedActiveItemWeightGrams(Item source, Item target)
  {
    int rat = getActiveItem() != null ? getActiveItem().getRatio() : 0;
    if ((source.isLiquid()) && (rat != 0)) {
      return target.getWeightGrams() * rat / 100;
    }
    return source.getWeightGrams();
  }
  
  public Recipe.LiquidResult getNewWeightGrams(Item container)
  {
    Recipe.LiquidResult liquidResult = new Recipe.LiquidResult(this);
    Map<Short, Recipe.Liquid> liquids = new HashMap();
    for (Iterator localIterator = getAllIngredients(true).values().iterator(); localIterator.hasNext();)
    {
      in = (Ingredient)localIterator.next();
      if (in.getTemplate().isLiquid())
      {
        id = (short)in.getTemplateId();
        ratio = in.getRatio();
        String name = Recipes.getIngredientName(in, false);
        int loss = in.getLoss();
        liquids.put(Short.valueOf(id), new Recipe.Liquid(this, id, name, ratio, loss));
      }
    }
    Ingredient in;
    short id;
    int ratio;
    int solidWeight = 0;
    for (Item item : container.getItemsAsArray()) {
      if (item.isLiquid())
      {
        short id = (short)item.getTemplateId();
        int liquidWeight = item.getWeightGrams();
        Recipe.Liquid liquid = (Recipe.Liquid)liquids.get(Short.valueOf(id));
        if (liquid == null)
        {
          short fgid = (short)item.getTemplate().getFoodGroup();
          liquid = (Recipe.Liquid)liquids.get(Short.valueOf(fgid));
        }
        if (liquid != null)
        {
          if (liquid.getRatio() != 0) {
            liquid.setWeight(liquidWeight);
          }
        }
        else {
          logger.info("Liquid Item " + item.getName() + " missing ingredient?");
        }
      }
      else
      {
        solidWeight += item.getWeightGrams();
      }
    }
    int newWeight = solidWeight;
    for (Recipe.Liquid liquid : liquids.values()) {
      if (liquid.getWeight() > 0)
      {
        int neededWeight = solidWeight * liquid.getRatio() / 100;
        int minLiquid = (int)(neededWeight * 0.8D);
        int maxLiquid = (int)(neededWeight * 1.2D);
        if (liquid.getWeight() < minLiquid) {
          liquidResult.add(liquid.getId(), "not enough " + liquid.getName() + ", looks like it should use between " + minLiquid + " and " + maxLiquid + " grams.");
        } else if (liquid.getWeight() > maxLiquid) {
          liquidResult.add(liquid.getId(), "too much " + liquid.getName() + ", looks like it should use between " + minLiquid + " and " + maxLiquid + " grams.");
        }
        newWeight += liquid.getWeight() * (100 - liquid.getLoss());
      }
    }
    liquidResult.setNewWeight(newWeight);
    return liquidResult;
  }
  
  public boolean isTargetActionType()
  {
    return (this.trigger == 2) && (this.containers.isEmpty());
  }
  
  public boolean isContainerActionType()
  {
    return (this.trigger == 2) && (!this.containers.isEmpty());
  }
  
  public boolean isHeatType()
  {
    return this.trigger == 1;
  }
  
  public boolean isTimeType()
  {
    return this.trigger == 0;
  }
  
  public String[] getCookers()
  {
    List<String> cookerList = new ArrayList();
    for (String cooker : this.cookers.values()) {
      cookerList.add(cooker);
    }
    return (String[])cookerList.toArray(new String[cookerList.size()]);
  }
  
  public String getCookersAsString()
  {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (String s : this.cookers.values())
    {
      if (first) {
        first = false;
      } else {
        buf.append(",");
      }
      buf.append(s);
    }
    return buf.toString();
  }
  
  public String[] getContainers()
  {
    List<String> containerList = new ArrayList();
    for (String container : this.containers.values()) {
      containerList.add(container);
    }
    return (String[])containerList.toArray(new String[containerList.size()]);
  }
  
  public String getContainersAsString()
  {
    StringBuilder buf = new StringBuilder();
    boolean first = true;
    for (String s : this.containers.values())
    {
      if (first) {
        first = false;
      } else {
        buf.append(",");
      }
      buf.append(s);
    }
    return buf.toString();
  }
  
  boolean matchesResult(Ingredient ingredient, boolean exactOnly)
  {
    if (this.resultItem.getTemplateId() == ingredient.getTemplateId())
    {
      boolean ok = (!this.resultItem.hasCState()) && (!ingredient.hasCState());
      if (!ok) {
        ok = (this.resultItem.hasCState()) && (ingredient.hasCState()) && (this.resultItem.getCState() == ingredient.getCState());
      }
      if (!ok) {
        ok = (exactOnly) && (!ingredient.hasCState()) && (this.resultItem.hasCState());
      }
      if (!ok) {
        return false;
      }
      ok = (!this.resultItem.hasPState()) && (!ingredient.hasPState());
      if (!ok) {
        ok = (this.resultItem.hasPState()) && (ingredient.hasPState()) && (this.resultItem.getPState() == ingredient.getPState());
      }
      if (!ok) {
        ok = (exactOnly) && (!ingredient.hasPState()) && (this.resultItem.hasPState());
      }
      if (!ok) {
        return false;
      }
      if (ingredient.hasRealTemplate()) {
        if (this.resultItem.hasRealTemplate())
        {
          if (this.resultItem.getRealTemplateId() != ingredient.getRealTemplateId())
          {
            if (exactOnly) {
              return false;
            }
            if ((this.resultItem.getRealItemTemplate() != null) && (ingredient.getRealItemTemplate() != null))
            {
              if ((this.resultItem.getRealItemTemplate().isFoodGroup()) && 
                (this.resultItem.getRealItemTemplate().getFoodGroup() != ingredient.getRealItemTemplate().getFoodGroup())) {
                return false;
              }
              if ((ingredient.getRealItemTemplate().isFoodGroup()) && 
                (this.resultItem.getRealItemTemplate().getFoodGroup() != ingredient.getRealItemTemplate().getFoodGroup())) {
                return false;
              }
            }
            else
            {
              return false;
            }
          }
        }
        else if (this.resultItem.hasRealTemplateRef())
        {
          boolean match = false;
          if (hasTargetItem()) {
            if (this.targetItem.getTemplateName().equalsIgnoreCase(this.resultItem.getRealTemplateRef()))
            {
              Ingredient refingredient = this.targetItem;
              if (ingredient.getRealItemTemplate() == null)
              {
                if (refingredient.getTemplate() != null) {
                  return false;
                }
                match = true;
              }
              else if (refingredient.getTemplateId() == ingredient.getRealItemTemplate().getTemplateId())
              {
                match = true;
              }
              else if (!exactOnly)
              {
                if ((refingredient.getTemplate().getFoodGroup() == ingredient.getRealItemTemplate().getFoodGroup()) || (
                  (refingredient.getTemplateId() == 369) && 
                  (ingredient.getRealItemTemplate().getFoodGroup() == 1201))) {
                  match = true;
                }
              }
              else
              {
                return false;
              }
            }
          }
          if (!match)
          {
            IngredientGroup group = getGroupByType(1);
            if (group != null)
            {
              Ingredient refingredient = group.getIngredientByName(this.resultItem.getRealTemplateRef());
              if (refingredient != null)
              {
                if (ingredient.getRealItemTemplate() == null)
                {
                  if (refingredient.getTemplate() != null) {
                    return false;
                  }
                  match = true;
                }
                else if (!refingredient.hasRealTemplateId())
                {
                  if (exactOnly) {
                    return false;
                  }
                  if (refingredient.getTemplate().getFoodGroup() == ingredient.getRealItemTemplate().getFoodGroup())
                  {
                    match = true;
                  }
                  else
                  {
                    Recipe[] ning = Recipes.getRecipesByResult(new Ingredient(refingredient.getTemplate(), false, refingredient.getGroupId()));
                    if ((ning == null) || (ning.length == 0)) {
                      return false;
                    }
                  }
                }
                else if (refingredient.getTemplateId() == ingredient.getRealItemTemplate().getTemplateId())
                {
                  match = true;
                }
                else if (!exactOnly)
                {
                  if ((refingredient.getTemplate().getFoodGroup() == ingredient.getRealItemTemplate().getFoodGroup()) || (
                    (refingredient.getTemplateId() == 369) && 
                    (ingredient.getRealItemTemplate().getFoodGroup() == 1201))) {
                    match = true;
                  }
                }
                else
                {
                  return false;
                }
              }
              else {
                return false;
              }
            }
            else
            {
              return false;
            }
          }
        }
        else
        {
          return false;
        }
      }
      if ((ingredient.hasMaterial()) && (this.resultItem.hasMaterial())) {
        if (ingredient.getMaterial() != this.resultItem.getMaterial()) {
          return false;
        }
      }
      if ((ingredient.hasMaterial()) && (this.resultItem.hasMaterialRef())) {
        if (this.targetItem != null)
        {
          if (!isInMaterialGroup(this.targetItem.getTemplateId(), ingredient.getMaterial())) {
            return false;
          }
        }
        else
        {
          IngredientGroup group = getGroupByType(1);
          if (group != null)
          {
            Ingredient refingredient = group.getIngredientByName(this.resultItem.getMaterialRef());
            if (refingredient != null)
            {
              if (!isInMaterialGroup(refingredient.getTemplateId(), ingredient.getMaterial())) {
                return false;
              }
            }
            else {
              return false;
            }
          }
          else
          {
            return false;
          }
        }
      }
      return true;
    }
    if (this.resultItem.getTemplate().isFoodGroup())
    {
      if (this.targetItem != null)
      {
        if ((!exactOnly) || (this.targetItem.getTemplate().getFoodGroup() != ingredient.getTemplate().getFoodGroup())) {
          return false;
        }
      }
      else {
        return false;
      }
      if ((ingredient.hasCState()) && (this.resultItem.hasCState()) && (this.resultItem.getCState() != ingredient.getCState())) {
        return false;
      }
      if ((ingredient.hasPState()) && (this.resultItem.hasPState()) && (this.resultItem.getPState() != ingredient.getPState())) {
        return false;
      }
      return true;
    }
    if ((!exactOnly) && (ingredient.getTemplate().isFoodGroup()))
    {
      if (this.resultItem.getTemplate().getFoodGroup() != ingredient.getTemplateId()) {
        return false;
      }
      if ((this.resultItem.hasCState()) && (this.resultItem.getCState() != ingredient.getCState())) {
        return false;
      }
      if ((this.resultItem.hasPState()) && (this.resultItem.getPState() != ingredient.getPState())) {
        return false;
      }
      return true;
    }
    return false;
  }
  
  private boolean isInMaterialGroup(int templateGroup, byte material)
  {
    switch (templateGroup)
    {
    case 1261: 
      switch (material)
      {
      case 2: 
      case 72: 
      case 73: 
      case 74: 
      case 75: 
      case 76: 
      case 77: 
      case 78: 
      case 79: 
      case 80: 
      case 81: 
      case 82: 
      case 83: 
      case 84: 
      case 85: 
      case 86: 
      case 87: 
        return true;
      }
      return false;
    case 200: 
    case 201: 
    case 1157: 
      switch (material)
      {
      case 3: 
      case 4: 
      case 5: 
      case 6: 
        return true;
      }
      return false;
    }
    return false;
  }
  
  public String getIngredientsAsString()
  {
    StringBuilder buf = new StringBuilder();
    byte groupId = -1;
    IngredientGroup group = null;
    for (Ingredient ingredient : this.allIngredients.values())
    {
      group = getGroupById(ingredient.getGroupId());
      if ((group != null) && (group.getGroupType() > 0))
      {
        byte newGroupId = ingredient.getGroupId();
        if (groupId != newGroupId)
        {
          IngredientGroup oldGroup;
          if ((groupId > -1) && ((oldGroup = getGroupById(groupId)) != null))
          {
            switch (oldGroup.getGroupType())
            {
            case 3: 
              buf.append(")");
              break;
            case 4: 
              buf.append(")+");
              break;
            case 2: 
              buf.append("]");
            }
            buf.append(",");
          }
          switch (group.getGroupType())
          {
          case 5: 
            buf.append("[");
            break;
          case 3: 
            buf.append("(");
            break;
          case 4: 
            buf.append("(");
            break;
          case 2: 
            buf.append("[");
          }
        }
        else
        {
          switch (group.getGroupType())
          {
          case 1: 
            buf.append(",");
            break;
          case 5: 
            buf.append(",[");
            break;
          case 3: 
            buf.append("|");
            break;
          case 4: 
            buf.append("|");
            break;
          case 2: 
            buf.append("|");
          }
        }
        buf.append(Recipes.getIngredientName(ingredient));
        groupId = newGroupId;
        switch (group.getGroupType())
        {
        case 5: 
          buf.append("]");
        }
      }
    }
    if (group != null) {
      switch (group.getGroupType())
      {
      case 3: 
        buf.append(")");
        break;
      case 4: 
        buf.append(")+");
        break;
      case 2: 
        buf.append("]");
      }
    }
    return buf.toString();
  }
  
  void clearFound()
  {
    for (IngredientGroup ig : this.ingredientGroups) {
      ig.clearFound();
    }
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("Recipe:");
    buf.append("recipeId:" + this.recipeId);
    if (this.name.length() > 0) {
      buf.append(",name:" + this.name);
    }
    if (this.skillId > 0) {
      buf.append(",skill:" + this.skillName + "(" + this.skillId + ")");
    }
    buf.append(",trigger:" + getTriggerName());
    if (!this.cookers.isEmpty())
    {
      buf.append(",cookers[");
      boolean first = true;
      for (Map.Entry<Short, String> me : this.cookers.entrySet())
      {
        if (first) {
          first = false;
        } else {
          buf.append(",");
        }
        buf.append((String)me.getValue() + "(" + me.getKey() + "),dif=" + this.cookersDif.get(me.getKey()));
      }
      buf.append("]");
    }
    if (!this.containers.isEmpty())
    {
      buf.append(",containers[");
      boolean first = true;
      for (Map.Entry<Short, String> me : this.containers.entrySet())
      {
        if (first) {
          first = false;
        } else {
          buf.append(",");
        }
        buf.append((String)me.getValue() + "(" + me.getKey() + "),dif=" + this.containersDif.get(me.getKey()));
      }
      buf.append("]");
    }
    if (this.activeItem != null) {
      buf.append(",activeItem:" + this.activeItem.toString());
    }
    if (this.targetItem != null) {
      buf.append(",target:" + this.targetItem.toString());
    }
    if (!this.ingredientGroups.isEmpty())
    {
      buf.append(",ingredients{");
      boolean first = true;
      for (IngredientGroup ig : this.ingredientGroups)
      {
        if (first) {
          first = false;
        } else {
          buf.append(",");
        }
        buf.append(ig.toString());
      }
      buf.append("}");
    }
    if (this.resultItem != null) {
      buf.append(",result:" + this.resultItem.toString());
    }
    if (this.achievementId != -1)
    {
      buf.append(",achievementTriggered{");
      buf.append(this.achievementName + "(" + this.achievementId + ")");
      buf.append("}");
    }
    buf.append("}");
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\Recipe.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */