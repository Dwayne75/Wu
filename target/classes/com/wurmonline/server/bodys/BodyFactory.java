package com.wurmonline.server.bodys;

import com.wurmonline.server.creatures.Creature;
import com.wurmonline.shared.exceptions.WurmServerException;
import java.util.HashMap;
import java.util.Map;

public final class BodyFactory
{
  private static final Map<Byte, BodyTemplate> bodyTemplates = new HashMap();
  
  static
  {
    bodyTemplates.put(Byte.valueOf((byte)0), new BodyHuman());
    bodyTemplates.put(Byte.valueOf((byte)3), new BodyDog());
    bodyTemplates.put(Byte.valueOf((byte)1), new BodyHorse());
    bodyTemplates.put(Byte.valueOf((byte)4), new BodyEttin());
    bodyTemplates.put(Byte.valueOf((byte)5), new BodyCyclops());
    bodyTemplates.put(Byte.valueOf((byte)2), new BodyBear());
    bodyTemplates.put(Byte.valueOf((byte)6), new BodyDragon());
    bodyTemplates.put(Byte.valueOf((byte)7), new BodyBird());
    bodyTemplates.put(Byte.valueOf((byte)8), new BodySpider());
    bodyTemplates.put(Byte.valueOf((byte)9), new BodySnake());
  }
  
  public static Body getBody(Creature creature, byte typ, short centimetersHigh, short centimetersLong, short centimetersWide)
    throws Exception
  {
    BodyTemplate template = (BodyTemplate)bodyTemplates.get(Byte.valueOf(typ));
    if (template != null) {
      return new Body(template, creature, centimetersHigh, centimetersLong, centimetersWide);
    }
    throw new WurmServerException("No such bodytype: " + Byte.toString(typ));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\server\bodys\BodyFactory.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */