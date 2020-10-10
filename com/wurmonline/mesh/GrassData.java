package com.wurmonline.mesh;

public final class GrassData
{
  public static enum GrowthSeason
  {
    WINTER,  SPRING,  SUMMER,  AUTUMN;
    
    private GrowthSeason() {}
  }
  
  public static enum GrowthStage
  {
    SHORT((byte)0),  MEDIUM((byte)1),  TALL((byte)2),  WILD((byte)3);
    
    private byte code;
    private static final int NUMBER_OF_STAGES = values().length;
    private static final GrowthStage[] stages = values();
    
    private GrowthStage(byte code)
    {
      this.code = code;
    }
    
    public byte getCode()
    {
      return this.code;
    }
    
    public byte getEncodedData()
    {
      return (byte)(this.code << 6 & 0xC0);
    }
    
    public static GrowthStage fromInt(int i)
    {
      return stages[i];
    }
    
    public static GrowthStage decodeTileData(int tileData)
    {
      return fromInt(tileData >> 6 & 0x3);
    }
    
    public static GrowthStage decodeTreeData(int tileData)
    {
      int len = Math.max((tileData & 0x3) - 1, 0);
      return fromInt(len);
    }
    
    public static short getYield(GrowthStage growthStage)
    {
      short yield;
      short yield;
      short yield;
      short yield;
      short yield;
      switch (GrassData.1.$SwitchMap$com$wurmonline$mesh$GrassData$GrowthStage[growthStage.ordinal()])
      {
      case 1: 
        yield = 0;
        break;
      case 2: 
        yield = 1;
        break;
      case 3: 
        yield = 2;
        break;
      case 4: 
        yield = 3;
        break;
      default: 
        yield = 0;
      }
      return yield;
    }
    
    public GrowthStage getNextStage()
    {
      int num = ordinal();
      num = Math.min(num + 1, NUMBER_OF_STAGES - 1);
      return fromInt(num);
    }
    
    public final boolean isMax()
    {
      return ordinal() >= NUMBER_OF_STAGES - 1;
    }
    
    public GrowthStage getPreviousStage()
    {
      int num = ordinal();
      num = Math.max(num - 1, 0);
      return fromInt(num);
    }
  }
  
  public static enum GrowthTreeStage
  {
    LAWN((byte)0),  SHORT((byte)1),  MEDIUM((byte)2),  TALL((byte)3);
    
    private byte code;
    private static final int NUMBER_OF_STAGES = values().length;
    private static final GrowthTreeStage[] stages = values();
    
    private GrowthTreeStage(byte code)
    {
      this.code = code;
    }
    
    public byte getCode()
    {
      return this.code;
    }
    
    public byte getEncodedData()
    {
      return (byte)(this.code & 0x3);
    }
    
    public static GrowthTreeStage fromInt(int i)
    {
      return stages[i];
    }
    
    public static GrowthTreeStage decodeTileData(int tileData)
    {
      return fromInt(tileData & 0x3);
    }
    
    public static short getYield(GrowthTreeStage growthStage)
    {
      short yield;
      short yield;
      short yield;
      short yield;
      switch (GrassData.1.$SwitchMap$com$wurmonline$mesh$GrassData$GrowthTreeStage[growthStage.ordinal()])
      {
      case 1: 
        yield = 0;
        break;
      case 2: 
        yield = 1;
        break;
      case 3: 
        yield = 2;
        break;
      default: 
        yield = 0;
      }
      return yield;
    }
    
    public GrowthTreeStage getNextStage()
    {
      int num = ordinal();
      num = Math.min(num + 1, NUMBER_OF_STAGES - 1);
      return fromInt(num);
    }
    
    public final boolean isMax()
    {
      return ordinal() >= NUMBER_OF_STAGES - 1;
    }
    
    public GrowthTreeStage getPreviousStage()
    {
      int num = ordinal();
      num = Math.max(num - 1, 1);
      return fromInt(num);
    }
  }
  
  public static enum GrassType
  {
    GRASS((byte)0),  REED((byte)1),  KELP((byte)2),  UNUSED((byte)3);
    
    private byte type;
    private static final GrassType[] types = values();
    
    private GrassType(byte type)
    {
      this.type = type;
    }
    
    public byte getType()
    {
      return this.type;
    }
    
    public byte getEncodedData()
    {
      return (byte)(this.type << 4 & 0x30);
    }
    
    public static GrassType fromInt(int i)
    {
      return types[i];
    }
    
    public static GrassType decodeTileData(int tile)
    {
      return fromInt(tile >> 4 & 0x3);
    }
    
    public String getName()
    {
      switch (GrassData.1.$SwitchMap$com$wurmonline$mesh$GrassData$GrassType[ordinal()])
      {
      case 1: 
        return "Grass";
      case 2: 
        return "Kelp";
      case 3: 
        return "Reed";
      }
      return "Unknown";
    }
    
    public int getGrowthRateInSeason(GrassData.GrowthSeason season)
    {
      switch (GrassData.1.$SwitchMap$com$wurmonline$mesh$GrassData$GrowthSeason[season.ordinal()])
      {
      case 1: 
        return 15;
      case 2: 
        return 40;
      case 3: 
        return 30;
      case 4: 
        return 20;
      }
      return 5;
    }
  }
  
  public static enum FlowerType
  {
    NONE((byte)0),  FLOWER_1((byte)1),  FLOWER_2((byte)2),  FLOWER_3((byte)3),  FLOWER_4((byte)4),  FLOWER_5((byte)5),  FLOWER_6((byte)6),  FLOWER_7((byte)7),  FLOWER_8((byte)8),  FLOWER_9((byte)9),  FLOWER_10((byte)10),  FLOWER_11((byte)11),  FLOWER_12((byte)12),  FLOWER_13((byte)13),  FLOWER_14((byte)14),  FLOWER_15((byte)15);
    
    private byte type;
    private static final FlowerType[] types = values();
    
    private FlowerType(byte type)
    {
      this.type = type;
    }
    
    public byte getType()
    {
      return this.type;
    }
    
    public byte getEncodedData()
    {
      return (byte)(this.type & 0xFF);
    }
    
    public static FlowerType fromInt(int i)
    {
      return types[i];
    }
    
    public static FlowerType decodeTileData(int tileData)
    {
      return fromInt(tileData & 0xF);
    }
    
    public String getDescription()
    {
      return GrassData.getFlowerName(this);
    }
  }
  
  private static String getFlowerName(FlowerType flowerType)
  {
    switch (flowerType)
    {
    case NONE: 
      return "";
    case FLOWER_1: 
      return "Yellow flowers";
    case FLOWER_2: 
      return "Orange-red flowers";
    case FLOWER_3: 
      return "Purple flowers";
    case FLOWER_4: 
      return "White flowers";
    case FLOWER_5: 
      return "Blue flowers";
    case FLOWER_6: 
      return "Greenish-yellow flowers";
    case FLOWER_7: 
      return "White-dotted flowers";
    }
    return "Unknown grass";
  }
  
  public static String getModelResourceName(FlowerType flowerType)
  {
    switch (flowerType)
    {
    }
    return "model.flower.unknown";
  }
  
  public static String getHelpSubject(int type)
  {
    return "Terrain:" + GrassType.values()[type].name().replace(' ', '_');
  }
  
  public static int getFlowerType(byte data)
  {
    return FlowerType.decodeTileData(data).getType() & 0xFFFF;
  }
  
  public static String getFlowerTypeName(byte data)
  {
    return getFlowerName(FlowerType.decodeTileData(data));
  }
  
  public static byte encodeGrassTileData(GrowthStage growthStage, GrassType grassType, FlowerType flowerType)
  {
    return (byte)(growthStage.getEncodedData() | grassType.getEncodedData() | flowerType.getEncodedData());
  }
  
  public static byte encodeGrassTileData(GrowthStage growthStage, FlowerType flowerType)
  {
    return (byte)(growthStage.getEncodedData() | flowerType.getEncodedData());
  }
  
  public static String getHover(byte data)
  {
    return GrassType.decodeTileData(data).getName();
  }
  
  public static int getGrowthRateFor(GrassType grassType, GrowthSeason season)
  {
    return grassType.getGrowthRateInSeason(season);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\mesh\GrassData.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */