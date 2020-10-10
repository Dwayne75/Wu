package com.wurmonline.shared.util;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.mesh.Tiles.Tile;
import java.io.PrintStream;
import java.util.logging.Logger;

public abstract class MovementChecker
{
  protected static final Logger logger = Logger.getLogger(MovementChecker.class.getName());
  int blocks = 0;
  public static final float DEGS_TO_RADS = 0.017453292F;
  public static final int BIT_FORWARD = 1;
  public static final int BIT_BACK = 2;
  public static final int BIT_LEFT = 4;
  public static final int BIT_RIGHT = 8;
  private static final float WALK_SPEED = 0.08F;
  public static final float FLOATING_HEIGHT = -1.45F;
  public boolean serverWestAvailable = true;
  public boolean serverNorthAvailable = true;
  public boolean serverEastAvailable = true;
  public boolean serverSouthAvailable = true;
  private static final float CLIMB_SPEED_MODIFIER = 0.25F;
  private float speedMod = 1.0F;
  private boolean climbing = false;
  private long bridgeId = -10L;
  private int bridgeCounter = 0;
  private float x;
  private float y;
  private float z;
  private float xRot;
  public float xOld;
  public float yOld;
  private float zOld;
  private float xa;
  private float ya;
  private float za;
  private float groundOffset;
  private int targetGroundOffset;
  private byte bitmask;
  public boolean onGround = false;
  public boolean inWater = false;
  private int layer = 0;
  private boolean abort = false;
  public boolean ignoreErrors = false;
  public boolean started = false;
  public float diffWindX = 0.0F;
  public float diffWindY = 0.0F;
  private float windRotation = 0.0F;
  private float windStrength = 0.0F;
  private float windImpact = 0.0F;
  private float mountSpeed = 0.1F;
  public boolean commandingBoat = false;
  private float vehicleRotation = 0.0F;
  private float currx;
  private float curry;
  private boolean first = true;
  public static final float MINHEIGHTC = -3000.0F;
  public static final float MAXHEIGHTC = 3000.0F;
  private static final float fallMod = 0.04F;
  private static final float deltaH = 1.0F;
  private static final float moveMod = 0.4F;
  private boolean movingVehicle = false;
  private float leftTurnMod = 1.0F;
  private float rightTurnMod = 1.0F;
  public float offZ = 0.0F;
  private boolean flying = false;
  protected boolean wasOnStair = false;
  private int counter = 0;
  private boolean acceptedError = false;
  private boolean isFalling = false;
  private boolean onFloorOverridden = false;
  
  protected strictfp boolean isPressed(int key)
  {
    return (this.bitmask & key) != 0;
  }
  
  public final strictfp void setFlying(boolean fly)
  {
    this.flying = fly;
  }
  
  public final strictfp void setIsFalling(boolean falling)
  {
    this.isFalling = falling;
  }
  
  public strictfp boolean isFalling()
  {
    return this.isFalling;
  }
  
  public final strictfp boolean isFlying()
  {
    return this.flying;
  }
  
  public strictfp boolean isAborted()
  {
    return this.abort;
  }
  
  protected strictfp void setAbort(boolean abort)
  {
    this.abort = abort;
  }
  
  public strictfp boolean isKeyPressed()
  {
    return (isPressed(1)) || (isPressed(2)) || (isPressed(4)) || (isPressed(8));
  }
  
  public strictfp void resetBm()
  {
    this.bitmask = 0;
  }
  
  float mhdlog = 0.0F;
  
  public final strictfp void movestep(float maxHeightDiff, float xTarget, float zTarget, float yTarget, float maxDepth, float maxHeight, float rotation, byte _bitmask, int estimatedLayer)
  {
    this.abort = false;
    this.currx = xTarget;
    this.curry = yTarget;
    this.movingVehicle = false;
    
    this.mhdlog = maxHeightDiff;
    if (estimatedLayer != this.layer) {
      handleWrongLayer(estimatedLayer, this.layer);
    }
    boolean isCommanding = isCommanding(maxDepth, maxHeight);
    if (isCommanding) {
      maybePrintDebugInfo(1);
    }
    if ((xTarget != this.x) || (yTarget != this.y))
    {
      float expxDist = this.x - this.xOld;
      float expyDist = this.y - this.yOld;
      float expectedDistance = (float)StrictMath.sqrt(expxDist * expxDist + expyDist * expyDist);
      
      float realxDist = xTarget - this.xOld;
      float realyDist = yTarget - this.yOld;
      float realDistance = (float)StrictMath.sqrt(realxDist * realxDist + realyDist * realyDist);
      if ((this.bridgeCounter <= 0) && (!movedOnStair())) {
        if ((!this.isFalling) && (realDistance > expectedDistance))
        {
          if (this.acceptedError) {
            handleMoveTooFar(realDistance, expectedDistance);
          } else {
            this.acceptedError = true;
          }
        }
        else if (this.acceptedError) {
          handleMoveTooShort(realDistance, expectedDistance);
        } else {
          this.acceptedError = true;
        }
      }
    }
    else if ((zTarget != this.z) && (Math.abs(zTarget - this.z) > 0.25F))
    {
      if ((!this.isFalling) && 
        (this.bridgeCounter <= 0) && (this.bridgeId == -10L) && (!movedOnStair())) {
        if (this.acceptedError) {
          handleZError(zTarget, this.z);
        } else {
          this.acceptedError = true;
        }
      }
    }
    else
    {
      this.acceptedError = false;
    }
    this.bridgeCounter = Math.max(0, this.bridgeCounter - 1);
    if (this.abort) {
      return;
    }
    int currentTileX = (int)(xTarget / 4.0F);
    int currentTileY = (int)(yTarget / 4.0F);
    
    this.x = xTarget;
    this.y = yTarget;
    this.z = zTarget;
    this.layer = estimatedLayer;
    this.xRot = rotation;
    this.bitmask = _bitmask;
    
    float speedModifier = this.speedMod;
    float heightTarget = getHeight(this.x, this.y, -3000.0F);
    
    this.inWater = (this.z + this.za <= -1.0F);
    if (isCommanding) {
      this.inWater = false;
    }
    int dirs = 0;
    float xPosMod = 0.0F;
    float yPosMod = 0.0F;
    if ((!this.onGround) && (!this.inWater) && (!this.commandingBoat) && (!isOnFloor())) {
      speedModifier *= 0.1F;
    }
    speedModifier *= 1.5F;
    if (this.climbing) {
      speedModifier *= 0.25F;
    }
    if (isPressed(1))
    {
      dirs++;
      if (isCommanding)
      {
        if (speedModifier > 0.0F)
        {
          xPosMod += (float)StrictMath.sin(this.vehicleRotation * 0.017453292F) * this.mountSpeed;
          if ((!this.serverWestAvailable) && (xPosMod < 0.0F)) {
            xPosMod = 0.0F;
          } else if ((!this.serverEastAvailable) && (xPosMod > 0.0F)) {
            xPosMod = 0.0F;
          }
          yPosMod -= (float)StrictMath.cos(this.vehicleRotation * 0.017453292F) * this.mountSpeed;
          if ((!this.serverNorthAvailable) && (yPosMod < 0.0F)) {
            yPosMod = 0.0F;
          } else if ((!this.serverSouthAvailable) && (yPosMod > 0.0F)) {
            yPosMod = 0.0F;
          }
          this.movingVehicle = true;
        }
      }
      else
      {
        xPosMod += (float)StrictMath.sin(rotation * 0.017453292F) * 0.08F * speedModifier;
        yPosMod -= (float)StrictMath.cos(rotation * 0.017453292F) * 0.08F * speedModifier;
      }
    }
    if (isPressed(2))
    {
      dirs++;
      if (isCommanding)
      {
        if (speedModifier > 0.0F)
        {
          xPosMod -= (float)StrictMath.sin(this.vehicleRotation * 0.017453292F) * this.mountSpeed * 0.3F;
          if ((!this.serverWestAvailable) && (xPosMod < 0.0F)) {
            xPosMod = 0.0F;
          } else if ((!this.serverEastAvailable) && (xPosMod > 0.0F)) {
            xPosMod = 0.0F;
          }
          yPosMod += (float)StrictMath.cos(this.vehicleRotation * 0.017453292F) * this.mountSpeed * 0.3F;
          if ((!this.serverNorthAvailable) && (yPosMod < 0.0F)) {
            yPosMod = 0.0F;
          } else if ((!this.serverSouthAvailable) && (yPosMod > 0.0F)) {
            yPosMod = 0.0F;
          }
          this.movingVehicle = true;
        }
      }
      else
      {
        xPosMod -= (float)StrictMath.sin(rotation * 0.017453292F) * 0.08F * speedModifier;
        yPosMod += (float)StrictMath.cos(rotation * 0.017453292F) * 0.08F * speedModifier;
      }
    }
    if (isPressed(4))
    {
      dirs++;
      if (isCommanding)
      {
        if ((!this.commandingBoat) || (this.windImpact != 0.0F)) {
          if (!this.commandingBoat)
          {
            if (this.movingVehicle)
            {
              this.leftTurnMod += 1.0F;
              int mod = 3;
              if (this.leftTurnMod > 20.0F) {
                mod = 2;
              }
              if (this.leftTurnMod > 40.0F) {
                mod = 1;
              }
              this.vehicleRotation = normalizeAngle(this.vehicleRotation - mod);
            }
            else if (speedModifier > 0.0F)
            {
              this.leftTurnMod += 1.0F;
              int mod = 3;
              if (this.leftTurnMod > 20.0F) {
                mod = 2;
              }
              if (this.leftTurnMod > 40.0F) {
                mod = 1;
              }
              this.vehicleRotation = normalizeAngle(this.vehicleRotation - mod);
              xPosMod += (float)StrictMath.sin(this.vehicleRotation * 0.017453292F) * this.mountSpeed * 0.3F;
              if ((!this.serverWestAvailable) && (xPosMod < 0.0F)) {
                xPosMod = 0.0F;
              } else if ((!this.serverEastAvailable) && (xPosMod > 0.0F)) {
                xPosMod = 0.0F;
              }
              yPosMod -= (float)StrictMath.cos(this.vehicleRotation * 0.017453292F) * this.mountSpeed * 0.3F;
              if ((!this.serverNorthAvailable) && (yPosMod < 0.0F)) {
                yPosMod = 0.0F;
              } else if ((!this.serverSouthAvailable) && (yPosMod > 0.0F)) {
                yPosMod = 0.0F;
              }
              this.movingVehicle = true;
            }
          }
          else {
            this.vehicleRotation = normalizeAngle(this.vehicleRotation - 1.0F);
          }
        }
      }
      else
      {
        xPosMod -= (float)StrictMath.cos(rotation * 0.017453292F) * 0.08F * speedModifier;
        yPosMod -= (float)StrictMath.sin(rotation * 0.017453292F) * 0.08F * speedModifier;
      }
    }
    else
    {
      this.leftTurnMod = 0.0F;
    }
    if (isPressed(8))
    {
      dirs++;
      if (isCommanding)
      {
        if ((!this.commandingBoat) || (this.windImpact != 0.0F)) {
          if (!this.commandingBoat)
          {
            if (this.movingVehicle)
            {
              this.rightTurnMod += 1.0F;
              int mod = 3;
              if (this.rightTurnMod > 20.0F) {
                mod = 2;
              }
              if (this.rightTurnMod > 40.0F) {
                mod = 1;
              }
              this.vehicleRotation = normalizeAngle(this.vehicleRotation + mod);
            }
            else if (speedModifier > 0.0F)
            {
              this.rightTurnMod += 1.0F;
              int mod = 3;
              if (this.rightTurnMod > 20.0F) {
                mod = 2;
              }
              if (this.rightTurnMod > 40.0F) {
                mod = 1;
              }
              this.vehicleRotation = normalizeAngle(this.vehicleRotation + mod);
              xPosMod += (float)StrictMath.sin(this.vehicleRotation * 0.017453292F) * this.mountSpeed * 0.3F;
              if ((!this.serverWestAvailable) && (xPosMod < 0.0F)) {
                xPosMod = 0.0F;
              } else if ((!this.serverEastAvailable) && (xPosMod > 0.0F)) {
                xPosMod = 0.0F;
              }
              yPosMod -= (float)StrictMath.cos(this.vehicleRotation * 0.017453292F) * this.mountSpeed * 0.3F;
              if ((!this.serverNorthAvailable) && (yPosMod < 0.0F)) {
                yPosMod = 0.0F;
              } else if ((!this.serverSouthAvailable) && (yPosMod > 0.0F)) {
                yPosMod = 0.0F;
              }
              this.movingVehicle = true;
            }
          }
          else {
            this.vehicleRotation = normalizeAngle(this.vehicleRotation + 1.0F);
          }
        }
      }
      else
      {
        xPosMod += (float)StrictMath.cos(rotation * 0.017453292F) * 0.08F * speedModifier;
        yPosMod += (float)StrictMath.sin(rotation * 0.017453292F) * 0.08F * speedModifier;
      }
    }
    else
    {
      this.rightTurnMod = 0.0F;
    }
    if (dirs > 0)
    {
      this.xa = ((float)(this.xa + xPosMod / StrictMath.sqrt(dirs)));
      this.ya = ((float)(this.ya + yPosMod / StrictMath.sqrt(dirs)));
    }
    if ((this.windImpact != 0.0F) && (speedModifier > 0.0F))
    {
      float strength = getWindPower(this.windRotation - 180.0F, this.vehicleRotation);
      float driftx = this.diffWindX * this.windImpact * 0.05F;
      float drifty = this.diffWindY * this.windImpact * 0.05F;
      if ((!this.serverWestAvailable) && (driftx < 0.0F)) {
        driftx = 0.0F;
      }
      if ((!this.serverEastAvailable) && (driftx > 0.0F)) {
        driftx = 0.0F;
      }
      if ((!this.serverSouthAvailable) && (drifty > 0.0F)) {
        drifty = 0.0F;
      }
      if ((!this.serverNorthAvailable) && (drifty < 0.0F)) {
        drifty = 0.0F;
      }
      this.xa += driftx;
      this.ya += drifty;
      
      float windx = (float)StrictMath.sin(this.vehicleRotation * 0.017453292F) * Math.abs(this.windStrength) * this.windImpact * strength;
      
      float windy = (float)StrictMath.cos(this.vehicleRotation * 0.017453292F) * Math.abs(this.windStrength) * this.windImpact * strength;
      if ((!this.serverWestAvailable) && (windx < 0.0F)) {
        windx = 0.0F;
      }
      if ((!this.serverEastAvailable) && (windx > 0.0F)) {
        windx = 0.0F;
      }
      if ((!this.serverSouthAvailable) && (windy > 0.0F)) {
        windy = 0.0F;
      }
      if ((!this.serverNorthAvailable) && (windy < 0.0F)) {
        windy = 0.0F;
      }
      this.xa += windx;
      this.ya -= windy;
    }
    float waterHeight = -1.45F;
    if (this.commandingBoat)
    {
      float dHeight = getHeight(this.x + this.xa, this.y + this.ya, getHeight(this.x, this.y, -3000.0F));
      if ((dHeight < maxDepth) || (dHeight > maxHeight))
      {
        this.xa = 0.0F;
        this.ya = 0.0F;
      }
      if ((this.layer == 0) && (getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id))
      {
        this.layer = -1;
        setLayer(this.layer);
      }
    }
    else if (heightTarget < waterHeight)
    {
      heightTarget = waterHeight;
      this.xa *= 0.6F;
      this.ya *= 0.6F;
      float dHeight = getHeight(this.x + this.xa, this.y + this.ya, getHeight(this.x, this.y, -3000.0F));
      if ((dHeight < maxDepth) || (dHeight > maxHeight))
      {
        this.xa = 0.0F;
        this.ya = 0.0F;
      }
      if (this.onGround) {
        if ((this.layer == 0) && (getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id))
        {
          this.layer = -1;
          setLayer(this.layer);
        }
      }
    }
    else if (this.onGround)
    {
      if ((this.layer == 0) && (getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id))
      {
        this.layer = -1;
        setLayer(this.layer);
      }
      float tileSpeedMod = isOnFloor() ? 1.0F : getSpeedForTile(currentTileX, currentTileY, this.layer);
      this.xa *= tileSpeedMod;
      this.ya *= tileSpeedMod;
      float dHeight = getHeight(this.x + this.xa, this.y + this.ya, getHeight(this.x, this.y, -3000.0F));
      if ((dHeight < maxDepth) || (dHeight > maxHeight))
      {
        this.xa = 0.0F;
        this.ya = 0.0F;
      }
      else
      {
        float hDiff = getHeight(this.x + this.xa, this.y + this.ya, heightTarget) - heightTarget;
        if (hDiff > 0.0F)
        {
          float dist = (float)StrictMath.sqrt(this.xa * this.xa + this.ya * this.ya);
          this.xa /= (hDiff * hDiff / dist * 10.0F + 1.0F);
          this.ya /= (hDiff * hDiff / dist * 10.0F + 1.0F);
        }
        int ntx = (int)StrictMath.floor((this.x + this.xa) / 4.0F);
        int nty = (int)StrictMath.floor((this.y + this.ya) / 4.0F);
        if ((currentTileX != ntx) || (currentTileY != nty))
        {
          byte text = getTextureForTile(ntx, nty, this.layer, this.bridgeId);
          if ((!Tiles.isSolidCave(text)) && (text != Tiles.Tile.TILE_HOLE.id)) {
            if (getTileSteepness(ntx, nty, this.layer) > maxHeightDiff * 100.0F) {
              if ((getHeightOfBridge(ntx, nty, this.layer) <= -1000.0F) || ((this.bridgeId <= 0L) && (hDiff > 0.0F)))
              {
                this.xa = 0.0F;
                this.ya = 0.0F;
              }
            }
          }
        }
        if ((this.started) && (!this.climbing) && (!isCommanding))
        {
          float suggestedHeight = getHeight(this.x, this.y, -3000.0F);
          float xSlip = (getHeight(this.x - 0.25F, this.y, suggestedHeight) - getHeight(this.x + 0.25F, this.y, suggestedHeight)) / 0.5F;
          
          float ySlip = (getHeight(this.x, this.y - 0.25F, suggestedHeight) - getHeight(this.x, this.y + 0.25F, suggestedHeight)) / 0.5F;
          
          float slipTreshold = 0.6F;
          float slipDampen = 0.3F;
          if (xSlip > 0.6F) {
            xSlip -= 0.3F;
          } else if (xSlip < -0.6F) {
            xSlip += 0.3F;
          } else {
            xSlip = 0.0F;
          }
          if (ySlip > 0.6F) {
            ySlip -= 0.3F;
          } else if (ySlip < -0.6F) {
            ySlip += 0.3F;
          } else {
            ySlip = 0.0F;
          }
          if ((xSlip != 0.0F) || (ySlip != 0.0F))
          {
            float slipDist = xSlip * xSlip + ySlip * ySlip;
            float dist = slipDist * 0.25F;
            if (dist > 0.2F) {
              dist = 0.2F;
            }
            slipDist = (float)Math.sqrt(slipDist);
            
            xSlip = xSlip * dist / slipDist;
            ySlip = ySlip * dist / slipDist;
            
            this.xa += xSlip;
            this.ya += ySlip;
          }
        }
      }
    }
    else if ((this.layer == 0) && (getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id))
    {
      this.layer = -1;
      setLayer(this.layer);
    }
    else
    {
      if ((isOnFloor()) && (this.bridgeId <= 0L))
      {
        float tileSpeedMod = 1.0F;
        this.xa *= 1.0F;
        this.ya *= 1.0F;
      }
      else if (this.bridgeId > 0L)
      {
        float tileSpeedMod = getSpeedForTile(currentTileX, currentTileY, this.layer);
        this.xa *= tileSpeedMod;
        this.ya *= tileSpeedMod;
      }
      boolean onBridge = false;
      if ((isCommanding) && (!this.commandingBoat) && (this.bridgeId == -10L))
      {
        float hDiff = getHeight(this.x + this.xa, this.y + this.ya, heightTarget) - heightTarget;
        if (hDiff > 0.0F)
        {
          float dist = (float)StrictMath.sqrt(this.xa * this.xa + this.ya * this.ya);
          this.xa /= (hDiff * hDiff / dist * 20.0F + 1.0F);
          this.ya /= (hDiff * hDiff / dist * 20.0F + 1.0F);
        }
        int ntx = (int)StrictMath.floor((this.x + this.xa) / 4.0F);
        int nty = (int)StrictMath.floor((this.y + this.ya) / 4.0F);
        if ((currentTileX != ntx) || (currentTileY != nty))
        {
          byte text = getTextureForTile(ntx, nty, this.layer, this.bridgeId);
          if ((!Tiles.isSolidCave(text)) && (text != Tiles.Tile.TILE_HOLE.id)) {
            if (getTileSteepness(ntx, nty, this.layer) > maxHeightDiff * 100.0F) {
              if ((getHeightOfBridge(ntx, nty, this.layer) <= -1000.0F) || ((this.bridgeId <= 0L) && (hDiff > 0.0F)))
              {
                this.xa = 0.0F;
                this.ya = 0.0F;
              }
              else
              {
                onBridge = true;
              }
            }
          }
        }
      }
      else if (this.bridgeId != -10L)
      {
        float hDiff = getHeight(this.x + this.xa, this.y + this.ya, heightTarget) - heightTarget;
        if ((hDiff > 0.0F) && (hDiff < 1.0F))
        {
          float dist = (float)StrictMath.sqrt(this.xa * this.xa + this.ya * this.ya);
          this.xa /= (hDiff * hDiff / dist * 10.0F + 1.0F);
          this.ya /= (hDiff * hDiff / dist * 10.0F + 1.0F);
        }
        maybePrintDebugInfo(75);
      }
      float dHeight = getHeight(this.x + this.xa, this.y + this.ya, getHeight(this.x, this.y, -3000.0F));
      if ((!onBridge) && ((dHeight < maxDepth) || (dHeight > maxHeight)))
      {
        this.xa = 0.0F;
        this.ya = 0.0F;
      }
    }
    if (Math.abs(getTargetGroundOffset() - getGroundOffset()) > 3.0F)
    {
      this.xa = 0.0F;
      this.ya = 0.0F;
    }
    float dist = this.xa * this.xa + this.ya * this.ya;
    float maxSpeed = 0.65000004F;
    if (dist > 0.42250004F)
    {
      dist = (float)Math.sqrt(dist);
      this.xa = (this.xa / dist * 0.65000004F);
      this.ya = (this.ya / dist * 0.65000004F);
      this.za = (this.za / dist * 0.65000004F);
    }
    this.xOld = this.x;
    this.yOld = this.y;
    this.zOld = this.z;
    
    int nextTileX = (int)((this.x + this.xa) / 4.0F);
    int nextTileY = (int)((this.y + this.ya) / 4.0F);
    if ((this.layer == -1) && (Tiles.isSolidCave(getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId)))) {
      handlePlayerInRock();
    } else if ((this.layer == -1) && (getTextureForTile(currentTileX, currentTileY, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_EXIT.id)) {
      if (Tiles.isSolidCave(getTextureForTile(nextTileX, nextTileY, this.layer, this.bridgeId)))
      {
        this.layer = 0;
        float dHeight = getHeight(this.x + this.xa, this.y + this.ya, getHeight(this.x, this.y, -3000.0F));
        if (dHeight > maxHeight)
        {
          this.layer = -1;
          this.xa = 0.0F;
          this.ya = 0.0F;
        }
        else
        {
          setLayer(this.layer);
        }
      }
      else
      {
        int diffx = nextTileX - currentTileX;
        int diffy = nextTileY - currentTileY;
        if ((diffx != 0) && (diffy != 0))
        {
          if ((diffx < 0) && (diffy < 0))
          {
            byte text = getTextureForTile(currentTileX - 1, currentTileY, -1, this.bridgeId);
            byte text2 = getTextureForTile(currentTileX, currentTileY - 1, -1, this.bridgeId);
            if ((Tiles.isSolidCave(text)) && (Tiles.isSolidCave(text2)))
            {
              this.xa = 0.0F;
              this.ya = 0.0F;
            }
          }
          if ((diffx > 0) && (diffy < 0))
          {
            byte text = getTextureForTile(currentTileX + 1, currentTileY, -1, this.bridgeId);
            byte text2 = getTextureForTile(currentTileX, currentTileY - 1, -1, this.bridgeId);
            if ((Tiles.isSolidCave(text)) && (Tiles.isSolidCave(text2)))
            {
              this.xa = 0.0F;
              this.ya = 0.0F;
            }
          }
          if ((diffx > 0) && (diffy > 0))
          {
            byte text = getTextureForTile(currentTileX + 1, currentTileY, -1, this.bridgeId);
            byte text2 = getTextureForTile(currentTileX, currentTileY + 1, -1, this.bridgeId);
            if ((Tiles.isSolidCave(text)) && (Tiles.isSolidCave(text2)))
            {
              this.xa = 0.0F;
              this.ya = 0.0F;
            }
          }
          if ((diffx < 0) && (diffy > 0))
          {
            byte text = getTextureForTile(currentTileX - 1, currentTileY, -1, this.bridgeId);
            byte text2 = getTextureForTile(currentTileX, currentTileY + 1, -1, this.bridgeId);
            if ((Tiles.isSolidCave(text)) && (Tiles.isSolidCave(text2)))
            {
              this.xa = 0.0F;
              this.ya = 0.0F;
            }
          }
        }
      }
    }
    this.x += this.xa;
    this.y += this.ya;
    this.z += this.za;
    
    updateGroundOffset();
    
    float nextHeightTarget = Math.max(getHeight(this.x, this.y, -3000.0F), waterHeight);
    
    this.onGround = ((this.z <= nextHeightTarget) && (!isOnFloor()));
    if (((isCommanding) || (this.offZ != 0.0F)) && ((!isOnFloor()) || (!this.commandingBoat)))
    {
      this.onGround = false;
      this.inWater = false;
      if (!this.commandingBoat) {
        if ((isOnFloor()) && (this.z - nextHeightTarget > 2.9D + this.groundOffset / 10.0F))
        {
          this.za = 0.0F;
        }
        else
        {
          this.z = (nextHeightTarget + (isOnFloor() ? 0.25F : 0.0F));
          this.za = 0.0F;
        }
      }
    }
    else if (this.onGround)
    {
      boolean landed = false;
      if ((this.za < -0.25D) && (!this.inWater) && (this.bridgeId <= 0L))
      {
        hitGround(-this.za);
        landed = true;
      }
      if ((landed) && (nextHeightTarget > heightTarget))
      {
        float dzPlayer = this.z - this.zOld;
        float dzTerrain = nextHeightTarget - heightTarget;
        float intersection = (this.zOld - heightTarget) / (dzTerrain - dzPlayer);
        
        this.xa = 0.0F;
        this.ya = 0.0F;
        this.za = 0.0F;
        
        this.x = (this.xOld + intersection * (this.x - this.xOld));
        this.y = (this.yOld + intersection * (this.y - this.yOld));
        this.z = (this.zOld + intersection * dzPlayer);
      }
      else
      {
        this.z = nextHeightTarget;
        this.za = 0.0F;
      }
    }
    else if (isOnFloor())
    {
      if ((this.bridgeId <= 0L) || (this.z < nextHeightTarget)) {
        if ((isAdjustingGroundOffset()) || ((this.xa == 0.0F) && (this.ya == 0.0F))) {
          this.z = nextHeightTarget;
        } else {
          this.z = this.zOld;
        }
      }
      if ((this.za < -0.25D) && (!this.inWater) && (isOnFloor()) && (!isCommanding)) {
        hitGround(-this.za);
      }
      this.za = 0.0F;
    }
    if ((this.onGround) || (this.inWater) || (isCommanding) || (isOnFloor()))
    {
      this.xa *= getMoveMod();
      this.ya *= getMoveMod();
    }
    if (((isCommanding) || (this.offZ != 0.0F) || (this.flying)) && (!isFalling())) {
      this.za = 0.0F;
    } else if (this.started) {
      this.za -= getFallMod();
    }
    if (this.wasOnStair) {
      this.wasOnStair = false;
    }
    if (isCommanding) {
      maybePrintDebugInfo(100);
    }
  }
  
  protected strictfp float getWaterLevel(float x, float y)
  {
    return 0.0F;
  }
  
  private final strictfp void maybePrintDebugInfo(int step)
  {
    maybePrintDebugInfo(step, 0.0F, 0.0F, 0.0F);
  }
  
  private final strictfp void maybePrintDebugInfo(int step, float val1, float val2, float val3) {}
  
  public strictfp void setOnFloorOverride(boolean onFloor)
  {
    if (onFloor != this.onFloorOverridden) {
      this.counter = 0;
    }
    if (onFloor) {
      this.onFloorOverridden = true;
    } else {
      this.onFloorOverridden = false;
    }
  }
  
  public strictfp boolean getOnFloorOverride()
  {
    return this.onFloorOverridden;
  }
  
  public strictfp boolean isOnFloor()
  {
    return ((getGroundOffset() > 0.0F) && (!isAdjustingGroundOffset())) || (this.bridgeId > 0L) || (this.onFloorOverridden == true);
  }
  
  public final strictfp float getFallMod()
  {
    return 0.04F;
  }
  
  public final strictfp float getMoveMod()
  {
    return 0.4F;
  }
  
  public strictfp boolean movedOnStair()
  {
    return this.wasOnStair;
  }
  
  private final strictfp boolean isCommanding(float maxDepth, float maxHeight)
  {
    return (maxDepth > -2500.0F) || (maxHeight < 2500.0F);
  }
  
  protected abstract void hitGround(float paramFloat);
  
  public abstract float getTileSteepness(int paramInt1, int paramInt2, int paramInt3);
  
  private strictfp float getHeight(float xp, float yp, float suggestedHeight)
  {
    int xx = (int)StrictMath.floor(xp / 4.0F);
    int yy = (int)StrictMath.floor(yp / 4.0F);
    if ((this.layer == 0) && (getTextureForTile(xx, yy, this.layer, this.bridgeId) == Tiles.Tile.TILE_HOLE.id)) {
      return getHeight(xp, yp, suggestedHeight, -1);
    }
    if ((this.layer == -1) && (Tiles.isSolidCave(getTextureForTile(xx, yy, this.layer, this.bridgeId)))) {
      return suggestedHeight;
    }
    return getHeight(xp, yp, suggestedHeight, this.layer);
  }
  
  private final strictfp float getHeight(float xp, float yp, float suggestedHeight, int layer)
  {
    int xx = (int)StrictMath.floor(xp / 4.0F);
    int yy = (int)StrictMath.floor(yp / 4.0F);
    
    float xa = xp / 4.0F - xx;
    float ya = yp / 4.0F - yy;
    if ((layer == -1) && (suggestedHeight > -2999.0F))
    {
      byte id = getTextureForTile(xx, yy, layer, this.bridgeId);
      if ((id == Tiles.Tile.TILE_CAVE_WALL.id) || (id == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id)) {
        return suggestedHeight;
      }
    }
    float[] hts = getNodeHeights(xx, yy, layer, this.bridgeId);
    
    float height = hts[0] * (1.0F - xa) * (1.0F - ya) + hts[1] * xa * (1.0F - ya) + hts[2] * (1.0F - xa) * ya + hts[3] * xa * ya;
    
    return height + getCurrentGroundOffset() / 10.0F;
  }
  
  public final strictfp void setSpeedModifier(float speedModifier)
  {
    this.speedMod = speedModifier;
  }
  
  public final strictfp void setPosition(float x, float y, float z, float xRot, int _layer)
  {
    this.abort = true;
    this.onGround = false;
    this.inWater = false;
    this.x = x;
    this.y = y;
    this.z = z;
    this.xRot = xRot;
    this.layer = _layer;
    this.xa = 0.0F;
    this.ya = 0.0F;
    this.za = 0.0F;
    if (this.layer != _layer) {
      setLayer(_layer);
    }
  }
  
  public final strictfp void changeLayer(int _layer)
  {
    this.layer = _layer;
  }
  
  private strictfp float getSpeedForTile(int xTile, int yTile, int layer)
  {
    try
    {
      return Tiles.getTile(getTextureForTile(xTile, yTile, layer, this.bridgeId)).getSpeed();
    }
    catch (NullPointerException e)
    {
      System.out.println("Can't get speed for tile " + xTile + ", " + yTile + ", layer " + layer + ", since it's of id " + 
        getTextureForTile(xTile, yTile, layer, this.bridgeId));
    }
    return 0.1F;
  }
  
  public static final strictfp byte buildBitmap(boolean f, boolean b, boolean l, boolean r)
  {
    byte result = 0;
    if (f) {
      result = (byte)(result | 0x1);
    }
    if (b) {
      result = (byte)(result | 0x2);
    }
    if (l) {
      result = (byte)(result | 0x4);
    }
    if (r) {
      result = (byte)(result | 0x8);
    }
    return result;
  }
  
  public final strictfp float getX()
  {
    return this.x;
  }
  
  public final strictfp float getY()
  {
    return this.y;
  }
  
  public final strictfp float getZ()
  {
    return this.z;
  }
  
  public final strictfp float getRot()
  {
    return this.xRot;
  }
  
  protected strictfp boolean isServerWestAvailable()
  {
    return this.serverWestAvailable;
  }
  
  protected strictfp void setServerWestAvailable(boolean serverWestAvailable)
  {
    this.serverWestAvailable = serverWestAvailable;
  }
  
  protected strictfp boolean isServerNorthAvailable()
  {
    return this.serverNorthAvailable;
  }
  
  protected strictfp void setServerNorthAvailable(boolean serverNorthAvailable)
  {
    this.serverNorthAvailable = serverNorthAvailable;
  }
  
  protected strictfp boolean isServerEastAvailable()
  {
    return this.serverEastAvailable;
  }
  
  protected strictfp void setServerEastAvailable(boolean serverEastAvailable)
  {
    this.serverEastAvailable = serverEastAvailable;
  }
  
  protected strictfp boolean isServerSouthAvailable()
  {
    return this.serverSouthAvailable;
  }
  
  protected strictfp void setServerSouthAvailable(boolean serverSouthAvailable)
  {
    this.serverSouthAvailable = serverSouthAvailable;
  }
  
  protected strictfp float getXa()
  {
    return this.xa;
  }
  
  protected strictfp void setXa(float xa)
  {
    this.xa = xa;
  }
  
  protected strictfp float getYa()
  {
    return this.ya;
  }
  
  protected strictfp void setYa(float ya)
  {
    this.ya = ya;
  }
  
  protected strictfp float getZa()
  {
    return this.za;
  }
  
  protected strictfp void setZa(float za)
  {
    this.za = za;
  }
  
  protected strictfp boolean isOnGround()
  {
    return this.onGround;
  }
  
  protected strictfp void setOnGround(boolean onGround)
  {
    this.onGround = onGround;
  }
  
  public strictfp boolean isInWater()
  {
    return this.inWater;
  }
  
  protected strictfp void setInWater(boolean inWater)
  {
    this.inWater = inWater;
  }
  
  protected strictfp boolean isIgnoreErrors()
  {
    return this.ignoreErrors;
  }
  
  protected strictfp void setIgnoreErrors(boolean ignoreErrors)
  {
    this.ignoreErrors = ignoreErrors;
  }
  
  protected strictfp boolean isStarted()
  {
    return this.started;
  }
  
  protected strictfp void setStarted(boolean started)
  {
    this.started = started;
  }
  
  protected strictfp float getDiffWindX()
  {
    return this.diffWindX;
  }
  
  protected strictfp void setDiffWindX(float diffWindX)
  {
    this.diffWindX = diffWindX;
  }
  
  protected strictfp float getDiffWindY()
  {
    return this.diffWindY;
  }
  
  protected strictfp void setDiffWindY(float diffWindY)
  {
    this.diffWindY = diffWindY;
  }
  
  protected strictfp boolean isCommandingBoat()
  {
    return this.commandingBoat;
  }
  
  protected strictfp void setCommandingBoat(boolean commandingBoat)
  {
    this.commandingBoat = commandingBoat;
  }
  
  protected strictfp float getCurrx()
  {
    return this.currx;
  }
  
  protected strictfp float getCurry()
  {
    return this.curry;
  }
  
  protected strictfp boolean isFirst()
  {
    return this.first;
  }
  
  protected strictfp void setFirst(boolean first)
  {
    this.first = first;
  }
  
  protected strictfp boolean isMovingVehicle()
  {
    return this.movingVehicle;
  }
  
  protected strictfp float getOffZ()
  {
    return this.offZ;
  }
  
  protected strictfp void setOffZ(float offZ)
  {
    this.offZ = offZ;
  }
  
  protected strictfp boolean isClimbing()
  {
    return this.climbing;
  }
  
  protected strictfp void setX(float x)
  {
    this.x = x;
  }
  
  protected strictfp void setY(float y)
  {
    this.y = y;
  }
  
  protected strictfp void setZ(float z)
  {
    this.z = z;
  }
  
  public abstract float getHeightOfBridge(int paramInt1, int paramInt2, int paramInt3);
  
  protected abstract byte getTextureForTile(int paramInt1, int paramInt2, int paramInt3, long paramLong);
  
  protected abstract float getCeilingForNode(int paramInt1, int paramInt2);
  
  protected abstract float getHeightForNode(int paramInt1, int paramInt2, int paramInt3);
  
  protected abstract float[] getNodeHeights(int paramInt1, int paramInt2, int paramInt3, long paramLong);
  
  protected abstract boolean handleWrongLayer(int paramInt1, int paramInt2);
  
  protected abstract boolean handleMoveTooFar(float paramFloat1, float paramFloat2);
  
  protected abstract boolean handleMoveTooShort(float paramFloat1, float paramFloat2);
  
  protected abstract boolean handleZError(float paramFloat1, float paramFloat2);
  
  protected abstract void handlePlayerInRock();
  
  protected strictfp void setLayer(int layer) {}
  
  public final strictfp void fly(float xTarget, float yTarget, float zTarget, float xRot, float yRot, byte bitmask, int layerTarget)
  {
    this.x = xTarget;
    this.y = yTarget;
    this.z = zTarget;
    this.layer = layerTarget;
    this.onGround = false;
    this.xRot = xRot;
    this.bitmask = bitmask;
    
    float speedModifier = 1.0F;
    
    int dirs = 0;
    float xPosMod = 0.0F;
    float yPosMod = 0.0F;
    float zPosMod = 0.0F;
    if (isPressed(1))
    {
      dirs++;
      
      xPosMod = xPosMod + (float)StrictMath.sin(xRot * 0.017453292F) * 0.08F * 1.0F * (float)StrictMath.cos(yRot * 0.017453292F);
      
      yPosMod = yPosMod - (float)StrictMath.cos(xRot * 0.017453292F) * 0.08F * 1.0F * (float)StrictMath.cos(yRot * 0.017453292F);
      zPosMod -= (float)StrictMath.sin(yRot * 0.017453292F) * 0.08F * 1.0F;
    }
    if (isPressed(2))
    {
      dirs++;
      
      xPosMod = xPosMod - (float)StrictMath.sin(xRot * 0.017453292F) * 0.08F * 1.0F * (float)StrictMath.cos(yRot * 0.017453292F);
      
      yPosMod = yPosMod + (float)StrictMath.cos(xRot * 0.017453292F) * 0.08F * 1.0F * (float)StrictMath.cos(yRot * 0.017453292F);
      
      zPosMod += (float)StrictMath.sin(yRot * 0.017453292F) * 0.08F * 1.0F;
    }
    if (isPressed(4))
    {
      dirs++;
      xPosMod -= (float)StrictMath.cos(xRot * 0.017453292F) * 0.08F * 1.0F;
      yPosMod -= (float)StrictMath.sin(xRot * 0.017453292F) * 0.08F * 1.0F;
    }
    if (isPressed(8))
    {
      dirs++;
      xPosMod += (float)StrictMath.cos(xRot * 0.017453292F) * 0.08F * 1.0F;
      yPosMod += (float)StrictMath.sin(xRot * 0.017453292F) * 0.08F * 1.0F;
    }
    if (dirs > 0)
    {
      this.xa = ((float)(this.xa + xPosMod / StrictMath.sqrt(dirs)));
      this.ya = ((float)(this.ya + yPosMod / StrictMath.sqrt(dirs)));
      this.za = ((float)(this.za + zPosMod / StrictMath.sqrt(dirs)));
    }
    float height = getHeight(this.x, this.y, -3000.0F);
    if (height < -1.45D) {
      height = -1.45F;
    }
    float dist = this.xa * this.xa + this.ya * this.ya;
    float maxSpeed = 0.65000004F;
    if (dist > 0.42250004F)
    {
      dist = (float)Math.sqrt(dist);
      this.xa = (this.xa / dist * 0.65000004F);
      this.ya = (this.ya / dist * 0.65000004F);
      this.za = (this.za / dist * 0.65000004F);
    }
    this.xOld = this.x;
    this.yOld = this.y;
    this.zOld = this.z;
    
    int xx = (int)(this.x / 4.0F);
    int yy = (int)(this.y / 4.0F);
    
    this.x += this.xa;
    this.y += this.ya;
    this.z += this.za;
    
    int newxx = (int)(this.x / 4.0F);
    int newyy = (int)(this.y / 4.0F);
    if ((this.layer == -1) && 
      (getTextureForTile(xx, yy, this.layer, this.bridgeId) != Tiles.Tile.TILE_CAVE.id) && 
      (!Tiles.isReinforcedFloor(getTextureForTile(xx, yy, this.layer, this.bridgeId))) && 
      (getTextureForTile(xx, yy, this.layer, this.bridgeId) != Tiles.Tile.TILE_CAVE_EXIT.id)) {
      handlePlayerInRock();
    } else if ((this.layer == -1) && (getTextureForTile(xx, yy, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_EXIT.id)) {
      if ((getTextureForTile(newxx, newyy, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_WALL.id) || 
        (getTextureForTile(newxx, newyy, this.layer, this.bridgeId) == Tiles.Tile.TILE_CAVE_WALL_REINFORCED.id))
      {
        this.layer = 0;
        setLayer(this.layer);
      }
      else
      {
        if (newyy == yy)
        {
          int xa = newxx < xx ? 0 : 1;
          if ((getCeilingForNode(xx + xa, yy) < 0.5F) && (getCeilingForNode(xx + xa, yy + 1) < 0.5F))
          {
            this.layer = 0;
            setLayer(this.layer);
          }
        }
        if (newxx == xx)
        {
          int ya = newyy < yy ? 0 : 1;
          if ((getCeilingForNode(xx, yy + ya) < 0.5F) && (getCeilingForNode(xx + 1, yy + ya) < 0.5F))
          {
            this.layer = 0;
            setLayer(this.layer);
          }
        }
      }
    }
    if (this.z < height)
    {
      this.z = height;
      this.za = 0.0F;
    }
    this.xa *= 0.9F;
    this.ya *= 0.9F;
    this.za *= 0.9F;
  }
  
  public final strictfp void setClimbing(boolean climbing)
  {
    this.climbing = climbing;
  }
  
  public final strictfp int getLayer()
  {
    return this.layer;
  }
  
  public strictfp void setMountSpeed(float newMountSpeed)
  {
    this.mountSpeed = newMountSpeed;
  }
  
  public strictfp float getWindImpact()
  {
    return this.windImpact;
  }
  
  public strictfp void setWindImpact(float wrot)
  {
    this.windImpact = wrot;
  }
  
  public static final strictfp float normalizeAngle(float angle)
  {
    angle -= (int)(angle / 360.0F) * 360;
    if (angle < 0.0F) {
      angle += 360.0F;
    }
    return angle;
  }
  
  public strictfp void reset()
  {
    setMountSpeed(0.0F);
    setWindImpact(0.0F);
    setWindRotation(0.0F);
    setWindStrength(0.0F);
    this.diffWindX = 0.0F;
    this.diffWindY = 0.0F;
  }
  
  public static final strictfp float getWindPower(float aWindRotation, float aVehicleRotation)
  {
    float lWindRotation = normalizeAngle(aWindRotation);
    float lVehicleRotation;
    float lVehicleRotation;
    if (lWindRotation > aVehicleRotation) {
      lVehicleRotation = normalizeAngle(lWindRotation - aVehicleRotation);
    } else {
      lVehicleRotation = normalizeAngle(aVehicleRotation - lWindRotation);
    }
    if ((lVehicleRotation > 150.0F) && (lVehicleRotation < 210.0F)) {
      return 0.0F;
    }
    if ((lVehicleRotation > 120.0F) && (lVehicleRotation < 240.0F)) {
      return 0.5F;
    }
    if ((lVehicleRotation > 90.0F) && (lVehicleRotation < 270.0F)) {
      return 0.65F;
    }
    if ((lVehicleRotation > 60.0F) && (lVehicleRotation < 300.0F)) {
      return 0.8F;
    }
    if ((lVehicleRotation > 30.0F) && (lVehicleRotation < 330.0F)) {
      return 1.0F;
    }
    return 0.9F;
  }
  
  public final strictfp float getSpeedMod()
  {
    return this.speedMod;
  }
  
  public final strictfp float getMountSpeed()
  {
    return this.mountSpeed;
  }
  
  protected strictfp float getXold()
  {
    return this.xOld;
  }
  
  protected strictfp float getYold()
  {
    return this.yOld;
  }
  
  protected strictfp float getZold()
  {
    return this.zOld;
  }
  
  protected strictfp void setLog(boolean log) {}
  
  public strictfp byte getBitMask()
  {
    return this.bitmask;
  }
  
  public strictfp float getVehicleRotation()
  {
    return this.vehicleRotation;
  }
  
  public strictfp void setVehicleRotation(float rotation)
  {
    this.vehicleRotation = rotation;
  }
  
  public strictfp float getWindStrength()
  {
    return this.windStrength;
  }
  
  public strictfp void setWindStrength(float wstr)
  {
    this.windStrength = wstr;
  }
  
  public strictfp float getWindRotation()
  {
    return this.windRotation;
  }
  
  public strictfp void setWindRotation(float wrot)
  {
    this.windRotation = wrot;
  }
  
  public strictfp void setGroundOffset(int newOffset, boolean immediately)
  {
    setTargetGroundOffset(Math.min(getMaxTargetGroundOffset(newOffset), newOffset));
    if (immediately) {
      setGroundOffset(getTargetGroundOffset());
    }
  }
  
  public strictfp int getMaxTargetGroundOffset(int suggestedOffset)
  {
    return suggestedOffset;
  }
  
  public final strictfp float getTargetGroundOffset()
  {
    return this.targetGroundOffset;
  }
  
  public final strictfp void setTargetGroundOffset(int newOffset)
  {
    this.targetGroundOffset = newOffset;
  }
  
  public final strictfp float getGroundOffset()
  {
    return this.groundOffset;
  }
  
  public final strictfp void setGroundOffset(float newOffset)
  {
    this.groundOffset = newOffset;
  }
  
  private final strictfp float getCurrentGroundOffset()
  {
    return getGroundOffset();
  }
  
  private final strictfp void updateGroundOffset()
  {
    if (getTargetGroundOffset() > getGroundOffset() + 1.0F) {
      setGroundOffset(getGroundOffset() + 1.0F);
    } else if (getTargetGroundOffset() < getGroundOffset() - 1.0F) {
      setGroundOffset(getGroundOffset() - 1.0F);
    } else {
      setGroundOffset(getTargetGroundOffset());
    }
  }
  
  public final strictfp boolean isAdjustingGroundOffset()
  {
    return getGroundOffset() != getTargetGroundOffset();
  }
  
  public strictfp String getInfo()
  {
    return "commanding boat: " + this.commandingBoat + "in water=" + this.inWater + " onground=" + this.onGround + " speedmod=" + this.speedMod + ",mountspeed=" + this.mountSpeed + " vehic rot " + this.vehicleRotation + " windrot=" + this.windRotation + " wind str=" + this.windStrength + " windImpact=" + this.windImpact;
  }
  
  public strictfp long getBridgeId()
  {
    return this.bridgeId;
  }
  
  public strictfp void setBridgeCounter(int nums)
  {
    this.bridgeCounter = nums;
  }
  
  public strictfp void setBridgeId(long bridgeId)
  {
    this.bridgeId = bridgeId;
    this.bridgeCounter = 10;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\MovementChecker.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */