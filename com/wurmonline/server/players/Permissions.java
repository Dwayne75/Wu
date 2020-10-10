package com.wurmonline.server.players;

import java.util.BitSet;

public class Permissions
{
  public static enum Allow
    implements Permissions.IPermission
  {
    SETTLEMENT_MAY_MANAGE(0, "Allow Settlememnt to Manage", "Allow", "Manage", ""),  NOT_RUNEABLE(7, "Item Attributes", "Cannot be", "Runed", ""),  SEALED_BY_PLAYER(8, "Item Attributes", "Cannot", "Take / Put / Eat or Drink", ""),  NO_EAT_OR_DRINK(9, "Item Attributes", "Cannot", "Eat or Drink", ""),  OWNER_TURNABLE(10, "Item Attributes", "Turnable", "by Owner", ""),  OWNER_MOVEABLE(11, "Item Attributes", "Moveable", "by Owner", ""),  NO_DRAG(12, "Item Attributes", "Cannot be", "Dragged", ""),  NO_IMPROVE(13, "Item Attributes", "Cannot be", "Improved", ""),  NO_DROP(14, "Item Attributes", "Cannot be", "Dropped", ""),  NO_REPAIR(15, "Item Attributes", "Cannot be", "Repaired", ""),  PLANTED(16, "Item Attributes", "Is", "Planted", ""),  AUTO_FILL(17, "Item Attributes", "Auto", "Fills", ""),  AUTO_LIGHT(18, "Item Attributes", "Auto", "Lights", ""),  ALWAYS_LIT(19, "Item Attributes", "Always", "Lit", ""),  HAS_COURIER(20, "Item Attributes", "Has", "Courier", ""),  HAS_DARK_MESSENGER(21, "Item Attributes", "Has", "Dark Messanger", ""),  DECAY_DISABLED(22, "Item Attributes", "Decay", "Disabled", ""),  NO_TAKE(23, "Item Attributes", "Cannot be", "Taken", ""),  NO_SPELLS(24, "Item Restrictions", "Cannot be", "Cast Upon", ""),  NO_BASH(25, "Item Restrictions", "Cannot be", "Bashed / Destroyed", ""),  NOT_LOCKABLE(26, "Item Restrictions", "Cannot be", "Locked", ""),  NOT_LOCKPICKABLE(27, "Item Restrictions", "Cannot be", "Lockpicked", ""),  NOT_MOVEABLE(28, "Item Restrictions", "Cannot be", "Moved", ""),  NOT_TURNABLE(29, "Item Restrictions", "Cannot be", "Turned", ""),  NOT_PAINTABLE(30, "Item Restrictions", "Cannot be", "Painted", ""),  NO_PUT(31, "Item Attributes", "Cannot", "Put items inside", "");
    
    final byte bit;
    final String description;
    final String header1;
    final String header2;
    final String hover;
    
    private Allow(int aBit, String aDescription, String aHeader1, String aHeader2, String aHover)
    {
      this.bit = ((byte)aBit);
      this.description = aDescription;
      this.header1 = aHeader1;
      this.header2 = aHeader2;
      this.hover = aHover;
    }
    
    public byte getBit()
    {
      return this.bit;
    }
    
    public int getValue()
    {
      return 1 << this.bit;
    }
    
    public String getDescription()
    {
      return this.description;
    }
    
    public String getHeader1()
    {
      return this.header1;
    }
    
    public String getHeader2()
    {
      return this.header2;
    }
    
    public String getHover()
    {
      return this.hover;
    }
    
    private static final Allow[] types = values();
    
    public static Permissions.IPermission[] getPermissions()
    {
      return types;
    }
  }
  
  private int permissions = 0;
  protected BitSet permissionBits = new BitSet(32);
  
  public void setPermissionBits(int newPermissions)
  {
    this.permissions = newPermissions;
    this.permissionBits.clear();
    for (int x = 0; x < 32; x++) {
      if ((newPermissions >>> x & 0x1) == 1) {
        this.permissionBits.set(x);
      }
    }
  }
  
  public final boolean hasPermission(int permissionBit)
  {
    if (this.permissions != 0) {
      return this.permissionBits.get(permissionBit);
    }
    return false;
  }
  
  private final int getPermissionsInt()
  {
    int ret = 0;
    for (int x = 0; x < 32; x++) {
      if (this.permissionBits.get(x)) {
        ret = (int)(ret + (1L << x));
      }
    }
    return ret;
  }
  
  public final void setPermissionBit(int bit, boolean value)
  {
    this.permissionBits.set(bit, value);
    this.permissions = getPermissionsInt();
  }
  
  public int getPermissions()
  {
    return this.permissions;
  }
  
  public static abstract interface IAllow
  {
    public abstract boolean canBeAlwaysLit();
    
    public abstract boolean canBeAutoFilled();
    
    public abstract boolean canBeAutoLit();
    
    public abstract boolean canBePeggedByPlayer();
    
    public abstract boolean canBePlanted();
    
    public abstract boolean canBeSealedByPlayer();
    
    public abstract boolean canChangeCreator();
    
    public abstract boolean canDisableDecay();
    
    public abstract boolean canDisableDestroy();
    
    public abstract boolean canDisableDrag();
    
    public abstract boolean canDisableDrop();
    
    public abstract boolean canDisableEatAndDrink();
    
    public abstract boolean canDisableImprove();
    
    public abstract boolean canDisableLocking();
    
    public abstract boolean canDisableLockpicking();
    
    public abstract boolean canDisableMoveable();
    
    public abstract boolean canDisableOwnerMoveing();
    
    public abstract boolean canDisableOwnerTurning();
    
    public abstract boolean canDisablePainting();
    
    public abstract boolean canDisablePut();
    
    public abstract boolean canDisableRepair();
    
    public abstract boolean canDisableRuneing();
    
    public abstract boolean canDisableSpellTarget();
    
    public abstract boolean canDisableTake();
    
    public abstract boolean canDisableTurning();
    
    public abstract boolean canHaveCourier();
    
    public abstract boolean canHaveDakrMessenger();
    
    public abstract String getCreatorName();
    
    public abstract float getDamage();
    
    public abstract String getName();
    
    public abstract float getQualityLevel();
    
    public abstract boolean hasCourier();
    
    public abstract boolean hasDarkMessenger();
    
    public abstract boolean hasNoDecay();
    
    public abstract boolean isAlwaysLit();
    
    public abstract boolean isAutoFilled();
    
    public abstract boolean isAutoLit();
    
    public abstract boolean isIndestructible();
    
    public abstract boolean isNoDrag();
    
    public abstract boolean isNoDrop();
    
    public abstract boolean isNoEatOrDrink();
    
    public abstract boolean isNoImprove();
    
    public abstract boolean isNoMove();
    
    public abstract boolean isNoPut();
    
    public abstract boolean isNoRepair();
    
    public abstract boolean isNoTake();
    
    public abstract boolean isNotLockable();
    
    public abstract boolean isNotLockpickable();
    
    public abstract boolean isNotPaintable();
    
    public abstract boolean isNotRuneable();
    
    public abstract boolean isNotSpellTarget();
    
    public abstract boolean isNotTurnable();
    
    public abstract boolean isOwnerMoveable();
    
    public abstract boolean isOwnerTurnable();
    
    public abstract boolean isPlanted();
    
    public abstract boolean isSealedByPlayer();
    
    public abstract void setCreator(String paramString);
    
    public abstract boolean setDamage(float paramFloat);
    
    public abstract void setHasCourier(boolean paramBoolean);
    
    public abstract void setHasDarkMessenger(boolean paramBoolean);
    
    public abstract void setHasNoDecay(boolean paramBoolean);
    
    public abstract void setIsAlwaysLit(boolean paramBoolean);
    
    public abstract void setIsAutoFilled(boolean paramBoolean);
    
    public abstract void setIsAutoLit(boolean paramBoolean);
    
    public abstract void setIsIndestructible(boolean paramBoolean);
    
    public abstract void setIsNoDrag(boolean paramBoolean);
    
    public abstract void setIsNoDrop(boolean paramBoolean);
    
    public abstract void setIsNoEatOrDrink(boolean paramBoolean);
    
    public abstract void setIsNoImprove(boolean paramBoolean);
    
    public abstract void setIsNoMove(boolean paramBoolean);
    
    public abstract void setIsNoPut(boolean paramBoolean);
    
    public abstract void setIsNoRepair(boolean paramBoolean);
    
    public abstract void setIsNoTake(boolean paramBoolean);
    
    public abstract void setIsNotLockable(boolean paramBoolean);
    
    public abstract void setIsNotLockpickable(boolean paramBoolean);
    
    public abstract void setIsNotPaintable(boolean paramBoolean);
    
    public abstract void setIsNotRuneable(boolean paramBoolean);
    
    public abstract void setIsNotSpellTarget(boolean paramBoolean);
    
    public abstract void setIsNotTurnable(boolean paramBoolean);
    
    public abstract void setIsOwnerMoveable(boolean paramBoolean);
    
    public abstract void setIsOwnerTurnable(boolean paramBoolean);
    
    public abstract void setIsPlanted(boolean paramBoolean);
    
    public abstract void setIsSealedByPlayer(boolean paramBoolean);
    
    public abstract boolean setQualityLevel(float paramFloat);
    
    public abstract void setOriginalQualityLevel(float paramFloat);
    
    public abstract void savePermissions();
  }
  
  public static abstract interface IPermission
  {
    public abstract byte getBit();
    
    public abstract int getValue();
    
    public abstract String getDescription();
    
    public abstract String getHeader1();
    
    public abstract String getHeader2();
    
    public abstract String getHover();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\players\Permissions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */