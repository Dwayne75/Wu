package com.wurmonline.server;

import com.wurmonline.mesh.MeshIO;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class MeshSaver
  implements Runnable
{
  private static final Logger logger = Logger.getLogger(MeshSaver.class.getName());
  private final MeshIO iMapLayer;
  private final String iMapLayerName;
  private final int iNumberOfRowsPerCall;
  
  MeshSaver(MeshIO aMapLayerToSave, String aMapLayerName, int aNumberOfRowsPerCall)
  {
    this.iMapLayer = aMapLayerToSave;
    this.iMapLayerName = aMapLayerName;
    this.iNumberOfRowsPerCall = aNumberOfRowsPerCall;
    logger.info("Created MeshSaver for map layer: '" + this.iMapLayerName + "', " + aMapLayerToSave + ", rowsPerCall: " + aNumberOfRowsPerCall);
  }
  
  MeshSaver(MeshIO aMapLayerToSave, String aMapLayerName)
  {
    this(aMapLayerToSave, aMapLayerName, 1);
  }
  
  public void run()
  {
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("Running MeshSaver for calling MeshIO.saveDirtyRow() for '" + this.iMapLayerName + "', " + this.iMapLayer + ", rowsPerCall: " + this.iNumberOfRowsPerCall);
    }
    try
    {
      long now = System.nanoTime();
      int numberOfRowsSaved = this.iNumberOfRowsPerCall;
      if (this.iNumberOfRowsPerCall <= 0) {
        numberOfRowsSaved = this.iMapLayer.saveAllDirtyRows();
      } else if (this.iNumberOfRowsPerCall > this.iMapLayer.getSize()) {
        this.iMapLayer.saveAll();
      } else {
        for (int i = 0; i < this.iNumberOfRowsPerCall; i++) {
          if (this.iMapLayer.saveNextDirtyRow()) {
            break;
          }
        }
      }
      float lElapsedTime = (float)(System.nanoTime() - now) / 1000000.0F;
      if ((lElapsedTime > (float)Constants.lagThreshold) || (logger.isLoggable(Level.FINER))) {
        logger.info("Finished saving " + numberOfRowsSaved + " rows for '" + this.iMapLayerName + "', which took " + lElapsedTime + " millis.");
      }
    }
    catch (RuntimeException e)
    {
      logger.log(Level.WARNING, "Caught exception in MeshSaver while saving Mesh for '" + this.iMapLayerName + "' " + this.iMapLayer, e);
      throw e;
    }
    catch (IOException e)
    {
      logger.log(Level.WARNING, "Caught exception in MeshSaver while saving Mesh for '" + this.iMapLayerName + "' " + this.iMapLayer, e);
      throw new RuntimeException(e);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\MeshSaver.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */