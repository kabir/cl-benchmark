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

import java.net.URL;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ClassLoaderInfo
{
   private static final ClassLoader SYSTEM = ClassLoader.getSystemClassLoader();

   private final ClassPathElementInfo elementInfo;
   //private final VFSClassLoaderFactory factory;
   private ClassLoader loader;
   private LoadingResult result;
   private String[] classesToLoad;
   
   ClassLoaderInfo(ClassPathElementInfo info)
   {
      this.elementInfo = info;
   }

   void initialize(LoadingResult result, ClassLoader loader)
   {
      this.loader = loader;
      this.result = result;
   }
   
   void addClassesToLoad(String...classes)
   {
      if (classesToLoad == null)
         classesToLoad = classes;
      else 
      {
         String[] tmp = new String[classesToLoad.length + classes.length];
         System.arraycopy(classesToLoad, 0, tmp, 0, classesToLoad.length);
         System.arraycopy(classes, 0, tmp, 0, classes.length);
         classesToLoad = tmp;
      }
   }
   
   boolean isLoadClasses()
   {
      return elementInfo.isLoadClasses();
   }
   
   public void loadClass(String className)
   {
      try
      {
         Class<?> clazz = loader.loadClass(className);
         if (clazz.getClassLoader() == SYSTEM)
            result.incrementBadFilter();
         else
            result.incrementSuccess();
      }
      catch(ClassNotFoundException e)
      {
         result.incrementFailed();
      }
      catch(NoClassDefFoundError e)
      {
         result.incrementFailed();
      }
      
   }

   public String getName()
   {
      return elementInfo.getName();
   }

   public String[] getPackageNames()
   {
      return elementInfo.getPackageNames();
   }

   public URL getUrl()
   {
      return elementInfo.getUrl();
   }
   
   public String[] getClassesToLoad()
   {
      return classesToLoad;
   }

}
