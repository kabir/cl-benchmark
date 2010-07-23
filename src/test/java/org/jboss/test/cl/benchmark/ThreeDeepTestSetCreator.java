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
public class ThreeDeepTestSetCreator extends AbstractTestSetCreator
{
   public ThreeDeepTestSetCreator() throws Exception
   {
      super();
   }

   @Override
   protected void createClassesAndJars() throws Exception
   {
      createInterfaces();
      createAbstractImplClasses();
      createImplClasses();
   }
   
   @Override
   protected void createClassPathElementInfos() throws Exception
   {
      if (jarsDir.exists())
      for (int jar = 0 ; jar < NUMBER_JARS ; jar++)
      {
         List<String> interfacePackages = new ArrayList<String>();
         List<String> interfaces = new ArrayList<String>();
         List<String> abstractImplPackages = new ArrayList<String>();
         List<String> abstractImplClasses = new ArrayList<String>();
         List<String> implPackages = new ArrayList<String>();
         List<String> implClasses = new ArrayList<String>();
         
         for (int pkg = 0 ; pkg < PACKAGES_PER_JAR ; pkg++)
         {
            interfacePackages.add(getInterfacePkg(jar, pkg));
            abstractImplPackages.add(getAbstractImplPkg(jar, pkg));
            implPackages.add(getImplPkg(jar, pkg));
            for (int clazz = 0 ; clazz < CLASSES_PER_PACKAGE ; clazz++)
            {
               interfaces.add(getInterfaceName(jar, pkg, clazz));
               abstractImplClasses.add(getAbstractImplName(jar, pkg, clazz));
               implClasses.add(getImplName(jar, pkg, clazz));
            }
         }
         ClassPathElementInfo iface = createClassPathElementInfo(getInterfaceJarName(jar), interfacePackages, interfaces, false);
         addClassPathElement(iface);
         
         ClassPathElementInfo abstractImpl = createClassPathElementInfo(getAbstractImplJarName(jar), abstractImplPackages, abstractImplClasses, false, iface);
         addClassPathElement(abstractImpl);
         
         ClassPathElementInfo impl = createClassPathElementInfo(getImplJarName(jar), implPackages, implClasses, true, abstractImpl);
         addClassPathElement(impl);
      }
   }
   
   private void createInterfaces() throws Exception
   {
      for (int jar = 0 ; jar < NUMBER_JARS ; jar++)
      {
         for (int pkg = 0 ; pkg < PACKAGES_PER_JAR ; pkg++)
         {
            for (int clazz = 0 ; clazz < CLASSES_PER_PACKAGE ; clazz++)
            {
               createInterface(getInterfaceName(jar, pkg, clazz));
            }
         }
         createJar(getInterfaceJarName(jar), classesDir);
      }
   }
   
   private void createAbstractImplClasses() throws Exception
   {
      for (int jar = 0 ; jar < NUMBER_JARS ; jar++)
      {
         for (int pkg = 0 ; pkg < PACKAGES_PER_JAR ; pkg++)
         {
            for (int clazz = 0 ; clazz < CLASSES_PER_PACKAGE ; clazz++)
            {
               createClass(getAbstractImplName(jar, pkg, clazz), Object.class.getName(), getInterfaceName(jar, pkg, clazz));
            }
         }
         createJar(getAbstractImplJarName(jar), classesDir);
      }
   }
   
   private void createImplClasses() throws Exception
   {
      for (int jar = 0 ; jar < NUMBER_JARS ; jar++)
      {
         for (int pkg = 0 ; pkg < PACKAGES_PER_JAR ; pkg++)
         {
            for (int clazz = 0 ; clazz < CLASSES_PER_PACKAGE ; clazz++)
            {
               createClass(getImplName(jar, pkg, clazz), getAbstractImplName(jar, pkg, clazz));
            }
         }
         createJar(getImplJarName(jar), classesDir);
      }
   }
   
   private String getInterfaceJarName(int jar)
   {
      return "interface" + jar + ".jar";
   }
   
   private String getInterfacePkg(int jar, int pkg)
   {
      return "org.jboss.test.interface" + jar + ".pkg" + pkg;
   }
   
   private String getInterfaceName(int jar, int pkg, int clazz)
   {
      return getInterfacePkg(jar, pkg) + ".Interface" + clazz;
   }

   private String getAbstractImplJarName(int jar)
   {
      return "abstractimpl" + jar + ".jar";
   }
   
   private String getAbstractImplPkg(int jar, int pkg)
   {
      return "org.jboss.test.abstract" + jar + ".pkg" + pkg;
   }
   
   private String getAbstractImplName(int jar, int pkg, int clazz)
   {
      return getAbstractImplPkg(jar, pkg) + ".AbstractImpl" + clazz;
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
