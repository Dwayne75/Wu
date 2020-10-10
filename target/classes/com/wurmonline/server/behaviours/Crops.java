package com.wurmonline.server.behaviours;

import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.server.MiscConstants;

public final class Crops
  implements MiscConstants
{
  private final int number;
  private final String cropName;
  private final int templateId;
  private final String measure;
  private final int productId;
  private final double difficulty;
  static final int BARLEY = 0;
  static final int WHEAT = 1;
  static final int RYE = 2;
  static final int OAT = 3;
  static final int CORN = 4;
  static final int PUMPKIN = 5;
  static final int POTATO = 6;
  static final int COTTON = 7;
  static final int WEMP = 8;
  static final int GARLIC = 9;
  static final int ONION = 10;
  static final int REED = 11;
  static final int RICE = 12;
  static final int STRAWBERRIES = 13;
  static final int CARROTS = 14;
  static final int CABBAGE = 15;
  static final int TOMATOS = 16;
  static final int SUGAR_BEET = 17;
  static final int LETTUCE = 18;
  static final int PEAS = 19;
  static final int CUCUMBER = 20;
  private static final Crops[] cropTypes = { new Crops(0, "barley", 28, 28, "handfuls", 20.0D), new Crops(1, "wheat", 29, 29, "handfuls", 30.0D), new Crops(2, "rye", 30, 30, "handfuls", 10.0D), new Crops(3, "oat", 31, 31, "handfuls", 15.0D), new Crops(4, "corn", 32, 32, "stalks", 40.0D), new Crops(5, "pumpkin", 34, 33, "", 15.0D), new Crops(6, "potato", 35, 35, "", 4.0D), new Crops(7, "cotton", 145, 144, "bales", 7.0D), new Crops(8, "wemp", 317, 316, "bales", 10.0D), new Crops(9, "garlic", 356, 356, "bunch", 70.0D), new Crops(10, "onion", 355, 355, "bunch", 60.0D), new Crops(11, "reed", 744, 743, "bales", 20.0D), new Crops(12, "rice", 746, 746, "handfuls", 80.0D), new Crops(13, "strawberries", 750, 362, "handfuls", 60.0D), new Crops(14, "carrots", 1145, 1133, "handfuls", 25.0D), new Crops(15, "cabbage", 1146, 1134, "", 35.0D), new Crops(16, "tomatoes", 1147, 1135, "handfuls", 45.0D), new Crops(17, "sugar beet", 1148, 1136, "", 85.0D), new Crops(18, "lettuce", 1149, 1137, "", 55.0D), new Crops(19, "peas", 1150, 1138, "handfuls", 65.0D), new Crops(20, "cucumber", 1248, 1247, "", 15.0D) };
  
  private Crops(int aNumber, String aCropName, int aTemplateId, int aProductId, String aMeasure, double aDifficulty)
  {
    this.number = aNumber;
    this.cropName = aCropName;
    this.templateId = aTemplateId;
    this.productId = aProductId;
    this.measure = aMeasure;
    this.difficulty = aDifficulty;
  }
  
  String getCropName()
  {
    return this.cropName;
  }
  
  int getNumber()
  {
    return this.number;
  }
  
  int getTemplateId()
  {
    return this.templateId;
  }
  
  String getMeasure()
  {
    return this.measure;
  }
  
  int getProductId()
  {
    return this.productId;
  }
  
  double getDifficulty()
  {
    return this.difficulty;
  }
  
  public static final String getCropName(int cropNumber)
  {
    String cropString = "Unknown crop";
    for (int x = 0; x < cropTypes.length; x++) {
      if (cropTypes[x].getNumber() == cropNumber)
      {
        cropString = cropTypes[x].getCropName();
        return cropString;
      }
    }
    return cropString;
  }
  
  public static final byte getTileType(int cropNumber)
  {
    if (cropNumber < 16) {
      return Tiles.Tile.TILE_FIELD.id;
    }
    return Tiles.Tile.TILE_FIELD2.id;
  }
  
  static final int getItemTemplate(int cropNumber)
  {
    int itemTemplate = -10;
    for (int x = 0; x < cropTypes.length; x++) {
      if (cropTypes[x].getNumber() == cropNumber)
      {
        itemTemplate = cropTypes[x].getTemplateId();
        return itemTemplate;
      }
    }
    return itemTemplate;
  }
  
  static final int getProductTemplate(int cropNumber)
  {
    int productTemplate = -10;
    for (int x = 0; x < cropTypes.length; x++) {
      if (cropTypes[x].getNumber() == cropNumber)
      {
        productTemplate = cropTypes[x].getProductId();
        return productTemplate;
      }
    }
    return productTemplate;
  }
  
  static final int getNumber(int templateId)
  {
    int cropNumber = -10;
    for (int x = 0; x < cropTypes.length; x++) {
      if (cropTypes[x].getTemplateId() == templateId)
      {
        cropNumber = cropTypes[x].getNumber();
        return cropNumber;
      }
    }
    return cropNumber;
  }
  
  public static final int getCropNumber(byte tileType, byte tileData)
  {
    if (tileType == 7) {
      return tileData & 0xF;
    }
    return 16 + (tileData & 0xF);
  }
  
  public static final byte encodeFieldData(boolean tended, int fieldAge, int cropNumber)
  {
    return (byte)((tended ? 128 : 0) + (fieldAge << 4) + (cropNumber & 0xF));
  }
  
  public static final byte decodeFieldAge(byte data)
  {
    return (byte)(data >> 4 & 0x7);
  }
  
  public static final boolean decodeFieldState(byte data)
  {
    return (data & 0x80) != 0;
  }
  
  static final String getMeasure(int cropNumber)
  {
    String cropString = "";
    for (int x = 0; x < cropTypes.length; x++) {
      if (cropTypes[x].getNumber() == cropNumber)
      {
        cropString = cropTypes[x].getMeasure();
        return cropString;
      }
    }
    return cropString;
  }
  
  static final double getDifficultyFor(int cropNumber)
  {
    double diff = 10.0D;
    for (int x = 0; x < cropTypes.length; x++) {
      if (cropTypes[x].getNumber() == cropNumber)
      {
        diff = cropTypes[x].getDifficulty();
        return diff;
      }
    }
    return diff;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\behaviours\Crops.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */