package com.wurmonline.server.batchjobs;

import com.wurmonline.server.creatures.MineDoorPermission;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MineDoorBatchJob
{
  private static Logger logger = Logger.getLogger(MineDoorBatchJob.class.getName());
  
  public static final void convertToNewPermissions()
  {
    logger.log(Level.INFO, "Converting Mine Doors to New Permission System.");
    int minedoorsDone = 0;
    for (MineDoorPermission md : MineDoorPermission.getAllMineDoors()) {
      if (md.convertToNewPermissions()) {
        minedoorsDone++;
      }
    }
    logger.log(Level.INFO, "Converted " + minedoorsDone + " Mine Doors to New Permissions System.");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\batchjobs\MineDoorBatchJob.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */