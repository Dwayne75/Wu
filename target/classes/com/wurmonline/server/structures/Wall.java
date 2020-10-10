package com.wurmonline.server.structures;

import com.wurmonline.math.TilePos;
import com.wurmonline.math.Vector3f;
import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.TileBorderDirection;
import com.wurmonline.server.DbConnector;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.TimeConstants;
import com.wurmonline.server.behaviours.MethodsStructure;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Permissions;
import com.wurmonline.server.players.Permissions.Allow;
import com.wurmonline.server.players.Permissions.IAllow;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.utils.DbUtilities;
import com.wurmonline.server.utils.StringUtil;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.server.zones.NoSuchTileException;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.VolaTile;
import com.wurmonline.server.zones.Zone;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.constants.StructureMaterialEnum;
import com.wurmonline.shared.constants.StructureStateEnum;
import com.wurmonline.shared.constants.StructureTypeEnum;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Wall
  implements MiscConstants, TimeConstants, Blocker, StructureSupport, Permissions.IAllow
{
  public int x1;
  public int x2;
  public int y1;
  public int y2;
  private static final Vector3f normalHoriz = new Vector3f(0.0F, 1.0F, 0.0F);
  private static final Vector3f normalVertical = new Vector3f(1.0F, 0.0F, 0.0F);
  private Vector3f centerPoint;
  private static final Map<Long, Set<Wall>> walls = new HashMap();
  private static final String GETALLWALLS = "SELECT * FROM WALLS WHERE STARTX<ENDX OR STARTY<ENDY";
  Permissions permissions = new Permissions();
  private static final Set<Wall> rubbleWalls = new HashSet();
  public long structureId = -10L;
  int number = -10;
  private static final Logger logger = Logger.getLogger(Wall.class.getName());
  public float originalQL;
  public float currentQL;
  public float damage;
  public StructureTypeEnum type = StructureTypeEnum.SOLID;
  public int tilex;
  public int tiley;
  private int floorLevel = 0;
  public int heightOffset = 0;
  byte layer = 0;
  public long lastUsed;
  public StructureStateEnum state = StructureStateEnum.INITIALIZED;
  private StructureMaterialEnum material = StructureMaterialEnum.WOOD;
  int color = -1;
  boolean wallOrientationFlag = false;
  private static final String WOOD = "wood";
  private static final String STONE = "stone";
  private static final String TIMBER_FRAMED = "timber framed";
  private static final String PLAIN_STONE = "plain stone";
  private static final String SLATE = "slate";
  private static final String ROUNDED_STONE = "rounded stone";
  private static final String POTTERY = "pottery";
  private static final String SANDSTONE = "sandstone";
  private static final String RENDERED = "rendered";
  private static final String MARBLE = "marble";
  private static final int[] emptyArr = new int[0];
  protected boolean isIndoor = false;
  
  public Wall(StructureTypeEnum aType, int aTileX, int aTileY, int aStartX, int aStartY, int aEndX, int aEndY, float aQualityLevel, long aStructure, StructureMaterialEnum _material, boolean _isIndoor, int _heightOffset, int _layer)
  {
    this.structureId = aStructure;
    this.tilex = aTileX;
    this.tiley = aTileY;
    this.x1 = aStartX;
    this.y1 = aStartY;
    this.x2 = aEndX;
    this.y2 = aEndY;
    this.currentQL = aQualityLevel;
    this.originalQL = aQualityLevel;
    this.lastUsed = System.currentTimeMillis();
    this.type = aType;
    this.material = _material;
    this.isIndoor = _isIndoor;
    this.heightOffset = _heightOffset;
    setFloorLevel();
    this.layer = ((byte)(_layer & 0xFF));
  }
  
  public Wall(int wallid, StructureTypeEnum typ, int tx, int ty, int xs, int ys, int xe, int ye, float qualityLevel, float origQl, float dam, long structure, long last, StructureStateEnum stat, int col, StructureMaterialEnum _material, boolean _isIndoor, int _heightOffset, int _layer, boolean _wallOrientation, int aSettings)
  {
    this.number = wallid;
    this.type = typ;
    this.tilex = tx;
    this.tiley = ty;
    this.x1 = xs;
    this.y1 = ys;
    this.x2 = xe;
    this.y2 = ye;
    this.currentQL = qualityLevel;
    this.originalQL = origQl;
    this.damage = dam;
    this.structureId = structure;
    this.lastUsed = last;
    this.state = stat;
    this.color = col;
    this.material = _material;
    this.isIndoor = _isIndoor;
    this.heightOffset = _heightOffset;
    setFloorLevel();
    this.layer = ((byte)(_layer & 0xFF));
    this.wallOrientationFlag = _wallOrientation;
    setSettings(aSettings);
  }
  
  public Wall(int wallid, boolean load)
    throws IOException
  {
    this.number = wallid;
    if (load) {
      load();
    }
  }
  
  public final String getIdName()
  {
    if (this.material == StructureMaterialEnum.WOOD)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "wooden_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "wooden_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "wooden_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "wooden_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "wooden_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "wooden_portcullis";
      }
      if (this.type == StructureTypeEnum.CANOPY_DOOR) {
        return "wooden_canopy_door";
      }
      if (this.type == StructureTypeEnum.WIDE_WINDOW) {
        return "wooden_wide_window";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "wooden_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "wooden_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "wooden_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.STONE)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "stone_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "stone_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "sturdy_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "sturdy_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "study_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "sturdy_portcullis";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "stone_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "sturdy_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "sturdy_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "sturdy_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.PLAIN_STONE)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "plain_stone_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "plain_stone_window";
      }
      if (this.type == StructureTypeEnum.NARROW_WINDOW) {
        return "plain_narrow_stone_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "plain_stone_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "plain_stone_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "plain_stone_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "plain_stone_portcullis";
      }
      if (this.type == StructureTypeEnum.BARRED) {
        return "plain_stone_barred_wall";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "plain_stone_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "plain_stone_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "plain_stone_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "plain_stone_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.SLATE)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "slate_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "slate_window";
      }
      if (this.type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow_slate_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "slate_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "slate_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "slate_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "slate_portcullis";
      }
      if (this.type == StructureTypeEnum.BARRED) {
        return "slate_barred_wall";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "slate_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "slate_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "slate_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "slate_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.ROUNDED_STONE)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "rounded_stone_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "rounded_stone_window";
      }
      if (this.type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow_rounded_stone_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "rounded_stone_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "rounded_stone_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "rounded_stone_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "rounded_stone_portcullis";
      }
      if (this.type == StructureTypeEnum.BARRED) {
        return "rounded_stone_barred_wall";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "rounded_stone_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "rounded_stone_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "rounded_stone_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "rounded_stone_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.POTTERY)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "pottery_brick_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "pottery_brick_window";
      }
      if (this.type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow_pottery_brick_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "pottery_brick_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "pottery_brick_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "pottery_brick_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "pottery_brick_portcullis";
      }
      if (this.type == StructureTypeEnum.BARRED) {
        return "pottery_brick_barred_wall";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "pottery_brick_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "pottery_brick_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "pottery_brick_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "pottery_brick_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.SANDSTONE)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "sandstone_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "sandstone_window";
      }
      if (this.type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow_sandstone_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "sandstone_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "sandstone_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "sandstone_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "sandstone_portcullis";
      }
      if (this.type == StructureTypeEnum.BARRED) {
        return "sandstone_barred_wall";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "sandstone_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "sandstone_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "sandstone_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "sandstone_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.RENDERED)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "rendered_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "rendered_window";
      }
      if (this.type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow_rendered_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "rendered_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "rendered_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "rendered_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "rendered_portcullis";
      }
      if (this.type == StructureTypeEnum.BARRED) {
        return "rendered_barred_wall";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "rendered_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "rendered_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "rendered_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "rendered_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.MARBLE)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "marble_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "marble_window";
      }
      if (this.type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow_marble_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "marble_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "marble_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "marble_arched";
      }
      if (this.type == StructureTypeEnum.PORTCULLIS) {
        return "marble_portcullis";
      }
      if (this.type == StructureTypeEnum.BARRED) {
        return "marble_barred_wall";
      }
      if (this.type == StructureTypeEnum.ORIEL) {
        return "marble_oriel";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "marble_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "marble_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "marble_arched_t";
      }
    }
    else if (this.material == StructureMaterialEnum.TIMBER_FRAMED)
    {
      if (this.type == StructureTypeEnum.SOLID) {
        return "timber_framed_wall";
      }
      if (this.type == StructureTypeEnum.WINDOW) {
        return "timber_framed_window";
      }
      if (this.type == StructureTypeEnum.DOOR) {
        return "timber_framed_door";
      }
      if (this.type == StructureTypeEnum.DOUBLE_DOOR) {
        return "timber_framed_double_door";
      }
      if (this.type == StructureTypeEnum.ARCHED) {
        return "timber_framed_arched";
      }
      if (this.type == StructureTypeEnum.BALCONY) {
        return "timber_framed_balcony";
      }
      if (this.type == StructureTypeEnum.JETTY) {
        return "timber_framed_jetty";
      }
      if (this.type == StructureTypeEnum.ARCHED_LEFT) {
        return "timber_framed_arched_left";
      }
      if (this.type == StructureTypeEnum.ARCHED_RIGHT) {
        return "timber_framed_arched_right";
      }
      if (this.type == StructureTypeEnum.ARCHED_T) {
        return "timber_framed_arched_t";
      }
    }
    if (this.type == StructureTypeEnum.PLAN) {
      return "wall_plan";
    }
    return "unknown_wall";
  }
  
  public final String getName()
  {
    return getName(this.type, this.material);
  }
  
  public static final String getName(StructureTypeEnum type, StructureMaterialEnum material)
  {
    if (type == StructureTypeEnum.RUBBLE) {
      return "pile of debris";
    }
    if (material == StructureMaterialEnum.WOOD)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "wooden wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "wooden window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "wooden door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "wooden double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "wooden arched wall";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "wooden portcullis";
      }
      if (type == StructureTypeEnum.CANOPY_DOOR) {
        return "wooden canopy door";
      }
      if (type == StructureTypeEnum.WIDE_WINDOW) {
        return "wooden wide window";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "wooden left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "wooden right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "wooden T arch";
      }
      if (type == StructureTypeEnum.SCAFFOLDING) {
        return "wooden scaffolding";
      }
    }
    else if (material == StructureMaterialEnum.STONE)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "stone wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "stone window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "sturdy door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "sturdy double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "sturdy arched wall";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "sturdy portcullis";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "stone oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "sturdy left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "sturdy right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "sturdy T arch";
      }
    }
    else if (material == StructureMaterialEnum.PLAIN_STONE)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "plain stone wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "plain stone window";
      }
      if (type == StructureTypeEnum.NARROW_WINDOW) {
        return "plain narrow stone window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "plain stone door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "plain stone double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "plain stone arched wall";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "plain stone portcullis";
      }
      if (type == StructureTypeEnum.BARRED) {
        return "plain stone barred wall";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "plain stone oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "plain stone left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "plain stone right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "plain stone T arch";
      }
    }
    else if (material == StructureMaterialEnum.SLATE)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "slate wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "slate window";
      }
      if (type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow slate window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "slate door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "slate double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "slate arched";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "slate portcullis";
      }
      if (type == StructureTypeEnum.BARRED) {
        return "slate barred wall";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "slate oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "slate left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "slate right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "slate T arch";
      }
    }
    else if (material == StructureMaterialEnum.ROUNDED_STONE)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "rounded stone wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "rounded stone window";
      }
      if (type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow rounded stone window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "rounded stone door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "rounded stone double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "rounded stone arched";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "rounded stone portcullis";
      }
      if (type == StructureTypeEnum.BARRED) {
        return "rounded stone barred wall";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "rounded stone oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "rounded stone left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "rounded stone right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "rounded stone T arch";
      }
    }
    else if (material == StructureMaterialEnum.POTTERY)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "pottery brick wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "pottery brick window";
      }
      if (type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow pottery brick window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "pottery brick door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "pottery brick double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "pottery brick arched";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "pottery brick portcullis";
      }
      if (type == StructureTypeEnum.BARRED) {
        return "pottery brick barred wall";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "pottery brick oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "pottery brick left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "pottery brick right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "pottery brick T arch";
      }
    }
    else if (material == StructureMaterialEnum.SANDSTONE)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "sandstone wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "sandstone window";
      }
      if (type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow sandstone window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "sandstone door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "sandstone double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "sandstone arched";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "sandstone portcullis";
      }
      if (type == StructureTypeEnum.BARRED) {
        return "sandstone barred wall";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "sandstone oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "sandstone left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "sandstone right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "sandstone T arch";
      }
    }
    else if (material == StructureMaterialEnum.RENDERED)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "rendered wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "rendered window";
      }
      if (type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow rendered window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "rendered door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "rendered double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "rendered arched";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "rendered portcullis";
      }
      if (type == StructureTypeEnum.BARRED) {
        return "rendered barred wall";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "rendered oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "rendered left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "rendered right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "rendered T arch";
      }
    }
    else if (material == StructureMaterialEnum.MARBLE)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "marble wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "marble window";
      }
      if (type == StructureTypeEnum.NARROW_WINDOW) {
        return "narrow marble window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "marble door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "marble double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "marble arched";
      }
      if (type == StructureTypeEnum.PORTCULLIS) {
        return "marble portcullis";
      }
      if (type == StructureTypeEnum.BARRED) {
        return "marble barred wall";
      }
      if (type == StructureTypeEnum.ORIEL) {
        return "marble oriel";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "marble left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "marble right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "marble T arch";
      }
    }
    else if (material == StructureMaterialEnum.TIMBER_FRAMED)
    {
      if (type == StructureTypeEnum.SOLID) {
        return "timber framed wall";
      }
      if (type == StructureTypeEnum.WINDOW) {
        return "timber framed window";
      }
      if (type == StructureTypeEnum.DOOR) {
        return "timber framed door";
      }
      if (type == StructureTypeEnum.DOUBLE_DOOR) {
        return "timber framed double door";
      }
      if (type == StructureTypeEnum.ARCHED) {
        return "timber framed arched wall";
      }
      if (type == StructureTypeEnum.JETTY) {
        return "timber framed jetty";
      }
      if (type == StructureTypeEnum.BALCONY) {
        return "timber framed balcony";
      }
      if (type == StructureTypeEnum.ARCHED_LEFT) {
        return "timber framed left arch";
      }
      if (type == StructureTypeEnum.ARCHED_RIGHT) {
        return "timber framed right arch";
      }
      if (type == StructureTypeEnum.ARCHED_T) {
        return "timber framed T arch";
      }
    }
    if (type == StructureTypeEnum.PLAN) {
      return "wall plan";
    }
    return "unknown wall";
  }
  
  public static final String getMaterialName(StructureMaterialEnum material)
  {
    if (material == StructureMaterialEnum.WOOD) {
      return "Wooden";
    }
    if (material == StructureMaterialEnum.STONE) {
      return "Stone brick";
    }
    if (material == StructureMaterialEnum.PLAIN_STONE) {
      return "Plain stone";
    }
    if (material == StructureMaterialEnum.SLATE) {
      return "Slate";
    }
    if (material == StructureMaterialEnum.ROUNDED_STONE) {
      return "Rounded stone";
    }
    if (material == StructureMaterialEnum.POTTERY) {
      return "Pottery";
    }
    if (material == StructureMaterialEnum.SANDSTONE) {
      return "Sandstone";
    }
    if (material == StructureMaterialEnum.MARBLE) {
      return "Marble";
    }
    if (material == StructureMaterialEnum.TIMBER_FRAMED) {
      return "Timber framed";
    }
    return "unknown";
  }
  
  public static final Wall[] getRubbleWalls()
  {
    return (Wall[])rubbleWalls.toArray(new Wall[rubbleWalls.size()]);
  }
  
  protected static final void addRubble(Wall wall)
  {
    rubbleWalls.add(wall);
  }
  
  protected static final void removeRubble(Wall wall)
  {
    rubbleWalls.remove(wall);
  }
  
  public final boolean isFence()
  {
    return false;
  }
  
  public final boolean isWall()
  {
    return true;
  }
  
  public final boolean isFloor()
  {
    return false;
  }
  
  public final boolean isRoof()
  {
    return false;
  }
  
  public final boolean isStair()
  {
    return false;
  }
  
  public final boolean isTile()
  {
    return false;
  }
  
  public final boolean isDoor()
  {
    return (this.type == StructureTypeEnum.DOOR) || (this.type == StructureTypeEnum.DOUBLE_DOOR) || (this.type == StructureTypeEnum.PORTCULLIS) || (this.type == StructureTypeEnum.CANOPY_DOOR) || 
    
      (isArched(this.type));
  }
  
  public final Vector3f getNormal()
  {
    if (isHorizontal()) {
      return normalHoriz;
    }
    return normalVertical;
  }
  
  private final Vector3f calculateCenterPoint()
  {
    int sx = Math.min(this.x1, this.x2);
    int sy = Math.min(this.y1, this.y2);
    
    return new Vector3f(isHorizontal() ? sx * 4 + 2 : sx * 4, isHorizontal() ? sy * 4 : sy * 4 + 2, getMinZ() + 1.5F);
  }
  
  public final Vector3f getCenterPoint()
  {
    if (this.centerPoint == null) {
      this.centerPoint = calculateCenterPoint();
    }
    return this.centerPoint;
  }
  
  public final Vector3f isBlocking(Creature creature, Vector3f startPos, Vector3f endPos, Vector3f normal, int blockType, long target, boolean followGround)
  {
    if (target == getId()) {
      return null;
    }
    if ((this.type == StructureTypeEnum.PLAN) || (this.type == StructureTypeEnum.RUBBLE) || (isArched(this.type))) {
      return null;
    }
    if ((blockType == 5) && ((isWindow()) || (isBalcony()) || (isJetty()) || (isOriel()))) {
      return null;
    }
    if (!isFinished()) {
      return null;
    }
    if ((blockType == 6) || (blockType == 8)) {
      if (isDoor()) {
        if (getDoor() != null)
        {
          if (getDoor().canBeOpenedBy(creature, true)) {
            return null;
          }
          return getIntersectionPoint(startPos, endPos, normal, creature, blockType, followGround);
        }
      }
    }
    return getIntersectionPoint(startPos, endPos, normal, creature, blockType, followGround);
  }
  
  public final boolean canBeOpenedBy(Creature creature, boolean wentThroughDoor)
  {
    if ((this.type == StructureTypeEnum.PLAN) || (isArched())) {
      return true;
    }
    if (!isFinished()) {
      return true;
    }
    if (isDoor()) {
      if (getDoor() != null) {
        if (getDoor().canBeOpenedBy(creature, true)) {
          return true;
        }
      }
    }
    return false;
  }
  
  public final float getBlockPercent(Creature creature)
  {
    if (this.type == StructureTypeEnum.RUBBLE) {
      return 0.0F;
    }
    if (isFinished())
    {
      if ((isWindow()) || (isJetty())) {
        return 70.0F;
      }
      if (isOriel()) {
        return 80.0F;
      }
      if (isBalcony()) {
        return 10.0F;
      }
      return 100.0F;
    }
    return Math.max(0, getState().state);
  }
  
  public final Vector3f getIntersectionPoint(Vector3f startPos, Vector3f endPos, Vector3f normal, Creature creature, int blockType, boolean followGround)
  {
    Vector3f spcopy = startPos.clone();
    Vector3f epcopy = endPos.clone();
    if (getFloorLevel() == 0) {
      if ((followGround) || (spcopy.z <= getMinZ()))
      {
        spcopy.z = (getMinZ() + 1.75F);
        if (followGround) {
          epcopy.z = (getMinZ() + 0.5F);
        }
      }
    }
    Vector3f diff = getCenterPoint().subtract(spcopy);
    
    Vector3f diffend = epcopy.subtract(spcopy);
    if (isHorizontal())
    {
      float steps = diff.y / normal.y;
      Vector3f intersection = spcopy.add(normal.mult(steps));
      Vector3f interDiff = intersection.subtract(spcopy);
      if (diffend.length() + 0.01F < interDiff.length()) {
        return null;
      }
      if (isWithinBounds(intersection, followGround))
      {
        float u = getNormal().dot(getCenterPoint().subtract(startPos)) / getNormal().dot(epcopy.subtract(spcopy));
        if ((u >= 0.0F) && (u <= 1.0F)) {
          return intersection;
        }
        return null;
      }
    }
    else
    {
      float steps = diff.x / normal.x;
      Vector3f intersection = spcopy.add(normal.mult(steps));
      Vector3f interDiff = intersection.subtract(spcopy);
      if (diffend.length() < interDiff.length()) {
        return null;
      }
      if (isWithinBounds(intersection, followGround))
      {
        float u = getNormal().dot(getCenterPoint().subtract(spcopy)) / getNormal().dot(epcopy.subtract(spcopy));
        if ((u >= 0.0F) && (u <= 1.0F)) {
          return intersection;
        }
        return null;
      }
    }
    return null;
  }
  
  private final boolean isWithinBounds(Vector3f pointToCheck, boolean followGround)
  {
    if (isHorizontal())
    {
      if ((pointToCheck.getY() >= this.y1 * 4 - 0.1F) && 
        (pointToCheck.getY() <= this.y2 * 4 + 0.1F)) {
        if ((pointToCheck.getX() >= Math.min(this.x1, this.x2) * 4) && (pointToCheck.getX() <= Math.max(this.x2, this.x1) * 4) && (
          ((followGround) && (getFloorLevel() == 0)) || (
          (pointToCheck.getZ() >= getMinZ()) && (pointToCheck.getZ() <= getMaxZ())))) {
          return true;
        }
      }
    }
    else if ((pointToCheck.getX() >= this.x1 * 4 - 0.1F) && 
      (pointToCheck.getX() <= this.x2 * 4 + 0.1F)) {
      if ((pointToCheck.getY() >= Math.min(this.y1, this.y2) * 4) && (pointToCheck.getY() <= Math.max(this.y2, this.y1) * 4) && (
        ((followGround) && (getFloorLevel() == 0)) || (
        (pointToCheck.getZ() >= getMinZ()) && (pointToCheck.getZ() <= getMaxZ())))) {
        return true;
      }
    }
    return false;
  }
  
  public final int getTileX()
  {
    return this.tilex;
  }
  
  public final int getColor()
  {
    return this.color;
  }
  
  public final boolean getWallOrientationFlag()
  {
    return this.wallOrientationFlag;
  }
  
  public final int getNumber()
  {
    return this.number;
  }
  
  public final int getTileY()
  {
    return this.tiley;
  }
  
  public final Tiles.TileBorderDirection getDir()
  {
    if (isHorizontal()) {
      return Tiles.TileBorderDirection.DIR_HORIZ;
    }
    return Tiles.TileBorderDirection.DIR_DOWN;
  }
  
  public final boolean isHorizontal()
  {
    return this.y1 == this.y2;
  }
  
  public final float getCurrentQualityLevel()
  {
    return this.currentQL * Math.max(1.0F, 100.0F - this.damage) / 100.0F;
  }
  
  public final boolean isOnPvPServer()
  {
    if (isHorizontal())
    {
      if (Zones.isOnPvPServer(this.x1, this.y1)) {
        return true;
      }
      if (Zones.isOnPvPServer(this.x1, this.y1 - 1)) {
        return true;
      }
    }
    else
    {
      if (Zones.isOnPvPServer(this.x1, this.y1)) {
        return true;
      }
      if (Zones.isOnPvPServer(this.x1 - 1, this.y1)) {
        return true;
      }
    }
    return false;
  }
  
  public final float getOriginalQualityLevel()
  {
    return this.originalQL;
  }
  
  public final int getStartX()
  {
    return this.x1;
  }
  
  public final int getStartY()
  {
    return this.y1;
  }
  
  public final int getMinX()
  {
    return Math.min(this.x1, this.x2);
  }
  
  public final int getMinY()
  {
    return Math.min(this.y1, this.y2);
  }
  
  public final int getEndX()
  {
    return this.x2;
  }
  
  public final int getEndY()
  {
    return this.y2;
  }
  
  public final float getPositionX()
  {
    return (this.x1 * 4 + this.x2 * 4) / 2.0F;
  }
  
  public final float getPositionY()
  {
    return (this.y1 * 4 + this.y2 * 4) / 2.0F;
  }
  
  public final void setStructureId(long structure)
  {
    this.structureId = structure;
  }
  
  public final long getStructureId()
  {
    return this.structureId;
  }
  
  public final long getOLDId()
  {
    if (this.y1 == this.y2)
    {
      if (this.x1 < this.x2) {
        return 0L + (this.x1 << 32) + (this.y1 << 16) + 5L;
      }
      if (this.x1 > this.x2) {
        return 72057594037927936L + (this.x2 << 32) + (this.y1 << 16) + 5L;
      }
      throw new IllegalStateException("Found a broken wall.");
    }
    if (this.x1 == this.x2)
    {
      if (this.y1 < this.y2) {
        return 72339069014638592L + (this.x1 << 32) + (this.y1 << 16) + 5L;
      }
      if (this.y1 > this.y2) {
        return 281474976710656L + (this.x2 << 32) + (this.y2 << 16) + 5L;
      }
      throw new IllegalStateException("Found a broken wall.");
    }
    throw new IllegalStateException("Found a broken wall.");
  }
  
  public final boolean equals(StructureSupport support)
  {
    return support.getId() == getId();
  }
  
  public final long getId()
  {
    if (this.y1 == this.y2)
    {
      if (this.x1 < this.x2) {
        return Tiles.getHouseWallId(this.x1, this.y1, this.heightOffset, getLayer(), (byte)0);
      }
      if (this.x1 > this.x2) {
        return Tiles.getHouseWallId(this.x2, this.y1, this.heightOffset, getLayer(), (byte)0);
      }
      throw new IllegalStateException("Found a broken wall.");
    }
    if (this.x1 == this.x2)
    {
      if (this.y1 < this.y2) {
        return Tiles.getHouseWallId(this.x1, this.y1, this.heightOffset, getLayer(), (byte)1);
      }
      if (this.y1 > this.y2) {
        return Tiles.getHouseWallId(this.x2, this.y2, this.heightOffset, getLayer(), (byte)1);
      }
      throw new IllegalStateException("Found a broken wall.");
    }
    throw new IllegalStateException("Found a broken wall.");
  }
  
  public final void setType(StructureTypeEnum aType)
  {
    this.type = aType;
    this.lastUsed = System.currentTimeMillis();
  }
  
  public final StructureTypeEnum getType()
  {
    return this.type;
  }
  
  public final boolean isArched()
  {
    return isArched(this.type);
  }
  
  public final boolean isLRArch()
  {
    switch (Wall.1.$SwitchMap$com$wurmonline$shared$constants$StructureTypeEnum[this.type.ordinal()])
    {
    case 1: 
    case 2: 
      return true;
    }
    return false;
  }
  
  public final boolean isHalfArch()
  {
    return isHalfArch(this.type);
  }
  
  public final StructureStateEnum getState()
  {
    return this.state;
  }
  
  public final StructureStateEnum getNeeded()
  {
    int needed = getFinalState().state - getState().state;
    if (isHalfArch()) {
      needed--;
    }
    return StructureStateEnum.getStateByValue((byte)needed);
  }
  
  public final StructureStateEnum getFinalState()
  {
    int extra = (isHalfArch()) && (!isWood()) ? 1 : 0;
    if (isTimberFramed()) {
      return StructureStateEnum.getStateByValue((byte)(26 + extra));
    }
    if (this.type == StructureTypeEnum.SCAFFOLDING) {
      return StructureStateEnum.getStateByValue((byte)5);
    }
    return StructureStateEnum.getStateByValue((byte)(21 + extra));
  }
  
  public final boolean isFinished()
  {
    return this.state == StructureStateEnum.FINISHED;
  }
  
  public abstract void setState(StructureStateEnum paramStructureStateEnum);
  
  public final VolaTile getOrCreateOuterTile(boolean surfaced)
    throws NoSuchZoneException, NoSuchTileException
  {
    if (isHorizontal())
    {
      VolaTile t = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
      if (t.getStructure() == null) {
        return t;
      }
      VolaTile t2 = Zones.getZone(this.x1, this.y1 - 1, surfaced).getOrCreateTile(this.x1, this.y1 - 1);
      return t2;
    }
    VolaTile t = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
    if (t.getStructure() == null) {
      return t;
    }
    VolaTile t2 = Zones.getZone(this.x1 - 1, this.y1, surfaced).getOrCreateTile(this.x1 - 1, this.y1);
    return t2;
  }
  
  public final VolaTile getOrCreateInnerTile(boolean surfaced)
    throws NoSuchZoneException, NoSuchTileException
  {
    if (isHorizontal())
    {
      VolaTile toReturn = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
      if (toReturn.getStructure() != null)
      {
        if (toReturn.isTransition()) {
          return Zones.getZone(this.x1, this.y1, false).getOrCreateTile(this.x1, this.y1);
        }
        return toReturn;
      }
      VolaTile t2 = Zones.getZone(this.x1, this.y1 - 1, surfaced).getOrCreateTile(this.x1, this.y1 - 1);
      if (t2.getStructure() == null) {
        logger.log(Level.INFO, t2 + " has no structure, so no inner wall exists.", new Exception());
      }
      if (t2.isTransition()) {
        return Zones.getZone(this.x1, this.y1 - 1, false).getOrCreateTile(this.x1, this.y1 - 1);
      }
      return t2;
    }
    VolaTile toReturn = Zones.getZone(this.x1, this.y1, surfaced).getOrCreateTile(this.x1, this.y1);
    if (toReturn.getStructure() != null)
    {
      if (toReturn.isTransition()) {
        return Zones.getZone(this.x1, this.y1, false).getOrCreateTile(this.x1, this.y1);
      }
      return toReturn;
    }
    VolaTile t2 = Zones.getZone(this.x1 - 1, this.y1, surfaced).getOrCreateTile(this.x1 - 1, this.y1);
    if (t2.getStructure() == null) {
      logger.log(Level.INFO, t2 + " has no structure, so no inner wall exists.", new Exception());
    }
    if (t2.isTransition()) {
      return Zones.getZone(this.x1 - 1, this.y1, false).getOrCreateTile(this.x1 - 1, this.y1);
    }
    return t2;
  }
  
  public final void poll(long currTime, VolaTile t, Structure struct)
  {
    if (this.type == StructureTypeEnum.PLAN) {
      return;
    }
    if (this.type == StructureTypeEnum.RUBBLE)
    {
      setDamage(getDamage() + 4.0F);
      return;
    }
    if (struct == null)
    {
      logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + " no structure attached.");
      return;
    }
    if (currTime - struct.getCreationDate() <= 172800000L) {
      return;
    }
    float mod = 1.0F;
    
    Village village = null;
    if (t != null)
    {
      village = t.getVillage();
      if (village == null) {
        if (!isHorizontal())
        {
          Village westTile = Zones.getVillage(this.tilex - 1, this.tiley, true);
          if ((westTile != null) && (getStartX() == this.tilex)) {
            village = westTile;
          }
          Village eastTile = Zones.getVillage(this.tilex + 1, this.tiley, true);
          if ((eastTile != null) && (getStartX() == this.tilex + 1)) {
            village = eastTile;
          }
        }
        else
        {
          Village northTile = Zones.getVillage(this.tilex, this.tiley - 1, true);
          if ((northTile != null) && (getStartY() == this.tiley)) {
            village = northTile;
          }
          Village southTile = Zones.getVillage(this.tilex, this.tiley + 1, true);
          if ((southTile != null) && (getStartY() == this.tiley + 1)) {
            village = southTile;
          }
        }
      }
      if ((village != null) && (!village.lessThanWeekLeft()))
      {
        if (village.moreThanMonthLeft()) {
          return;
        }
        mod *= 10.0F;
      }
      else if ((t.getKingdom() == 0) || (Servers.localServer.HOMESERVER))
      {
        mod *= 0.5F;
      }
      if (!t.isOnSurface()) {
        mod *= 0.75F;
      }
    }
    if (((float)(currTime - this.lastUsed) > (Servers.localServer.testServer ? 60000.0F * mod : 8.64E7F * mod)) && (!hasNoDecay()))
    {
      long ownerId = struct.getOwnerId();
      if (ownerId == -10L)
      {
        this.damage += 20.0F + Server.rand.nextFloat() * 10.0F;
      }
      else
      {
        boolean ownerIsInactive = false;
        long aMonth = Servers.isThisATestServer() ? 86400000L : 2419200000L;
        PlayerInfo pInfo = PlayerInfoFactory.getPlayerInfoWithWurmId(ownerId);
        if (pInfo == null) {
          ownerIsInactive = true;
        } else if ((pInfo.lastLogin == 0L) && (pInfo.lastLogout < System.currentTimeMillis() - 3L * aMonth)) {
          ownerIsInactive = true;
        }
        if (ownerIsInactive) {
          this.damage += 3.0F;
        }
        if ((t != null) && (village == null))
        {
          Village v = Villages.getVillageWithPerimeterAt(t.tilex, t.tiley, t.isOnSurface());
          if (v != null) {
            if (!v.isCitizen(ownerId)) {
              if (ownerIsInactive) {
                this.damage += 3.0F;
              }
            }
          }
        }
      }
      setLastUsed(currTime);
      setDamage(this.damage + 0.1F * getDamageModifier());
    }
  }
  
  public static final boolean isArched(StructureTypeEnum type)
  {
    switch (Wall.1.$SwitchMap$com$wurmonline$shared$constants$StructureTypeEnum[type.ordinal()])
    {
    case 1: 
    case 2: 
    case 3: 
    case 4: 
    case 5: 
      return true;
    }
    return false;
  }
  
  public static final boolean isHalfArch(StructureTypeEnum type)
  {
    switch (Wall.1.$SwitchMap$com$wurmonline$shared$constants$StructureTypeEnum[type.ordinal()])
    {
    case 1: 
    case 2: 
    case 4: 
      return true;
    }
    return false;
  }
  
  public static final List<Wall> getWallsAsArrayListFor(long structureId)
  {
    List<Wall> toReturn = new ArrayList();
    
    Set<Wall> flset = (Set)walls.get(Long.valueOf(structureId));
    if (flset != null) {
      toReturn.addAll(flset);
    }
    return toReturn;
  }
  
  public static final void loadAllWalls()
    throws IOException
  {
    logger.log(Level.INFO, "Loading all walls.");
    long s = System.nanoTime();
    Connection dbcon = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try
    {
      dbcon = DbConnector.getZonesDbCon();
      ps = dbcon.prepareStatement("SELECT * FROM WALLS WHERE STARTX<ENDX OR STARTY<ENDY");
      rs = ps.executeQuery();
      while (rs.next())
      {
        long sid = rs.getLong("STRUCTURE");
        Set<Wall> flset = (Set)walls.get(Long.valueOf(sid));
        if (flset == null)
        {
          flset = new HashSet();
          walls.put(Long.valueOf(sid), flset);
        }
        flset.add(new DbWall(rs.getInt("ID"), 
          StructureTypeEnum.getTypeByINDEX(rs.getByte("TYPE")), rs
          .getInt("TILEX"), rs
          .getInt("TILEY"), rs
          .getInt("STARTX"), rs
          .getInt("STARTY"), rs
          .getInt("ENDX"), rs
          .getInt("ENDY"), rs
          .getFloat("CURRENTQL"), rs
          .getFloat("ORIGINALQL"), rs
          .getFloat("DAMAGE"), sid, rs
          .getLong("LASTMAINTAINED"), 
          StructureStateEnum.getStateByValue(rs.getByte("STATE")), rs
          .getInt("COLOR"), 
          StructureMaterialEnum.getEnumByMaterial(rs.getByte("MATERIAL")), rs
          .getBoolean("ISINDOOR"), rs
          .getInt("HEIGHTOFFSET"), rs
          .getInt("LAYER"), rs
          .getBoolean("WALLORIENTATION"), rs
          .getInt("SETTINGS")));
      }
    }
    catch (SQLException sqx)
    {
      long e;
      logger.log(Level.WARNING, "Failed to load walls! " + sqx.getMessage(), sqx);
      throw new IOException(sqx);
    }
    finally
    {
      DbUtilities.closeDatabaseObjects(ps, rs);
      DbConnector.returnConnection(dbcon);
      long e = System.nanoTime();
      logger.log(Level.INFO, "Loaded " + walls.size() + " wall. That took " + (float)(e - s) / 1000000.0F + " ms.");
    }
  }
  
  public final float getDamageModifier()
  {
    if (this.type == StructureTypeEnum.RUBBLE) {
      return 0.001F;
    }
    if ((isStone()) || (isPlainStone()) || (isSlate()) || (isRoundedStone()) || 
      (isMarble()) || (isRendered()) || (isPottery()) || (isSandstone())) {
      return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F) * 0.3F;
    }
    return 100.0F / Math.max(1.0F, this.currentQL * (100.0F - this.damage) / 100.0F);
  }
  
  public final Door getDoor()
  {
    if (isDoor())
    {
      try
      {
        for (Door door : getOrCreateInnerTile(getLayer() == 0).getDoors()) {
          try
          {
            if (door.getWall() == this) {
              return door;
            }
          }
          catch (NoSuchWallException localNoSuchWallException) {}
        }
      }
      catch (NoSuchTileException nst)
      {
        logger.log(Level.WARNING, "Why: " + nst.getMessage() + " " + getTileX() + "," + getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this);
      }
      catch (NoSuchZoneException nst)
      {
        logger.log(Level.WARNING, "Why: " + ((NoSuchZoneException)nst).getMessage() + " " + getTileX() + "," + getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this);
      }
      try
      {
        for (Door door : getOrCreateOuterTile(true).getDoors()) {
          try
          {
            if (door.getWall() == this) {
              return door;
            }
          }
          catch (NoSuchWallException localNoSuchWallException1) {}
        }
      }
      catch (NoSuchTileException nst)
      {
        logger.log(Level.WARNING, "Why: " + nst.getMessage() + " " + getTileX() + "," + getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this);
      }
      catch (NoSuchZoneException nst)
      {
        logger.log(Level.WARNING, "Why: " + nst.getMessage() + " " + getTileX() + "," + getTileY() + ", StructureId: " + this.structureId + ", wall id=" + this);
      }
    }
    return null;
  }
  
  public final VolaTile getTile()
  {
    try
    {
      Structure struct = Structures.getStructure(this.structureId);
      return struct.getTileFor(this);
    }
    catch (NoSuchStructureException nss)
    {
      logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + nss
        .getMessage(), nss);
    }
    return null;
  }
  
  private final void removeDoors()
    throws NoSuchStructureException
  {
    Structure struct = Structures.getStructure(this.structureId);
    if (isDoor())
    {
      Door[] doors = struct.getAllDoors();
      for (int x = 0; x < doors.length; x++) {
        try
        {
          if (doors[x].getWall() == this)
          {
            struct.removeDoor(doors[x]);
            doors[x].removeFromTiles();
          }
        }
        catch (NoSuchWallException nsw)
        {
          logger.log(Level.WARNING, "Problem removing doors from wall in StructureId: " + this.structureId + " - " + nsw
            .getMessage(), nsw);
        }
      }
    }
  }
  
  private final void setPlanData()
  {
    this.type = StructureTypeEnum.PLAN;
    this.state = StructureStateEnum.INITIALIZED;
    this.currentQL = 1.0F;
    this.originalQL = 1.0F;
    this.damage = 0.0F;
    this.material = StructureMaterialEnum.WOOD;
  }
  
  private final void setRubbleData()
  {
    this.type = StructureTypeEnum.RUBBLE;
    this.state = StructureStateEnum.FINISHED;
    this.currentQL = 100.0F;
    this.originalQL = 1.0F;
    this.damage = 0.0F;
  }
  
  public final void setAsRubble()
  {
    try
    {
      Structure struct = Structures.getStructure(this.structureId);
      struct.setFinished(false);
      VolaTile tile = struct.getTileFor(this);
      if (tile != null)
      {
        removeDoors();
        setRubbleData();
        setColor(-1);
        tile.updateWall(this);
        addRubble(this);
      }
      else
      {
        logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ": no tile!?  StructureId: " + this.structureId);
      }
    }
    catch (NoSuchStructureException nss)
    {
      logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + nss
        .getMessage(), nss);
    }
  }
  
  public final void setAsPlan()
  {
    try
    {
      Structure struct = Structures.getStructure(this.structureId);
      struct.setFinished(false);
      VolaTile tile = struct.getTileFor(this);
      if (tile != null)
      {
        removeDoors();
        setPlanData();
        setColor(-1);
        tile.updateWall(this);
        removeRubble(this);
      }
      else
      {
        logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ": no tile!?  StructureId: " + this.structureId);
      }
    }
    catch (NoSuchStructureException nss)
    {
      logger.log(Level.WARNING, "wall at " + this.x1 + ", " + this.y1 + "-" + this.x2 + "," + this.y2 + ", StructureId: " + this.structureId + " - " + nss
        .getMessage(), nss);
    }
  }
  
  public final boolean isWindow()
  {
    return (this.type == StructureTypeEnum.WINDOW) || (this.type == StructureTypeEnum.WIDE_WINDOW);
  }
  
  public final boolean isJetty()
  {
    return this.type == StructureTypeEnum.JETTY;
  }
  
  public final boolean isBalcony()
  {
    return this.type == StructureTypeEnum.BALCONY;
  }
  
  public final boolean isOriel()
  {
    return this.type == StructureTypeEnum.ORIEL;
  }
  
  public final float getDamageModifierForItem(Item item)
  {
    float mod = 0.0F;
    if (this.type == StructureTypeEnum.RUBBLE) {
      return 0.01F;
    }
    if ((isWood()) || (isTimberFramed()))
    {
      if (item.isWeaponAxe()) {
        mod = 0.03F;
      } else if (item.isWeaponCrush()) {
        mod = 0.02F;
      } else if (item.isWeaponSlash()) {
        mod = 0.015F;
      } else if (item.isWeaponPierce()) {
        mod = 0.01F;
      } else if (item.isWeaponMisc()) {
        mod = 0.007F;
      }
    }
    else if ((isStone()) || (isPlainStone()) || (isSlate()) || (isRoundedStone()) || 
      (isMarble()) || (isRendered()) || (isPottery()) || (isSandstone())) {
      if (item.getTemplateId() == 20) {
        mod = 0.02F;
      } else if (item.isWeaponCrush()) {
        mod = 0.015F;
      } else if (item.isWeaponAxe()) {
        mod = 0.0075F;
      } else if (item.isWeaponSlash()) {
        mod = 0.005F;
      } else if (item.isWeaponPierce()) {
        mod = 0.005F;
      } else if (item.isWeaponMisc()) {
        mod = 0.002F;
      }
    }
    return mod;
  }
  
  public final StructureMaterialEnum getMaterial()
  {
    return this.material;
  }
  
  public final void setMaterial(StructureMaterialEnum aMaterial)
  {
    this.material = aMaterial;
  }
  
  public final boolean isStone()
  {
    return this.material == StructureMaterialEnum.STONE;
  }
  
  public final boolean isPlainStone()
  {
    return this.material == StructureMaterialEnum.PLAIN_STONE;
  }
  
  public final boolean isSlate()
  {
    return this.material == StructureMaterialEnum.SLATE;
  }
  
  public final boolean isRendered()
  {
    return this.material == StructureMaterialEnum.RENDERED;
  }
  
  public final boolean isRoundedStone()
  {
    return this.material == StructureMaterialEnum.ROUNDED_STONE;
  }
  
  public final boolean isPottery()
  {
    return this.material == StructureMaterialEnum.POTTERY;
  }
  
  public final boolean isSandstone()
  {
    return this.material == StructureMaterialEnum.SANDSTONE;
  }
  
  public final boolean isPlastered()
  {
    return this.material == StructureMaterialEnum.RENDERED;
  }
  
  public final boolean isMarble()
  {
    return this.material == StructureMaterialEnum.MARBLE;
  }
  
  public final boolean canSupportStoneBridges()
  {
    return (isStone()) || (isPlainStone()) || (isMarble()) || (isSandstone()) || (isRoundedStone()) || (isSlate()) || 
      (isRendered()) || (isPottery());
  }
  
  public final boolean isWood()
  {
    return this.material == StructureMaterialEnum.WOOD;
  }
  
  public final boolean isMetal()
  {
    return this.material == StructureMaterialEnum.METAL;
  }
  
  public final boolean isTimberFramed()
  {
    return this.material == StructureMaterialEnum.TIMBER_FRAMED;
  }
  
  public final int getCover()
  {
    if (isFinished())
    {
      if ((isWindow()) || (isJetty())) {
        return 70;
      }
      if (isOriel()) {
        return 80;
      }
      if (isBalcony()) {
        return 10;
      }
      return 100;
    }
    return Math.max(0, getState().state);
  }
  
  public static final int[] getItemTemplatesDealtForWall(StructureTypeEnum type, StructureStateEnum state, boolean finished)
  {
    if (finished)
    {
      int[] toReturn = new int[20];
      for (int x = 0; x < toReturn.length; x++) {
        toReturn[x] = 22;
      }
      return toReturn;
    }
    if ((state.state > 0) && (type != StructureTypeEnum.PLAN))
    {
      int[] toReturn = new int[state.state];
      for (int x = 0; x < state.state; x++) {
        toReturn[x] = 22;
      }
      return toReturn;
    }
    return EMPTY_INT_ARRAY;
  }
  
  public final void setColor(int newcolor)
  {
    changeColor(newcolor);
  }
  
  public final String getMaterialString()
  {
    if (isStone()) {
      return "stone";
    }
    if (isWood()) {
      return "wood";
    }
    if (isTimberFramed()) {
      return "timber framed";
    }
    if (isPlainStone()) {
      return "plain stone";
    }
    if (isSlate()) {
      return "slate";
    }
    if (isRoundedStone()) {
      return "rounded stone";
    }
    if (isPottery()) {
      return "pottery";
    }
    if (isSandstone()) {
      return "sandstone";
    }
    if (isPlastered()) {
      return "rendered";
    }
    if (isMarble()) {
      return "marble";
    }
    return "wood";
  }
  
  public final int[] getTemplateIdsNeededForNextState(StructureTypeEnum type)
  {
    int[] templatesNeeded;
    int[] templatesNeeded;
    if (isHalfArch(type))
    {
      int[] templatesNeeded;
      if (isWood())
      {
        if (this.state == StructureStateEnum.INITIALIZED)
        {
          int[] templatesNeeded = new int[2];
          templatesNeeded[0] = 860;
          templatesNeeded[1] = 217;
        }
        else if (this.state == StructureStateEnum.STATE_2_NEEDED)
        {
          int[] templatesNeeded = new int[1];
          templatesNeeded[0] = 22;
        }
        else if (!isFinished())
        {
          int[] templatesNeeded = new int[1];
          templatesNeeded[0] = 22;
        }
        else
        {
          templatesNeeded = emptyArr;
        }
      }
      else
      {
        int[] templatesNeeded;
        if (isTimberFramed())
        {
          if ((this.state == StructureStateEnum.INITIALIZED) || (this.state.state < 7))
          {
            int[] templatesNeeded = new int[1];
            templatesNeeded[0] = 860;
          }
          else if (this.state.state < 17)
          {
            int[] templatesNeeded = new int[2];
            templatesNeeded[0] = 620;
            templatesNeeded[1] = 130;
          }
          else if (this.state.state < getFinalState().state)
          {
            int[] templatesNeeded = new int[1];
            templatesNeeded[0] = 130;
          }
          else
          {
            templatesNeeded = emptyArr;
          }
        }
        else if (this.state == StructureStateEnum.INITIALIZED)
        {
          int[] templatesNeeded = new int[1];
          templatesNeeded[0] = 681;
        }
        else if (!isFinished())
        {
          int[] templatesNeeded = new int[2];
          templatesNeeded[0] = getBrickFromType();
          templatesNeeded[1] = 492;
        }
        else
        {
          templatesNeeded = emptyArr;
        }
      }
    }
    else
    {
      int[] templatesNeeded;
      if (isWood())
      {
        if (this.state == StructureStateEnum.INITIALIZED)
        {
          int[] templatesNeeded = new int[2];
          templatesNeeded[0] = 22;
          templatesNeeded[1] = 217;
        }
        else if (!isFinished())
        {
          int[] templatesNeeded = new int[1];
          templatesNeeded[0] = 22;
        }
        else
        {
          templatesNeeded = emptyArr;
        }
      }
      else
      {
        int[] templatesNeeded;
        if (isTimberFramed())
        {
          if ((this.state == StructureStateEnum.INITIALIZED) || (this.state.state < 6))
          {
            int[] templatesNeeded = new int[1];
            templatesNeeded[0] = 860;
          }
          else if (this.state.state < 16)
          {
            int[] templatesNeeded = new int[2];
            templatesNeeded[0] = 620;
            templatesNeeded[1] = 130;
          }
          else if (this.state.state < getFinalState().state)
          {
            int[] templatesNeeded = new int[1];
            templatesNeeded[0] = 130;
          }
          else
          {
            templatesNeeded = emptyArr;
          }
        }
        else if (!isFinished())
        {
          int[] templatesNeeded = new int[2];
          templatesNeeded[0] = getBrickFromType();
          templatesNeeded[1] = 492;
        }
        else
        {
          templatesNeeded = emptyArr;
        }
      }
    }
    return templatesNeeded;
  }
  
  public final String getBrickName()
  {
    String brickType;
    String brickType;
    if (isSlate())
    {
      brickType = "slate brick";
    }
    else
    {
      String brickType;
      if (isRoundedStone())
      {
        brickType = "rounded stone";
      }
      else
      {
        String brickType;
        if (isPottery())
        {
          brickType = "pottery brick";
        }
        else
        {
          String brickType;
          if (isSandstone())
          {
            brickType = "sandstone brick";
          }
          else
          {
            String brickType;
            if (isMarble()) {
              brickType = "marble brick";
            } else {
              brickType = "stone brick";
            }
          }
        }
      }
    }
    return brickType;
  }
  
  public final int getBrickFromType()
  {
    return getBrickFromType(this.material);
  }
  
  public static final int getBrickFromType(StructureMaterialEnum material)
  {
    if (material == StructureMaterialEnum.SLATE) {
      return 1123;
    }
    if (material == StructureMaterialEnum.ROUNDED_STONE) {
      return 1122;
    }
    if (material == StructureMaterialEnum.POTTERY) {
      return 776;
    }
    if (material == StructureMaterialEnum.SANDSTONE) {
      return 1121;
    }
    if (material == StructureMaterialEnum.MARBLE) {
      return 786;
    }
    return 132;
  }
  
  public final int getRepairItemTemplate()
  {
    if (isWood()) {
      return 22;
    }
    if ((isStone()) || (isPlainStone()) || (isRendered())) {
      return 132;
    }
    if (isRoundedStone()) {
      return 1122;
    }
    if (isPottery()) {
      return 776;
    }
    if (isSandstone()) {
      return 1121;
    }
    if (isMarble()) {
      return 786;
    }
    if (isSlate()) {
      return 1123;
    }
    return 22;
  }
  
  public static final Wall getWall(long wid)
  {
    int x = Tiles.decodeTileX(wid);
    int y = Tiles.decodeTileY(wid);
    boolean onSurface = Tiles.decodeLayer(wid) == 0;
    for (int xx = 1; xx >= -1; xx--) {
      for (int yy = 1; yy >= -1; yy--) {
        try
        {
          Zone zone = Zones.getZone(x + xx, y + yy, onSurface);
          VolaTile tile = zone.getTileOrNull(x + xx, y + yy);
          if (tile != null)
          {
            Wall[] wallarr = tile.getWalls();
            for (int s = 0; s < wallarr.length; s++) {
              if (wallarr[s].getId() == wid) {
                return wallarr[s];
              }
            }
          }
        }
        catch (NoSuchZoneException localNoSuchZoneException) {}
      }
    }
    return null;
  }
  
  public long getLastUsed()
  {
    return this.lastUsed;
  }
  
  public abstract void save()
    throws IOException;
  
  abstract void load()
    throws IOException;
  
  public abstract void setLastUsed(long paramLong);
  
  public abstract void improveOrigQualityLevel(float paramFloat);
  
  abstract boolean changeColor(int paramInt);
  
  public abstract void setWallOrientation(boolean paramBoolean);
  
  public abstract void delete();
  
  public final int getHeight()
  {
    return this.heightOffset;
  }
  
  public void setHeightOffset(int newHeightOffset)
  {
    this.heightOffset = newHeightOffset;
    setFloorLevel();
  }
  
  public final boolean isOnFloorLevel(int level)
  {
    return level == this.floorLevel;
  }
  
  public final int getFloorLevel()
  {
    return this.floorLevel;
  }
  
  private final void setFloorLevel()
  {
    this.floorLevel = (this.heightOffset / 30);
  }
  
  public float getFloorZ()
  {
    return this.heightOffset / 10;
  }
  
  public float getMinZ()
  {
    return Zones.getHeightForNode(getTileX(), getTileY(), getLayer()) + getFloorZ();
  }
  
  public float getMaxZ()
  {
    return getMinZ() + 3.0F;
  }
  
  public boolean isWithinZ(float maxZ, float minZ, boolean followGround)
  {
    return ((getFloorLevel() == 0) && (followGround)) || ((minZ <= getMaxZ()) && (maxZ >= getMinZ()));
  }
  
  public byte getLayer()
  {
    return this.layer;
  }
  
  public boolean isOnSurface()
  {
    return this.layer == 0;
  }
  
  public abstract void setIndoor(boolean paramBoolean);
  
  public boolean isIndoor()
  {
    return this.isIndoor;
  }
  
  public void destroy()
  {
    if (!isIndoor())
    {
      setAsPlan();
      return;
    }
    if (!MethodsStructure.isWallInsideStructure(this, isOnSurface()))
    {
      setAsPlan();
      return;
    }
    removeRubble(this);
    removeIndoorWall();
  }
  
  private final void removeIndoorWall()
  {
    if (!isIndoor())
    {
      logger.log(Level.WARNING, "Tried to wall.remove() completely for an outdoor wall!");
      return;
    }
    if (!MethodsStructure.isWallInsideStructure(this, isOnSurface()))
    {
      logger.log(Level.WARNING, "Tried to wall.remove() completely next to a wall without structure tiles on both sides!");
      return;
    }
    try
    {
      removeDoors();
    }
    catch (NoSuchStructureException nse)
    {
      logger.log(Level.WARNING, "Structure not found when trying to remove doors from wall " + getStructureId());
      return;
    }
    delete();
    
    VolaTile myTile = getTile();
    if (myTile != null) {
      myTile.removeWall(this, false);
    } else {
      logger.log(Level.INFO, getName() + " at " + getTileX() + "," + getTileY() + " not removed from tile since we couldn't locate it.");
    }
    Set<Wall> flset = (Set)walls.get(Long.valueOf(getStructureId()));
    if (flset != null) {
      flset.remove(this);
    }
  }
  
  public boolean isWallPlan()
  {
    return getState() == StructureStateEnum.INITIALIZED;
  }
  
  public final boolean isRubble()
  {
    return getType() == StructureTypeEnum.RUBBLE;
  }
  
  public boolean isAlwaysOpen()
  {
    return isArched();
  }
  
  public final boolean isWithinFloorLevels(int maxFloorLevel, int minFloorLevel)
  {
    return (this.floorLevel <= maxFloorLevel) && (this.floorLevel >= minFloorLevel);
  }
  
  public boolean supports(StructureSupport support)
  {
    if (!supports()) {
      return false;
    }
    if (support.isFloor())
    {
      if ((getFloorLevel() == support.getFloorLevel()) || (getFloorLevel() == support.getFloorLevel() - 1)) {
        if (isHorizontal())
        {
          if (getMinX() == support.getMinX()) {
            if ((getMinY() == support.getStartY()) || (getStartY() == support.getEndY())) {
              return true;
            }
          }
        }
        else if (getMinY() == support.getMinY()) {
          if ((getMinX() == support.getStartX()) || (getMinX() == support.getEndX())) {
            return true;
          }
        }
      }
    }
    else
    {
      int levelMod = support.supports() ? -1 : 0;
      if ((support.getFloorLevel() >= getFloorLevel() + levelMod) && (support.getFloorLevel() <= getFloorLevel() + 1)) {
        if ((support.getMinX() == getMinX()) && (support.getMinY() == getMinY()) && 
          (isHorizontal() == support.isHorizontal())) {
          return true;
        }
      }
    }
    return false;
  }
  
  public final boolean supports()
  {
    return true;
  }
  
  public boolean isSupportedByGround()
  {
    return getFloorLevel() == 0;
  }
  
  public String toString()
  {
    return 
      "Wall [number=" + this.number + ", structureId=" + this.structureId + ", type=" + this.type + ", material=" + getMaterial() + ", QL=" + getQualityLevel() + ", DMG=" + getDamage() + "]";
  }
  
  public long getTempId()
  {
    return -10L;
  }
  
  public String getTypeName()
  {
    return WallEnum.getWall(getType(), getMaterial()).getName();
  }
  
  public boolean setTile(int newTileX, int newTileY)
  {
    this.tilex = newTileX;
    this.tiley = newTileY;
    try
    {
      save();
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, StringUtil.format("Failed to move wall to %d,%d: %s", new Object[] { Integer.valueOf(newTileX), Integer.valueOf(newTileY), e.getMessage() }), e);
    }
    return true;
  }
  
  void setSettings(int aSettings)
  {
    this.permissions.setPermissionBits(aSettings);
  }
  
  Permissions getSettings()
  {
    return this.permissions;
  }
  
  public boolean canBeAlwaysLit()
  {
    return false;
  }
  
  public boolean canBeAutoFilled()
  {
    return false;
  }
  
  public boolean canBeAutoLit()
  {
    return false;
  }
  
  public final boolean canBePeggedByPlayer()
  {
    return false;
  }
  
  public boolean canBePlanted()
  {
    return false;
  }
  
  public final boolean canBeSealedByPlayer()
  {
    return false;
  }
  
  public boolean canChangeCreator()
  {
    return false;
  }
  
  public boolean canDisableDecay()
  {
    return true;
  }
  
  public boolean canDisableDestroy()
  {
    return true;
  }
  
  public boolean canDisableDrag()
  {
    return false;
  }
  
  public boolean canDisableDrop()
  {
    return false;
  }
  
  public boolean canDisableEatAndDrink()
  {
    return false;
  }
  
  public boolean canDisableImprove()
  {
    return true;
  }
  
  public boolean canDisableLocking()
  {
    return isDoor();
  }
  
  public boolean canDisableLockpicking()
  {
    return isDoor();
  }
  
  public boolean canDisableMoveable()
  {
    return false;
  }
  
  public final boolean canDisableOwnerMoveing()
  {
    return false;
  }
  
  public final boolean canDisableOwnerTurning()
  {
    return false;
  }
  
  public boolean canDisablePainting()
  {
    return true;
  }
  
  public boolean canDisablePut()
  {
    return false;
  }
  
  public boolean canDisableRepair()
  {
    return true;
  }
  
  public boolean canDisableRuneing()
  {
    return false;
  }
  
  public boolean canDisableSpellTarget()
  {
    return false;
  }
  
  public boolean canDisableTake()
  {
    return false;
  }
  
  public boolean canDisableTurning()
  {
    return true;
  }
  
  public boolean canHaveCourier()
  {
    return false;
  }
  
  public boolean canHaveDakrMessenger()
  {
    return false;
  }
  
  public String getCreatorName()
  {
    return null;
  }
  
  public float getDamage()
  {
    return this.damage;
  }
  
  public float getQualityLevel()
  {
    return this.currentQL;
  }
  
  public boolean hasCourier()
  {
    return this.permissions.hasPermission(Permissions.Allow.HAS_COURIER.getBit());
  }
  
  public boolean hasDarkMessenger()
  {
    return this.permissions.hasPermission(Permissions.Allow.HAS_DARK_MESSENGER.getBit());
  }
  
  public boolean hasNoDecay()
  {
    return this.permissions.hasPermission(Permissions.Allow.DECAY_DISABLED.getBit());
  }
  
  public boolean isAlwaysLit()
  {
    return this.permissions.hasPermission(Permissions.Allow.ALWAYS_LIT.getBit());
  }
  
  public boolean isAutoFilled()
  {
    return this.permissions.hasPermission(Permissions.Allow.AUTO_FILL.getBit());
  }
  
  public boolean isAutoLit()
  {
    return this.permissions.hasPermission(Permissions.Allow.AUTO_LIGHT.getBit());
  }
  
  public boolean isIndestructible()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_BASH.getBit());
  }
  
  public boolean isNoDrag()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_DRAG.getBit());
  }
  
  public boolean isNoDrop()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_DROP.getBit());
  }
  
  public boolean isNoEatOrDrink()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_EAT_OR_DRINK.getBit());
  }
  
  public boolean isNoImprove()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_IMPROVE.getBit());
  }
  
  public boolean isNoMove()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_MOVEABLE.getBit());
  }
  
  public boolean isNoPut()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_PUT.getBit());
  }
  
  public boolean isNoRepair()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_REPAIR.getBit());
  }
  
  public boolean isNoTake()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_TAKE.getBit());
  }
  
  public boolean isNotLockable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKABLE.getBit());
  }
  
  public boolean isNotLockpickable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_LOCKPICKABLE.getBit());
  }
  
  public boolean isNotPaintable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_PAINTABLE.getBit());
  }
  
  public boolean isNotRuneable()
  {
    return true;
  }
  
  public boolean isNotSpellTarget()
  {
    return this.permissions.hasPermission(Permissions.Allow.NO_SPELLS.getBit());
  }
  
  public boolean isNotTurnable()
  {
    return this.permissions.hasPermission(Permissions.Allow.NOT_TURNABLE.getBit());
  }
  
  public boolean isOwnerMoveable()
  {
    return this.permissions.hasPermission(Permissions.Allow.OWNER_MOVEABLE.getBit());
  }
  
  public boolean isOwnerTurnable()
  {
    return this.permissions.hasPermission(Permissions.Allow.OWNER_TURNABLE.getBit());
  }
  
  public boolean isPlanted()
  {
    return this.permissions.hasPermission(Permissions.Allow.PLANTED.getBit());
  }
  
  public final boolean isSealedByPlayer()
  {
    if (this.permissions.hasPermission(Permissions.Allow.SEALED_BY_PLAYER.getBit())) {
      return true;
    }
    return false;
  }
  
  public void setCreator(String aNewCreator) {}
  
  public abstract boolean setDamage(float paramFloat);
  
  public void setHasCourier(boolean aCourier)
  {
    this.permissions.setPermissionBit(Permissions.Allow.HAS_COURIER.getBit(), aCourier);
  }
  
  public void setHasDarkMessenger(boolean aDarkmessenger)
  {
    this.permissions.setPermissionBit(Permissions.Allow.HAS_DARK_MESSENGER.getBit(), aDarkmessenger);
  }
  
  public void setHasNoDecay(boolean aNoDecay)
  {
    this.permissions.setPermissionBit(Permissions.Allow.DECAY_DISABLED.getBit(), aNoDecay);
  }
  
  public void setIsAlwaysLit(boolean aAlwaysLit)
  {
    this.permissions.setPermissionBit(Permissions.Allow.ALWAYS_LIT.getBit(), aAlwaysLit);
  }
  
  public void setIsAutoFilled(boolean aAutoFill)
  {
    this.permissions.setPermissionBit(Permissions.Allow.AUTO_FILL.getBit(), aAutoFill);
  }
  
  public void setIsAutoLit(boolean aAutoLight)
  {
    this.permissions.setPermissionBit(Permissions.Allow.AUTO_LIGHT.getBit(), aAutoLight);
  }
  
  public void setIsIndestructible(boolean aNoDestroy)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_BASH.getBit(), aNoDestroy);
  }
  
  public void setIsNoDrag(boolean aNoDrag)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_DRAG.getBit(), aNoDrag);
  }
  
  public void setIsNoDrop(boolean aNoDrop)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_DROP.getBit(), aNoDrop);
  }
  
  public void setIsNoEatOrDrink(boolean aNoEatOrDrink)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_EAT_OR_DRINK.getBit(), aNoEatOrDrink);
  }
  
  public void setIsNoImprove(boolean aNoImprove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_IMPROVE.getBit(), aNoImprove);
  }
  
  public void setIsNoMove(boolean aNoMove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_MOVEABLE.getBit(), aNoMove);
  }
  
  public void setIsNoPut(boolean aNoPut)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_PUT.getBit(), aNoPut);
  }
  
  public void setIsNoRepair(boolean aNoRepair)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_REPAIR.getBit(), aNoRepair);
  }
  
  public void setIsNoTake(boolean aNoTake)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_TAKE.getBit(), aNoTake);
  }
  
  public void setIsNotLockable(boolean aNoLock)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKABLE.getBit(), aNoLock);
  }
  
  public void setIsNotLockpickable(boolean aNoLockpick)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_LOCKPICKABLE.getBit(), aNoLockpick);
  }
  
  public void setIsNotPaintable(boolean aNoPaint)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_PAINTABLE.getBit(), aNoPaint);
  }
  
  public void setIsNotRuneable(boolean aNoRune)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_RUNEABLE.getBit(), aNoRune);
  }
  
  public void setIsNotSpellTarget(boolean aNoSpells)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NO_SPELLS.getBit(), aNoSpells);
  }
  
  public void setIsNotTurnable(boolean aNoTurn)
  {
    this.permissions.setPermissionBit(Permissions.Allow.NOT_TURNABLE.getBit(), aNoTurn);
  }
  
  public void setIsOwnerMoveable(boolean aOwnerMove)
  {
    this.permissions.setPermissionBit(Permissions.Allow.OWNER_MOVEABLE.getBit(), aOwnerMove);
  }
  
  public void setIsOwnerTurnable(boolean aOwnerTurn)
  {
    this.permissions.setPermissionBit(Permissions.Allow.OWNER_TURNABLE.getBit(), aOwnerTurn);
  }
  
  public void setIsPlanted(boolean aPlant)
  {
    this.permissions.setPermissionBit(Permissions.Allow.PLANTED.getBit(), aPlant);
  }
  
  public void setIsSealedByPlayer(boolean aSealed)
  {
    this.permissions.setPermissionBit(Permissions.Allow.SEALED_BY_PLAYER.getBit(), aSealed);
  }
  
  public abstract boolean setQualityLevel(float paramFloat);
  
  public void setOriginalQualityLevel(float newQL) {}
  
  public abstract void savePermissions();
  
  public final boolean isOnSouthBorder(TilePos pos)
  {
    return ((getStartX() == pos.x) || (getEndX() == pos.x)) && (getEndY() == pos.y + 1) && (getStartY() == pos.y + 1);
  }
  
  public final boolean isOnNorthBorder(TilePos pos)
  {
    return ((getStartX() == pos.x) || (getEndX() == pos.x)) && (getEndY() == pos.y) && (getStartY() == pos.y);
  }
  
  public final boolean isOnWestBorder(TilePos pos)
  {
    return (getStartX() == pos.x) && (getEndX() == pos.x) && ((getEndY() == pos.y) || (getStartY() == pos.y));
  }
  
  public final boolean isOnEastBorder(TilePos pos)
  {
    return (getStartX() == pos.x + 1) && (getEndX() == pos.x + 1) && ((getEndY() == pos.y) || (getStartY() == pos.y));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\structures\Wall.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */