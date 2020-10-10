package com.wurmonline.server.questions;

import com.wurmonline.server.creatures.Communicator;
import com.wurmonline.server.creatures.Creature;
import java.util.Properties;

public final class WurmInfo2
  extends Question
{
  public WurmInfo2(Creature aResponder)
  {
    super(aResponder, "Cooking changelog", "Change log v1", 15, -10L);
  }
  
  public void answer(Properties answers) {}
  
  public void sendQuestion()
  {
    StringBuilder buf = new StringBuilder();
    buf.append(getBmlHeader());
    
    buf.append("label{text=\"\"}");
    buf.append("label{type=\"bold\";text=\"General\"}");
    buf.append("label{text=\" * I've keep some of the ways that HFC was used for skill increases. But no junk food, \"}");
    buf.append("label{text=\"   e.g. nails are not used as an ingredient and if present in a container, they will stop the \"}");
    buf.append("label{text=\"   food item being made.\"}");
    buf.append("label{text=\" * Meats now have a material, e.g. Meat, Dragon and Meat, Game\"}");
    buf.append("label{text=\"    o As you can see it does not use the animal type, but a category as we have so \"}");
    buf.append("label{text=\"      many animal types. Some of the categories are: (there are 16 total)\"}");
    buf.append("label{text=\"       . Dragon\"}");
    buf.append("label{text=\"       . Game\"}");
    buf.append("label{text=\"       . Human\"}");
    buf.append("label{text=\"       . Humanoid\"}");
    buf.append("label{text=\"       . Snake\"}");
    buf.append("label{text=\" * Meat and fillets can now be put in FSB and Crates.\"}");
    buf.append("label{text=\" * Fish and fish fillets can now be put in FSB and Crates.\"}");
    buf.append("label{text=\" * Existing Zombified milk will lose its zombie status. New zombie milk should be fine.\"}");
    buf.append("label{text=\" * You will be able to seal some containers so long as they only have one liquid in them, \"}");
    buf.append("label{text=\"   this will stop their decay.\"}");
    buf.append("label{text=\"    o Small and Large Amphoria.\"}");
    buf.append("label{text=\"    o Pottery Jar.\"}");
    buf.append("label{text=\"    o Pottery Flask.\"}");
    buf.append("label{text=\"    o Water Skin.\"}");
    buf.append("label{text=\"    o Small Barrel.\"}");
    buf.append("harray{label{text=\" * \"};label{type=\"bold\";text=\"Bees\"};label{text=\" have been added.\"}}");
    
    buf.append("harray{label{text=\" * Cooking will now be from \"};label{type=\"bold\";text=\"recipes\"};label{text=\", this does not mean that you cannot continue \"}}");
    
    buf.append("label{text=\"   cooking like you used to though, although some recipes will have changed.\"}");
    buf.append("harray{label{text=\" * A personal \"};label{type=\"bold\";text=\"cookbook\"};label{text=\" is now available which has the recipes that you know about in.\"}}");
    
    buf.append("label{text=\" * Hens eggs can now be found when foraging on grass tiles. And you will be able to put\"}");
    buf.append("label{text=\"   in FSB, but that makes them infertile.\"}");
    buf.append("label{text=\" * Old containers and tools - now with more use.\"}");
    buf.append("label{text=\"    o Sauce pan - had to change size of this, but that means some recipes need a \"}");
    buf.append("label{text=\"      new one which is larger.\"}");
    buf.append("label{text=\"    o Pottery bowl - can now be used to hold liquids as well, a lot of recipes use this.\"}");
    buf.append("label{text=\"    o Hand - used to make some mixes, and other things. Note if a recipe says a \"}");
    buf.append("label{text=\"      hand must be used, actually any active item will work.\"}");
    buf.append("label{text=\"    o Fork - how else were you going to mix some stuff!\"}");
    buf.append("label{text=\"    o Knife - used a lot in food preparation.\"}");
    buf.append("label{text=\"    o Spoon - An alternative way to mix things (so same ingredients can be used in \"}");
    buf.append("label{text=\"      multiple recipes. Also can be used to scoop.\"}");
    buf.append("label{text=\"    o Press - can be used to squash something.\"}");
    buf.append("label{text=\"    o Branch - branching out, could be used as a spit ...\"}");
    buf.append("label{text=\" * New containers and tools\"}");
    buf.append("label{text=\"    o Stoneware. - used to make things like breads, biscuits etc\"}");
    buf.append("label{text=\"    o Cake tin - used to make cakes\"}");
    buf.append("label{text=\"    o Pie dish - used to make pies and tarts\"}");
    buf.append("label{text=\"    o Roasting dish - used to roast food.\"}");
    buf.append("label{text=\"    o Plate - used to make salads and sandwiches on.\"}");
    buf.append("label{text=\"    o Mortar+Pestle - used to grind small things (e.g. spices)\"}");
    buf.append("label{text=\"    o Measuring Jug - used to get a specific amount of liquid, its volume can be \"}");
    buf.append("label{text=\"      adjusted (volume is same as weight for this).\"}");
    buf.append("label{text=\"    o Still - used for distilling.\"}");
    buf.append("label{text=\" * New crops\"}");
    buf.append("label{text=\"    o Carrots\"}");
    buf.append("label{text=\"    o Cabbage\"}");
    buf.append("label{text=\"    o Tomatos\"}");
    buf.append("label{text=\"    o Sugar Beet\"}");
    buf.append("label{text=\"    o Lettuce\"}");
    buf.append("label{text=\"    o Peas\"}");
    buf.append("label{text=\"    o Cucumbers\"}");
    buf.append("label{text=\" * New Bush\"}");
    buf.append("label{text=\"    o Hazelnut bush - now you know where the hazelnuts come from.\"}");
    buf.append("label{text=\" * New Tree\"}");
    buf.append("label{text=\"    o Orange tree - because it seemed like a good idea.\"}");
    buf.append("harray{label{text=\" * Spices - all can be planted in a \"};label{type=\"bold\";text=\"planter\"}label{text=\", except Nutmeg.\"}}");
    
    buf.append("label{text=\"    o Cumin\"}");
    buf.append("label{text=\"    o Ginger\"}");
    buf.append("label{text=\"    o Paprika\"}");
    buf.append("label{text=\"    o Turmeric\"}");
    buf.append("harray{label{text=\" * New Herbs - all Herbs can be planted in a \"};label{type=\"bold\";text=\"planter\"};}");
    
    buf.append("label{text=\"    o Fennel\"}");
    buf.append("label{text=\"    o Mint\"}");
    buf.append("label{text=\" * New items that are only found by forage / botanize. Note all above spices and herbs \"}");
    buf.append("label{text=\"   and the new vegs can be found this way as well)\"}");
    buf.append("label{text=\"    o Cocoa bean\"}");
    buf.append("label{text=\"    o Nutmeg (note this is a spice but cannot be planted in a planter)\"}");
    buf.append("label{text=\"    o Raspberries\"}");
    buf.append("label{text=\"    o Hazelnut sprout.\"}");
    buf.append("label{text=\"    o Orange sprout.\"}");
    buf.append("label{text=\" * Rocksalt\"}");
    buf.append("label{text=\"    o Rock tiles that would of produced salt when mining will now be shown as \"}");
    buf.append("label{text=\"      Rocksalt veins (this may take a day or two to show), but have a limited life (e.g. \"}");
    buf.append("label{text=\"      you get 45-50 rocksalt from one).\"}");
    buf.append("label{text=\"    o The Rocksalt can then be ground into salt using a grindstone. You can get\"}");
    buf.append("label{text=\"      more than one salt from each Rocksalt bepending on your milling skill.\"}");
    buf.append("label{text=\"    o Veins that had salt in, will be unaffected, e.g.you will still be able to get the \"}");
    buf.append("label{text=\"      random salt when mining them.\"}");
    buf.append("label{text=\" * Trellis\"}");
    buf.append("label{text=\"    o A new trellis has been added for hops.\"}");
    buf.append("label{text=\"    o Trellis can now be harvested when their produce is in season (except ivy ones \"}");
    buf.append("label{text=\"      don't have a season).\"}");
    buf.append("label{text=\"    o To help plant your trellis in nice straight lines, you can plant them using a wall, \"}");
    buf.append("label{text=\"      fence or tile border. And have three options, on left, center and on right.\"}");
    buf.append("label{text=\"    o There is a limit of 4 planted trellis per tile. Any extras that are currently planted \"}");
    buf.append("label{text=\"      on same tile will become unplanted.\"}");
    buf.append("label{text=\" * Flowers\"}");
    buf.append("label{text=\"    o Flowers can now be used in some recipes, and therefore will now only go into \"}");
    buf.append("label{text=\"      a food storage bin. This also applies to rose petals, oleander, lavender and \"}");
    buf.append("label{text=\"      camellia.\"}");
    buf.append("label{text=\"    o Any existing flowers in bulk storage bins are fine, you can still take them out,\"}");
    buf.append("label{text=\"      but will not be able to put them back into the bulk storage bin, but they will go \"}");
    buf.append("label{text=\"      into the food storage bin.\"}");
    buf.append("label{text=\" * The goodness of food\"}");
    buf.append("label{text=\"    o Each meal made will have a bonus attached to it, so the same ingredients \"}");
    buf.append("label{text=\"      making the same meal (in same cooker and same container) will end up with \"}");
    buf.append("label{text=\"      this same bonus.\"}");
    buf.append("label{text=\"       . This bonus will give a timed affinity to a skill, but can be different per \"}");
    buf.append("label{text=\"         player, e.g. fish and chips may give a temp weaponsmithing affinity to \"}");
    buf.append("label{text=\"         Joe, but to Tom it gives carpentry, (also may not give it to any skill).\"}");
    buf.append("label{text=\"    o Nutrition has not been changed.\"}");
    
    buf.append("label{type=\"bold\";text=\"Bees\"}");
    buf.append("label{text=\" * Wild bee hives will appear in spring at random locations and they will vanish at the end \"}");
    buf.append("label{text=\"   of the year (in winter). Note they will be in different locations each year.\"}");
    buf.append("label{text=\" * As time passes honey will be made in hives together with bees wax, the amount will\"}");
    buf.append("label{text=\"   depend on nearby flowers, fields and trees.\"}");
    buf.append("label{text=\" * Each wild hive will start with one queen bee, this may increase by one every wurm \"}");
    buf.append("label{text=\"   month, to a maximum of two, so long as the hive has over a certain amount of honey in \"}");
    buf.append("label{text=\"   it. When there is two queen bees if there is a domestic hive nearby it may migrate to it.\"}");
    buf.append("label{text=\" * Domestic hives will be loadable. Even with a queen in it. So you can move it to \"}");
    buf.append("label{text=\"   somewhere, e.g. your own deed. Watch out bees sting!\"}");
    buf.append("label{text=\" * Domestic hives that had a queen in it, will go dormant over the winter period and will \"}");
    buf.append("label{text=\"   become active again in spring. But it is possible for the queen to die over winter if no \"}");
    buf.append("label{text=\"   honey is left in the hive (Note can put sugar in hive to keep the queen alive.\"}");
    buf.append("label{text=\" * Honey ( and beeswax) will be collectable from hives.. But you may need a bee \"}");
    buf.append("label{text=\"   smoker.. So bees do not sting you, note that this bee smoker is useful for other times, \"}");
    buf.append("label{text=\"   like if you want to chop down a tree that has a hive, or load/unload a domestic hive \"}");
    buf.append("label{text=\"   which has a queen in it.\"}");
    
    buf.append("label{type=\"bold\";text=\"Recipes\"}");
    buf.append("label{text=\" * As well as being able to examine a food container to see what it will make, you can \"}");
    buf.append("harray{label{text=\"   also use \"};label{type=\"bold\";text=\"LORE\"};label{text=\", to get hints on what ingredient you could add into the container to be \"}}");
    
    buf.append("label{text=\"   able to make something.\"}");
    buf.append("label{text=\" * Some more specialised recipes will call for a meat of a specific category, or a specific \"}");
    buf.append("label{text=\"   fish, but most will use any meat or any fish or even any veg.\"}");
    buf.append("label{text=\" * Most new recipes only require one of each item, main exception is making sandwiches \"}");
    buf.append("label{text=\"   which normally requires 2 slices of bread.\"}");
    buf.append("label{text=\" * Some recipes are an intermediate step, or some sauce which is used later e.g. there is \"}");
    buf.append("label{text=\"   cake mix and white sauce.\"}");
    buf.append("label{text=\" * Lots of new food category types e.g.\"}");
    buf.append("label{text=\"    o Curry\"}");
    buf.append("label{text=\"    o Pizza\"}");
    buf.append("label{text=\"    o Cookies\"}");
    buf.append("label{text=\"    o Pie\"}");
    buf.append("label{text=\"    o Tarts\"}");
    buf.append("label{text=\"    o Biscuits\"}");
    buf.append("label{text=\"    o Scones\"}");
    buf.append("label{text=\"    o Salads\"}");
    buf.append("label{text=\" * And some of your old favorites like.\"}");
    buf.append("label{text=\"    o Cakes\"}");
    buf.append("label{text=\"    o Sandwiches\"}");
    buf.append("label{text=\"    o Tea\"}");
    buf.append("label{text=\"    o Wine\"}");
    buf.append("label{text=\"    o Meal\"}");
    buf.append("label{text=\" * And there are some new drinks which will need distilling.\"}");
    buf.append("label{text=\" * Note you will need to experiment to find their recipes, but do note some items need a \"}");
    buf.append("label{text=\"   mix before adding other items, e.g. you will now need a cake mix to make cakes.\"}");
    buf.append("label{text=\" * Some ingredients will only be found doing forage/botanize actions, whilst others, once \"}");
    buf.append("harray{label{text=\"   found, they may be able to be planted as a \"};label{type=\"bold\";text=\"crop\"};label{text=\" or even in a \"};label{type=\"bold\";text=\"planter\"};label{text=\" (e.g. most spices \"}}");
    
    buf.append("label{text=\"   and herbs can be planted in a planter).\"}");
    buf.append("label{text=\"    o Fresh is an attribute of an item when just found from foraging or picking, if you \"}");
    buf.append("label{text=\"      put it in a FSB it looses that attribute.\"}");
    buf.append("label{text=\" * Some recipes are nameable this means that whoever is the first to make the item for \"}");
    buf.append("label{text=\"   that recipe, will have their name added to the front of that recipe name, e.g. if Pifa was \"}");
    buf.append("label{text=\"   first to make a meat curry (assuming that was nameable) then it would show to \"}");
    buf.append("label{text=\"   everyone, when they discover it,  as ''Pifa's meat curry''.\"}");
    buf.append("label{text=\" * Note only one recipe can be named per person.\"}");
    buf.append("label{text=\" * Some recipes may only be makeable once you have that recipe in your cookbook,\"}");
    buf.append("label{text=\"   these recipes are only available from killing certain creatures.\"}");
    buf.append("label{text=\" * Recipes can be inscribed onto papryus (or paper), to do this you need to be looking at\"}");
    buf.append("label{text=\"   the recipe in your cookbook, and then use the reed pen on a blank papryus (or paper).\"}");
    buf.append("label{text=\"    o You can then mail these or trade them to others, where they can add it to their\"}");
    buf.append("label{text=\"      cookbook, if they don't know it, by either reading the recipe and selecting to add\"}");
    buf.append("label{text=\"      to their cookbook or activate it and r-click on the cookbook menu option.\"}");
    
    buf.append("label{type=\"bold\";text=\"Planter\"}");
    buf.append("label{text=\" * Items can be planted in a planter, e.g. a herb or a spice (not all spices).\"}");
    buf.append("label{text=\" * The planted item will start growing.\"}");
    buf.append("label{text=\" * After a while it will be available to be harvested.\"}");
    buf.append("label{text=\" * Harvesting will be available daily,\"}");
    buf.append("label{text=\" * Each time you harvest it will prolong its life\"}");
    buf.append("label{text=\" * Eventually it will get too woody to be harvested, then it is time to start afresh.\"}");
    
    buf.append("label{type=\"bold\";text=\"LORE\"}");
    buf.append("label{text=\"Using LORE on a container, will let you know what could be made, e.g.\"}");
    buf.append("label{text=\" * If the contents match a known recipe (known by you that is). You would get a message \"}");
    buf.append("label{text=\"   like:\"}");
    buf.append("label{text=\"    o 'The ingredients in the frying pan would make an omlette when cooked'.\"}");
    buf.append("label{text=\" * If the contents match an unknown recipe. Message would be like: \"}");
    buf.append("label{text=\"    o 'You think this may well work when cooked'.\"}");
    buf.append("label{text=\" * If the contents would make any recipe but has the incorrect amount of a liquid then \"}");
    buf.append("label{text=\"   you would get something like:\"}");
    buf.append("label{text=\"    o 'The ingredients in the saucepan would make tea when cooked, but...'\"}");
    buf.append("label{text=\"      'There is too much water, try between 300g and 400g.'\"}");
    buf.append("label{text=\" * Partial Matches\"}");
    buf.append("label{text=\"    o It performs checks in this order \"}");
    buf.append("label{text=\"       . Unknown recipe that is not nameable.\"}");
    buf.append("label{text=\"       . Unknown recipe that is nameable.\"}");
    buf.append("label{text=\"       . Known recipe that is not nameable.\"}");
    buf.append("label{text=\"       . Known recipe that is nameable.\"}");
    buf.append("label{text=\"    o If the contents form part of any recipe, it will give a hint as to what to add to \"}");
    buf.append("label{text=\"      make that recipe. E.g. 'have you tried adding a chopped onion?'.\"}");
    buf.append("label{text=\"       . Note the recipe is picked at random from a list of possible recipes and \"}");
    buf.append("label{text=\"         so is the shown ingredient.\"}");
    
    buf.append("label{type=\"bold\";text=\"Cookbook\"}");
    buf.append("label{text=\" * Every person has a cookbook, where your known recipes are shown.\"}");
    buf.append("label{text=\" * Some recipes are known by everyone by default, you have to find the others and \"}");
    buf.append("label{text=\"   make them for them to appear in your cookbook..\"}");
    buf.append("label{text=\" * The initial page of your cookbook allows you to select what recipes to view, i.e. \"}");
    buf.append("label{text=\"    o Target action - these are the ones where you use one item on another, e.g. \"}");
    buf.append("label{text=\"      grinding cereals to make flour.\"}");
    buf.append("label{text=\"    o Container action - these ones are when you use a tool of some kind on a \"}");
    buf.append("label{text=\"      container to change the contents of the container into a different item. E.g. \"}");
    buf.append("label{text=\"      using your hand  on a pottery bowl which containers flour, water, salt and butter \"}");
    buf.append("label{text=\"      to make pastry.\"}");
    buf.append("label{text=\"    o Heat - these ones are your basic cooking recipes, where you put ingredients \"}");
    buf.append("label{text=\"      into a food container, and put in an cooker and after the ingredients get hot the \"}");
    buf.append("label{text=\"      container items change to the result, e.g. putting maple sap into a saucepan in \"}");
    buf.append("label{text=\"      a lit oven, will result in maple syrup after sometime. Not all recipes work in all \"}");
    buf.append("label{text=\"      cookers.\"}");
    buf.append("label{text=\"    o Time - these ones are used for brewing.\"}");
    buf.append("label{text=\" * Also on the initial page you also have links to view recipes (that you know) by\"}");
    buf.append("label{text=\"    o Tool - this gives a list of the tools that you know are used for cooking, selecting \"}");
    buf.append("label{text=\"      a tool from that list will give you the known recipes that can be made from it.\"}");
    buf.append("label{text=\"    o Cooker - this will give a list of cookers, and selecting a cooker from that list will \"}");
    buf.append("label{text=\"      lead you to a list of known recipes that can be made in it\"}");
    buf.append("label{text=\"    o Container - this will give you a list of containers that can be used by known \"}");
    buf.append("label{text=\"      recipes, selecting one will then give a list of the known recipes that use that \"}");
    buf.append("label{text=\"      container.\"}");
    buf.append("label{text=\"    o Ingredients - gives a list of all your known ingredients, and again selecting one \"}");
    buf.append("label{text=\"      of them will then show a list of known recipes that use that ingredient.\"}");
    buf.append("label{text=\" * Also you can search your recipes.\"}");
    buf.append("label{text=\" * From any list of recipes, you can select one and view what you think is used to make \"}");
    buf.append("label{text=\"   that item\"}");
    buf.append("label{text=\"    o Note that there are optional ingredients, and unless you have used them for an \"}");
    buf.append("label{text=\"      ingredient, then they will not show in your version.\"}");
    buf.append("label{text=\"    o Note that some recipes may use any type of meat, or fish, or veg, or herb, or \"}");
    buf.append("label{text=\"      spice, when you attempt the same recipe with a different type, your recipe will \"}");
    buf.append("label{text=\"      be updated to show that information, e.g. if your recipe says that it uses beef \"}");
    buf.append("label{text=\"      meat, and you try with canine meat, then if it works, the recipe will update \"}");
    buf.append("label{text=\"      to show any meat. \"}");
    buf.append("label{text=\"    o Note that not all recipes can use all types.\"}");
    
    buf.append(createAnswerButton2());
    getResponder().getCommunicator().sendBml(480, 500, true, true, buf.toString(), 200, 200, 200, this.title);
  }
  
  public static String getInfo()
  {
    return "";
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\questions\WurmInfo2.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */