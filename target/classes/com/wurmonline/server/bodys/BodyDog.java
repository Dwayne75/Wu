package com.wurmonline.server.bodys;

import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.util.Random;

final class BodyDog
  extends BodyTemplate
{
  BodyDog()
  {
    super((byte)3);
    this.leftOverArmS = "thigh of the left foreleg";
    this.rightOverArmS = "thigh of the right foreleg";
    this.leftThighS = "thigh of the left hindleg";
    this.rightThighS = "thigh of the right hindleg";
    this.leftUnderArmS = "calf of the left foreleg";
    this.rightUnderArmS = "calf of the right foreleg";
    this.leftCalfS = "calf of the left hindleg";
    this.rightCalfS = "calf of the right hindleg";
    this.leftHandS = "left paw";
    this.rightHandS = "right paw";
    this.leftFootS = "left paw";
    this.rightFootS = "right paw";
    this.leftArmS = "left foreleg";
    this.rightArmS = "right foreleg";
    this.leftLegS = "left hindleg";
    this.rightLegS = "right hindleg";
    this.typeString = new String[] { this.bodyS, this.headS, this.torsoS, this.leftArmS, this.rightArmS, this.leftOverArmS, this.rightOverArmS, this.leftThighS, this.rightThighS, this.leftUnderArmS, this.rightUnderArmS, this.leftCalfS, this.rightCalfS, this.leftHandS, this.rightHandS, this.leftFootS, this.rightFootS, this.neckS, this.leftEyeS, this.rightEyeS, this.centerEyeS, this.chestS, this.topBackS, this.stomachS, this.lowerBackS, this.crotchS, this.leftShoulderS, this.rightShoulderS, this.secondHeadS, this.faceS, this.leftLegS, this.rightLegS, this.hipS, this.baseOfNoseS, this.legsS };
  }
  
  public byte getRandomWoundPos()
    throws Exception
  {
    int rand = Server.rand.nextInt(1000);
    if (rand < 3) {
      return 1;
    }
    if (rand < 80) {
      return 5;
    }
    if (rand < 130) {
      return 6;
    }
    if (rand < 180) {
      return 7;
    }
    if (rand < 230) {
      return 8;
    }
    if (rand < 280) {
      return 9;
    }
    if (rand < 320) {
      return 10;
    }
    if (rand < 370) {
      return 11;
    }
    if (rand < 420) {
      return 12;
    }
    if (rand < 460) {
      return 13;
    }
    if (rand < 500) {
      return 14;
    }
    if (rand < 540) {
      return 15;
    }
    if (rand < 580) {
      return 16;
    }
    if (rand < 600) {
      return 17;
    }
    if (rand < 601) {
      return 18;
    }
    if (rand < 602) {
      return 19;
    }
    if (rand < 730) {
      return 21;
    }
    if (rand < 780) {
      return 22;
    }
    if (rand < 830) {
      return 23;
    }
    if (rand < 890) {
      return 24;
    }
    if (rand < 900) {
      return 25;
    }
    if (rand < 950) {
      return 26;
    }
    if (rand < 1000) {
      return 27;
    }
    throw new WurmServerException("Bad randomizer");
  }
  
  void buildBody(Item[] spaces, Creature owner)
  {
    spaces[0].setOwner(owner.getWurmId(), true);
    spaces[0].insertItem(spaces[1]);
    spaces[1].insertItem(spaces[29]);
    spaces[0].insertItem(spaces[2]);
    spaces[2].insertItem(spaces[34]);
    spaces[34].insertItem(spaces[15]);
    spaces[34].insertItem(spaces[16]);
    spaces[34].insertItem(spaces[3]);
    spaces[34].insertItem(spaces[4]);
    spaces[3].insertItem(spaces[13]);
    spaces[4].insertItem(spaces[14]);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\bodys\BodyDog.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */