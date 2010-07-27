/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors. 
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/ 
package org.jboss.test.cl.benchmark;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class SiblingTestSetCreator extends AbstractTestSetCreator
{
   final static int NUMBER_JARS = 100;
   final static int PACKAGES_PER_JAR = 3;
   final static int CLASSES_PER_PACKAGE = 4;
   

   public SiblingTestSetCreator() throws Exception
   {
      super("sibling");
   }

   @Override
   protected void createClassesAndJars() throws Exception
   {
      for (int jar = 0 ; jar < NUMBER_JARS ; jar++)
      {
         for (int pkg = 0 ; pkg < PACKAGES_PER_JAR ; pkg++)
         {
            for (int clazz = 0 ; clazz < CLASSES_PER_PACKAGE ; clazz++)
            {
               createClass(getImplName(jar, pkg, clazz), Object.class.getName());
            }
         }
         createJar(getImplJarName(jar), classesDir);
      }
   }
   
   @Override
   protected void createClassPathElementInfos() throws Exception
   {
      if (jarsDir.exists())
      for (int jar = 0 ; jar < NUMBER_JARS ; jar++)
      {
         List<String> implPackages = new ArrayList<String>();
         List<String> implClasses = new ArrayList<String>();
         
         for (int pkg = 0 ; pkg < PACKAGES_PER_JAR ; pkg++)
         {
            implPackages.add(getImplPkg(jar, pkg));
            for (int clazz = 0 ; clazz < CLASSES_PER_PACKAGE ; clazz++)
            {
               implClasses.add(getImplName(jar, pkg, clazz));
            }
         }
         ClassPathElementInfo impl = createClassPathElementInfo(getImplJarName(jar), implPackages, implClasses, true);
         addClassPathElement(impl);
      }
   }
   
   private String getImplJarName(int jar)
   {
      return "impl" + jar + ".jar";
   }
   
   private String getImplPkg(int jar, int pkg)
   {
      return "org.jboss.test.impl" + jar + ".pkg" + pkg;
   }
   
   private String getImplName(int jar, int pkg, int clazz)
   {
      return getImplPkg(jar, pkg) + ".Impl" + clazz;
   }

}
