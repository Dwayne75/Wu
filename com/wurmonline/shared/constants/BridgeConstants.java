package com.wurmonline.shared.constants;

import java.util.Locale;

public abstract interface BridgeConstants
{
  public static enum BridgeMaterial
  {
    ROPE((byte)1, "rope", "Rope", 621),  BRICK((byte)2, "stone", "Stone brick", 60, 9),  MARBLE((byte)3, "marble", "Marble brick", 60, 9),  WOOD((byte)4, "wood", "Wood", 60, 6),  SLATE((byte)5, "slate", "Slate brick", 60, 9),  ROUNDED_STONE((byte)6, "roundedstone", "Rounded stone", 60, 9),  POTTERY((byte)7, "pottery", "Pottery brick", 60, 9),  SANDSTONE((byte)8, "sandstone", "Sandstone brick", 60, 9),  RENDERED((byte)9, "rendered", "Rendered brick", 60, 9);
    
    private final byte material;
    private final String texture;
    private final int supportExtensionOffset;
    private final String name;
    private final int icon;
    private static final BridgeMaterial[] types = values();
    
    private BridgeMaterial(byte newMaterial, String newTexture, String newName, int newIcon, int newSupportExtensionOffset)
    {
      this.material = newMaterial;
      this.texture = newTexture;
      this.supportExtensionOffset = newSupportExtensionOffset;
      this.name = newName;
      this.icon = newIcon;
    }
    
    private BridgeMaterial(byte newMaterial, String newTexture, String newName, int newIcon)
    {
      this(newMaterial, newTexture, newName, newIcon, 0);
    }
    
    public byte getCode()
    {
      return this.material;
    }
    
    public String getTextureName()
    {
      return this.texture;
    }
    
    public final float getExtensionOffset()
    {
      return this.supportExtensionOffset;
    }
    
    private final int getIcon()
    {
      return this.icon;
    }
    
    public static BridgeMaterial fromByte(byte typeByte)
    {
      for (int i = 0; i < types.length; i++) {
        if (typeByte == types[i].getCode()) {
          return types[i];
        }
      }
      return null;
    }
    
    public final String getName()
    {
      return this.name;
    }
    
    public static final String getTextureName(BridgeConstants.BridgeType type, BridgeMaterial material)
    {
      return "img.texture.floor." + type.getTextureName() + "." + material.getTextureName().replace(" ", "");
    }
  }
  
  public static enum BridgeType
  {
    ABUTMENT_NARROW(0, "abutment.narrow", "abutment"),  BRACING_NARROW(1, "bracing.narrow", "bracing"),  CROWN_NARROW(2, "crown.narrow", "crown"),  DOUBLE_NARROW(3, "double.narrow", "double bracing"),  END_NARROW(4, "end.narrow", "double abutment"),  FLOATING_NARROW(5, "floating.narrow", "floating"),  SUPPORT_NARROW(6, "support.narrow", "support", "extension.narrow"),  ABUTMENT_CENTER(7, "abutment.center", "abutment"),  BRACING_CENTER(8, "bracing.center", "bracing"),  CROWN_CENTER(9, "crown.center", "crown"),  DOUBLE_CENTER(10, "double.center", "double bracing"),  END_CENTER(11, "end.center", "double abutment"),  FLOATING_CENTER(12, "floating.center", "floating"),  SUPPORT_CENTER(13, "support.center", "support", "extension.center"),  ABUTMENT_LEFT(14, "abutment.left", "abutment"),  ABUTMENT_RIGHT(15, "abutment.right", "abutment"),  BRACING_LEFT(16, "bracing.left", "bracing"),  BRACING_RIGHT(17, "bracing.right", "bracing"),  CROWN_SIDE(18, "crown.side", "crown"),  DOUBLE_SIDE(19, "double.side", "double bracing"),  END_SIDE(20, "end.side", "double abutment"),  FLOATING_SIDE(21, "floating.side", "floating"),  SUPPORT_SIDE(22, "support.side", "support", "extension.side");
    
    private final byte type;
    private final String texture;
    private final String extensionTexture;
    private final String name;
    private final boolean isNarrow;
    private final boolean isSide;
    private final boolean isLeft;
    private final boolean isRight;
    private final boolean isCenter;
    private final boolean isAbutment;
    private final boolean isBracing;
    private final boolean isCrown;
    private final boolean isFloating;
    private final boolean isEnd;
    private final boolean isDouble;
    
    private BridgeType(int newType, String newTexture, String newName, String newExtensionTexture)
    {
      this.type = ((byte)newType);
      this.texture = newTexture;
      this.extensionTexture = newExtensionTexture;
      this.name = newName;
      this.isNarrow = this.texture.contains(".narrow");
      this.isSide = this.texture.contains(".side");
      this.isLeft = this.texture.contains(".left");
      this.isRight = this.texture.contains(".right");
      this.isCenter = this.texture.contains(".center");
      this.isAbutment = this.texture.startsWith("abutment.");
      this.isBracing = this.texture.startsWith("bracing.");
      this.isCrown = this.texture.startsWith("crown.");
      this.isFloating = this.texture.startsWith("floating.");
      this.isEnd = this.texture.startsWith("end.");
      this.isDouble = this.texture.startsWith("double.");
    }
    
    private BridgeType(int newType, String newTexture, String newName)
    {
      this(newType, newTexture, newName, "");
    }
    
    public byte getCode()
    {
      return this.type;
    }
    
    private static final BridgeType[] types = values();
    
    public static BridgeType fromByte(byte typeByte)
    {
      for (int i = 0; i < types.length; i++) {
        if (typeByte == types[i].getCode()) {
          return types[i];
        }
      }
      return null;
    }
    
    public final String getTextureName()
    {
      return this.texture;
    }
    
    public final String getExtensionTextureName()
    {
      return this.extensionTexture;
    }
    
    public final String getName()
    {
      return this.name.toLowerCase(Locale.ENGLISH);
    }
    
    public final boolean isSupportType()
    {
      return this.extensionTexture.length() > 0;
    }
    
    public final boolean isNarrow()
    {
      return this.isNarrow;
    }
    
    public final boolean isSide()
    {
      return this.isSide;
    }
    
    public final boolean isLeft()
    {
      return this.isLeft;
    }
    
    public final boolean isRight()
    {
      return this.isRight;
    }
    
    public final int wallCount()
    {
      if (isNarrow()) {
        return 2;
      }
      if ((isLeft()) || (isRight()) || (isSide())) {
        return 1;
      }
      return 0;
    }
    
    public final boolean isCenter()
    {
      return this.isCenter;
    }
    
    public final boolean isAbutment()
    {
      return this.isAbutment;
    }
    
    public final boolean isBracing()
    {
      return this.isBracing;
    }
    
    public final boolean isCrown()
    {
      return this.isCrown;
    }
    
    public final boolean isFloating()
    {
      return this.isFloating;
    }
    
    public final boolean isDoubleAbutment()
    {
      return this.isEnd;
    }
    
    public final boolean isDoubleBracing()
    {
      return this.isDouble;
    }
    
    public static final String getModelName(BridgeType type, BridgeConstants.BridgeMaterial material, BridgeConstants.BridgeState state)
    {
      String plan = "";
      if (state == BridgeConstants.BridgeState.PLANNED) {
        plan = ".plan";
      }
      if (state.isBeingBuilt()) {
        plan = ".build";
      }
      String modelName = "model.structure.bridge" + plan + "." + type.getTextureName() + "." + material.getTextureName().replace(" ", "");
      return modelName;
    }
    
    public static final int getIconId(BridgeType type, BridgeConstants.BridgeMaterial material, BridgeConstants.BridgeState state)
    {
      switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeMaterial[material.ordinal()])
      {
      case 1: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
          return 440;
        case 4: 
        case 5: 
          return 441;
        case 6: 
        case 7: 
          return 442;
        }
        return 60;
      case 2: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
        case 8: 
          return 443;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          return 444;
        case 4: 
        case 5: 
        case 13: 
          return 445;
        case 14: 
        case 15: 
        case 16: 
          return 446;
        case 17: 
        case 18: 
        case 19: 
          return 447;
        case 20: 
        case 21: 
        case 22: 
          return 448;
        case 6: 
        case 7: 
        case 23: 
          return 449;
        }
        return 60;
      case 3: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
        case 8: 
          return 450;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          return 451;
        case 4: 
        case 5: 
        case 13: 
          return 452;
        case 14: 
        case 15: 
        case 16: 
          return 453;
        case 17: 
        case 18: 
        case 19: 
          return 454;
        case 20: 
        case 21: 
        case 22: 
          return 455;
        case 6: 
        case 7: 
        case 23: 
          return 456;
        }
        return 60;
      case 4: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
        case 8: 
          return 430;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          return 431;
        case 4: 
        case 5: 
        case 13: 
          return 432;
        case 14: 
        case 15: 
        case 16: 
          return 433;
        case 17: 
        case 18: 
        case 19: 
          return 434;
        case 20: 
        case 21: 
        case 22: 
          return 435;
        case 6: 
        case 7: 
        case 23: 
          return 436;
        }
        return 60;
      case 5: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
        case 8: 
          return 410;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          return 411;
        case 4: 
        case 5: 
        case 13: 
          return 412;
        case 14: 
        case 15: 
        case 16: 
          return 413;
        case 17: 
        case 18: 
        case 19: 
          return 414;
        case 20: 
        case 21: 
        case 22: 
          return 415;
        case 6: 
        case 7: 
        case 23: 
          return 416;
        }
        return 60;
      case 6: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
        case 8: 
          return 390;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          return 391;
        case 4: 
        case 5: 
        case 13: 
          return 392;
        case 14: 
        case 15: 
        case 16: 
          return 393;
        case 17: 
        case 18: 
        case 19: 
          return 394;
        case 20: 
        case 21: 
        case 22: 
          return 395;
        case 6: 
        case 7: 
        case 23: 
          return 396;
        }
        return 60;
      case 7: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
        case 8: 
          return 370;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          return 371;
        case 4: 
        case 5: 
        case 13: 
          return 372;
        case 14: 
        case 15: 
        case 16: 
          return 373;
        case 17: 
        case 18: 
        case 19: 
          return 374;
        case 20: 
        case 21: 
        case 22: 
          return 375;
        case 6: 
        case 7: 
        case 23: 
          return 376;
        }
        return 60;
      case 8: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
        case 2: 
        case 3: 
        case 8: 
          return 350;
        case 9: 
        case 10: 
        case 11: 
        case 12: 
          return 351;
        case 4: 
        case 5: 
        case 13: 
          return 352;
        case 14: 
        case 15: 
        case 16: 
          return 353;
        case 17: 
        case 18: 
        case 19: 
          return 354;
        case 20: 
        case 21: 
        case 22: 
          return 355;
        case 6: 
        case 7: 
        case 23: 
          return 356;
        }
        return 60;
      case 9: 
        switch (BridgeConstants.1.$SwitchMap$com$wurmonline$shared$constants$BridgeConstants$BridgeType[type.ordinal()])
        {
        case 1: 
          return 457;
        case 4: 
          return 458;
        case 17: 
          return 459;
        }
        return 60;
      }
      return 60;
    }
    
    public static final String getExtensionModelName(BridgeType type, BridgeConstants.BridgeMaterial material, BridgeConstants.BridgeState state)
    {
      String modelName = "";
      if (type.isSupportType())
      {
        String plan = "";
        if (state == BridgeConstants.BridgeState.PLANNED) {
          plan = ".plan";
        }
        if (state.isBeingBuilt()) {
          plan = ".build";
        }
        modelName = "model.structure.bridge" + plan + "." + type.getExtensionTextureName() + "." + material.getTextureName().replace(" ", "");
      }
      return modelName;
    }
  }
  
  public static enum BridgeState
  {
    PLANNED((byte)-1, false, ""),  STAGE1((byte)0, true, "first "),  STAGE2((byte)1, true, "second "),  STAGE3((byte)2, true, "third "),  STAGE4((byte)3, true, "fourth "),  STAGE5((byte)4, true, "fifth "),  STAGE6((byte)5, true, "sixth "),  STAGE7((byte)6, true, "seventh "),  COMPLETED((byte)Byte.MAX_VALUE, false, "");
    
    private byte state;
    private boolean beingBuilt;
    private String desc;
    private static final BridgeState[] types = values();
    
    private BridgeState(byte newState, boolean newBeingBuilt, String description)
    {
      this.state = newState;
      this.beingBuilt = newBeingBuilt;
      this.desc = description;
    }
    
    public byte getCode()
    {
      return this.state;
    }
    
    public boolean isBeingBuilt()
    {
      return this.beingBuilt;
    }
    
    public String getDescription()
    {
      return this.desc;
    }
    
    public static BridgeState fromByte(byte bridgeStateByte)
    {
      for (int i = 0; i < types.length; i++) {
        if (bridgeStateByte == types[i].getCode()) {
          return types[i];
        }
      }
      return PLANNED;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\constants\BridgeConstants.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */