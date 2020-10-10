package com.wurmonline.server.items;

import com.wurmonline.server.NoSuchEntryException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CreationMatrix
{
  private static Map<Integer, List<CreationEntry>> matrix = new HashMap();
  private static CreationMatrix instance;
  private static Map<Integer, List<CreationEntry>> advancedEntries = new HashMap();
  private static Map<Integer, List<CreationEntry>> simpleEntries = new HashMap();
  
  public static CreationMatrix getInstance()
  {
    if (instance == null) {
      instance = new CreationMatrix();
    }
    return instance;
  }
  
  public void addCreationEntry(CreationEntry entry)
  {
    Integer space = Integer.valueOf(entry.getObjectTarget());
    List<CreationEntry> entrys = (List)matrix.get(space);
    if (entrys == null) {
      entrys = new LinkedList();
    }
    entrys.add(entry);
    matrix.put(space, entrys);
    
    space = Integer.valueOf(entry.getObjectCreated());
    if ((entry instanceof AdvancedCreationEntry))
    {
      entrys = (List)advancedEntries.get(space);
      if (entrys == null) {
        entrys = new LinkedList();
      }
      entrys.add(entry);
      advancedEntries.put(space, entrys);
    }
    entrys = (List)simpleEntries.get(space);
    if (entrys == null) {
      entrys = new LinkedList();
    }
    entrys.add(entry);
    simpleEntries.put(space, entrys);
  }
  
  public CreationEntry[] getCreationOptionsFor(int sourceTemplateId, int targetTemplateId)
  {
    List<CreationEntry> entrys = (List)matrix.get(Integer.valueOf(targetTemplateId));
    List<CreationEntry> options = new LinkedList();
    if (entrys != null) {
      for (CreationEntry entry : entrys) {
        if ((entry.getObjectSource() == sourceTemplateId) && 
        
          (entry.getObjectTarget() == targetTemplateId)) {
          options.add(entry);
        }
      }
    }
    entrys = (List)matrix.get(Integer.valueOf(sourceTemplateId));
    if (entrys != null) {
      for (CreationEntry entry : entrys) {
        if ((entry.getObjectSource() == targetTemplateId) && 
        
          (entry.getObjectTarget() == sourceTemplateId)) {
          options.add(entry);
        }
      }
    }
    CreationEntry[] toReturn = new CreationEntry[options.size()];
    return (CreationEntry[])options.toArray(toReturn);
  }
  
  public CreationEntry[] getCreationOptionsFor(Item source, Item target)
  {
    Integer space = Integer.valueOf(target.getTemplateId());
    List<CreationEntry> entrys = (List)matrix.get(space);
    List<CreationEntry> options = new LinkedList();
    if (entrys != null) {
      for (CreationEntry entry : entrys) {
        if ((entry.getObjectSource() == source.getTemplateId()) && 
        
          ((entry.getObjectSourceMaterial() == 0) || 
          (entry.getObjectSourceMaterial() == source.getMaterial())) && 
          
          (entry.getObjectTarget() == target.getTemplateId()) && (
          
          (entry.getObjectTargetMaterial() == 0) || 
          (entry.getObjectTargetMaterial() == target.getMaterial()))) {
          options.add(entry);
        }
      }
    }
    entrys = (List)matrix.get(Integer.valueOf(source.getTemplateId()));
    if (entrys != null) {
      for (CreationEntry entry : entrys) {
        if ((entry.getObjectSource() == target.getTemplateId()) && 
        
          ((entry.getObjectSourceMaterial() == 0) || 
          (entry.getObjectSourceMaterial() == target.getMaterial())) && 
          
          (entry.getObjectTarget() == source.getTemplateId()) && (
          
          (entry.getObjectTargetMaterial() == 0) || 
          (entry.getObjectTargetMaterial() == source.getMaterial()))) {
          options.add(entry);
        }
      }
    }
    CreationEntry[] toReturn = new CreationEntry[options.size()];
    return (CreationEntry[])options.toArray(toReturn);
  }
  
  public AdvancedCreationEntry getAdvancedCreationEntry(int objectCreated)
    throws NoSuchEntryException
  {
    if (advancedEntries != null)
    {
      LinkedList<CreationEntry> alist = (LinkedList)advancedEntries.get(Integer.valueOf(objectCreated));
      if (alist == null) {
        throw new NoSuchEntryException("No entry with id " + objectCreated);
      }
      AdvancedCreationEntry toReturn = (AdvancedCreationEntry)alist.getFirst();
      return toReturn;
    }
    throw new NoSuchEntryException("No entry with id " + objectCreated);
  }
  
  public final CreationEntry[] getAdvancedEntries()
  {
    List<CreationEntry> list = new ArrayList();
    for (Integer in : advancedEntries.keySet())
    {
      entrys = (List)advancedEntries.get(in);
      for (CreationEntry ee : entrys) {
        if (!list.contains(ee)) {
          list.addAll(entrys);
        }
      }
    }
    List<CreationEntry> entrys;
    return (CreationEntry[])list.toArray(new CreationEntry[list.size()]);
  }
  
  public final Map<Integer, List<CreationEntry>> getAdvancedEntriesMap()
  {
    return advancedEntries;
  }
  
  public final CreationEntry[] getAdvancedEntriesNotEpicMission()
  {
    Set<CreationEntry> advanced = new HashSet();
    for (List<CreationEntry> entrys : advancedEntries.values()) {
      for (CreationEntry entry : entrys) {
        if ((!entry.isOnlyCreateEpicTargetMission) && (entry.isCreateEpicTargetMission)) {
          advanced.add(entry);
        }
      }
    }
    return (CreationEntry[])advanced.toArray(new CreationEntry[advanced.size()]);
  }
  
  public final CreationEntry[] getSimpleEntries()
  {
    List<CreationEntry> list = new ArrayList();
    for (Integer in : simpleEntries.keySet())
    {
      entrys = (List)simpleEntries.get(in);
      for (CreationEntry ee : entrys) {
        if (!list.contains(ee)) {
          list.addAll(entrys);
        }
      }
    }
    List<CreationEntry> entrys;
    return (CreationEntry[])list.toArray(new CreationEntry[list.size()]);
  }
  
  public CreationEntry getCreationEntry(int objectCreated)
  {
    CreationEntry toReturn = null;
    try
    {
      toReturn = getAdvancedCreationEntry(objectCreated);
    }
    catch (NoSuchEntryException nse)
    {
      Integer space = Integer.valueOf(objectCreated);
      List<CreationEntry> entrys = (List)simpleEntries.get(space);
      if (entrys == null) {
        return toReturn;
      }
      Iterator localIterator = entrys.iterator();
      if (localIterator.hasNext()) {
        return (CreationEntry)localIterator.next();
      }
    }
    return toReturn;
  }
  
  public CreationEntry getCreationEntry(int objectSource, int objectTarget, int objectCreated)
    throws NoSuchEntryException
  {
    CreationEntry toReturn = null;
    Integer space = Integer.valueOf(objectTarget);
    List<CreationEntry> entrys = (List)matrix.get(space);
    if (entrys != null) {
      for (CreationEntry entry : entrys) {
        if (((entry.getObjectSource() == objectSource) && (entry.getObjectTarget() == objectTarget)) || (
          (entry.getObjectSource() == objectTarget) && (entry.getObjectTarget() == objectSource) && 
          (entry.getObjectCreated() == objectCreated))) {
          toReturn = entry;
        }
      }
    }
    if (toReturn == null)
    {
      entrys = (List)matrix.get(Integer.valueOf(objectSource));
      if (entrys != null) {
        for (CreationEntry entry : entrys) {
          if (((entry.getObjectSource() == objectSource) && (entry.getObjectTarget() == objectTarget)) || (
            (entry.getObjectSource() == objectTarget) && (entry.getObjectTarget() == objectSource) && 
            (entry.getObjectCreated() == objectCreated))) {
            toReturn = entry;
          }
        }
      }
    }
    if (toReturn == null) {
      throw new NoSuchEntryException("No creation entry found for objectSource=" + objectSource + ", objectTarget=" + objectTarget + ", objectCreated=" + objectCreated);
    }
    return toReturn;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\items\CreationMatrix.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */