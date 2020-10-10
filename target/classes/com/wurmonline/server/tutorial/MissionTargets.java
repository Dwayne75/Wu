package com.wurmonline.server.tutorial;

import com.wurmonline.server.WurmId;
import com.wurmonline.server.structures.Wall;
import com.wurmonline.shared.constants.CounterTypes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.annotation.Nullable;

public final class MissionTargets
  implements CounterTypes
{
  private static final Map<Long, MissionTarget> missionTargets = new HashMap();
  private static final Logger logger = Logger.getLogger(MissionTargets.class.getName());
  
  public static void destroyMissionTarget(long missionTarget, boolean destroyTriggers)
  {
    MissionTarget m = (MissionTarget)missionTargets.remove(Long.valueOf(missionTarget));
    if ((m != null) && (destroyTriggers)) {
      m.destroy();
    }
  }
  
  public static boolean isMissionTarget(long potentialTarget)
  {
    return missionTargets.containsKey(Long.valueOf(potentialTarget));
  }
  
  public static MissionTarget getMissionTargetFor(long potentialTarget)
  {
    return (MissionTarget)missionTargets.get(Long.valueOf(potentialTarget));
  }
  
  public static Long[] getTargetIds()
  {
    return (Long[])missionTargets.keySet().toArray(new Long[missionTargets.size()]);
  }
  
  public static boolean destroyStructureTargets(long structureId, @Nullable String possibleCreatorName)
  {
    boolean found = false;
    Long[] targs = getTargetIds();
    for (Long tid : targs) {
      if (tid != null)
      {
        long targetId = tid.longValue();
        if (WurmId.getType(targetId) == 5)
        {
          Wall w = Wall.getWall(targetId);
          if (w != null) {
            if (w.getStructureId() == structureId)
            {
              MissionTarget mt = getMissionTargetFor(targetId);
              if (mt != null)
              {
                MissionTrigger[] mits = mt.getMissionTriggers();
                for (MissionTrigger missionT : mits) {
                  if ((possibleCreatorName == null) || 
                    (missionT.getCreatorName().toLowerCase().equals(possibleCreatorName)))
                  {
                    found = true;
                    missionT.destroy();
                  }
                }
              }
            }
          }
        }
      }
    }
    return found;
  }
  
  public static void addMissionTrigger(MissionTrigger trigger)
  {
    MissionTarget mt = getMissionTargetFor(trigger.getTarget());
    if ((mt == null) && (trigger.getTarget() > 0L))
    {
      mt = new MissionTarget(trigger.getTarget());
      missionTargets.put(Long.valueOf(trigger.getTarget()), mt);
    }
    if (mt != null) {
      mt.addMissionTrigger(trigger);
    }
  }
  
  public static void removeMissionTrigger(MissionTrigger trigger, boolean destroyAllTriggers)
  {
    if (trigger != null)
    {
      MissionTarget mt = getMissionTargetFor(trigger.getTarget());
      if (mt != null)
      {
        mt.removeMissionTrigger(trigger);
        if (mt.getNumTriggers() == 0) {
          destroyMissionTarget(mt.getId(), destroyAllTriggers);
        }
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\tutorial\MissionTargets.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */