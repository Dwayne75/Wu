package com.wurmonline.server.creatures;

import com.wurmonline.server.skills.Skills;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class CreatureTemplateFactory
  implements CreatureTemplateIds
{
  private static CreatureTemplateFactory instance;
  private static Map<Integer, CreatureTemplate> templates = new HashMap();
  private static Map<String, CreatureTemplate> templatesByName = new HashMap();
  
  public static CreatureTemplateFactory getInstance()
  {
    if (instance == null) {
      instance = new CreatureTemplateFactory();
    }
    return instance;
  }
  
  public static final boolean isNameOkay(String aName)
  {
    String lName = aName.toLowerCase();
    if (lName.startsWith("wurm")) {
      return false;
    }
    for (CreatureTemplate template : templates.values()) {
      if (template.getName().toLowerCase().equals(lName)) {
        return false;
      }
    }
    return true;
  }
  
  public final CreatureTemplate getTemplate(int id)
    throws NoSuchCreatureTemplateException
  {
    CreatureTemplate toReturn = (CreatureTemplate)templates.get(Integer.valueOf(id));
    if (toReturn == null) {
      throw new NoSuchCreatureTemplateException("No Creature template with id " + id);
    }
    return toReturn;
  }
  
  public CreatureTemplate getTemplate(String name)
    throws Exception
  {
    CreatureTemplate toReturn = (CreatureTemplate)templatesByName.get(name.toLowerCase());
    if (toReturn == null) {
      throw new WurmServerException("No Creature template with name " + name);
    }
    return toReturn;
  }
  
  public CreatureTemplate[] getTemplates()
  {
    CreatureTemplate[] toReturn = new CreatureTemplate[templates.size()];
    return (CreatureTemplate[])templates.values().toArray(toReturn);
  }
  
  public CreatureTemplate createCreatureTemplate(int id, String name, String plural, String longDesc, String modelName, int[] types, byte bodyType, Skills skills, short vision, byte sex, short centimetersHigh, short centimetersLong, short centimetersWide, String deathSndMale, String deathSndFemale, String hitSndMale, String hitSndFemale, float naturalArmour, float handDam, float kickDam, float biteDam, float headDam, float breathDam, float speed, int moveRate, int[] itemsButchered, int maxHuntDist, int aggress, byte meatMaterial)
    throws IOException
  {
    CreatureTemplate toReturn = new DbCreatureTemplate(id, name, plural, longDesc, modelName, types, bodyType, skills, vision, sex, centimetersHigh, centimetersLong, centimetersWide, deathSndMale, deathSndFemale, hitSndMale, hitSndFemale, naturalArmour, handDam, kickDam, biteDam, headDam, breathDam, speed, moveRate, itemsButchered, maxHuntDist, aggress, meatMaterial);
    
    templates.put(Integer.valueOf(id), toReturn);
    templatesByName.put(name.toLowerCase(), toReturn);
    return toReturn;
  }
  
  public String getModelNameOrNull(String templateName)
  {
    CreatureTemplate t = (CreatureTemplate)templatesByName.get(templateName);
    if (t == null) {
      return null;
    }
    return t.getModelName();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\creatures\CreatureTemplateFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */