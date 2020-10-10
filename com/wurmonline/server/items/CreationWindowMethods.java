package com.wurmonline.server.items;

import com.wurmonline.communication.SocketConnection;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.GeneralUtilities;
import com.wurmonline.server.Items;
import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.NoSuchEntryException;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.BuildMaterial;
import com.wurmonline.server.behaviours.CaveWallBehaviour;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.Skills;
import com.wurmonline.server.structures.BridgePartEnum;
import com.wurmonline.server.structures.Fence;
import com.wurmonline.server.structures.RoofFloorEnum;
import com.wurmonline.server.structures.Structure;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.server.structures.WallEnum;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.shared.constants.BridgeConstants.BridgeMaterial;
import com.wurmonline.shared.constants.ProtoConstants;
import com.wurmonline.shared.constants.StructureConstants.FloorType;
import com.wurmonline.shared.constants.StructureConstantsEnum;
import com.wurmonline.shared.constants.WallConstants;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CreationWindowMethods
  implements ProtoConstants, MiscConstants
{
  private static final Logger logger = Logger.getLogger(CreationWindowMethods.class.getName());
  private static final String CHARSET_ENCODING_FOR_COMMS = "UTF-8";
  
  public static final boolean createWallBuildingBuffer(SocketConnection connection, @Nonnull Wall wall, @Nonnull Player player, long toolId)
  {
    Item tool = null;
    if (toolId != -10L)
    {
      Optional<Item> optTool = Items.getItemOptional(toolId);
      if (!optTool.isPresent()) {
        return false;
      }
      tool = (Item)optTool.get();
    }
    WallEnum wallEnum = WallEnum.WALL_PLAN;
    wallEnum = WallEnum.getWall(wall.getType(), wall.getMaterial());
    if (wallEnum == WallEnum.WALL_PLAN) {
      return false;
    }
    boolean sendNeededTool = (tool == null) || (!WallEnum.isCorrectTool(wallEnum, player, tool));
    
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)1);
    
    buffer.putShort((short)(sendNeededTool ? 2 : 1));
    if (sendNeededTool) {
      if (!addToolsNeededForWall(buffer, wallEnum, player))
      {
        connection.clearBuffer();
        return false;
      }
    }
    addStringToBuffer(buffer, "Item(s) needed in inventory", false);
    
    int[] needed = WallEnum.getMaterialsNeeded(wall);
    
    buffer.putShort((short)(needed.length / 2));
    for (int i = 0; i < needed.length; i += 2)
    {
      ItemTemplate template = getItemTemplate(needed[i]);
      if (template == null)
      {
        connection.clearBuffer();
        return false;
      }
      String name = getFenceMaterialName(template);
      addStringToBuffer(buffer, name, false);
      
      buffer.putShort(template.getImageNumber());
      
      short chance = (short)needed[(i + 1)];
      buffer.putShort(chance);
      
      buffer.putShort(wallEnum.getActionId());
    }
    return true;
  }
  
  private static boolean addToolsNeededForWall(ByteBuffer buffer, WallEnum wallEnum, @Nonnull Player player)
  {
    addStringToBuffer(buffer, "Needed tool in crafting window", false);
    
    List<Integer> list = WallEnum.getToolsForWall(wallEnum, player);
    
    buffer.putShort((short)list.size());
    for (Integer tid : list)
    {
      ItemTemplate template = getItemTemplate(tid.intValue());
      if (template == null) {
        return false;
      }
      String name = getFenceMaterialName(template);
      addStringToBuffer(buffer, name, false);
      
      buffer.putShort(template.getImageNumber());
      
      short chance = 1;
      buffer.putShort((short)1);
      buffer.putShort(wallEnum.getActionId());
    }
    return true;
  }
  
  private static final String getFenceMaterialName(ItemTemplate template)
  {
    if (template.getTemplateId() == 218) {
      return "small iron " + template.getName();
    }
    if (template.getTemplateId() == 217) {
      return "large iron " + template.getName();
    }
    return template.getName();
  }
  
  public static final boolean createWallPlanBuffer(SocketConnection connection, @Nonnull Structure structure, @Nonnull Wall wall, @Nonnull Player player, long toolId)
  {
    if (toolId == -10L) {
      return false;
    }
    Optional<Item> optTool = Items.getItemOptional(toolId);
    if (!optTool.isPresent()) {
      return false;
    }
    Item tool = (Item)optTool.get();
    
    List<WallEnum> wallList = WallEnum.getWallsByTool(player, tool, structure.needsDoor(), 
      MethodsStructure.hasInsideFence(wall));
    if (wallList.size() == 0) {
      return false;
    }
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)0);
    buffer.putShort((short)1);
    
    addStringToBuffer(buffer, "Walls", false);
    
    buffer.putShort((short)wallList.size());
    for (WallEnum en : wallList)
    {
      addStringToBuffer(buffer, en.getName(), false);
      
      buffer.putShort(en.getIcon());
      
      boolean canBuild = WallEnum.canBuildWall(wall, en.getMaterial(), player);
      short chance = (short)(canBuild ? 100 : 0);
      buffer.putShort(chance);
      
      buffer.putShort(en.getActionId());
    }
    return true;
  }
  
  public static final boolean createCaveCladdingBuffer(SocketConnection connection, int tilex, int tiley, int tile, byte type, Player player, long toolId)
  {
    Item tool = null;
    if (toolId != -10L)
    {
      Optional<Item> optTool = Items.getItemOptional(toolId);
      if (!optTool.isPresent()) {
        return false;
      }
      tool = (Item)optTool.get();
    }
    boolean sendNeededTool = (tool == null) || (!CaveWallBehaviour.isCorrectTool(type, player, tool));
    
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)1);
    
    buffer.putShort((short)(sendNeededTool ? 3 : 2));
    if (sendNeededTool) {
      if (!addToolsNeededForWall(buffer, type, player))
      {
        connection.clearBuffer();
        return false;
      }
    }
    addStringToBuffer(buffer, "Item(s) needed in inventory", false);
    
    short action = CaveWallBehaviour.actionFromWallType(type);
    int[] needed = CaveWallBehaviour.getMaterialsNeeded(tilex, tiley, type);
    
    buffer.putShort((short)(needed.length / 2));
    for (int i = 0; i < needed.length; i += 2)
    {
      ItemTemplate template = getItemTemplate(needed[i]);
      if (template == null)
      {
        connection.clearBuffer();
        return false;
      }
      String name = getFenceMaterialName(template);
      addStringToBuffer(buffer, name, false);
      
      buffer.putShort(template.getImageNumber());
      
      short chance = 1;
      buffer.putShort((short)1);
      
      buffer.putShort(action);
    }
    addStringToBuffer(buffer, "Total materials needed", false);
    if ((needed.length == 1) && (needed[0] == -1))
    {
      buffer.putShort((short)0);
    }
    else
    {
      buffer.putShort((short)(needed.length / 2));
      for (int i = 0; i < needed.length; i += 2)
      {
        ItemTemplate template = getItemTemplate(needed[i]);
        String name = getFenceMaterialName(template);
        addStringToBuffer(buffer, name, false);
        buffer.putShort(template.getImageNumber());
        short chance = (short)needed[(i + 1)];
        buffer.putShort(chance);
        buffer.putShort(action);
      }
    }
    return true;
  }
  
  private static boolean addToolsNeededForWall(ByteBuffer buffer, byte type, @Nonnull Player player)
  {
    addStringToBuffer(buffer, "Needed tool in crafting window", false);
    
    List<Integer> list = CaveWallBehaviour.getToolsForType(type, player);
    
    buffer.putShort((short)list.size());
    for (Integer tid : list)
    {
      ItemTemplate template = getItemTemplate(tid.intValue());
      if (template == null) {
        return false;
      }
      String name = getFenceMaterialName(template);
      addStringToBuffer(buffer, name, false);
      
      buffer.putShort(template.getImageNumber());
      
      short chance = 1;
      buffer.putShort((short)1);
      buffer.putShort(CaveWallBehaviour.actionFromWallType(type));
    }
    return true;
  }
  
  public static final boolean createCaveReinforcedBuffer(SocketConnection connection, Player player, long toolId)
  {
    if (toolId == -10L) {
      return false;
    }
    Optional<Item> optTool = Items.getItemOptional(toolId);
    if (!optTool.isPresent()) {
      return false;
    }
    Item tool = (Item)optTool.get();
    
    byte[] canMake = CaveWallBehaviour.getMaterialsFromToolType(player, tool);
    if (canMake.length == 0) {
      return false;
    }
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)0);
    buffer.putShort((short)1);
    
    addStringToBuffer(buffer, "CaveWalls", false);
    
    buffer.putShort((short)canMake.length);
    for (byte type : canMake)
    {
      Tiles.Tile theTile = Tiles.getTile(type);
      
      addStringToBuffer(buffer, theTile.getName(), false);
      
      buffer.putShort((short)theTile.getIconId());
      
      boolean canBuild = CaveWallBehaviour.canCladWall(type, player);
      short chance = (short)(canBuild ? 100 : 0);
      buffer.putShort(chance);
      
      buffer.putShort(CaveWallBehaviour.actionFromWallType(type));
    }
    return true;
  }
  
  public static final boolean createHedgeCreationBuffer(SocketConnection connection, @Nonnull Item sprout, long borderId, @Nonnull Player player)
  {
    StructureConstantsEnum hedgeType = Fence.getLowHedgeType(sprout.getMaterial());
    if (hedgeType == StructureConstantsEnum.FENCE_PLAN_WOODEN) {
      return false;
    }
    int x = Tiles.decodeTileX(borderId);
    int y = Tiles.decodeTileY(borderId);
    
    Tiles.TileBorderDirection dir = Tiles.decodeDirection(borderId);
    
    Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, true);
    if (structure != null) {
      return false;
    }
    if (!player.isOnSurface()) {
      return false;
    }
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)0);
    
    buffer.putShort((short)1);
    
    addStringToBuffer(buffer, "Hedges", false);
    
    buffer.putShort((short)1);
    
    String name = WallConstants.getName(hedgeType);
    
    addStringToBuffer(buffer, name, false);
    
    buffer.putShort((short)60);
    
    Skill gardening = player.getSkills().getSkillOrLearn(10045);
    
    short chance = (short)(int)gardening.getChance(1.0F + sprout.getDamage(), null, sprout.getQualityLevel());
    buffer.putShort(chance);
    
    buffer.putShort(com.wurmonline.server.behaviours.Actions.actionEntrys['º'].getNumber());
    
    return true;
  }
  
  public static final boolean createFlowerbedBuffer(SocketConnection connection, @Nonnull Item tool, long borderId, @Nonnull Player player)
  {
    StructureConstantsEnum flowerbedType = Fence.getFlowerbedType(tool.getTemplateId());
    
    int x = Tiles.decodeTileX(borderId);
    int y = Tiles.decodeTileY(borderId);
    
    Tiles.TileBorderDirection dir = Tiles.decodeDirection(borderId);
    
    Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, true);
    if (structure != null) {
      return false;
    }
    if (!player.isOnSurface()) {
      return false;
    }
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)0);
    
    buffer.putShort((short)1);
    
    addStringToBuffer(buffer, "Flowerbeds", false);
    
    buffer.putShort((short)1);
    
    String name = WallConstants.getName(flowerbedType);
    
    addStringToBuffer(buffer, name, false);
    
    buffer.putShort((short)60);
    
    Skill gardening = player.getSkills().getSkillOrLearn(10045);
    
    short chance = (short)(int)gardening.getChance(1.0F + tool.getDamage(), null, tool.getQualityLevel());
    
    buffer.putShort(chance);
    
    buffer.putShort(com.wurmonline.server.behaviours.Actions.actionEntrys['ȳ'].getNumber());
    
    return true;
  }
  
  public static final boolean createFenceListBuffer(SocketConnection connection, long borderId)
  {
    int x = Tiles.decodeTileX(borderId);
    int y = Tiles.decodeTileY(borderId);
    
    Tiles.TileBorderDirection dir = Tiles.decodeDirection(borderId);
    int heightOffset = Tiles.decodeHeightOffset(borderId);
    boolean onSurface = true;
    boolean hasArch = false;
    if (MethodsStructure.doesTileBorderContainWallOrFence(x, y, heightOffset, dir, true, false)) {
      hasArch = true;
    }
    Structure structure = MethodsStructure.getStructureOrNullAtTileBorder(x, y, dir, true);
    
    Map<String, List<ActionEntry>> fenceList = createFenceCreationList(structure != null, false, hasArch);
    if (Items.getMarker(x, y, true, 0, -10L) != null) {
      return false;
    }
    if ((dir == Tiles.TileBorderDirection.DIR_HORIZ) && (Items.getMarker(x + 1, y, true, 0, -10L) != null)) {
      return false;
    }
    if ((dir == Tiles.TileBorderDirection.DIR_DOWN) && (Items.getMarker(x, y + 1, true, 0, -10L) != null)) {
      return false;
    }
    if (fenceList.size() == 0) {
      return false;
    }
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)0);
    
    buffer.putShort((short)fenceList.size());
    for (String category : fenceList.keySet())
    {
      addStringToBuffer(buffer, category, false);
      
      List<ActionEntry> fences = (List)fenceList.get(category);
      
      buffer.putShort((short)fences.size());
      for (ActionEntry ae : fences)
      {
        StructureConstantsEnum type = Fence.getFencePlanType(ae.getNumber());
        
        String name = WallConstants.getName(Fence.getFenceForPlan(type));
        
        addStringToBuffer(buffer, name, false);
        
        buffer.putShort((short)60);
        
        short chance = 100;
        buffer.putShort((short)100);
        
        buffer.putShort(ae.getNumber());
      }
    }
    return true;
  }
  
  private static final Map<String, List<ActionEntry>> createFenceCreationList(boolean inStructure, boolean showAll, boolean borderHasArch)
  {
    Map<String, List<ActionEntry>> list = new HashMap();
    if ((!inStructure) || (showAll)) {
      list.put("Log", new ArrayList());
    }
    list.put("Plank", new ArrayList());
    list.put("Rope", new ArrayList());
    list.put("Shaft", new ArrayList());
    list.put("Woven", new ArrayList());
    list.put("Stone", new ArrayList());
    list.put("Iron", new ArrayList());
    list.put("Slate", new ArrayList());
    list.put("Rounded stone", new ArrayList());
    list.put("Pottery", new ArrayList());
    list.put("Sandstone", new ArrayList());
    list.put("Marble", new ArrayList());
    if ((!inStructure) || (showAll))
    {
      ((List)list.get("Log")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['¥']);
      ((List)list.get("Log")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['§']);
    }
    ((List)list.get("Plank")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['¦']);
    ((List)list.get("Plank")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['¨']);
    ((List)list.get("Plank")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ȉ']);
    ((List)list.get("Plank")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ȑ']);
    if (((inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Plank")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ȅ']);
    }
    ((List)list.get("Rope")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ȟ']);
    ((List)list.get("Rope")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ƞ']);
    
    ((List)list.get("Shaft")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ȏ']);
    ((List)list.get("Shaft")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ȏ']);
    ((List)list.get("Shaft")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ȑ']);
    
    ((List)list.get("Woven")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ǟ']);
    if ((!inStructure) || (showAll)) {
      ((List)list.get("Stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['£']);
    }
    if ((!inStructure) || (!borderHasArch) || (showAll)) {
      ((List)list.get("Stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['¤']);
    }
    if (((!inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ʎ']);
    }
    ((List)list.get("Stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ȝ']);
    ((List)list.get("Stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ȟ']);
    if (((inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ȅ']);
    }
    ((List)list.get("Iron")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ǝ']);
    ((List)list.get("Iron")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ǟ']);
    if ((!inStructure) || (!borderHasArch) || (showAll))
    {
      ((List)list.get("Iron")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ȡ']);
      ((List)list.get("Iron")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ȣ']);
    }
    ((List)list.get("Iron")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ɣ']);
    if ((inStructure) || (showAll)) {
      ((List)list.get("Iron")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ȉ']);
    }
    ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['̀']);
    ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['́']);
    ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͂']);
    if ((!inStructure) || (!borderHasArch) || (showAll)) {
      ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͦ']);
    }
    if (((!inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͧ']);
    }
    if ((!inStructure) || (!borderHasArch) || (showAll))
    {
      ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͨ']);
      ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͩ']);
    }
    if (((inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͪ']);
    }
    ((List)list.get("Slate")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͫ']);
    
    ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['̓']);
    ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['̈́']);
    ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͅ']);
    if ((!inStructure) || (!borderHasArch) || (showAll)) {
      ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͬ']);
    }
    if (((!inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͭ']);
    }
    if ((!inStructure) || (!borderHasArch) || (showAll))
    {
      ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͮ']);
      ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͯ']);
    }
    if (((inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ͱ']);
    }
    ((List)list.get("Rounded stone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͱ']);
    
    ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͆']);
    ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͇']);
    ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͈']);
    if ((!inStructure) || (!borderHasArch) || (showAll)) {
      ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys[';']);
    }
    if (((!inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ϳ']);
    }
    if ((!inStructure) || (!borderHasArch) || (showAll))
    {
      ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['΀']);
      ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['΁']);
    }
    if (((inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['΂']);
    }
    ((List)list.get("Pottery")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['΃']);
    
    ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͉']);
    ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͊']);
    ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͋']);
    if ((!inStructure) || (!borderHasArch) || (showAll)) {
      ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ͳ']);
    }
    if (((!inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͳ']);
    }
    if ((!inStructure) || (!borderHasArch) || (showAll))
    {
      ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ʹ']);
      ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͵']);
    }
    if (((inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ͷ']);
    }
    ((List)list.get("Sandstone")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['ͷ']);
    
    ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͌']);
    ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͍']);
    ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['͎']);
    if ((!inStructure) || (!borderHasArch) || (showAll)) {
      ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['΄']);
    }
    if (((!inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['΅']);
    }
    if ((!inStructure) || (!borderHasArch) || (showAll))
    {
      ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ά']);
      ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['·']);
    }
    if (((inStructure) && (!borderHasArch)) || (showAll)) {
      ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Έ']);
    }
    ((List)list.get("Marble")).add(com.wurmonline.server.behaviours.Actions.actionEntrys['Ή']);
    
    return list;
  }
  
  private static final List<ActionEntry> createCaveWallCreationList()
  {
    List<ActionEntry> list = new ArrayList();
    
    list.add(com.wurmonline.server.behaviours.Actions.actionEntrys['͘']);
    list.add(com.wurmonline.server.behaviours.Actions.actionEntrys['͙']);
    list.add(com.wurmonline.server.behaviours.Actions.actionEntrys['͚']);
    list.add(com.wurmonline.server.behaviours.Actions.actionEntrys['͛']);
    list.add(com.wurmonline.server.behaviours.Actions.actionEntrys['͜']);
    list.add(com.wurmonline.server.behaviours.Actions.actionEntrys['͝']);
    list.add(com.wurmonline.server.behaviours.Actions.actionEntrys['͞']);
    
    return list;
  }
  
  public static final boolean createCreationListBuffer(SocketConnection connection, @Nonnull Item source, @Nonnull Item target, @Nonnull Player player)
  {
    Map<String, Map<CreationEntry, Integer>> map = GeneralUtilities.getCreationList(source, target, player);
    ByteBuffer buffer;
    if (map.size() == 0)
    {
      Recipe recipe = Recipes.getRecipeFor(player.getWurmId(), (byte)2, source, target, true, false);
      if (recipe == null) {
        return false;
      }
      buffer = connection.getBuffer();
      addPartialRequestHeader(buffer);
      buffer.put((byte)0);
      buffer.putShort((short)1);
      addStringToBuffer(buffer, "Cooking", false);
      buffer.putShort((short)1);
      
      Item realSource = source;
      Item realTarget = target;
      if ((recipe.hasActiveItem()) && (source != null) && (recipe.getActiveItem().getTemplateId() != realSource.getTemplateId()))
      {
        realSource = target;
        realTarget = source;
      }
      ItemTemplate template = recipe.getResultTemplate(realTarget);
      if (template == null)
      {
        connection.clearBuffer();
        return false;
      }
      addStringToBuffer(buffer, recipe.getSubMenuName(realTarget), false);
      buffer.putShort(template.getImageNumber());
      buffer.putShort((short)(int)recipe.getChanceFor(realSource, realTarget, player));
      buffer.putShort(recipe.getMenuId());
      
      return true;
    }
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)0);
    
    buffer.putShort((short)map.size());
    for (String category : map.keySet())
    {
      addStringToBuffer(buffer, category, false);
      
      Map<CreationEntry, Integer> entries = (Map)map.get(category);
      
      buffer.putShort((short)entries.size());
      if (!addCreationEntriesToPartialList(buffer, entries))
      {
        connection.clearBuffer();
        return false;
      }
    }
    return true;
  }
  
  public static final boolean createUnfinishedCreationListBuffer(SocketConnection connection, @Nonnull Item source, @Nonnull Player player)
  {
    AdvancedCreationEntry entry = getAdvancedCreationEntry(source.getRealTemplateId());
    if (entry == null) {
      return false;
    }
    List<String> itemNames = new ArrayList();
    List<Integer> numberOfItemsNeeded = new ArrayList();
    List<Short> icons = new ArrayList();
    if (!fillRequirmentsLists(entry, source, itemNames, numberOfItemsNeeded, icons)) {
      return false;
    }
    ByteBuffer buffer = connection.getBuffer();
    
    addPartialRequestHeader(buffer);
    
    buffer.put((byte)1);
    
    buffer.putShort((short)1);
    String category = "Needed items";
    addStringToBuffer(buffer, "Needed items", false);
    
    buffer.putShort((short)numberOfItemsNeeded.size());
    for (int i = 0; i < numberOfItemsNeeded.size(); i++)
    {
      String itemName = (String)itemNames.get(i);
      addStringToBuffer(buffer, itemName, false);
      
      buffer.putShort(((Short)icons.get(i)).shortValue());
      
      short count = ((Integer)numberOfItemsNeeded.get(i)).shortValue();
      buffer.putShort(count);
      
      buffer.putShort((short)0);
    }
    return true;
  }
  
  private static final boolean fillRequirmentsLists(AdvancedCreationEntry entry, Item source, List<String> itemNames, List<Integer> numberOfItemsNeeded, List<Short> icons)
  {
    CreationRequirement[] requirements = entry.getRequirements();
    if (requirements.length == 0) {
      return false;
    }
    for (CreationRequirement requirement : requirements)
    {
      int remaining = requirement.getResourceNumber() - AdvancedCreationEntry.getStateForRequirement(requirement, source);
      if (remaining > 0)
      {
        int templateNeeded = requirement.getResourceTemplateId();
        ItemTemplate needed = getItemTemplate(templateNeeded);
        if (needed == null) {
          return false;
        }
        itemNames.add(buildTemplateName(needed, null, (byte)0));
        icons.add(Short.valueOf(needed.getImageNumber()));
        numberOfItemsNeeded.add(Integer.valueOf(remaining));
      }
    }
    return true;
  }
  
  private static final AdvancedCreationEntry getAdvancedCreationEntry(int id)
  {
    try
    {
      return CreationMatrix.getInstance().getAdvancedCreationEntry(id);
    }
    catch (NoSuchEntryException nse)
    {
      logger.log(Level.WARNING, "No advanced creation entry with id: " + id, nse);
    }
    return null;
  }
  
  private static final boolean addCreationEntriesToPartialList(ByteBuffer buffer, Map<CreationEntry, Integer> entries)
  {
    for (CreationEntry entry : entries.keySet())
    {
      ItemTemplate template = getItemTemplate(entry.getObjectCreated());
      if (template == null) {
        return false;
      }
      String entryName = buildTemplateName(template, entry, (byte)0);
      
      addStringToBuffer(buffer, entryName, false);
      
      buffer.putShort(template.getImageNumber());
      
      short chance = ((Integer)entries.get(entry)).shortValue();
      buffer.putShort(chance);
      
      buffer.putShort((short)(10000 + entry.getObjectCreated()));
    }
    return true;
  }
  
  private static final ItemTemplate getItemTemplate(int templateId)
  {
    try
    {
      return ItemTemplateFactory.getInstance().getTemplate(templateId);
    }
    catch (NoSuchTemplateException nst)
    {
      logger.log(Level.WARNING, "Unable to find item template with id: " + templateId, nst);
    }
    return null;
  }
  
  private static final String buildTemplateCaptionName(ItemTemplate toCreate, ItemTemplate source, ItemTemplate target)
  {
    String nameFormat = "%s %s";
    String materialFormat = "%s, %s";
    String name = toCreate.getName();
    String sourceMaterial = Item.getMaterialString(source.getMaterial());
    String targetMaterial = Item.getMaterialString(target.getMaterial());
    String createMaterial = Item.getMaterialString(toCreate.getMaterial());
    if (toCreate.sizeString.length() > 0) {
      name = StringUtil.format("%s %s", new Object[] { toCreate.sizeString.trim(), name });
    }
    if (toCreate.isMetal())
    {
      if ((!name.equals("lump")) && (!name.equals("sheet")))
      {
        if ((!source.isTool()) && (source.isMetal()) && (!sourceMaterial.equals("unknown"))) {
          name = StringUtil.format("%s, %s", new Object[] { name, sourceMaterial });
        } else if ((!target.isTool()) && (target.isMetal()) && (!targetMaterial.equals("unknown"))) {
          name = StringUtil.format("%s, %s", new Object[] { name, targetMaterial });
        }
      }
      else if (!createMaterial.equals("unknown")) {
        name = StringUtil.format("%s %s", new Object[] { createMaterial, name });
      }
    }
    else if (toCreate.isLiquidCooking())
    {
      if (target.isFood()) {
        name = StringUtil.format("%s, %s", new Object[] { name, target.getName() });
      }
    }
    else if (toCreate.getTemplateId() == 74)
    {
      if (!createMaterial.equals("unknown")) {
        name = StringUtil.format("%s %s", new Object[] { createMaterial, name });
      }
    }
    else if (toCreate.getTemplateId() == 891) {
      name = StringUtil.format("%s %s", new Object[] { "wooden", toCreate.getName() });
    } else if (toCreate.getTemplateId() == 404) {
      name = StringUtil.format("%s %s", new Object[] { "stone", toCreate.getName() });
    } else if (toCreate.isStone())
    {
      if ((name.equals("shards")) && (!createMaterial.equals("unknown"))) {
        name = StringUtil.format("%s %s", new Object[] { createMaterial, name });
      } else if (name.equals("altar")) {
        name = StringUtil.format("%s %s", new Object[] { "stone", name });
      } else if (toCreate.getTemplateId() == 593) {
        name = StringUtil.format("%s %s", new Object[] { "stone", name });
      }
    }
    else if (toCreate.getTemplateId() == 322)
    {
      if (name.equals("altar")) {
        name = StringUtil.format("%s %s", new Object[] { "wooden", name });
      }
    }
    else if (toCreate.getTemplateId() == 592) {
      name = StringUtil.format("%s %s", new Object[] { "plank", name });
    }
    return name;
  }
  
  private static final String buildTemplateName(ItemTemplate template, @Nullable CreationEntry entry, byte materialOverride)
  {
    String nameFormat = "%s %s";
    String materialFormat = "%s, %s";
    String name = template.getName();
    String material = Item.getMaterialString(template.getMaterial());
    if (template.sizeString.length() > 0) {
      name = StringUtil.format("%s %s", new Object[] { template.sizeString.trim(), name });
    }
    if ((template.isMetal()) && ((name.equals("lump")) || (name.equals("sheet")))) {
      name = StringUtil.format("%s %s", new Object[] { material, name });
    } else if (materialOverride != 0) {
      name = StringUtil.format("%s, %s", new Object[] { name, Materials.convertMaterialByteIntoString(materialOverride) });
    } else if (name.equals("barding"))
    {
      if (template.isCloth()) {
        name = StringUtil.format("%s %s", new Object[] { "cloth", name });
      } else if (template.isMetal()) {
        name = StringUtil.format("%s %s", new Object[] { "chain", name });
      } else {
        name = StringUtil.format("%s %s", new Object[] { material, name });
      }
    }
    else if (name.equals("rock")) {
      name = StringUtil.format("%s, %s", new Object[] { name, "iron" });
    } else if ((template.getTemplateId() == 216) || (template.getTemplateId() == 215)) {
      name = StringUtil.format("%s, %s", new Object[] { name, material });
    } else if (template.isStone())
    {
      if ((name.equals("shards")) && (!material.equals("unknown"))) {
        name = StringUtil.format("%s, %s", new Object[] { name, material });
      }
    }
    else if (name.equals("fur"))
    {
      if (entry != null) {
        if (entry.getObjectCreated() == 846) {
          name = "black bear fur";
        } else if (entry.getObjectCreated() == 847) {
          name = "brown bear fur";
        } else if (entry.getObjectCreated() == 849) {
          name = "black wolf fur";
        }
      }
    }
    else if (name.equals("pelt")) {
      if (entry != null) {
        if (entry.getObjectCreated() == 848) {
          name = "mountain lion pelt";
        }
      }
    }
    return name;
  }
  
  private static void addStringToBuffer(ByteBuffer buffer, String string, boolean shortLength)
  {
    byte[] bytes = getEncodedBytesFromString(string);
    if (!shortLength) {
      buffer.put((byte)bytes.length);
    } else {
      buffer.putShort((short)bytes.length);
    }
    buffer.put(bytes);
  }
  
  private static final byte[] getEncodedBytesFromString(String string)
  {
    try
    {
      return string.getBytes("UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return new byte[0];
  }
  
  private static void addPartialRequestHeader(ByteBuffer buffer)
  {
    buffer.put((byte)-46);
    buffer.put((byte)0);
  }
  
  public static boolean sendAllCraftingRecipes(SocketConnection connection, @Nonnull Player player)
  {
    RecipesListParameter params = new RecipesListParameter();
    short numberOfEntries = buildCreationsList(params);
    if (!sendCreationListCategories(connection, params, numberOfEntries))
    {
      player.setLink(false);
      return false;
    }
    if (!sendCreationRecipes(connection, player, params)) {
      return false;
    }
    if (!sendFenceRecipes(connection, player, params)) {
      return false;
    }
    if (!sendHedgeRecipes(connection, player, params)) {
      return false;
    }
    if (!sendFlowerbedRecipes(connection, player, params)) {
      return false;
    }
    if (!sendWallRecipes(connection, player, params)) {
      return false;
    }
    if (!sendRoofFloorRecipes(connection, player, params)) {
      return false;
    }
    if (!sendBridgePartRecipes(connection, player, params)) {
      return false;
    }
    if (!sendCaveWallRecipes(connection, player, params)) {
      return false;
    }
    return true;
  }
  
  private static final boolean sendRoofFloorRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getRoofs_floors().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<RoofFloorEnum> entries = (List)params.getRoofs_floors().get(category);
      for (RoofFloorEnum entry : entries)
      {
        int[] tools = RoofFloorEnum.getValidToolsForMaterial(entry.getMaterial());
        for (int tool : tools)
        {
          ByteBuffer buffer = connection.getBuffer();
          
          addCreationRecipesMessageHeaders(buffer);
          
          addCategoryIdToBuffer(params, category, buffer);
          
          addRoofFloorRecipeInfoToBuffer(entry, buffer);
          
          ItemTemplate toolTemplate = getItemTemplate(tool);
          if (toolTemplate == null)
          {
            logger.log(Level.WARNING, "sendRoofFlorRecipes() No item template found with id: " + tool);
            connection.clearBuffer();
            return false;
          }
          addRoofFloorToolInfoToBuffer(buffer, toolTemplate);
          
          addWallPlanInfoToBuffer(buffer, entry);
          if (!addAdditionalMaterialsForRoofsFloors(buffer, entry))
          {
            connection.clearBuffer();
            return false;
          }
          try
          {
            connection.flush();
          }
          catch (IOException ex)
          {
            logger.log(Level.WARNING, "Failed to flush floor|roof recipes!", ex);
            player.setLink(false);
            return false;
          }
        }
      }
    }
    String category;
    return true;
  }
  
  private static final boolean sendBridgePartRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getBridgeParts().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<BridgePartEnum> entries = (List)params.getBridgeParts().get(category);
      for (BridgePartEnum entry : entries)
      {
        int[] tools = BridgePartEnum.getValidToolsForMaterial(entry.getMaterial());
        for (int tool : tools)
        {
          ByteBuffer buffer = connection.getBuffer();
          
          addCreationRecipesMessageHeaders(buffer);
          
          addCategoryIdToBuffer(params, category, buffer);
          
          addBridgePartRecipeInfoToBuffer(entry, buffer);
          
          ItemTemplate toolTemplate = getItemTemplate(tool);
          if (toolTemplate == null)
          {
            logger.log(Level.WARNING, "sendRoofFlorRecipes() No item template found with id: " + tool);
            connection.clearBuffer();
            return false;
          }
          addRoofFloorToolInfoToBuffer(buffer, toolTemplate);
          
          buffer.putShort((short)60);
          addStringToBuffer(buffer, entry.getName() + " plan", true);
          if (!addTotalMaterialsForBridgeParts(buffer, entry))
          {
            connection.clearBuffer();
            return false;
          }
          try
          {
            connection.flush();
          }
          catch (IOException ex)
          {
            logger.log(Level.WARNING, "Failed to flush bridge part recipes!", ex);
            player.setLink(false);
            return false;
          }
        }
      }
    }
    String category;
    return true;
  }
  
  private static final boolean addAdditionalMaterialsForRoofsFloors(ByteBuffer buffer, RoofFloorEnum entry)
  {
    List<BuildMaterial> list = entry.getTotalMaterialsNeeded();
    buffer.putShort((short)list.size());
    for (BuildMaterial bMat : list)
    {
      ItemTemplate mat = getItemTemplate(bMat.getTemplateId());
      if (mat == null)
      {
        logger.log(Level.WARNING, "Unable to find item template with id: " + bMat.getTemplateId());
        return false;
      }
      buffer.putShort(mat.getImageNumber());
      
      addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
      
      buffer.putShort((short)bMat.getNeededQuantity());
    }
    return true;
  }
  
  private static final boolean addTotalMaterialsForBridgeParts(ByteBuffer buffer, BridgePartEnum entry)
  {
    List<BuildMaterial> list = entry.getTotalMaterialsNeeded();
    buffer.putShort((short)list.size());
    for (BuildMaterial bMat : list)
    {
      ItemTemplate mat = getItemTemplate(bMat.getTemplateId());
      if (mat == null)
      {
        logger.log(Level.WARNING, "Unable to find item template with id: " + bMat.getTemplateId());
        return false;
      }
      buffer.putShort(mat.getImageNumber());
      addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
      buffer.putShort((short)bMat.getNeededQuantity());
    }
    return true;
  }
  
  private static void addRoofFloorToolInfoToBuffer(ByteBuffer buffer, ItemTemplate toolTemplate)
  {
    buffer.putShort(toolTemplate.getImageNumber());
    
    addStringToBuffer(buffer, toolTemplate.getName(), true);
  }
  
  private static void addRoofFloorRecipeInfoToBuffer(RoofFloorEnum entry, ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    addStringToBuffer(buffer, entry.getName(), true);
    
    addStringToBuffer(buffer, SkillSystem.getNameFor(entry.getNeededSkillNumber()), true);
  }
  
  private static void addBridgePartRecipeInfoToBuffer(BridgePartEnum entry, ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    addStringToBuffer(buffer, entry.getName(), true);
    
    addStringToBuffer(buffer, SkillSystem.getNameFor(entry.getNeededSkillNumber()), true);
  }
  
  private static final boolean sendWallRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getWalls().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<WallEnum> entries = (List)params.getWalls().get(category);
      for (localIterator2 = entries.iterator(); localIterator2.hasNext();)
      {
        entry = (WallEnum)localIterator2.next();
        
        List<Integer> tools = WallEnum.getToolsForWall(entry, null);
        for (Integer tool : tools)
        {
          ByteBuffer buffer = connection.getBuffer();
          
          addCreationRecipesMessageHeaders(buffer);
          
          addCategoryIdToBuffer(params, category, buffer);
          
          addWallInfoToBuffer(entry, buffer);
          
          ItemTemplate toolTemplate = getItemTemplate(tool.intValue());
          if (toolTemplate == null)
          {
            connection.clearBuffer();
            logger.log(Level.WARNING, "Unable to find tool with id: " + tool.intValue());
            return false;
          }
          addWallToolIInfoToBuffer(buffer, toolTemplate);
          
          addWallPlanInfoToBuffer(buffer);
          if (!addAdditionalMaterialsForWall(buffer, entry))
          {
            connection.clearBuffer();
            return false;
          }
          try
          {
            connection.flush();
          }
          catch (IOException iex)
          {
            logger.log(Level.WARNING, "Failed to flush well recipe", iex);
            player.setLink(false);
            return false;
          }
        }
      }
    }
    String category;
    Iterator localIterator2;
    WallEnum entry;
    return true;
  }
  
  private static final boolean addAdditionalMaterialsForWall(ByteBuffer buffer, WallEnum entry)
  {
    int[] needed = entry.getTotalMaterialsNeeded();
    
    buffer.putShort((short)(needed.length / 2));
    for (int i = 0; i < needed.length; i += 2)
    {
      ItemTemplate mat = getItemTemplate(needed[i]);
      if (mat == null) {
        return false;
      }
      buffer.putShort(mat.getImageNumber());
      addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
      
      buffer.putShort((short)needed[(i + 1)]);
    }
    return true;
  }
  
  private static void addWallPlanInfoToBuffer(ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    addStringToBuffer(buffer, WallEnum.WALL_PLAN.getName(), true);
  }
  
  private static void addWallPlanInfoToBuffer(ByteBuffer buffer, RoofFloorEnum entry)
  {
    buffer.putShort((short)60);
    
    String planString = entry.isFloor() ? "planned floor" : "planned roof";
    
    addStringToBuffer(buffer, planString, true);
  }
  
  private static void addWallToolIInfoToBuffer(ByteBuffer buffer, ItemTemplate toolTemplate)
  {
    buffer.putShort(toolTemplate.getImageNumber());
    
    addStringToBuffer(buffer, toolTemplate.getName(), true);
  }
  
  private static void addWallInfoToBuffer(WallEnum entry, ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    addStringToBuffer(buffer, entry.getName(), true);
    
    addStringToBuffer(buffer, 
      SkillSystem.getNameFor(WallEnum.getSkillNumber(entry.getMaterial())), true);
  }
  
  private static final boolean sendFlowerbedRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getFlowerbeds().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<Short> entries = (List)params.getFlowerbeds().get(category);
      for (localIterator2 = entries.iterator(); localIterator2.hasNext();)
      {
        short entry = ((Short)localIterator2.next()).shortValue();
        
        short bedType = entry;
        String name = WallConstants.getName(StructureConstantsEnum.getEnumByValue(bedType));
        int flowerType = Fence.getFlowerTypeByFlowerbedType(StructureConstantsEnum.getEnumByValue(bedType));
        
        ByteBuffer buffer = connection.getBuffer();
        
        addCreationRecipesMessageHeaders(buffer);
        
        addCategoryIdToBuffer(params, category, buffer);
        
        addFlowerbedInfoToBuffer(name, buffer);
        
        ItemTemplate flower = getItemTemplate(flowerType);
        if (flower == null)
        {
          connection.clearBuffer();
          return false;
        }
        addWallToolIInfoToBuffer(buffer, flower);
        
        addTileBorderToBuffer(buffer);
        if (!addAdditionalMaterialsForFlowerbed(buffer, flower))
        {
          connection.clearBuffer();
          return false;
        }
        try
        {
          connection.flush();
        }
        catch (IOException ex)
        {
          logger.log(Level.WARNING, "IO Exception when sending flowerbed recipes.", ex);
          player.setLink(false);
          return false;
        }
      }
    }
    String category;
    Iterator localIterator2;
    return true;
  }
  
  private static final boolean addAdditionalMaterialsForFlowerbed(ByteBuffer buffer, ItemTemplate flower)
  {
    int[] needed = { flower.getTemplateId(), 4, 22, 3, 218, 1, 26, 1 };
    
    buffer.putShort((short)(needed.length / 2));
    for (int i = 0; i < needed.length; i += 2)
    {
      ItemTemplate mat = null;
      if (needed[i] == flower.getTemplateId())
      {
        mat = flower;
      }
      else
      {
        mat = getItemTemplate(needed[i]);
        if (mat == null) {
          return false;
        }
      }
      buffer.putShort((short)60);
      addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
      buffer.putShort((short)needed[(i + 1)]);
    }
    return true;
  }
  
  private static void addFlowerbedInfoToBuffer(String name, ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    addStringToBuffer(buffer, name, true);
    
    addStringToBuffer(buffer, SkillSystem.getNameFor(10045), true);
  }
  
  private static final boolean sendHedgeRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getHedges().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<Short> entries = (List)params.getHedges().get(category);
      for (localIterator2 = entries.iterator(); localIterator2.hasNext();)
      {
        short entry = ((Short)localIterator2.next()).shortValue();
        
        short hedgeType = entry;
        
        ByteBuffer buffer = connection.getBuffer();
        
        addCreationRecipesMessageHeaders(buffer);
        
        addCategoryIdToBuffer(params, category, buffer);
        
        addHedgeInfoToBuffer(StructureConstantsEnum.getEnumByValue(hedgeType), buffer);
        
        ItemTemplate sprout = getItemTemplate(266);
        if (sprout == null)
        {
          connection.clearBuffer();
          return false;
        }
        byte materialType = Fence.getMaterialForLowHedge(StructureConstantsEnum.getEnumByValue(hedgeType));
        String materialString = Item.getMaterialString(materialType);
        
        addSproutInfoToBuffer(sprout, materialString, buffer);
        
        addTileBorderToBuffer(buffer);
        
        addAdditionalMaterialsForHedge(buffer, sprout, materialString);
        try
        {
          connection.flush();
        }
        catch (IOException ex)
        {
          logger.log(Level.WARNING, "IO Exception when sending hedge recipes.", ex);
          player.setLink(false);
          return false;
        }
      }
    }
    String category;
    Iterator localIterator2;
    return true;
  }
  
  private static final void addAdditionalMaterialsForHedge(ByteBuffer buffer, ItemTemplate template, String material)
  {
    buffer.putShort((short)1);
    
    buffer.putShort(template.getImageNumber());
    addStringToBuffer(buffer, StringUtil.format("%s, %s", new Object[] { template.getName(), material }), true);
    buffer.putShort((short)4);
  }
  
  private static void addSproutInfoToBuffer(ItemTemplate sprout, String material, ByteBuffer buffer)
  {
    buffer.putShort(sprout.getImageNumber());
    
    String sproutName = StringUtil.format("%s, %s", new Object[] { sprout.getName(), material });
    
    addStringToBuffer(buffer, sproutName, true);
  }
  
  private static void addHedgeInfoToBuffer(StructureConstantsEnum hedgeType, ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    addStringToBuffer(buffer, WallConstants.getName(hedgeType), true);
    
    addStringToBuffer(buffer, SkillSystem.getNameFor(10045), true);
  }
  
  private static final boolean sendFenceRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getFences().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<ActionEntry> entries = (List)params.getFences().get(category);
      for (ActionEntry entry : entries)
      {
        StructureConstantsEnum originalFenceType = Fence.getFencePlanType(entry.getNumber());
        
        StructureConstantsEnum fenceType = Fence.getFenceForPlan(originalFenceType);
        
        int[] correctTools = MethodsStructure.getCorrectToolsForBuildingFences();
        for (int i = 0; i < correctTools.length; i++)
        {
          ByteBuffer buffer = connection.getBuffer();
          
          addCreationRecipesMessageHeaders(buffer);
          
          addCategoryIdToBuffer(params, category, buffer);
          
          addCreatedFenceToBuffer(originalFenceType, fenceType, buffer);
          if (!addFenceToolToBuffer(buffer, correctTools[i]))
          {
            connection.clearBuffer();
            return false;
          }
          addTileBorderToBuffer(buffer);
          if (!addAdditionalMaterialsForFence(buffer, originalFenceType))
          {
            connection.clearBuffer();
            return false;
          }
          try
          {
            connection.flush();
          }
          catch (IOException ex)
          {
            logger.log(Level.WARNING, "IO Exception when sending fence recipes.", ex);
            player.setLink(false);
            return false;
          }
        }
      }
    }
    String category;
    return true;
  }
  
  private static final boolean addAdditionalMaterialsForFence(ByteBuffer buffer, StructureConstantsEnum fence)
  {
    int[] items = Fence.getItemTemplatesNeededForFenceTotal(fence);
    if (items.length < 2)
    {
      buffer.putShort((short)0);
    }
    else
    {
      buffer.putShort((short)(items.length / 2));
      for (int i = 0; i < items.length; i += 2)
      {
        ItemTemplate mat = getItemTemplate(items[i]);
        if (mat == null) {
          return false;
        }
        buffer.putShort(mat.getImageNumber());
        
        addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
        
        buffer.putShort((short)items[(i + 1)]);
      }
    }
    return true;
  }
  
  private static void addTileBorderToBuffer(ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    addStringToBuffer(buffer, "Tile Border", true);
  }
  
  private static boolean addFenceToolToBuffer(ByteBuffer buffer, int toolId)
  {
    ItemTemplate toolTemplate = getItemTemplate(toolId);
    if (toolTemplate == null)
    {
      logger.log(Level.WARNING, "Unable to find tool template with id: " + toolId);
      return false;
    }
    buffer.putShort(toolTemplate.imageNumber);
    
    addStringToBuffer(buffer, toolTemplate.getName(), true);
    
    return true;
  }
  
  private static void addCreatedFenceToBuffer(StructureConstantsEnum originalFenceType, StructureConstantsEnum fenceType, ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    String fenceName = WallConstants.getName(fenceType);
    addStringToBuffer(buffer, fenceName, true);
    
    int skillNumber = Fence.getSkillNumberNeededForFence(originalFenceType);
    addStringToBuffer(buffer, SkillSystem.getNameFor(skillNumber), true);
  }
  
  private static void addReinforcedWallToBuffer(ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    addStringToBuffer(buffer, Tiles.Tile.TILE_CAVE_WALL_REINFORCED.getName(), true);
  }
  
  private static void addCreatedReinforcedWallToBuffer(byte partCladType, byte cladType, ByteBuffer buffer)
  {
    buffer.putShort((short)60);
    
    String fenceName = Tiles.getTile(cladType).getName();
    addStringToBuffer(buffer, fenceName, true);
    
    int skillNumber = CaveWallBehaviour.getSkillNumberNeededForCladding((short)partCladType);
    addStringToBuffer(buffer, SkillSystem.getNameFor(skillNumber), true);
  }
  
  private static final boolean addAdditionalMaterialsForReinforcedWall(ByteBuffer buffer, short action)
  {
    int[] items = CaveWallBehaviour.getMaterialsNeededTotal(action);
    if (items.length < 2)
    {
      buffer.putShort((short)0);
    }
    else
    {
      buffer.putShort((short)(items.length / 2));
      for (int i = 0; i < items.length; i += 2)
      {
        ItemTemplate mat = getItemTemplate(items[i]);
        if (mat == null) {
          return false;
        }
        buffer.putShort(mat.getImageNumber());
        
        addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
        
        buffer.putShort((short)items[(i + 1)]);
      }
    }
    return true;
  }
  
  private static final boolean sendCreationRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getCreationEntries().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<CreationEntry> entries = (List)params.getCreationEntries().get(category);
      for (CreationEntry entry : entries)
      {
        ByteBuffer buffer = connection.getBuffer();
        
        addCreationRecipesMessageHeaders(buffer);
        
        addCategoryIdToBuffer(params, category, buffer);
        
        ItemTemplate created = getItemTemplate(entry.getObjectCreated());
        ItemTemplate source = getItemTemplate(entry.getObjectSource());
        ItemTemplate target = getItemTemplate(entry.getObjectTarget());
        if ((created == null) || (source == null) || (target == null))
        {
          connection.clearBuffer();
          return false;
        }
        addItemCreatedToRecipesBuffer(entry, buffer, created, source, target);
        
        addInitialItemUsedToRecipesBuffer(entry, buffer, source, entry.getObjectSourceMaterial());
        
        addInitialItemUsedToRecipesBuffer(entry, buffer, target, entry.getObjectTargetMaterial());
        if (!addAditionalMaterialsForAdvancedEntries(buffer, entry))
        {
          connection.clearBuffer();
          return false;
        }
        try
        {
          connection.flush();
        }
        catch (IOException iex)
        {
          logger.log(Level.WARNING, "Failed to send creation entries to recipes list", iex);
          player.setLink(false);
          return false;
        }
      }
    }
    String category;
    return true;
  }
  
  private static final boolean addAditionalMaterialsForAdvancedEntries(ByteBuffer buffer, CreationEntry entry)
  {
    if ((entry instanceof AdvancedCreationEntry))
    {
      AdvancedCreationEntry adv = (AdvancedCreationEntry)entry;
      
      CreationRequirement[] reqs = adv.getRequirements();
      
      buffer.putShort((short)reqs.length);
      for (CreationRequirement req : reqs)
      {
        int id = req.getResourceTemplateId();
        ItemTemplate mat = getItemTemplate(id);
        if (mat == null) {
          return false;
        }
        buffer.putShort(mat.getImageNumber());
        
        addStringToBuffer(buffer, buildTemplateName(mat, null, (byte)0), true);
        
        buffer.putShort((short)req.getResourceNumber());
      }
    }
    else
    {
      buffer.putShort((short)0);
    }
    return true;
  }
  
  private static void addInitialItemUsedToRecipesBuffer(CreationEntry entry, ByteBuffer buffer, ItemTemplate item, byte materialOverride)
  {
    buffer.putShort(item.getImageNumber());
    
    addStringToBuffer(buffer, buildTemplateName(item, entry, materialOverride), true);
  }
  
  private static void addItemCreatedToRecipesBuffer(CreationEntry entry, ByteBuffer buffer, ItemTemplate created, ItemTemplate source, ItemTemplate target)
  {
    buffer.putShort(created.getImageNumber());
    
    addStringToBuffer(buffer, buildTemplateCaptionName(created, source, target), true);
    
    String skillName = SkillSystem.getNameFor(entry.getPrimarySkill());
    addStringToBuffer(buffer, skillName, true);
  }
  
  private static void addCategoryIdToBuffer(RecipesListParameter params, String category, ByteBuffer buffer)
  {
    buffer.putShort(((Integer)params.getCategoryIds().get(category)).shortValue());
  }
  
  private static void addCreationRecipesMessageHeaders(ByteBuffer buffer)
  {
    buffer.put((byte)-46);
    buffer.put((byte)3);
  }
  
  private static final boolean sendCreationListCategories(SocketConnection connection, RecipesListParameter params, short numberOfEntries)
  {
    ByteBuffer buffer = connection.getBuffer();
    
    addRecipesCategoryListMessageHeadersToBuffer(buffer);
    
    buffer.putShort((short)params.getTotalCategories());
    
    addCategoryToBuffer(buffer, params.getCreationEntries().keySet(), params.getCategoryIds());
    
    addCategoryToBuffer(buffer, params.getFences().keySet(), params.getCategoryIds());
    
    addCategoryToBuffer(buffer, params.getHedges().keySet(), params.getCategoryIds());
    
    addCategoryToBuffer(buffer, params.getFlowerbeds().keySet(), params.getCategoryIds());
    
    addCategoryToBuffer(buffer, params.getWalls().keySet(), params.getCategoryIds());
    
    addCategoryToBuffer(buffer, params.getRoofs_floors().keySet(), params.getCategoryIds());
    
    addCategoryToBuffer(buffer, params.getBridgeParts().keySet(), params.getCategoryIds());
    
    addCategoryToBuffer(buffer, params.getCaveWalls().keySet(), params.getCategoryIds());
    
    buffer.putShort(numberOfEntries);
    try
    {
      connection.flush();
      return true;
    }
    catch (IOException iex)
    {
      logger.log(Level.WARNING, "An error occured while flushing the categories for the recipes list.", iex);
      connection.clearBuffer();
    }
    return false;
  }
  
  private static void addCategoryToBuffer(ByteBuffer buffer, Set<String> categories, Map<String, Integer> categoryIds)
  {
    for (String categoryName : categories)
    {
      buffer.putShort(((Integer)categoryIds.get(categoryName)).shortValue());
      addStringToBuffer(buffer, categoryName, true);
    }
  }
  
  private static void addRecipesCategoryListMessageHeadersToBuffer(ByteBuffer buffer)
  {
    buffer.put((byte)-46);
    buffer.put((byte)4);
  }
  
  private static short addCraftingRecipesToRecipesList(RecipesListParameter params, CreationEntry[] toAdd, boolean isSimple)
  {
    short numberOfEntries = 0;
    for (CreationEntry entry : toAdd) {
      if ((!isSimple) || (
      
        (!CreationMatrix.getInstance().getAdvancedEntriesMap().containsKey(Integer.valueOf(entry.getObjectCreated()))) && 
        (entry.getObjectTarget() != 672)))
      {
        String categoryName = entry.getCategory().getCategoryName();
        
        List<CreationEntry> entries = null;
        if (!params.getCreationEntries().containsKey(categoryName)) {
          params.getCreationEntries().put(categoryName, new ArrayList());
        }
        assignCategoryId(categoryName, params);
        
        entries = (List)params.getCreationEntries().get(categoryName);
        
        entries.add(entry);
        
        numberOfEntries = (short)(numberOfEntries + 1);
      }
    }
    return numberOfEntries;
  }
  
  private static final short addFencesToCraftingRecipesList(RecipesListParameter param)
  {
    Map<String, List<ActionEntry>> flist = createFenceCreationList(true, true, false);
    
    int[] cTools = MethodsStructure.getCorrectToolsForBuildingFences();
    
    short numberOfEntries = 0;
    for (String name : flist.keySet())
    {
      String categoryName = StringUtil.format("%s %s", new Object[] { name, "fences" });
      if (!param.getFences().containsKey(categoryName)) {
        param.getFences().put(categoryName, new ArrayList());
      }
      assignCategoryId(categoryName, param);
      
      entries = (List)param.getFences().get(categoryName);
      for (ActionEntry entry : (List)flist.get(name))
      {
        entries.add(entry);
        
        numberOfEntries = (short)(numberOfEntries + cTools.length);
      }
    }
    List<ActionEntry> entries;
    return numberOfEntries;
  }
  
  private static final short addGenericRecipesToList(Map<String, List<Short>> list, RecipesListParameter param, short[] toAdd, String categoryToAdd)
  {
    short numberOfEntries = 0;
    
    assignCategoryId(categoryToAdd, param);
    for (int i = 0; i < toAdd.length; i++)
    {
      if (!list.containsKey(categoryToAdd)) {
        list.put(categoryToAdd, new ArrayList());
      }
      List<Short> entries = (List)list.get(categoryToAdd);
      entries.add(Short.valueOf(toAdd[i]));
      numberOfEntries = (short)(numberOfEntries + 1);
    }
    return numberOfEntries;
  }
  
  private static void assignCategoryId(String category, RecipesListParameter params)
  {
    if (!params.getCategoryIds().containsKey(category)) {
      params.getCategoryIds().put(category, Integer.valueOf(params.getCategoryIdsSize() + 1));
    }
  }
  
  private static final short addWallsToTheCraftingList(RecipesListParameter param)
  {
    short numberOfEntries = 0;
    String wallsCategory = "Walls";
    
    assignCategoryId("Walls", param);
    for (WallEnum en : WallEnum.values()) {
      if (en != WallEnum.WALL_PLAN)
      {
        if (!param.getWalls().containsKey("Walls")) {
          param.getWalls().put("Walls", new ArrayList());
        }
        List<WallEnum> entries = (List)param.getWalls().get("Walls");
        entries.add(en);
        
        numberOfEntries = (short)(numberOfEntries + WallEnum.getToolsForWall(en, null).size());
      }
    }
    return numberOfEntries;
  }
  
  private static final short addBridgePartsToTheCraftingList(RecipesListParameter param)
  {
    short numberOfEntries = 0;
    for (BridgePartEnum en : BridgePartEnum.values()) {
      if (en != BridgePartEnum.UNKNOWN)
      {
        String typeName = StringUtil.toLowerCase(en.getMaterial().getName());
        
        typeName = StringUtil.format("%s %s", new Object[] { "bridge,", typeName });
        String categoryName = LoginHandler.raiseFirstLetter(typeName);
        
        assignCategoryId(categoryName, param);
        if (!param.getBridgeParts().containsKey(categoryName)) {
          param.getBridgeParts().put(categoryName, new ArrayList());
        }
        List<BridgePartEnum> entries = (List)param.getBridgeParts().get(categoryName);
        entries.add(en);
        numberOfEntries = (short)(numberOfEntries + BridgePartEnum.getValidToolsForMaterial(en.getMaterial()).length);
      }
    }
    return numberOfEntries;
  }
  
  private static final short addCaveWallsToTheCraftingList(RecipesListParameter param)
  {
    String wallsCategory = "Cave walls";
    
    assignCategoryId("Cave walls", param);
    
    List<ActionEntry> flist = createCaveWallCreationList();
    
    short numberOfEntries = 0;
    if (!param.getCaveWalls().containsKey("Cave walls")) {
      param.getCaveWalls().put("Cave walls", new ArrayList());
    }
    List<ActionEntry> entries = (List)param.getCaveWalls().get("Cave walls");
    for (ActionEntry entry : flist)
    {
      entries.add(entry);
      
      numberOfEntries = (short)(numberOfEntries + CaveWallBehaviour.getCorrectToolsForCladding(entry.getNumber()).length);
    }
    return numberOfEntries;
  }
  
  private static final short addRoofsFloorsToTheCraftingList(RecipesListParameter param)
  {
    short numberOfEntries = 0;
    for (RoofFloorEnum en : RoofFloorEnum.values()) {
      if (en != RoofFloorEnum.UNKNOWN)
      {
        String typeName = en.getType().getName();
        if (typeName.contains("opening")) {
          typeName = StringUtil.format("%s %s%s", new Object[] { "floor", typeName, "s" });
        } else if (typeName.contains("staircase,")) {
          typeName = StringUtil.format("%s", new Object[] { typeName.replace("se,", "ses,") });
        } else {
          typeName = StringUtil.format("%s%s", new Object[] { typeName, "s" });
        }
        String categoryName = LoginHandler.raiseFirstLetter(typeName);
        
        assignCategoryId(categoryName, param);
        if (!param.getRoofs_floors().containsKey(categoryName)) {
          param.getRoofs_floors().put(categoryName, new ArrayList());
        }
        List<RoofFloorEnum> entries = (List)param.getRoofs_floors().get(categoryName);
        entries.add(en);
        numberOfEntries = (short)(numberOfEntries + RoofFloorEnum.getValidToolsForMaterial(en.getMaterial()).length);
      }
    }
    return numberOfEntries;
  }
  
  private static final boolean sendCaveWallRecipes(SocketConnection connection, @Nonnull Player player, RecipesListParameter params)
  {
    for (Iterator localIterator1 = params.getCaveWalls().keySet().iterator(); localIterator1.hasNext();)
    {
      category = (String)localIterator1.next();
      
      List<ActionEntry> entries = (List)params.getCaveWalls().get(category);
      for (ActionEntry entry : entries)
      {
        byte partCladType = CaveWallBehaviour.getPartReinforcedWallFromAction(entry.getNumber());
        
        byte cladType = CaveWallBehaviour.getReinforcedWallFromAction(entry.getNumber());
        
        int[] correctTools = CaveWallBehaviour.getCorrectToolsForCladding(entry.getNumber());
        for (int i = 0; i < correctTools.length; i++)
        {
          ByteBuffer buffer = connection.getBuffer();
          
          addCreationRecipesMessageHeaders(buffer);
          
          addCategoryIdToBuffer(params, category, buffer);
          
          addCreatedReinforcedWallToBuffer(partCladType, cladType, buffer);
          if (!addFenceToolToBuffer(buffer, correctTools[i]))
          {
            connection.clearBuffer();
            return false;
          }
          addReinforcedWallToBuffer(buffer);
          if (!addAdditionalMaterialsForReinforcedWall(buffer, entry.getNumber()))
          {
            connection.clearBuffer();
            return false;
          }
          try
          {
            connection.flush();
          }
          catch (IOException ex)
          {
            logger.log(Level.WARNING, "IO Exception when sending fence recipes.", ex);
            player.setLink(false);
            return false;
          }
        }
      }
    }
    String category;
    return true;
  }
  
  public static class RecipesListParameter
  {
    private Map<String, List<CreationEntry>> creationEntries;
    private Map<String, Integer> categoryIds;
    private Map<String, List<ActionEntry>> fences;
    private Map<String, List<Short>> hedges;
    private Map<String, List<Short>> flowerbeds;
    private Map<String, List<WallEnum>> walls;
    private Map<String, List<RoofFloorEnum>> roofs_floors;
    private Map<String, List<BridgePartEnum>> bridgeParts;
    private Map<String, List<ActionEntry>> cavewalls;
    
    public RecipesListParameter()
    {
      this.creationEntries = new HashMap();
      this.categoryIds = new HashMap();
      this.fences = new HashMap();
      this.hedges = new HashMap();
      this.flowerbeds = new HashMap();
      this.walls = new HashMap();
      this.roofs_floors = new HashMap();
      this.bridgeParts = new HashMap();
      this.cavewalls = new HashMap();
    }
    
    public Map<String, List<CreationEntry>> getCreationEntries()
    {
      return this.creationEntries;
    }
    
    public final int getCreationEntriesSize()
    {
      return this.creationEntries.size();
    }
    
    public Map<String, Integer> getCategoryIds()
    {
      return this.categoryIds;
    }
    
    public final int getCategoryIdsSize()
    {
      return this.categoryIds.size();
    }
    
    public Map<String, List<ActionEntry>> getFences()
    {
      return this.fences;
    }
    
    public final int getFencesSize()
    {
      return this.fences.size();
    }
    
    public Map<String, List<Short>> getHedges()
    {
      return this.hedges;
    }
    
    public final int getHedgesSize()
    {
      return this.hedges.size();
    }
    
    public Map<String, List<Short>> getFlowerbeds()
    {
      return this.flowerbeds;
    }
    
    public final int getFlowerbedsSize()
    {
      return this.flowerbeds.size();
    }
    
    public Map<String, List<WallEnum>> getWalls()
    {
      return this.walls;
    }
    
    public final int getWallsSize()
    {
      return this.walls.size();
    }
    
    public Map<String, List<RoofFloorEnum>> getRoofs_floors()
    {
      return this.roofs_floors;
    }
    
    public final int getRoofs_floorsSize()
    {
      return this.roofs_floors.size();
    }
    
    public Map<String, List<BridgePartEnum>> getBridgeParts()
    {
      return this.bridgeParts;
    }
    
    public final int getBridgePartsSize()
    {
      return this.bridgeParts.size();
    }
    
    public Map<String, List<ActionEntry>> getCaveWalls()
    {
      return this.cavewalls;
    }
    
    public final int getCaveWallsSize()
    {
      return this.cavewalls.size();
    }
    
    public final int getTotalCategories()
    {
      return 
      
        getCreationEntriesSize() + getFencesSize() + getHedgesSize() + getFlowerbedsSize() + getWallsSize() + getRoofs_floorsSize() + getBridgePartsSize() + getCaveWallsSize();
    }
  }
  
  private static final short buildCreationsList(RecipesListParameter param)
  {
    short numberOfEntries = 0;
    
    numberOfEntries = (short)(numberOfEntries + addCraftingRecipesToRecipesList(param, 
      CreationMatrix.getInstance().getSimpleEntries(), true));
    
    numberOfEntries = (short)(numberOfEntries + addCraftingRecipesToRecipesList(param, 
      CreationMatrix.getInstance().getAdvancedEntries(), false));
    
    numberOfEntries = (short)(numberOfEntries + addFencesToCraftingRecipesList(param));
    
    numberOfEntries = (short)(numberOfEntries + addGenericRecipesToList(param.getHedges(), param, Fence.getAllLowHedgeTypes(), "Hedges"));
    
    numberOfEntries = (short)(numberOfEntries + addGenericRecipesToList(param.getFlowerbeds(), param, Fence.getAllFlowerbeds(), "Flowerbeds"));
    
    numberOfEntries = (short)(numberOfEntries + addWallsToTheCraftingList(param));
    
    numberOfEntries = (short)(numberOfEntries + addRoofsFloorsToTheCraftingList(param));
    
    numberOfEntries = (short)(numberOfEntries + addBridgePartsToTheCraftingList(param));
    
    numberOfEntries = (short)(numberOfEntries + addCaveWallsToTheCraftingList(param));
    
    return numberOfEntries;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\items\CreationWindowMethods.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */