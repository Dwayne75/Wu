package com.wurmonline.server.loot;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.items.ItemTemplateFactory;
import com.wurmonline.server.items.NoSuchTemplateException;
import java.util.Optional;

public class LootItem
{
  private int itemTemplateId;
  private byte maxRarity;
  private byte minRarity;
  private float minQuality = 1.0F;
  private float maxQuality = 100.0F;
  private double itemChance = 1.0D;
  private ItemCreateFunc itemCreateFunc = new DefaultItemCreateFunc();
  
  public LootItem() {}
  
  public LootItem(int aItemTemplateId, byte aMinRarity, byte aMaxRarity, float aMinQuality, float aMaxQuality, double aItemChance, ItemCreateFunc aItemCreateFunc)
  {
    this(aItemTemplateId, aMinRarity, aMaxRarity, aMinQuality, aMaxQuality, aItemChance);
    this.itemCreateFunc = aItemCreateFunc;
  }
  
  public LootItem(int aItemTemplateId, byte aMinRarity, byte aMaxRarity, float aMinQuality, float aMaxQuality, double aItemChance)
  {
    this.itemTemplateId = aItemTemplateId;
    this.maxRarity = aMaxRarity;
    this.minRarity = aMinRarity;
    this.maxQuality = aMaxQuality;
    this.minQuality = aMinQuality;
    this.itemChance = aItemChance;
  }
  
  public byte getMaxRarity()
  {
    return this.maxRarity;
  }
  
  public LootItem setMaxRarity(byte aMaxRarity)
  {
    this.maxRarity = aMaxRarity;
    return this;
  }
  
  public byte getMinRarity()
  {
    return this.minRarity;
  }
  
  public LootItem setMinRarity(byte aMinRarity)
  {
    this.minRarity = aMinRarity;
    return this;
  }
  
  public float getMaxQuality()
  {
    return this.maxQuality;
  }
  
  public LootItem setMaxQuality(float aMaxQuality)
  {
    this.maxQuality = aMaxQuality;
    return this;
  }
  
  public float getMinQuality()
  {
    return this.minQuality;
  }
  
  public LootItem setMinQuality(float aMinQuality)
  {
    this.minQuality = aMinQuality;
    return this;
  }
  
  public double getItemChance()
  {
    return this.itemChance;
  }
  
  public LootItem setItemChance(double itemChance)
  {
    this.itemChance = itemChance;
    return this;
  }
  
  public LootItem setItemCreateFunc(ItemCreateFunc func)
  {
    this.itemCreateFunc = func;
    return this;
  }
  
  public Optional<Item> createItem(Creature victim, Creature receiver)
  {
    return this.itemCreateFunc.create(victim, receiver, this);
  }
  
  public int getItemTemplateId()
  {
    return this.itemTemplateId;
  }
  
  public LootItem setItemTemplateId(int id)
  {
    this.itemTemplateId = id;
    return this;
  }
  
  public String getItemName()
  {
    try
    {
      return ItemTemplateFactory.getInstance().getTemplate(getItemTemplateId()).getName();
    }
    catch (NoSuchTemplateException e)
    {
      e.printStackTrace();
    }
    return "<invalid template for id# " + getItemTemplateId() + ">";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\loot\LootItem.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */