package com.wurmonline.server.players;

import java.util.BitSet;

public class Permissions
{
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
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\Permissions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */