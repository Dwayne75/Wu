package com.wurmonline.server.intra;

import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.TimeConstants;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class MountTransfer
  implements MiscConstants, TimeConstants
{
  private static final Map<Long, MountTransfer> transfers = new HashMap();
  private static final Map<Long, MountTransfer> transfersPerCreature = new HashMap();
  private final Map<Long, Integer> seats = new HashMap();
  private static final Logger logger = Logger.getLogger(MountTransfer.class.getName());
  private final long vehicleid;
  private final long pilotid;
  private final long creationTime;
  
  public MountTransfer(long vehicleId, long pilotId)
  {
    this.vehicleid = vehicleId;
    this.pilotid = pilotId;
    this.creationTime = System.currentTimeMillis();
    transfers.put(Long.valueOf(vehicleId), this);
  }
  
  public void addToSeat(long wid, int seatid)
  {
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("Adding " + wid + ", seat=" + seatid);
    }
    this.seats.put(Long.valueOf(wid), Integer.valueOf(seatid));
    transfersPerCreature.put(Long.valueOf(wid), this);
  }
  
  public int getSeatFor(long wurmid)
  {
    if (this.seats.keySet().contains(Long.valueOf(wurmid))) {
      return ((Integer)this.seats.get(Long.valueOf(wurmid))).intValue();
    }
    return -1;
  }
  
  public void remove(long wurmid)
  {
    if (logger.isLoggable(Level.FINER)) {
      logger.finer("Removing " + wurmid);
    }
    this.seats.remove(Long.valueOf(wurmid));
    transfersPerCreature.remove(Long.valueOf(wurmid));
    if (this.seats.isEmpty()) {
      clearAndRemove();
    }
  }
  
  long getCreationTime()
  {
    return this.creationTime;
  }
  
  private void clearAndRemove()
  {
    for (Iterator<Long> seatIt = this.seats.keySet().iterator(); seatIt.hasNext();) {
      transfersPerCreature.remove(seatIt.next());
    }
    transfers.remove(Long.valueOf(this.vehicleid));
    this.seats.clear();
  }
  
  public long getVehicleId()
  {
    return this.vehicleid;
  }
  
  public long getPilotId()
  {
    return this.pilotid;
  }
  
  public static final MountTransfer getTransferFor(long wurmid)
  {
    return (MountTransfer)transfersPerCreature.get(Long.valueOf(wurmid));
  }
  
  public static final void pruneTransfers()
  {
    Set<MountTransfer> toRemove = new HashSet();
    for (Iterator<MountTransfer> it = transfers.values().iterator(); it.hasNext();)
    {
      MountTransfer mt = (MountTransfer)it.next();
      if (System.currentTimeMillis() - mt.getCreationTime() > 1800000L) {
        toRemove.add(mt);
      }
    }
    for (Iterator<MountTransfer> it2 = toRemove.iterator(); it2.hasNext();) {
      ((MountTransfer)it2.next()).clearAndRemove();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\intra\MountTransfer.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */