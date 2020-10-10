package com.wurmonline.server.combat;

import java.util.logging.Logger;

public final class WeaponCreator
{
  private static final Logger logger = Logger.getLogger(WeaponCreator.class.getName());
  
  public static final void createWeapons()
  {
    logger.info("Creating weapons");
    long start = System.nanoTime();
    
    Weapon awl = new Weapon(390, 1.0F, 3.0F, 0.0F, 1, 1, 0.0F, 2.0D);
    Weapon knifele = new Weapon(392, 0.5F, 2.0F, 0.0F, 1, 1, 0.0F, 2.0D);
    Weapon knifeca = new Weapon(8, 1.0F, 2.0F, 0.0F, 1, 1, 1.0F, 2.0D);
    Weapon knifebu = new Weapon(93, 1.5F, 2.0F, 0.0F, 1, 1, 1.0F, 1.0D);
    Weapon knifesa = new Weapon(792, 1.5F, 2.0F, 0.03F, 1, 1, 1.0F, 1.0D);
    Weapon knifecru = new Weapon(685, 1.0F, 4.0F, 0.0F, 1, 1, 0.0F, 3.0D);
    Weapon pickaxecru = new Weapon(687, 1.0F, 6.0F, 0.0F, 1, 1, 0.0F, 5.0D);
    Weapon shaftcru = new Weapon(691, 1.0F, 3.0F, 0.0F, 1, 1, 0.0F, 5.0D);
    Weapon shovelcru = new Weapon(690, 1.0F, 6.0F, 0.0F, 1, 1, 0.0F, 5.0D);
    Weapon branch = new Weapon(688, 1.0F, 6.0F, 0.0F, 1, 1, 0.0F, 3.0D);
    Weapon axecru = new Weapon(1011, 1.0F, 5.0F, 0.0F, 1, 1, 0.0F, 5.0D);
    Weapon scissor = new Weapon(394, 0.5F, 2.0F, 0.0F, 1, 1, 0.0F, 2.0D);
    Weapon swoshort = new Weapon(80, 4.0F, 3.0F, 0.1F, 2, 1, 1.0F, 0.0D);
    
    Weapon bowL = new Weapon(449, 0.0F, 5.0F, 0.0F, 1, 5, 1.0F, 9.0D);
    bowL.setDamagedByMetal(true);
    Weapon bowM = new Weapon(448, 0.0F, 5.0F, 0.0F, 1, 5, 1.0F, 9.0D);
    bowM.setDamagedByMetal(true);
    Weapon bowS = new Weapon(447, 0.0F, 5.0F, 0.0F, 1, 5, 1.0F, 9.0D);
    bowS.setDamagedByMetal(true);
    Weapon bowLN = new Weapon(461, 0.0F, 5.0F, 0.0F, 1, 5, 1.0F, 9.0D);
    bowLN.setDamagedByMetal(true);
    Weapon bowSN = new Weapon(459, 0.0F, 5.0F, 0.0F, 1, 5, 1.0F, 9.0D);
    bowSN.setDamagedByMetal(true);
    Weapon bowMN = new Weapon(460, 0.0F, 5.0F, 0.0F, 1, 5, 1.0F, 9.0D);
    bowMN.setDamagedByMetal(true);
    
    Weapon hatchet = new Weapon(7, 1.0F, 5.0F, 0.0F, 2, 2, 0.0F, 3.0D);
    hatchet.setDamagedByMetal(true);
    Weapon pickax = new Weapon(20, 1.5F, 5.0F, 0.0F, 3, 3, 0.1F, 3.0D);
    pickax.setDamagedByMetal(true);
    Weapon shovel = new Weapon(25, 1.0F, 5.0F, 0.0F, 4, 3, 1.0F, 3.0D);
    shovel.setDamagedByMetal(true);
    Weapon rake = new Weapon(27, 0.5F, 5.0F, 0.0F, 5, 2, 1.0F, 3.0D);
    rake.setDamagedByMetal(true);
    
    Weapon saw = new Weapon(24, 0.5F, 5.0F, 0.01F, 2, 3, 0.0F, 3.0D);
    Weapon sickle = new Weapon(267, 6.0F, 3.0F, 0.02F, 2, 3, 0.2F, 2.0D);
    Weapon scythe = new Weapon(268, 9.0F, 5.0F, 0.08F, 5, 4, 0.2F, 2.0D);
    scythe.setDamagedByMetal(true);
    
    Weapon longswo = new Weapon(21, 5.5F, 4.0F, 0.01F, 3, 3, 1.0F, 0.0D);
    Weapon twoswo = new Weapon(81, 9.0F, 5.0F, 0.05F, 4, 5, 1.0F, 0.0D);
    Weapon smallax = new Weapon(3, 5.0F, 3.0F, 0.0F, 2, 2, 0.3F, 0.0D);
    Weapon batax = new Weapon(90, 6.5F, 4.0F, 0.03F, 4, 5, 0.3F, 0.0D);
    Weapon twoaxe = new Weapon(87, 12.0F, 6.0F, 0.05F, 5, 5, 0.2F, 0.0D);
    Weapon swoMag = new Weapon(336, 15.0F, 5.0F, 0.08F, 4, 3, 1.0F, 0.0D);
    Weapon whipOne = new Weapon(514, 6.0F, 2.0F, 0.0F, 5, 1, 0.1F, 0.0D);
    
    Weapon spearLong = new Weapon(705, 8.0F, 5.0F, 0.06F, 7, 3, 1.0F, 0.0D);
    Weapon halberd = new Weapon(706, 9.0F, 5.0F, 0.06F, 6, 8, 1.0F, 0.0D);
    Weapon steelSpear = new Weapon(707, 9.0F, 5.0F, 0.06F, 7, 4, 1.0F, 0.0D);
    Weapon staffSteel = new Weapon(710, 8.0F, 4.0F, 0.0F, 3, 3, 1.0F, 0.0D);
    Weapon staffLand = new Weapon(986, 8.0F, 4.0F, 0.0F, 3, 3, 1.0F, 0.0D);
    
    Weapon fist = new Weapon(14, 1.0F, 1.0F, 0.0F, 1, 1, 0.0F, 2.0D);
    Weapon foot = new Weapon(19, 1.0F, 2.0F, 0.0F, 1, 1, 0.0F, 3.0D);
    Weapon plank = new Weapon(22, 0.5F, 4.0F, 0.0F, 2, 1, 1.0F, 3.0D);
    plank.setDamagedByMetal(true);
    Weapon shaft = new Weapon(23, 0.5F, 4.0F, 0.0F, 2, 2, 1.0F, 3.0D);
    shaft.setDamagedByMetal(true);
    Weapon staff = new Weapon(711, 2.0F, 3.0F, 0.0F, 2, 3, 1.0F, 0.0D);
    staff.setDamagedByMetal(true);
    
    Weapon metalh = new Weapon(62, 0.5F, 3.0F, 0.0F, 1, 1, 0.1F, 3.0D);
    metalh.setDamagedByMetal(true);
    Weapon woodenh = new Weapon(63, 0.3F, 3.0F, 0.0F, 1, 1, 0.1F, 3.0D);
    woodenh.setDamagedByMetal(true);
    Weapon maulL = new Weapon(290, 11.0F, 6.0F, 0.03F, 4, 5, 1.0F, 0.0D);
    Weapon maulM = new Weapon(292, 8.0F, 5.0F, 0.03F, 3, 2, 1.0F, 0.0D);
    
    Weapon maulS = new Weapon(291, 4.5F, 3.0F, 0.01F, 2, 2, 1.0F, 0.0D);
    
    Weapon belaying = new Weapon(567, 2.0F, 3.0F, 0.0F, 1, 1, 1.0F, 2.0D);
    belaying.setDamagedByMetal(true);
    Weapon clubH = new Weapon(314, 8.0F, 6.0F, 0.01F, 4, 6, 1.0F, 2.0D);
    clubH.setDamagedByMetal(true);
    Weapon hamMag = new Weapon(337, 18.0F, 6.0F, 0.08F, 4, 4, 1.0F, 0.0D);
    Weapon sceptre = new Weapon(340, 17.0F, 6.0F, 0.08F, 3, 3, 1.0F, 0.0D);
    
    Weapon steelCrowbar = new Weapon(1115, 4.5F, 3.0F, 0.01F, 2, 2, 1.0F, 0.0D);
    
    long end = System.nanoTime();
    logger.info("Creating weapons took " + (float)(end - start) / 1000000.0F + " ms");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\combat\WeaponCreator.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */