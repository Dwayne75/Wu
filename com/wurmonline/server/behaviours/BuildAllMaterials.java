package com.wurmonline.server.behaviours;

import com.wurmonline.server.items.NoSuchTemplateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class BuildAllMaterials
{
  private final List<BuildStageMaterials> bsms;
  
  public BuildAllMaterials()
  {
    this.bsms = new ArrayList();
  }
  
  public void add(BuildStageMaterials bms)
  {
    this.bsms.add(bms);
  }
  
  public List<BuildStageMaterials> getBuildStageMaterials()
  {
    return this.bsms;
  }
  
  public BuildStageMaterials getBuildStageMaterials(byte stage)
  {
    return (BuildStageMaterials)this.bsms.get(Math.max(0, stage));
  }
  
  public int getStageCount()
  {
    return this.bsms.size();
  }
  
  public List<BuildMaterial> getCurrentRequiredMaterials()
  {
    Iterator localIterator = this.bsms.iterator();
    if (localIterator.hasNext())
    {
      BuildStageMaterials bsm = (BuildStageMaterials)localIterator.next();
      
      return bsm.getRequiredMaterials();
    }
    return new ArrayList();
  }
  
  public String getStageCountAsString()
  {
    switch (this.bsms.size())
    {
    case 1: 
      return "one";
    case 2: 
      return "two";
    case 3: 
      return "three";
    case 4: 
      return "four";
    case 5: 
      return "five";
    case 6: 
      return "six";
    case 7: 
      return "seven";
    }
    return "" + this.bsms.size();
  }
  
  public void setNeeded(byte currentStage, int done)
  {
    for (int stage = 0; stage < this.bsms.size(); stage++) {
      if (currentStage > stage) {
        getBuildStageMaterials((byte)stage).setNoneNeeded();
      } else if (currentStage == stage) {
        getBuildStageMaterials((byte)stage).reduceNeededBy(done);
      } else {
        getBuildStageMaterials((byte)stage).setMaxNeeded();
      }
    }
  }
  
  public List<BuildMaterial> getTotalMaterialsNeeded()
  {
    BuildStageMaterials all = getTotalMaterialsRequired();
    return all.getBuildMaterials();
  }
  
  private BuildStageMaterials getTotalMaterialsRequired()
  {
    Map<Integer, Integer> mats = new HashMap();
    for (Iterator localIterator1 = this.bsms.iterator(); localIterator1.hasNext();)
    {
      bsm = (BuildStageMaterials)localIterator1.next();
      for (BuildMaterial bm : bsm.getBuildMaterials())
      {
        int qty = bm.getNeededQuantity();
        if (qty > 0)
        {
          Integer key = Integer.valueOf(bm.getTemplateId());
          if (mats.containsKey(key)) {
            qty += ((Integer)mats.get(key)).intValue();
          }
          mats.put(key, Integer.valueOf(qty));
        }
      }
    }
    BuildStageMaterials bsm;
    BuildStageMaterials all = new BuildStageMaterials("All");
    for (Object entry : mats.entrySet()) {
      try
      {
        all.add(((Integer)((Map.Entry)entry).getKey()).intValue(), ((Integer)((Map.Entry)entry).getValue()).intValue());
      }
      catch (NoSuchTemplateException localNoSuchTemplateException) {}
    }
    return all;
  }
  
  public BuildAllMaterials getRemainingMaterialsNeeded()
  {
    BuildAllMaterials toReturn = new BuildAllMaterials();
    for (BuildStageMaterials bsm : this.bsms) {
      if (!bsm.isStageComplete()) {
        toReturn.add(bsm);
      }
    }
    return toReturn;
  }
  
  public String getRequiredMaterialString(boolean detailed)
  {
    BuildStageMaterials all = getTotalMaterialsRequired();
    return all.getRequiredMaterialString(detailed);
  }
  
  public int getTotalQuantityRequired()
  {
    int count = 0;
    for (BuildStageMaterials bsm : this.bsms) {
      count += bsm.getTotalQuantityRequired();
    }
    return count;
  }
  
  public int getTotalQuantityDone()
  {
    int count = 0;
    for (BuildStageMaterials bsm : this.bsms) {
      count += bsm.getTotalQuantityDone();
    }
    return count;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\behaviours\BuildAllMaterials.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */