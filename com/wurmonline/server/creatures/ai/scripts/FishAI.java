package com.wurmonline.server.creatures.ai.scripts;

import com.wurmonline.server.behaviours.FishEnums.FishData;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ai.CreatureAI;
import com.wurmonline.server.creatures.ai.CreatureAIData;
import com.wurmonline.server.items.ItemTemplate;
import com.wurmonline.server.zones.NoSuchZoneException;
import com.wurmonline.server.zones.Zones;
import com.wurmonline.shared.util.StringUtilities;

public class FishAI
  extends CreatureAI
{
  protected boolean pollMovement(Creature c, long delta)
  {
    FishAIData aiData = (FishAIData)c.getCreatureAIData();
    float targetX = aiData.getTargetPosX();
    float targetY = aiData.getTargetPosY();
    if ((targetX < 0.0F) || (targetY < 0.0F)) {
      return false;
    }
    if ((c.getPosX() != targetX) || (c.getPosY() != targetY))
    {
      float diffX = c.getPosX() - targetX;
      float diffY = c.getPosY() - targetY;
      float totalDiff = (float)Math.sqrt(diffX * diffX + diffY * diffY);
      float movementSpeed = aiData.getSpeed() * aiData.getMovementSpeedModifier();
      if (totalDiff < movementSpeed) {
        movementSpeed = totalDiff;
      }
      double lRotation = Math.atan2(targetY - c.getPosY(), targetX - c.getPosX()) * 57.29577951308232D + 90.0D;
      float lXPosMod = (float)Math.sin(lRotation * 0.01745329238474369D) * movementSpeed;
      float lYPosMod = -(float)Math.cos(lRotation * 0.01745329238474369D) * movementSpeed;
      int lNewTileX = (int)(c.getPosX() + lXPosMod) >> 2;
      int lNewTileY = (int)(c.getPosY() + lYPosMod) >> 2;
      int lDiffTileX = lNewTileX - c.getTileX();
      int lDiffTileY = lNewTileY - c.getTileY();
      
      c.setPositionX(c.getPosX() + lXPosMod);
      c.setPositionY(c.getPosY() + lYPosMod);
      c.setRotation((float)lRotation);
      try
      {
        float minZ = Math.min(-0.1F, Zones.calculateHeight(c.getPosX(), c.getPosY(), c.isOnSurface()));
        if (c.getPositionZ() < minZ) {
          c.setPositionZ(minZ + Math.abs(minZ * 0.2F));
        } else if (c.getPositionZ() < minZ * 0.15F) {
          c.setPositionZ(minZ * 0.15F);
        }
      }
      catch (NoSuchZoneException localNoSuchZoneException) {}
      c.moved((int)(lXPosMod * 100.0F), (int)(lYPosMod * 100.0F), 0, lDiffTileX, lDiffTileY);
    }
    return false;
  }
  
  protected boolean pollAttack(Creature c, long delta)
  {
    return false;
  }
  
  protected boolean pollBreeding(Creature c, long delta)
  {
    return false;
  }
  
  public CreatureAIData createCreatureAIData()
  {
    return new FishAIData();
  }
  
  public void creatureCreated(Creature c) {}
  
  public class FishAIData
    extends CreatureAIData
  {
    private byte fishTypeId = 0;
    private double ql = 10.0D;
    private float qlperc = 1.0F;
    private int weight = 0;
    private float targetPosX = -1.0F;
    private float targetPosY = -1.0F;
    private float timeToTarget = 0.0F;
    private float bodyStrength = 1.0F;
    private float bodyStamina = 1.0F;
    private float bodyControl = 1.0F;
    private float mindSpeed = 1.0F;
    private float difficulty = -10.0F;
    private boolean racingAway = false;
    private static final int PERC_OFFSET = 25;
    private static final int SPEED_OFFSET = 75;
    
    public FishAIData() {}
    
    public byte getFishTypeId()
    {
      return this.fishTypeId;
    }
    
    public void setFishTypeId(byte fishTypeId)
    {
      this.fishTypeId = fishTypeId;
    }
    
    public float getSpeed()
    {
      float mod;
      float mod;
      if (this.racingAway) {
        mod = 2.5F;
      } else {
        mod = (75.0F + (float)this.ql) / 175.0F;
      }
      return getFishData().getBaseSpeed() * mod;
    }
    
    public FishEnums.FishData getFishData()
    {
      return FishEnums.FishData.fromInt(this.fishTypeId);
    }
    
    public void setQL(double ql)
    {
      this.ql = ql;
      this.qlperc = ((25.0F + (float)this.ql) / 125.0F);
      
      this.bodyStrength = Math.max(getFishData().getBodyStrength() * this.qlperc, 1.0F);
      this.bodyStamina = Math.max(getFishData().getBodyStamina() * this.qlperc, 1.0F);
      this.bodyControl = Math.max(getFishData().getBodyControl() * this.qlperc, 1.0F);
      this.mindSpeed = Math.max(getFishData().getMindSpeed() * this.qlperc, 1.0F);
      
      setSizeModifier(this.qlperc * getFishData().getScaleMod());
      
      ItemTemplate it = getFishData().getTemplate();
      if (it != null) {
        this.weight = ((int)(it.getWeightGrams() * (ql / 100.0D)));
      }
    }
    
    public void setTargetPos(float targetPosX, float targetPosY)
    {
      this.targetPosX = targetPosX;
      this.targetPosY = targetPosY;
      calcTimeToTarget();
    }
    
    public void setRaceAway(boolean raceAway)
    {
      this.racingAway = raceAway;
      calcTimeToTarget();
    }
    
    private void calcTimeToTarget()
    {
      float diffX = this.targetPosX - getCreature().getPosX();
      float diffY = this.targetPosY - getCreature().getPosY();
      float dist = (float)Math.sqrt(diffX * diffX + diffY * diffY);
      
      float movementSpeed = getSpeed() * getMovementSpeedModifier();
      this.timeToTarget = (dist / movementSpeed * 10.0F + 2.0F);
    }
    
    public float getTargetPosX()
    {
      return this.targetPosX;
    }
    
    public float getTargetPosY()
    {
      return this.targetPosY;
    }
    
    public float getTimeToTarget()
    {
      return this.timeToTarget;
    }
    
    public double getQL()
    {
      return this.ql;
    }
    
    public String getNameWithGenusAndSize()
    {
      return StringUtilities.addGenus(getNameWithSize(), false);
    }
    
    public String getNameWithSize()
    {
      StringBuilder buf = new StringBuilder();
      if (this.ql >= 99.0D) {
        buf.append("stupendous ");
      } else if (this.ql >= 95.0D) {
        buf.append("massive ");
      } else if (this.ql >= 85.0D) {
        buf.append("huge ");
      } else if (this.ql >= 75.0D) {
        buf.append("impressive ");
      } else if (this.ql >= 65.0D) {
        buf.append("large ");
      }
      if (this.ql < 15.0D) {
        buf.append("small ");
      }
      buf.append(getFishData().getName());
      return buf.toString();
    }
    
    public int getWeight()
    {
      return this.weight;
    }
    
    public float getBodyStrength()
    {
      return this.bodyStrength;
    }
    
    public float getBodyStamina()
    {
      return this.bodyStamina;
    }
    
    public void decBodyStamina(float bodyStamina)
    {
      this.bodyStamina = Math.max(this.bodyStamina - bodyStamina, 0.0F);
    }
    
    public float getBodyControl()
    {
      return this.bodyControl;
    }
    
    public float getMindSpeed()
    {
      return this.mindSpeed;
    }
    
    public void setDifficulty(float difficulty)
    {
      this.difficulty = difficulty;
    }
    
    public float getDifficulty()
    {
      return this.difficulty;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\creatures\ai\scripts\FishAI.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */