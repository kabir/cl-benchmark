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
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ClassPathElementInfo
{
   private final String name;
   private final URL url;
   private final String[] packageNames;
   private final String[] classNames;
   private final boolean loadClasses;
   private final List<ClassPathElementInfo> importedJars = new ArrayList<ClassPathElementInfo>();

   ClassPathElementInfo(String name, URL url, List<String> packageNames, List<String> classNames, boolean loadClasses)
   {
      this.name = name;
      this.packageNames = packageNames.toArray(new String[packageNames.size()]);
      this.classNames = classNames.toArray(new String[classNames.size()]);
      this.url = url;
      this.loadClasses = loadClasses;
   }
   
   public boolean isLoadClasses()
   {
      return loadClasses;
   }

   public String[] getPackageNames()
   {
      return packageNames;
   }

   public String[] getClassNames()
   {
      return classNames;
   }
   
   public String getName()
   {
      return name;
   }
   
   public URL getUrl()
   {
      return url;
   }
   
   void addImportedJar(ClassPathElementInfo imported)
   {
      importedJars.add(imported);
   }
   
   public List<ClassPathElementInfo> getImportedJars()
   {
      return importedJars;
   }

}
