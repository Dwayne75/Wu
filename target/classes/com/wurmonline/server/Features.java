package com.wurmonline.server;

import java.util.logging.Logger;

public final class Features
{
  private static final Logger logger = Logger.getLogger(Features.class.getName());
  private static int currentProdVersion = 129;
  
  public static void loadAllFeatures()
  {
    Features.Feature.access$000();
    
    logFeatureDetails();
  }
  
  public static void logFeatureDetails()
  {
    for (Features.Feature lFeature : ) {
      logger.info(lFeature.toString());
    }
  }
  
  public static int getVerionsNo()
  {
    return currentProdVersion;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\Features.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */