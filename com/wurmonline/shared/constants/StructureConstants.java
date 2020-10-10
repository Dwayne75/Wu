package com.wurmonline.shared.constants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract interface StructureConstants
{
  public static final byte STRUCTURE_HOUSE = 0;
  public static final byte STRUCTURE_BRIDGE = 1;
  
  public static enum FloorMaterial
  {
    WOOD((byte)0, "Wood", "wood"),  STONE_BRICK((byte)1, "Stone brick", "stone_brick"),  SANDSTONE_SLAB((byte)2, "Sandstone slab", "sandstone_slab"),  SLATE_SLAB((byte)3, "Slate slab", "slate_slab"),  THATCH((byte)4, "Thatch", "thatch"),  METAL_IRON((byte)5, "Iron", "metal_iron"),  METAL_STEEL((byte)6, "Steel", "metal_steel"),  METAL_COPPER((byte)7, "Copper", "metal_copper"),  CLAY_BRICK((byte)8, "Clay brick", "clay_brick"),  METAL_GOLD((byte)9, "Gold", "metal_gold"),  METAL_SILVER((byte)10, "Silver", "metal_silver"),  MARBLE_SLAB((byte)11, "Marble slab", "marble_slab"),  STANDALONE((byte)12, "Standalone", "standalone"),  STONE_SLAB((byte)13, "Stone slab", "stone_slab");
    
    private byte material;
    private String name;
    private String modelName;
    
    private FloorMaterial(byte newMaterial, String newName, String newModelName)
    {
      this.material = newMaterial;
      this.name = newName;
      this.modelName = newModelName;
    }
    
    public byte getCode()
    {
      return this.material;
    }
    
    private static final FloorMaterial[] types = values();
    
    public static FloorMaterial fromByte(byte typeByte)
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
    
    public final String getModelName()
    {
      return this.modelName;
    }
    
    public static final String getTextureName(StructureConstants.FloorType type, FloorMaterial material)
    {
      return StructureConstants.FloorMappings.getMapping(type, material);
    }
  }
  
  public static enum FloorType
  {
    UNKNOWN((byte)100, false, "unknown"),  FLOOR((byte)10, false, "floor"),  DOOR((byte)11, false, "hatch"),  OPENING((byte)12, false, "opening"),  ROOF((byte)13, false, "roof"),  SOLID((byte)14, false, "solid"),  STAIRCASE((byte)15, true, "staircase"),  WIDE_STAIRCASE((byte)16, true, "staircase, wide"),  RIGHT_STAIRCASE((byte)17, true, "staircase, right"),  LEFT_STAIRCASE((byte)18, true, "staircase, left"),  WIDE_STAIRCASE_RIGHT((byte)19, true, "staircase, wide with right banisters"),  WIDE_STAIRCASE_LEFT((byte)20, true, "staircase, wide with left banisters"),  WIDE_STAIRCASE_BOTH((byte)21, true, "staircase, wide with both banisters"),  CLOCKWISE_STAIRCASE((byte)22, true, "staircase, clockwise spiral"),  ANTICLOCKWISE_STAIRCASE((byte)23, true, "staircase, counter clockwise spiral"),  CLOCKWISE_STAIRCASE_WITH((byte)24, true, "staircase, clockwise spiral with banisters"),  ANTICLOCKWISE_STAIRCASE_WITH((byte)25, true, "staircase, counter clockwise spiral with banisters");
    
    private byte type;
    private String name;
    private boolean isStair;
    
    private FloorType(byte newType, boolean newIsStair, String newName)
    {
      this.type = newType;
      this.name = newName;
      this.isStair = newIsStair;
    }
    
    public byte getCode()
    {
      return this.type;
    }
    
    public boolean isStair()
    {
      return this.isStair;
    }
    
    private static final FloorType[] types = values();
    
    public static FloorType fromByte(byte typeByte)
    {
      for (int i = 0; i < types.length; i++) {
        if (typeByte == types[i].getCode()) {
          return types[i];
        }
      }
      return UNKNOWN;
    }
    
    public final String getName()
    {
      return this.name;
    }
    
    public static final String getModelName(FloorType type, StructureConstants.FloorMaterial material, StructureConstants.FloorState state)
    {
      if (type == STAIRCASE)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == CLOCKWISE_STAIRCASE)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.clockwise.none.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.clockwise.none.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.clockwise.none." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == CLOCKWISE_STAIRCASE_WITH)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.clockwise.with.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.clockwise.with.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.clockwise.with." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == ANTICLOCKWISE_STAIRCASE)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.anticlockwise.none.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.anticlockwise.none.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.anticlockwise.none." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == ANTICLOCKWISE_STAIRCASE_WITH)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.anticlockwise.with.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.anticlockwise.with.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.anticlockwise.with." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == WIDE_STAIRCASE)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.wide.none.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.wide.none.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.wide.none." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == WIDE_STAIRCASE_LEFT)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.wide.left.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.wide.left.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.wide.left." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == WIDE_STAIRCASE_RIGHT)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.wide.right.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.wide.right.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.wide.right." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == WIDE_STAIRCASE_BOTH)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.wide.both.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.wide.both.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.wide.both." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == RIGHT_STAIRCASE)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.right.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.right.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.right." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == LEFT_STAIRCASE)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.staircase.left.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.staircase.left.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.staircase.left." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      if (type == OPENING)
      {
        if (state == StructureConstants.FloorState.PLANNING) {
          return "model.structure.floor.opening.plan";
        }
        if (state == StructureConstants.FloorState.BUILDING) {
          return "model.structure.floor.opening.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        return "model.structure.floor.opening." + material.toString().toLowerCase(Locale.ENGLISH);
      }
      String modelName;
      String modelName;
      if (state == StructureConstants.FloorState.PLANNING)
      {
        modelName = "model.structure.floor.plan";
      }
      else
      {
        String modelName;
        if (state == StructureConstants.FloorState.BUILDING)
        {
          modelName = "model.structure.floor.plan." + material.toString().toLowerCase(Locale.ENGLISH);
        }
        else
        {
          String modelName;
          if (type == ROOF) {
            modelName = "model.structure.roof." + material.toString().toLowerCase(Locale.ENGLISH);
          } else {
            modelName = "model.structure.floor." + material.toString().toLowerCase(Locale.ENGLISH);
          }
        }
      }
      if (type == UNKNOWN) {
        modelName = "model.structure.floor.plan";
      }
      return modelName;
    }
    
    public static final int getIconId(FloorType type, StructureConstants.FloorMaterial material, StructureConstants.FloorState state)
    {
      if ((state == StructureConstants.FloorState.PLANNING) || (state == StructureConstants.FloorState.BUILDING)) {
        return 60;
      }
      if (type == ROOF) {
        return getRoofIconId(material);
      }
      return getFloorIconId(material);
    }
    
    private static int getFloorIconId(StructureConstants.FloorMaterial material)
    {
      int returnId = 60;
      switch (StructureConstants.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstants$FloorMaterial[material.ordinal()])
      {
      case 1: 
        returnId = 60;
        break;
      case 2: 
        returnId = 60;
        break;
      case 3: 
        returnId = 60;
        break;
      case 4: 
        returnId = 60;
        break;
      case 5: 
        returnId = 60;
        break;
      case 6: 
        returnId = 60;
        break;
      case 7: 
        returnId = 60;
        break;
      case 8: 
        returnId = 60;
        break;
      case 9: 
        returnId = 60;
        break;
      case 10: 
        returnId = 60;
        break;
      case 11: 
        returnId = 60;
        break;
      case 12: 
        returnId = 60;
        break;
      case 13: 
        returnId = 60;
        break;
      case 14: 
        returnId = 60;
        break;
      default: 
        returnId = 60;
      }
      return returnId;
    }
    
    private static int getRoofIconId(StructureConstants.FloorMaterial material)
    {
      int returnId = 60;
      switch (StructureConstants.1.$SwitchMap$com$wurmonline$shared$constants$StructureConstants$FloorMaterial[material.ordinal()])
      {
      case 1: 
        returnId = 60;
        break;
      case 2: 
        returnId = 60;
        break;
      case 3: 
        returnId = 60;
        break;
      case 4: 
        returnId = 60;
        break;
      case 5: 
        returnId = 60;
        break;
      case 6: 
        returnId = 60;
        break;
      case 7: 
        returnId = 60;
        break;
      case 8: 
        returnId = 60;
        break;
      case 9: 
        returnId = 60;
        break;
      case 10: 
        returnId = 60;
        break;
      case 11: 
        returnId = 60;
        break;
      case 13: 
        returnId = 60;
        break;
      case 12: 
        returnId = 60;
        break;
      default: 
        returnId = 60;
      }
      return returnId;
    }
  }
  
  public static enum FloorState
  {
    PLANNING((byte)-1),  BUILDING((byte)0),  COMPLETED((byte)Byte.MAX_VALUE);
    
    private byte state;
    
    private FloorState(byte newState)
    {
      this.state = newState;
    }
    
    public byte getCode()
    {
      return this.state;
    }
    
    private static final FloorState[] types = values();
    
    public static FloorState fromByte(byte floorStateByte)
    {
      for (int i = 0; i < types.length; i++) {
        if (floorStateByte == types[i].getCode()) {
          return types[i];
        }
      }
      return BUILDING;
    }
  }
  
  public static class Pair<K, V>
  {
    private final K key;
    private final V value;
    
    public Pair(K key, V value)
    {
      this.key = key;
      this.value = value;
    }
    
    public final K getKey()
    {
      return (K)this.key;
    }
    
    public final V getValue()
    {
      return (V)this.value;
    }
    
    public int hashCode()
    {
      return this.key.hashCode() ^ this.value.hashCode();
    }
    
    public boolean equals(Object o)
    {
      if (o == null) {
        return false;
      }
      if (!(o instanceof Pair)) {
        return false;
      }
      Pair mapping = (Pair)o;
      return (this.key.equals(mapping.getKey())) && (this.value.equals(mapping.getValue()));
    }
  }
  
  public static final class FloorMappings
  {
    public static final Map<StructureConstants.Pair<StructureConstants.FloorType, StructureConstants.FloorMaterial>, String> mappings = new HashMap();
    
    static
    {
      for (StructureConstants.FloorType t : StructureConstants.FloorType.values()) {
        for (StructureConstants.FloorMaterial m : StructureConstants.FloorMaterial.values())
        {
          String mapping = "img.texture.floor." + t.toString().toLowerCase() + "." + m.toString().toLowerCase();
          StructureConstants.Pair<StructureConstants.FloorType, StructureConstants.FloorMaterial> p = new StructureConstants.Pair(t, m);
          mappings.put(p, mapping);
        }
      }
    }
    
    public static final String getMapping(StructureConstants.FloorType t, StructureConstants.FloorMaterial m)
    {
      StructureConstants.Pair<StructureConstants.FloorType, StructureConstants.FloorMaterial> p = new StructureConstants.Pair(t, m);
      return (String)mappings.get(p);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\constants\StructureConstants.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */