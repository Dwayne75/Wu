package com.wurmonline.server.items;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.creatures.CreatureTemplate;
import com.wurmonline.server.creatures.CreatureTemplateFactory;
import com.wurmonline.server.creatures.NoSuchCreatureTemplateException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public class Ingredient
  implements MiscConstants
{
  private static final Logger logger = Logger.getLogger(Ingredient.class.getName());
  private ItemTemplate itemTemplate;
  private final boolean isResult;
  private String cstateName = "";
  private byte cstate = -1;
  private String pstateName = "";
  private byte pstate = -1;
  private int amount = 0;
  private int ratio = 0;
  private String materialName = "";
  private byte material = -1;
  private boolean hasRealTemplate = false;
  private ItemTemplate realItemTemplate = null;
  private String corpseDataName = "";
  private int corpseData = -1;
  private String materialRef = "";
  private String realTemplateRef = "";
  private int difficulty = 0;
  private int loss = 0;
  private String resultName = "";
  private String resultDescription = "";
  private boolean useResultTemplateWeight = false;
  private byte groupId;
  private byte ingredientId = -1;
  private int found = 0;
  private short icon = -1;
  
  public Ingredient(ItemTemplate itemTemplate, boolean isResult, byte groupId)
  {
    this.itemTemplate = itemTemplate;
    this.isResult = isResult;
    if (isResult) {
      this.difficulty = -100;
    }
    this.groupId = groupId;
  }
  
  public Ingredient(int templateId, byte cstate, byte pstate, byte material, boolean hasRealTemplate, int realTemplateId, int corpseType)
    throws NoSuchTemplateException
  {
    this.itemTemplate = ItemTemplateFactory.getInstance().getTemplate(templateId);
    this.isResult = false;
    this.groupId = 0;
    this.cstate = cstate;
    this.cstateName = generateCookingStateName(cstate);
    this.pstate = pstate;
    this.pstateName = generatePhysicalStateName(pstate);
    this.material = material;
    this.materialName = generateMaterialName();
    this.hasRealTemplate = hasRealTemplate;
    if (realTemplateId != -10L) {
      this.realItemTemplate = ItemTemplateFactory.getInstance().getTemplate(realTemplateId);
    }
    this.corpseData = corpseType;
    this.corpseDataName = generateCorpseName();
  }
  
  public Ingredient(DataInputStream dis)
    throws IOException, NoSuchTemplateException
  {
    this.ingredientId = dis.readByte();
    this.groupId = dis.readByte();
    
    short templateId = dis.readShort();
    this.itemTemplate = ItemTemplateFactory.getInstance().getTemplate(templateId);
    this.isResult = false;
    this.cstate = dis.readByte();
    this.cstateName = generateCookingStateName(this.cstate);
    this.pstate = dis.readByte();
    this.pstateName = generatePhysicalStateName(this.pstate);
    this.material = dis.readByte();
    this.materialName = generateMaterialName();
    if (templateId == 272)
    {
      this.corpseData = dis.readShort();
      this.corpseDataName = generateCorpseName();
    }
    else
    {
      this.hasRealTemplate = dis.readBoolean();
      short realItemTemplateId = dis.readShort();
      if (realItemTemplateId != -10L) {
        this.realItemTemplate = ItemTemplateFactory.getInstance().getTemplate(realItemTemplateId);
      }
    }
  }
  
  public void pack(DataOutputStream dos)
    throws IOException
  {
    dos.writeByte(this.ingredientId);
    dos.writeByte(this.groupId);
    dos.writeShort(this.itemTemplate.getTemplateId());
    dos.writeByte(this.cstate);
    dos.writeByte(this.pstate);
    dos.writeByte(this.material);
    if (this.itemTemplate.getTemplateId() == 272)
    {
      dos.writeShort(this.corpseData);
    }
    else
    {
      dos.writeBoolean(this.hasRealTemplate);
      dos.writeShort(getRealTemplateId());
    }
  }
  
  public byte getGroupId()
  {
    return this.groupId;
  }
  
  void setIngredientId(byte ingredientId)
  {
    this.ingredientId = ingredientId;
  }
  
  public byte getIngredientId()
  {
    return this.ingredientId;
  }
  
  public boolean matches(Item item)
  {
    if (((this.itemTemplate.isFoodGroup()) && (item.getTemplate().getFoodGroup() == this.itemTemplate.getTemplateId())) || 
      (getTemplateId() == item.getTemplateId())) {
      if (checkState(item)) {
        if (checkMaterial(item)) {
          if (checkRealTemplate(item)) {
            if (checkCorpseData(item)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }
  
  public Ingredient clone(@Nullable Item item)
  {
    Ingredient ingredient = new Ingredient(item != null ? item.getTemplate() : this.itemTemplate, this.isResult, this.groupId);
    ingredient.setIngredientId(this.ingredientId);
    if (item != null) {
      ingredient.setAmount(getAmount());
    }
    ingredient.setRatio(getRatio());
    ingredient.setLoss(getLoss());
    if (hasCState()) {
      ingredient.setCState(this.cstate, this.cstateName);
    }
    if (hasPState()) {
      ingredient.setPState(this.pstate, this.pstateName);
    }
    if (hasMaterial()) {
      ingredient.setMaterial(this.material, this.materialName);
    }
    if (hasRealTemplate()) {
      ingredient.setRealTemplate(this.realItemTemplate);
    }
    if (hasCorpseData()) {
      ingredient.setCorpseData(this.corpseData, this.corpseDataName);
    }
    return ingredient;
  }
  
  public void setResultName(String resultName)
  {
    this.resultName = resultName;
  }
  
  String getResultName()
  {
    return this.resultName;
  }
  
  public String getName(boolean withAmount)
  {
    StringBuilder buf = new StringBuilder();
    if (hasCState())
    {
      buf.append(this.cstateName);
      if ((hasPState()) && (this.pstateName.length() > 0)) {
        buf.append("+" + this.pstateName);
      }
      buf.append(" ");
    }
    else if ((hasPState()) && (this.pstateName.length() > 0))
    {
      buf.append(this.pstateName + " ");
    }
    if (hasCorpseData()) {
      buf.append(this.corpseDataName + " ");
    }
    buf.append(this.itemTemplate.getName());
    if (hasMaterial()) {
      buf.append(" (" + this.materialName + ")");
    }
    if ((!hasRealTemplate()) || (
    
      (withAmount) && (getAmount() > 1))) {
      if (getAmount() > 2) {
        buf.append(" (x3+)");
      } else {
        buf.append(" (x" + getAmount() + ")");
      }
    }
    return buf.toString().trim();
  }
  
  String getSubMenuName()
  {
    if (this.resultName.length() > 0) {
      return this.resultName;
    }
    if (hasCState())
    {
      StringBuilder buf = new StringBuilder();
      buf.append(this.cstateName);
      if (hasPState()) {
        buf.append("+" + this.pstateName);
      }
      buf.append(" ");
      buf.append(this.itemTemplate.getName());
      return buf.toString();
    }
    if (hasPState()) {
      return this.pstateName + " " + this.itemTemplate.getName();
    }
    return this.itemTemplate.getName();
  }
  
  String getCStateName()
  {
    return this.cstateName;
  }
  
  String getPStateName()
  {
    return this.pstateName;
  }
  
  void setUseResultTemplateWeight(boolean useTemplateWeight)
  {
    this.useResultTemplateWeight = useTemplateWeight;
  }
  
  public boolean useResultTemplateWeight()
  {
    return this.useResultTemplateWeight;
  }
  
  void setResultDescription(String description)
  {
    this.resultDescription = description;
  }
  
  boolean hasResultDescription()
  {
    return this.resultDescription.length() > 0;
  }
  
  String getResultDescription()
  {
    if (this.resultDescription.length() > 0) {
      return this.resultDescription;
    }
    return this.itemTemplate.getDescriptionLong();
  }
  
  public String getResultDescription(Item resultItem)
  {
    if (this.resultDescription.length() > 0)
    {
      String desc = this.resultDescription;
      if (desc.indexOf('#') >= 0) {
        if ((resultItem.getRealTemplateId() != -10) && (resultItem.getRealTemplate() != null)) {
          desc = desc.replace("#", resultItem.getRealTemplate().getName());
        } else {
          desc = desc.replace("# ", "").replace(" #", "");
        }
      }
      if (desc.indexOf('$') >= 0) {
        desc = desc.replace("$", generateMaterialName(resultItem.getMaterial()));
      }
      return desc;
    }
    return this.itemTemplate.getDescriptionLong();
  }
  
  public void setMaterialRef(String materialRef)
  {
    this.materialRef = materialRef;
  }
  
  public String getMaterialRef()
  {
    return this.materialRef;
  }
  
  public boolean hasMaterialRef()
  {
    return this.materialRef.length() > 0;
  }
  
  public void setRealTemplateRef(String realTemplateRef)
  {
    this.realTemplateRef = realTemplateRef;
  }
  
  public String getRealTemplateRef()
  {
    return this.realTemplateRef;
  }
  
  public boolean hasRealTemplateRef()
  {
    return this.realTemplateRef.length() > 0;
  }
  
  public void setCorpseData(int data)
  {
    this.corpseData = data;
    this.corpseDataName = generateCorpseName();
  }
  
  public void setCorpseData(int data, String dataName)
  {
    this.corpseData = data;
    this.corpseDataName = dataName;
  }
  
  public int getCorpseData()
  {
    return this.corpseData;
  }
  
  public boolean hasCorpseData()
  {
    return this.corpseData != -1;
  }
  
  public String getCorpseName()
  {
    return this.corpseDataName;
  }
  
  public void setCState(byte state)
  {
    this.cstate = state;
    this.cstateName = generateCookingStateName(this.cstate);
  }
  
  public void setCState(byte state, String stateName)
  {
    this.cstate = state;
    this.cstateName = generateCookingStateName(this.cstate);
  }
  
  public byte getCState()
  {
    return this.cstate;
  }
  
  public void setPState(byte state)
  {
    this.pstate = state;
    this.pstateName = generatePhysicalStateName(this.pstate);
  }
  
  public void setPState(byte state, String stateName)
  {
    this.pstate = state;
    this.pstateName = generatePhysicalStateName(this.pstate);
  }
  
  public byte getPState()
  {
    return this.pstate;
  }
  
  public boolean hasCState()
  {
    return this.cstate != -1;
  }
  
  public boolean hasPState()
  {
    return this.pstate != -1;
  }
  
  public byte getXState()
  {
    if (hasCState())
    {
      if (hasPState()) {
        return (byte)(getCState() + getPState());
      }
      return getCState();
    }
    return getPState();
  }
  
  public boolean hasXState()
  {
    return (this.cstate != -1) || (this.pstate != -1);
  }
  
  public boolean hasRealTemplate()
  {
    return this.hasRealTemplate;
  }
  
  public boolean hasRealTemplateId()
  {
    return this.realItemTemplate != null;
  }
  
  public void setAmount(int numb)
  {
    this.amount = numb;
  }
  
  public int getAmount()
  {
    if (this.amount == 0) {
      return 1;
    }
    return this.amount;
  }
  
  public void setRatio(int numb)
  {
    this.ratio = numb;
  }
  
  public int getRatio()
  {
    return this.ratio;
  }
  
  public void setMaterial(byte material)
  {
    this.material = material;
    this.materialName = generateMaterialName();
  }
  
  public void setMaterial(byte material, String materialName)
  {
    this.material = material;
    this.materialName = generateMaterialName();
  }
  
  public byte getMaterial()
  {
    return this.material;
  }
  
  public String getMaterialName()
  {
    return this.materialName;
  }
  
  public boolean hasMaterial()
  {
    return this.material != -1;
  }
  
  public void setDifficulty(int difficulty)
  {
    this.difficulty = difficulty;
  }
  
  public int getDifficulty()
  {
    return this.difficulty;
  }
  
  void setIcon(short icon)
  {
    this.icon = icon;
  }
  
  public short getIcon()
  {
    if (this.icon > -1) {
      return this.icon;
    }
    return this.itemTemplate.getImageNumber();
  }
  
  public void setLoss(int loss)
  {
    this.loss = Math.min(100, Math.max(0, loss));
  }
  
  public int getLoss()
  {
    return this.loss;
  }
  
  public void setTemplate(ItemTemplate itemTemplate)
  {
    this.itemTemplate = itemTemplate;
  }
  
  public void setRealTemplate(@Nullable ItemTemplate itemTemplate)
  {
    this.realItemTemplate = itemTemplate;
    this.hasRealTemplate = true;
  }
  
  @Nullable
  public ItemTemplate getRealItemTemplate()
  {
    return this.realItemTemplate;
  }
  
  public ItemTemplate getTemplate()
  {
    return this.itemTemplate;
  }
  
  public String getTemplateName()
  {
    return this.itemTemplate.getName();
  }
  
  public int getTemplateId()
  {
    return this.itemTemplate.getTemplateId();
  }
  
  public int getRealTemplateId()
  {
    if (this.realItemTemplate == null) {
      return -10;
    }
    return this.realItemTemplate.getTemplateId();
  }
  
  public boolean isLiquid()
  {
    return this.itemTemplate.isLiquid();
  }
  
  public boolean isDrinkable()
  {
    return this.itemTemplate.drinkable;
  }
  
  public boolean isFoodGroup()
  {
    return this.itemTemplate.isFoodGroup();
  }
  
  int setFound(boolean found)
  {
    if (found) {
      this.found += 1;
    } else {
      this.found = 0;
    }
    return this.difficulty;
  }
  
  boolean wasFound(boolean any, boolean optional)
  {
    if ((any) || (this.itemTemplate.isLiquid())) {
      return (this.found > 0) || (optional);
    }
    if ((this.amount >= 3) && (this.found >= 3)) {
      return true;
    }
    if (this.found == 0) {
      return optional;
    }
    return (this.amount == this.found) || ((this.amount == 0) && (this.found == 1));
  }
  
  public int getFound()
  {
    return this.found;
  }
  
  boolean checkFoodGroup(Item target)
  {
    if (isFoodGroup()) {
      return target.getTemplate().getFoodGroup() == getTemplateId();
    }
    return target.getTemplateId() == getTemplateId();
  }
  
  boolean checkState(Item target)
  {
    if ((hasCState()) || (hasPState())) {
      return target.isCorrectFoodState(getCState(), getPState());
    }
    return true;
  }
  
  boolean checkMaterial(Item target)
  {
    if (hasMaterial()) {
      return getMaterial() == target.getMaterial();
    }
    return true;
  }
  
  boolean checkCorpseData(Item target)
  {
    if (hasCorpseData()) {
      return (getCorpseData() == target.getData1()) && (!target.isButchered());
    }
    return true;
  }
  
  boolean checkRealTemplate(Item target)
  {
    if (hasRealTemplate())
    {
      if (getRealTemplateId() == target.getRealTemplateId()) {
        return true;
      }
      return (target.getRealTemplate() != null) && (target.getRealTemplate().getFoodGroup() == getRealTemplateId());
    }
    return true;
  }
  
  String generateCookingStateName(byte state)
  {
    StringBuilder builder = new StringBuilder();
    if (state != -1)
    {
      switch ((byte)(state & 0xF))
      {
      case 0: 
        if ((state & 0xF0) == 0) {
          builder.append("raw");
        }
        break;
      case 1: 
        builder.append("fried");
        break;
      case 2: 
        builder.append("grilled");
        break;
      case 3: 
        builder.append("boiled");
        break;
      case 4: 
        builder.append("roasted");
        break;
      case 5: 
        builder.append("steamed");
        break;
      case 6: 
        builder.append("baked");
        break;
      case 7: 
        builder.append("cooked");
        break;
      case 8: 
        builder.append("candied");
        break;
      case 9: 
        builder.append("chocolate coated");
      }
      if (state >= 16) {
        logger.info("Bad cooked state " + state + " for ingredient " + getName(true));
      }
    }
    return builder.toString();
  }
  
  String generatePhysicalStateName(byte state)
  {
    StringBuilder builder = new StringBuilder();
    if (state != -1)
    {
      if ((state & 0x10) != 0)
      {
        if (builder.length() > 0) {
          builder.append("+");
        }
        if ((this.itemTemplate.isHerb()) || (this.itemTemplate.isVegetable()) || (this.itemTemplate.isFish()) || (this.itemTemplate.isMushroom())) {
          builder.append("chopped");
        } else if (this.itemTemplate.isMeat()) {
          builder.append("diced");
        } else if (this.itemTemplate.isSpice()) {
          builder.append("ground");
        } else if (this.itemTemplate.canBeFermented()) {
          builder.append("unfermented");
        } else if (this.itemTemplate.getTemplateId() == 1249) {
          builder.append("whipped");
        } else {
          builder.append("zombified");
        }
      }
      if ((state & 0x20) != 0)
      {
        if (builder.length() > 0) {
          builder.append("+");
        }
        if (this.itemTemplate.isMeat()) {
          builder.append("minced");
        } else if (this.itemTemplate.isVegetable()) {
          builder.append("mashed");
        } else if (this.itemTemplate.canBeFermented()) {
          builder.append("fermenting");
        } else {
          builder.append("clotted");
        }
      }
      if ((state & 0x40) != 0)
      {
        if (builder.length() > 0) {
          builder.append("+");
        }
        if (this.itemTemplate.canBeDistilled()) {
          builder.append("undistilled");
        } else {
          builder.append("wrapped");
        }
      }
      if ((state & 0xFFFFFF80) != 0)
      {
        if (builder.length() > 0) {
          builder.append("+");
        }
        if (this.itemTemplate.isDish) {
          builder.append("salted");
        } else if ((this.itemTemplate.isHerb()) || (this.itemTemplate.isSpice())) {
          builder.append("fresh");
        }
      }
      if ((state < 16) && (state != 0)) {
        logger.info("Bad physical state " + state + " for ingredient " + getName(true));
      }
    }
    return builder.toString();
  }
  
  String generateMaterialName()
  {
    return generateMaterialName(this.material);
  }
  
  String generateMaterialName(byte mat)
  {
    if (mat != -1) {
      return Materials.convertMaterialByteIntoString(mat);
    }
    return "";
  }
  
  String generateCorpseName()
  {
    if (this.corpseData != -1) {
      try
      {
        CreatureTemplate ct = CreatureTemplateFactory.getInstance().getTemplate(this.corpseData);
        return ct.getName();
      }
      catch (NoSuchCreatureTemplateException e)
      {
        return "unknown";
      }
    }
    return "";
  }
  
  public String toString()
  {
    StringBuilder buf = new StringBuilder();
    buf.append("{name='" + getTemplateName() + "'(" + getTemplateId());
    if (isFoodGroup()) {
      buf.append("(isFoodGroup)");
    }
    buf.append(")");
    if (this.cstate != -1) {
      buf.append(",cstate='" + this.cstateName + "'(" + this.cstate + ")");
    }
    if (this.pstate != -1) {
      buf.append(",pstate='" + this.pstateName + "'(" + this.pstate + ")");
    }
    if (this.material != -1) {
      buf.append(",material='" + this.materialName + "'(" + this.material + ")");
    }
    if (this.isResult)
    {
      if (this.difficulty != -100) {
        buf.append(",baseDifficulty='" + this.difficulty);
      }
    }
    else if (this.difficulty != 0) {
      buf.append(",addDifficulty='" + this.difficulty);
    }
    if (isLiquid()) {
      buf.append(",ratio=" + this.ratio + "%");
    } else if (this.amount > 1) {
      buf.append(",need=" + this.amount);
    }
    if (this.corpseData != -1) {
      buf.append(",creature='" + this.corpseDataName + "'(" + this.corpseData + ")");
    }
    if (this.hasRealTemplate)
    {
      buf.append(",realTemplate='");
      if (this.realItemTemplate != null) {
        buf.append(this.realItemTemplate.getName());
      } else {
        buf.append("null");
      }
      buf.append("'(" + getRealTemplateId() + ")");
    }
    if (this.isResult)
    {
      if (this.materialRef.length() > 0) {
        buf.append(",materialRef='" + this.materialRef + "'");
      }
      if (this.realTemplateRef.length() > 0) {
        buf.append(",realTemplateRef='" + this.realTemplateRef + "'");
      }
      if (this.resultName.length() > 0) {
        buf.append(",resultName='" + this.resultName + "'");
      } else {
        buf.append(",resultName='" + getName(true) + "'");
      }
    }
    buf.append("}");
    return buf.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\Ingredient.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */