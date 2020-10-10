package com.wurmonline.server.players;

import java.util.LinkedList;

public class PermissionsHistory
{
  private LinkedList<PermissionsHistoryEntry> historyEntries = new LinkedList();
  
  void add(long eventTime, long playerId, String playerName, String event)
  {
    this.historyEntries.addFirst(new PermissionsHistoryEntry(eventTime, playerId, playerName, event.replace("\"", "'")));
  }
  
  public PermissionsHistoryEntry[] getHistoryEvents()
  {
    return (PermissionsHistoryEntry[])this.historyEntries.toArray(new PermissionsHistoryEntry[this.historyEntries.size()]);
  }
  
  public String[] getHistory(int numevents)
  {
    String[] hist = new String[0];
    int lHistorySize = this.historyEntries.size();
    if (lHistorySize > 0)
    {
      int numbersToFetch = Math.min(numevents, lHistorySize);
      
      hist = new String[numbersToFetch];
      PermissionsHistoryEntry[] events = getHistoryEvents();
      for (int x = 0; x < numbersToFetch; x++) {
        hist[x] = events[x].getLongDesc();
      }
    }
    return hist;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\players\PermissionsHistory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */