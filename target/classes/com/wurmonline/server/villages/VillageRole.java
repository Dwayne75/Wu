package com.wurmonline.server.villages;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.players.Permissions;
import java.io.IOException;

public abstract class VillageRole
  implements VillageStatus, MiscConstants
{
  public int id;
  byte status;
  int villageid;
  public String name = "";
  boolean mayTerraform = false;
  boolean mayCuttrees = false;
  boolean mayMine = false;
  boolean mayFarm = false;
  boolean mayBuild = false;
  boolean mayHire = false;
  boolean mayInvite = false;
  boolean mayDestroy = false;
  boolean mayManageRoles = false;
  boolean mayExpand = false;
  boolean mayLockFences = false;
  boolean mayPassAllFences = false;
  boolean diplomat = false;
  boolean mayAttackCitizens = false;
  boolean mayAttackNonCitizens = Servers.localServer.PVPSERVER;
  boolean mayFish = true;
  boolean mayCutOldTrees = false;
  boolean mayPushPullTurn = true;
  boolean mayUpdateMap = false;
  boolean mayLead = false;
  boolean mayPickup = false;
  boolean mayTame = false;
  boolean mayLoad = false;
  boolean mayButcher = false;
  boolean mayAttachLock = false;
  boolean mayPickLocks = false;
  int villageAppliedTo = 0;
  long playerAppliedTo = -10L;
  Permissions settings = new Permissions();
  Permissions moreSettings = new Permissions();
  Permissions extraSettings = new Permissions();
  
  VillageRole(int aVillageid, String aName, boolean aTerraform, boolean aCutTrees, boolean aMine, boolean aFarm, boolean aBuild, boolean aHire, boolean aMayInvite, boolean aMayDestroy, boolean aMayManageRoles, boolean aMayExpand, boolean aMayLockFences, boolean aMayPassFences, boolean aIsDiplomat, boolean aMayAttackCitizens, boolean aMayAttackNonCitizens, boolean aMayFish, boolean aMayCutOldTrees, byte aStatus, int appliedToVillage, boolean aMayPushPullTurn, boolean aMayUpdateMap, boolean aMayLead, boolean aMayPickup, boolean aMayTame, boolean aMayLoad, boolean aMayButcher, boolean aMayAttachLock, boolean aMayPickLocks, long appliedToPlayer, int aSettings, int aMoreSettings, int aExtraSettings)
    throws IOException
  {
    this.villageid = aVillageid;
    this.name = aName;
    this.villageAppliedTo = appliedToVillage;
    this.status = aStatus;
    
    this.mayTerraform = aTerraform;
    this.mayCuttrees = aCutTrees;
    this.mayMine = aMine;
    this.mayFarm = aFarm;
    this.mayBuild = aBuild;
    this.mayHire = aHire;
    this.mayInvite = aMayInvite;
    this.mayDestroy = aMayDestroy;
    this.mayManageRoles = aMayManageRoles;
    this.mayExpand = aMayExpand;
    this.mayLockFences = aMayLockFences;
    this.mayPassAllFences = aMayPassFences;
    this.diplomat = aIsDiplomat;
    this.mayAttackCitizens = aMayAttackCitizens;
    this.mayAttackNonCitizens = aMayAttackNonCitizens;
    this.mayFish = aMayFish;
    this.mayCutOldTrees = aMayCutOldTrees;
    this.mayPushPullTurn = aMayPushPullTurn;
    this.mayUpdateMap = aMayUpdateMap;
    this.mayLead = aMayLead;
    this.mayPickup = aMayPickup;
    this.mayTame = aMayTame;
    this.mayLoad = aMayLoad;
    this.mayButcher = aMayButcher;
    this.mayAttachLock = aMayAttachLock;
    this.mayPickLocks = aMayPickLocks;
    
    this.playerAppliedTo = appliedToPlayer;
    this.settings.setPermissionBits(aSettings);
    this.moreSettings.setPermissionBits(aMoreSettings);
    this.extraSettings.setPermissionBits(aExtraSettings);
    
    create();
  }
  
  VillageRole(int aId, int aVillageid, String aRoleName, boolean aMayTerraform, boolean aMayCuttrees, boolean aMayMine, boolean aMayFarm, boolean aMayBuild, boolean aMayHire, boolean aMayInvite, boolean aMayDestroy, boolean aMayManageRoles, boolean aMayExpand, boolean aMayPassAllFences, boolean aMayLockFences, boolean aMayAttackCitizens, boolean aMayAttackNonCitizens, boolean aMayFish, boolean aMayCutOldTrees, boolean aMayPushPullTurn, boolean aDiplomat, byte aStatus, int aVillageAppliedTo, boolean aMayUpdateMap, boolean aMayLead, boolean aMayPickup, boolean aMayTame, boolean aMayLoad, boolean aMayButcher, boolean aMayAttachLock, boolean aMayPickLocks, long aPlayerAppliedTo, int aSettings, int aMoreSettings, int aExtraSettings)
  {
    this.id = aId;
    this.villageid = aVillageid;
    this.name = aRoleName;
    this.mayTerraform = aMayTerraform;
    this.mayCuttrees = aMayCuttrees;
    this.mayMine = aMayMine;
    this.mayFarm = aMayFarm;
    this.mayBuild = aMayBuild;
    this.mayHire = aMayHire;
    this.mayInvite = aMayInvite;
    this.mayDestroy = aMayDestroy;
    this.mayManageRoles = aMayManageRoles;
    this.mayExpand = aMayExpand;
    this.mayPassAllFences = aMayPassAllFences;
    this.mayLockFences = aMayLockFences;
    this.mayAttackCitizens = aMayAttackCitizens;
    this.mayAttackNonCitizens = aMayAttackNonCitizens;
    this.mayFish = aMayFish;
    this.mayCutOldTrees = aMayCutOldTrees;
    this.mayPushPullTurn = aMayPushPullTurn;
    this.diplomat = aDiplomat;
    this.status = aStatus;
    this.villageAppliedTo = aVillageAppliedTo;
    this.mayUpdateMap = aMayUpdateMap;
    this.mayLead = aMayLead;
    this.mayPickup = aMayPickup;
    this.mayTame = aMayTame;
    this.mayLoad = aMayLoad;
    this.mayButcher = aMayButcher;
    this.mayAttachLock = aMayAttachLock;
    this.playerAppliedTo = aPlayerAppliedTo;
    if (getStatus() == 2)
    {
      this.settings.setPermissionBits(-1);
      this.moreSettings.setPermissionBits(-1);
      this.extraSettings.setPermissionBits(-1);
    }
    else
    {
      this.settings.setPermissionBits(aSettings);
      this.moreSettings.setPermissionBits(aMoreSettings);
      this.extraSettings.setPermissionBits(aExtraSettings);
    }
  }
  
  public void convertSettings()
  {
    if (this.status == 2)
    {
      this.settings.setPermissionBits(-1);
      this.moreSettings.setPermissionBits(-1);
      this.extraSettings.setPermissionBits(-1);
    }
    else
    {
      boolean isMayor = this.status == 2;
      boolean isAnyone = (isMayor) || (this.status == 3) || (this.status == 0) || (this.status == 5);
      
      boolean isCitizen = ((isMayor) || (this.status == 3) || (this.status == 0)) && (this.villageAppliedTo == 0);
      
      boolean mayPlaceMerchants = isCitizen;
      try
      {
        Village village = Villages.getVillage(this.villageid);
        mayPlaceMerchants = village.acceptsMerchants;
      }
      catch (NoSuchVillageException localNoSuchVillageException) {}
      setCanBreed((isMayor) || (this.mayLead));
      setCanButcher((isMayor) || (this.mayButcher));
      setCanGroom((isMayor) || (this.mayLead));
      setCanLead((isMayor) || (this.mayLead));
      setCanMilkShear((isMayor) || (this.mayFarm));
      setCanSacrifice((isMayor) || (this.mayButcher));
      setCanTame((isMayor) || (this.mayTame));
      
      setCanBuild((isMayor) || (this.mayBuild));
      setCanDestroyFence((isMayor) || (isAnyone));
      setCanDestroyItems((isMayor) || (isAnyone));
      setCanPickLocks((isMayor) || (this.mayPickLocks));
      setCanPlanBuildings((isMayor) || (this.mayBuild));
      
      setCanCultivate((isMayor) || (this.mayTerraform));
      setCanDigResource((isMayor) || (this.mayTerraform));
      setCanPack((isMayor) || (this.mayTerraform));
      setCanTerraform((isMayor) || (this.mayTerraform));
      
      setCanHarvestFields((isMayor) || (this.mayFarm));
      setCanSowFields((isMayor) || (this.mayFarm));
      setCanTendFields((isMayor) || (this.mayFarm));
      
      setCanChopDownAllTrees((isMayor) || (this.mayCuttrees));
      setCanChopDownOldTrees((isMayor) || (this.mayCutOldTrees));
      setCanCutGrass((isMayor) || (isAnyone));
      setCanHarvestFruit((isMayor) || (this.mayCuttrees));
      setCanMakeLawn((isMayor) || (this.mayTerraform));
      setCanPickSprouts((isMayor) || (this.mayCuttrees));
      setCanPlantFlowers((isMayor) || (this.mayCuttrees));
      setCanPlantSprouts((isMayor) || (this.mayCuttrees));
      setCanPrune((isMayor) || (this.mayCuttrees));
      
      setCanAttackCitizens((isMayor) || (this.mayAttackCitizens));
      setCanAttackNonCitizens((isMayor) || (this.mayAttackNonCitizens));
      setCanCastDeitySpells((isMayor) || (isAnyone));
      setCanCastSorcerySpells((isMayor) || (isAnyone));
      setCanForageBotanize((isMayor) || (this.mayFarm));
      setCanPave((isMayor) || (this.mayTerraform));
      setCanPlaceMerchants((isMayor) || (mayPlaceMerchants));
      setCanUseMeditationAbility((isMayor) || (isAnyone));
      
      setCanAttachLocks((isMayor) || (this.mayAttachLock));
      setCanDrop((isMayor) || (isAnyone));
      setCanImproveRepair((isMayor) || (isAnyone));
      setCanLoad((isMayor) || (this.mayLoad));
      setCanPickup((isMayor) || (this.mayPickup));
      setCanPickupPlanted((isMayor) || (this.mayPickup));
      setCanPullPushTurn((isMayor) || (this.mayPushPullTurn));
      setCanUnload((isMayor) || (this.mayLoad));
      
      setCanMineFloor((isMayor) || (this.mayMine));
      setCanMineIron((isMayor) || (this.mayMine));
      setCanMineOther((isMayor) || (this.mayMine));
      setCanMineRock((isMayor) || (this.mayMine));
      setCanSurface((isMayor) || (this.mayMine));
      setCanTunnel((isMayor) || (this.mayMine));
      
      SetCanPerformActionsOnAlliedDeeds(isCitizen);
      setCanDiplomat((isMayor) || (this.diplomat));
      setCanDestroyAnyBuilding((isMayor) || (this.mayDestroy));
      setCanManageGuards((isMayor) || (this.mayHire));
      setCanInviteCitizens((isMayor) || (this.mayInvite));
      setCanManageCitizenRoles((isMayor) || (this.mayManageRoles));
      setCanManageMap((isMayor) || (this.mayUpdateMap));
      setCanManageReputations((isMayor) || (this.mayManageRoles));
      setCanManageRoles((isMayor) || (this.mayManageRoles));
      setCanManageSettings((isMayor) || (this.mayManageRoles));
      setCanConfigureTwitter((isMayor) || (this.diplomat));
      setCanResizeSettlement((isMayor) || (this.mayExpand));
    }
  }
  
  public final String getName()
  {
    return this.name;
  }
  
  public final int getVillageId()
  {
    return this.villageid;
  }
  
  public final boolean mayAttachLock()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.ATTACH_LOCKS.getBit());
  }
  
  public final boolean mayAttackCitizens()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.ATTACK_CITIZENS.getBit());
  }
  
  public final boolean mayAttackNonCitizens()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.ATTACK_NON_CITIZENS.getBit());
  }
  
  public final boolean mayBrand()
  {
    return this.extraSettings.hasPermission(VillageRole.ExtraRolePermissions.BRAND.getBit());
  }
  
  public final boolean mayBreed()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.BREED.getBit());
  }
  
  public final boolean mayBuild()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.BUILD.getBit());
  }
  
  public final boolean mayButcher()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.BUTCHER.getBit());
  }
  
  public final boolean mayCastDeitySpells()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.CAST_DEITY_SPELLS.getBit());
  }
  
  public final boolean mayCastSorcerySpells()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.CAST_SORCERY_SPELLS.getBit());
  }
  
  public final boolean mayChopDownAllTrees()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.CHOP_DOWN_ALL_TREES.getBit());
  }
  
  public final boolean mayChopDownOldTrees()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.CHOP_DOWN_OLD_TREES.getBit());
  }
  
  public final boolean mayConfigureTwitter()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MAY_CONFIGURE_TWITTER.getBit());
  }
  
  public final boolean mayCultivate()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.CULTIVATE.getBit());
  }
  
  public final boolean mayCutGrass()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.CUT_GRASS.getBit());
  }
  
  public final boolean mayCuttrees()
  {
    return this.mayCuttrees;
  }
  
  public final boolean mayCutOldTrees()
  {
    return this.mayCutOldTrees;
  }
  
  public final boolean mayDestroy()
  {
    return this.mayDestroy;
  }
  
  public final boolean mayDestroyAnyBuilding()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.DESTROY_ANY_BUILDING.getBit());
  }
  
  public final boolean mayDestroyFences()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.DESTROY_FENCE.getBit());
  }
  
  public final boolean mayDestroyItems()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.DESTROY_ITEMS.getBit());
  }
  
  public final boolean mayDigResources()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.DIG_RESOURCE.getBit());
  }
  
  public final boolean isDiplomat()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.DIPLOMAT.getBit());
  }
  
  public final boolean mayDisbandSettlement()
  {
    return this.status == 2;
  }
  
  public final boolean mayDrop()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.DROP.getBit());
  }
  
  public final boolean mayExpand()
  {
    return this.mayExpand;
  }
  
  public final boolean mayFarm()
  {
    return this.mayFarm;
  }
  
  public final boolean mayFish()
  {
    return this.mayFish;
  }
  
  public final boolean mayForageAndBotanize()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.FORAGE.getBit());
  }
  
  public final boolean mayGroom()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.GROOM.getBit());
  }
  
  public final boolean mayHarvestFields()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.HARVEST_FIELDS.getBit());
  }
  
  public final boolean mayHarvestFruit()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.HARVEST_FRUIT.getBit());
  }
  
  public final boolean mayHire()
  {
    return this.mayHire;
  }
  
  public final boolean mayImproveAndRepair()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.IMPROVE_REPAIR.getBit());
  }
  
  public final boolean mayInviteCitizens()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.INVITE_CITIZENS.getBit());
  }
  
  public final boolean mayLead()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.LEAD.getBit());
  }
  
  public final boolean mayLoad()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.LOAD.getBit());
  }
  
  public final boolean mayLockFences()
  {
    return this.mayLockFences;
  }
  
  public final boolean mayMakeLawn()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.MAKE_LAWN.getBit());
  }
  
  public final boolean mayManageAllowedObjects()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MANAGE_ALLOWED_OBJECTS.getBit());
  }
  
  public final boolean mayManageCitizenRoles()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MANAGE_CITIZEN_ROLES.getBit());
  }
  
  public final boolean mayManageGuards()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MANAGE_GUARDS.getBit());
  }
  
  public final boolean mayManageMap()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MANAGE_MAP.getBit());
  }
  
  public final boolean mayManageReputations()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MANAGE_REPUTATIONS.getBit());
  }
  
  public final boolean mayManageRoles()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MANAGE_ROLES.getBit());
  }
  
  public final boolean mayManageSettings()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MANAGE_SETTINGS.getBit());
  }
  
  public final boolean mayMilkAndShear()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.MILK_SHEAR.getBit());
  }
  
  public final boolean mayMine()
  {
    return this.mayMine;
  }
  
  public final boolean mayMineFloor()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MINE_FLOOR.getBit());
  }
  
  public final boolean mayMineIronVeins()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MINE_IRON.getBit());
  }
  
  public final boolean mayMineOtherVeins()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MINE_OTHER.getBit());
  }
  
  public final boolean mayMineRock()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MINE_ROCK.getBit());
  }
  
  public final boolean mayMineSurface()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.SURFACE_MINING.getBit());
  }
  
  public final boolean mayPack()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.PACK.getBit());
  }
  
  public final boolean mayPassGates()
  {
    return this.extraSettings.hasPermission(VillageRole.ExtraRolePermissions.PASS_GATES.getBit());
  }
  
  public final boolean mayPave()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.PAVE.getBit());
  }
  
  public final boolean mayPerformActionsOnAlliedDeeds()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.ALLOW_ACTIONS_ON_ALLIED_DEED.getBit());
  }
  
  public final boolean mayPickLocks()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.PICK_LOCKS.getBit());
  }
  
  public final boolean mayPickSprouts()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.PICK_SPROUTS.getBit());
  }
  
  public final boolean mayPickup()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.PICKUP.getBit());
  }
  
  public final boolean mayPickupPlanted()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.PICKUP_PLANTED.getBit());
  }
  
  public final boolean mayPlaceMerchants()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.PLACE_MERCHANTS.getBit());
  }
  
  public final boolean mayPlanBuildings()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.PLAN_BUILDINGS.getBit());
  }
  
  public final boolean mayPlantFlowers()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.PLANT_FLOWERS.getBit());
  }
  
  public final boolean mayPlantItem()
  {
    return this.extraSettings.hasPermission(VillageRole.ExtraRolePermissions.PLANT_ITEM.getBit());
  }
  
  public final boolean mayPlantSprouts()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.PLANT_SPROUTS.getBit());
  }
  
  public final boolean mayPrune()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.PRUNE.getBit());
  }
  
  public final boolean mayPushPullTurn()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.PULL_PUSH.getBit());
  }
  
  public final boolean mayReinforce()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.REINFORCE.getBit());
  }
  
  public final boolean mayResizeSettlement()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.RESIZE.getBit());
  }
  
  public final boolean maySacrifice()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.SACRIFICE.getBit());
  }
  
  public final boolean maySowFields()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.SOW_FIELDS.getBit());
  }
  
  public final boolean mayTame()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.TAME.getBit());
  }
  
  public final boolean mayTendFields()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.TEND_FIELDS.getBit());
  }
  
  public final boolean mayTerraform()
  {
    return this.settings.hasPermission(VillageRole.RolePermissions.TERRAFORM.getBit());
  }
  
  public final boolean mayTunnel()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.TUNNEL.getBit());
  }
  
  public final boolean mayUnload()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.UNLOAD.getBit());
  }
  
  public final boolean mayUpdateMap()
  {
    return this.mayUpdateMap;
  }
  
  public final boolean mayUseMeditationAbilities()
  {
    return this.moreSettings.hasPermission(VillageRole.MoreRolePermissions.MEDITATION_ABILITY.getBit());
  }
  
  public final int getVillageAppliedTo()
  {
    return this.villageAppliedTo;
  }
  
  public final long getPlayerAppliedTo()
  {
    return this.playerAppliedTo;
  }
  
  public final byte getStatus()
  {
    return this.status;
  }
  
  public final int getId()
  {
    return this.id;
  }
  
  public void setCanBrand(boolean canBrand)
  {
    this.extraSettings.setPermissionBit(VillageRole.ExtraRolePermissions.BRAND.getBit(), canBrand);
  }
  
  public void setCanBreed(boolean canBreed)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.BREED.getBit(), canBreed);
  }
  
  public void setCanButcher(boolean canButcher)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.BUTCHER.getBit(), canButcher);
  }
  
  public void setCanGroom(boolean canGroom)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.GROOM.getBit(), canGroom);
  }
  
  public void setCanLead(boolean canLead)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.LEAD.getBit(), canLead);
  }
  
  public void setCanMilkShear(boolean canMilkShear)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.MILK_SHEAR.getBit(), canMilkShear);
  }
  
  public void setCanPassGates(boolean canPassGates)
  {
    this.extraSettings.setPermissionBit(VillageRole.ExtraRolePermissions.PASS_GATES.getBit(), canPassGates);
  }
  
  public void setCanSacrifice(boolean canSacrifice)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.SACRIFICE.getBit(), canSacrifice);
  }
  
  public void setCanTame(boolean canTame)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.TAME.getBit(), canTame);
  }
  
  public void setCanBuild(boolean canBuild)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.BUILD.getBit(), canBuild);
  }
  
  public void setCanDestroyFence(boolean canDestroyFence)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.DESTROY_FENCE.getBit(), canDestroyFence);
  }
  
  public void setCanDestroyItems(boolean canDestroyItems)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.DESTROY_ITEMS.getBit(), canDestroyItems);
  }
  
  public void setCanPickLocks(boolean canPickLocks)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.PICK_LOCKS.getBit(), canPickLocks);
  }
  
  public void setCanPlanBuildings(boolean canPlanBuildings)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.PLAN_BUILDINGS.getBit(), canPlanBuildings);
  }
  
  public void setCanCultivate(boolean canCultivate)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.CULTIVATE.getBit(), canCultivate);
  }
  
  public void setCanDigResource(boolean canDigResource)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.DIG_RESOURCE.getBit(), canDigResource);
  }
  
  public void setCanPack(boolean canPack)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PACK.getBit(), canPack);
  }
  
  public void setCanTerraform(boolean canTerraform)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.TERRAFORM.getBit(), canTerraform);
  }
  
  public void setCanHarvestFields(boolean canHarvestFields)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.HARVEST_FIELDS.getBit(), canHarvestFields);
  }
  
  public void setCanSowFields(boolean canSowFields)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.SOW_FIELDS.getBit(), canSowFields);
  }
  
  public void setCanTendFields(boolean canTendFields)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.TEND_FIELDS.getBit(), canTendFields);
  }
  
  public void setCanChopDownAllTrees(boolean canChopDownAllTrees)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.CHOP_DOWN_ALL_TREES.getBit(), canChopDownAllTrees);
  }
  
  public void setCanChopDownOldTrees(boolean canChopDownOldTrees)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.CHOP_DOWN_OLD_TREES.getBit(), canChopDownOldTrees);
  }
  
  public void setCanCutGrass(boolean canCutGrass)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.CUT_GRASS.getBit(), canCutGrass);
  }
  
  public void setCanHarvestFruit(boolean canHarvestFruit)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.HARVEST_FRUIT.getBit(), canHarvestFruit);
  }
  
  public void setCanMakeLawn(boolean canMakeLawn)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.MAKE_LAWN.getBit(), canMakeLawn);
  }
  
  public void setCanPickSprouts(boolean canPickSprouts)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.PICK_SPROUTS.getBit(), canPickSprouts);
  }
  
  public void setCanPlantFlowers(boolean canPlantFlowers)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.PLANT_FLOWERS.getBit(), canPlantFlowers);
  }
  
  public void setCanPlantSprouts(boolean canPlantSprouts)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.PLANT_SPROUTS.getBit(), canPlantSprouts);
  }
  
  public void setCanPrune(boolean canPrune)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.PRUNE.getBit(), canPrune);
  }
  
  public void setCanAttackCitizens(boolean canAttackCitizens)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_CITIZENS.getBit(), canAttackCitizens);
  }
  
  public void setCanAttackNonCitizens(boolean canAttackNonCitizens)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.ATTACK_NON_CITIZENS.getBit(), canAttackNonCitizens);
  }
  
  public void setCanCastDeitySpells(boolean canCastDeitySpells)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.CAST_DEITY_SPELLS.getBit(), canCastDeitySpells);
  }
  
  public void setCanCastSorcerySpells(boolean canCastSorcerySpells)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.CAST_SORCERY_SPELLS.getBit(), canCastSorcerySpells);
  }
  
  public void setCanForageBotanize(boolean canForageBotanize)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.FORAGE.getBit(), canForageBotanize);
  }
  
  public void setCanPlaceMerchants(boolean canPlaceMerchants)
  {
    this.settings.setPermissionBit(VillageRole.RolePermissions.PLACE_MERCHANTS.getBit(), canPlaceMerchants);
  }
  
  public void setCanPave(boolean canPave)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PAVE.getBit(), canPave);
  }
  
  public void setCanUseMeditationAbility(boolean canUseMeditationAbility)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MEDITATION_ABILITY.getBit(), canUseMeditationAbility);
  }
  
  public void setCanAttachLocks(boolean canAttachLocks)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.ATTACH_LOCKS.getBit(), canAttachLocks);
  }
  
  public void setCanDrop(boolean canDrop)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.DROP.getBit(), canDrop);
  }
  
  public void setCanImproveRepair(boolean canImproveRepair)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.IMPROVE_REPAIR.getBit(), canImproveRepair);
  }
  
  public void setCanLoad(boolean canLoad)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.LOAD.getBit(), canLoad);
  }
  
  public void setCanPickup(boolean canPickup)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PICKUP.getBit(), canPickup);
  }
  
  public void setCanPickupPlanted(boolean canPickupPlanted)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PICKUP_PLANTED.getBit(), canPickupPlanted);
  }
  
  public void setCanPlantItem(boolean canPlantItem)
  {
    this.extraSettings.setPermissionBit(VillageRole.ExtraRolePermissions.PLANT_ITEM.getBit(), canPlantItem);
  }
  
  public void setCanPullPushTurn(boolean canPullPushTurn)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.PULL_PUSH.getBit(), canPullPushTurn);
  }
  
  public void setCanUnload(boolean canUnload)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.UNLOAD.getBit(), canUnload);
  }
  
  public void setCanMineFloor(boolean canMineFloor)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_FLOOR.getBit(), canMineFloor);
  }
  
  public void setCanMineIron(boolean canMineIronVeins)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_IRON.getBit(), canMineIronVeins);
  }
  
  public void setCanMineOther(boolean canMineOtherVeins)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_OTHER.getBit(), canMineOtherVeins);
  }
  
  public void setCanMineRock(boolean canMineRock)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MINE_ROCK.getBit(), canMineRock);
  }
  
  public void setCanSurface(boolean canMineSurface)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.SURFACE_MINING.getBit(), canMineSurface);
  }
  
  public void setCanTunnel(boolean canTunnel)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.TUNNEL.getBit(), canTunnel);
  }
  
  public void setCanReinforce(boolean canReinforce)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.REINFORCE.getBit(), canReinforce);
  }
  
  public void setCanConfigureTwitter(boolean canConfigureTwitter)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MAY_CONFIGURE_TWITTER.getBit(), canConfigureTwitter);
  }
  
  public void setCanDiplomat(boolean canDiplomat)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.DIPLOMAT.getBit(), canDiplomat);
  }
  
  public void setCanDestroyAnyBuilding(boolean canDestroyAnyBuilding)
  {
    if ((getStatus() == 1) && (canDestroyAnyBuilding))
    {
      Thread.dumpStack();
      return;
    }
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.DESTROY_ANY_BUILDING.getBit(), canDestroyAnyBuilding);
  }
  
  public void setCanInviteCitizens(boolean canInviteCitizens)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.INVITE_CITIZENS.getBit(), canInviteCitizens);
  }
  
  public void setCanManageAllowedObjects(boolean canManageAllowedObjects)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MANAGE_ALLOWED_OBJECTS.getBit(), canManageAllowedObjects);
  }
  
  public void setCanManageCitizenRoles(boolean canManageCitizenRoles)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MANAGE_CITIZEN_ROLES.getBit(), canManageCitizenRoles);
  }
  
  public void setCanManageGuards(boolean canManageGuards)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MANAGE_GUARDS.getBit(), canManageGuards);
  }
  
  public void setCanManageMap(boolean canManageMap)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MANAGE_MAP.getBit(), canManageMap);
  }
  
  public void setCanManageReputations(boolean canManageReputations)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MANAGE_REPUTATIONS.getBit(), canManageReputations);
  }
  
  public void setCanManageRoles(boolean canManageRoles)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MANAGE_ROLES.getBit(), canManageRoles);
  }
  
  public void setCanManageSettings(boolean canManageSettings)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.MANAGE_SETTINGS.getBit(), canManageSettings);
  }
  
  public void setCanResizeSettlement(boolean canResizeSettlement)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.RESIZE.getBit(), canResizeSettlement);
  }
  
  public void SetCanPerformActionsOnAlliedDeeds(boolean canPerformActionsOnAlliedDeeds)
  {
    this.moreSettings.setPermissionBit(VillageRole.MoreRolePermissions.ALLOW_ACTIONS_ON_ALLIED_DEED.getBit(), canPerformActionsOnAlliedDeeds);
  }
  
  abstract void create()
    throws IOException;
  
  public abstract void setName(String paramString)
    throws IOException;
  
  public abstract void setMayHire(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayBuild(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayCuttrees(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayMine(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayFarm(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayManageRoles(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayDestroy(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayTerraform(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayExpand(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayInvite(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayPassAllFences(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayLockFences(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayAttackCitizens(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayAttackNonCitizens(boolean paramBoolean)
    throws IOException;
  
  public abstract void setDiplomat(boolean paramBoolean)
    throws IOException;
  
  public abstract void setVillageAppliedTo(int paramInt)
    throws IOException;
  
  public abstract void setMayFish(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayPushPullTurn(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayLead(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayPickup(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayTame(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayLoad(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayButcher(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayAttachLock(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayPickLocks(boolean paramBoolean)
    throws IOException;
  
  public abstract void setMayUpdateMap(boolean paramBoolean)
    throws IOException;
  
  public abstract void setCutOld(boolean paramBoolean)
    throws IOException;
  
  public abstract void delete()
    throws IOException;
  
  public abstract void save()
    throws IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\villages\VillageRole.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */