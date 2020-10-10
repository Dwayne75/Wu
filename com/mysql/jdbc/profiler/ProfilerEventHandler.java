package com.mysql.jdbc.profiler;

import com.mysql.jdbc.Extension;

public abstract interface ProfilerEventHandler
  extends Extension
{
  public abstract void consumeEvent(ProfilerEvent paramProfilerEvent);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\profiler\ProfilerEventHandler.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */