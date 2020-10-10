package com.wurmonline.server.combat;

import com.wurmonline.server.Constants;
import com.wurmonline.server.creatures.Creature;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Battle
{
  private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private final SimpleDateFormat filedf = new SimpleDateFormat("yyyy-MM-ddHHmmss");
  private final Set<Creature> creatures;
  private List<Creature> casualties;
  private final List<BattleEvent> events;
  private final long startTime;
  private long endTime;
  private final String name;
  private static final Logger logger = Logger.getLogger(Battle.class.getName());
  private static final String header = "<HTML> <HEAD><TITLE>Wurm battle log</TITLE></HEAD><BODY><BR><BR><B>";
  private static final String footer = "</BODY></HTML>";
  
  Battle(Creature attacker, Creature defender)
  {
    this.creatures = new HashSet();
    this.creatures.add(attacker);
    this.creatures.add(defender);
    this.startTime = System.currentTimeMillis();
    this.endTime = System.currentTimeMillis();
    
    attacker.setBattle(this);
    defender.setBattle(this);
    this.events = new LinkedList();
    this.name = ("Battle_" + attacker.getName() + "_vs_" + defender.getName());
  }
  
  boolean containsCreature(Creature creature)
  {
    return this.creatures.contains(creature);
  }
  
  void addCreature(Creature creature)
  {
    if (!this.creatures.contains(creature))
    {
      this.creatures.add(creature);
      this.events.add(new BattleEvent((short)-1, creature.getName()));
      creature.setBattle(this);
    }
    touch();
  }
  
  public void removeCreature(Creature creature)
  {
    this.creatures.remove(creature);
    creature.setBattle(null);
    this.events.add(new BattleEvent((short)-2, creature.getName()));
    touch();
  }
  
  void clearCreatures()
  {
    this.creatures.clear();
  }
  
  public void addCasualty(Creature dead)
  {
    if (this.casualties == null) {
      this.casualties = new LinkedList();
    }
    this.casualties.add(dead);
    this.events.add(new BattleEvent((short)-3, dead.getName()));
    this.creatures.remove(dead);
    dead.setBattle(null);
    touch();
  }
  
  void touch()
  {
    this.endTime = System.currentTimeMillis();
  }
  
  public void addCasualty(Creature killer, Creature dead)
  {
    if (this.casualties == null) {
      this.casualties = new LinkedList();
    }
    this.casualties.add(dead);
    this.events.add(new BattleEvent((short)-3, dead.getName(), killer.getName()));
    this.creatures.remove(dead);
    dead.setBattle(null);
    touch();
  }
  
  public void addEvent(BattleEvent event)
  {
    this.events.add(event);
    touch();
  }
  
  Creature[] getCreatures()
  {
    return (Creature[])this.creatures.toArray(new Creature[this.creatures.size()]);
  }
  
  public long getStartTime()
  {
    return this.startTime;
  }
  
  public long getEndTime()
  {
    return this.endTime;
  }
  
  void save()
  {
    Writer output;
    if ((this.casualties != null) && (this.casualties.size() > 0))
    {
      output = null;
      try
      {
        Date d = new Date(this.startTime);
        String dir = Constants.webPath;
        if (!dir.endsWith(File.separator)) {
          dir = dir + File.separator;
        }
        File aFile = new File(dir + this.name + "_" + this.filedf.format(d) + ".html");
        
        output = new BufferedWriter(new FileWriter(aFile));
        
        String start = this.name + "</B><BR><I>started at " + this.df.format(d) + " and ended on " + this.df.format(new Date(this.endTime)) + "</I><BR><BR>";
        try
        {
          output.write("<HTML> <HEAD><TITLE>Wurm battle log</TITLE></HEAD><BODY><BR><BR><B>");
          output.write(start);
        }
        catch (IOException iox)
        {
          logger.log(Level.WARNING, iox.getMessage(), iox);
        }
        for (BattleEvent lBattleEvent : this.events)
        {
          String ts = lBattleEvent.toString();
          try
          {
            output.write(ts);
          }
          catch (IOException iox)
          {
            logger.log(Level.WARNING, iox.getMessage(), iox);
          }
        }
        output.write("</BODY></HTML>");
        try
        {
          if (output != null) {
            output.close();
          }
        }
        catch (IOException localIOException1) {}
        output = this.creatures.iterator();
      }
      catch (IOException iox)
      {
        logger.log(Level.WARNING, "Failed to close " + this.name, iox);
      }
      finally
      {
        try
        {
          if (output != null) {
            output.close();
          }
        }
        catch (IOException localIOException3) {}
      }
    }
    while (output.hasNext())
    {
      Creature cret = (Creature)output.next();
      
      cret.setBattle(null);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\combat\Battle.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */