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
import java.util.List;

import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ClassLoaderInfo
{
   private static final ClassLoader SYSTEM = ClassLoader.getSystemClassLoader();

   private final ClassPathElementInfo elementInfo;
   private final VFSClassLoaderFactory factory;
   private ClassLoader loader;
   private LoadingResult result;
   private String[] classesToLoad;
   private ClassLoaderInfo loaderWhoseClassesToLoad;
   
   public ClassLoaderInfo(ClassPathElementInfo info, VFSClassLoaderFactory factory)
   {
      this.elementInfo = info;
      this.factory = factory;
   }

   public VFSClassLoaderFactory getFactory()
   {
      return factory;
   }
   
   void initialize(LoadingResult result, ClassLoader loader, int num)
   {
      this.loader = loader;
      this.result = result;
      
      if (classesToLoad != null)
         return;
      
      List<String> all = elementInfo.getClassNames();
      
      if (all.size() <= num)
         classesToLoad = all.toArray(new String[all.size()]);
      else
      {
         int index = all.size() / 2;
         classesToLoad = new String[num];
         for (int i = 0 ; i < num ; i++)
         {
            index += i * (i % 2 == 1 ? -1 : +1);
            //int j = (i * (i % 2 == 1 ? -1 : +1) + index); 
            classesToLoad[i] = all.get(index);
         }
      }
      
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

   List<String> getClassNames()
   {
      return elementInfo.getClassNames();
   }

   public String getName()
   {
      return elementInfo.getName();
   }

   public List<String> getPackageNames()
   {
      return elementInfo.getPackageNames();
   }

   public URL getUrl()
   {
      return elementInfo.getUrl();
   }
   
   public void setLoaderWhoseClassesToLoad(ClassLoaderInfo other)
   {
      loaderWhoseClassesToLoad = other;
   }

   public String[] getOwnClassesToLoad()
   {
      return classesToLoad;
   }

   public String[] getOtherClassesToLoad()
   {
      if (loaderWhoseClassesToLoad != null)
         return loaderWhoseClassesToLoad.classesToLoad;
      return classesToLoad;
   }
   
}
