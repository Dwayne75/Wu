package com.sun.tools.xjc.reader.xmlschema;

import com.sun.msv.grammar.Expression;
import com.sun.tools.xjc.grammar.ClassItem;
import com.sun.tools.xjc.grammar.FieldItem;
import com.sun.tools.xjc.grammar.util.Multiplicity;
import com.sun.xml.xsom.XSParticle;

class DefaultParticleBinder
  extends ParticleBinder
{
  DefaultParticleBinder(BGMBuilder builder)
  {
    super(builder);
  }
  
  public Expression build(XSParticle p, ClassItem superClass)
  {
    DefaultParticleBinder.Checker checker = new DefaultParticleBinder.Checker(this, null);
    if (superClass != null) {
      checker.readSuperClass(superClass);
    }
    checker.particle(p);
    if (checker.hasNameCollision())
    {
      FieldItem fi = new FieldItem(superClass == null ? "Content" : "Rest", this.builder.typeBuilder.build(p), p.getLocator());
      
      fi.multiplicity = Multiplicity.star;
      fi.collisionExpected = true;
      
      fi.javadoc = Messages.format("DefaultParticleBinder.FallbackJavadoc", checker.getCollisionInfo().toString());
      
      return fi;
    }
    return new DefaultParticleBinder.Builder(this, checker.markedParticles).build(p);
  }
  
  public boolean checkFallback(XSParticle p, ClassItem superClass)
  {
    DefaultParticleBinder.Checker checker = new DefaultParticleBinder.Checker(this, null);
    if (superClass != null) {
      checker.readSuperClass(superClass);
    }
    checker.particle(p);
    
    return checker.hasNameCollision();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\DefaultParticleBinder.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */