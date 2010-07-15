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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ClassPathElementInfo
{
   //Exclude
   private static final String[] EXCLUDED_JARS = new String[]{"-client", 
      "aspects", 
      "jboss-metadata-ejb", 
      "jboss-integration", 
      "ejb3", 
      "jboss-as-connector", 
      ".osgi.",
      "-osgi-",
      "pax-web-extender",
      "pax-swissbox",
      "-jmx-adaptor-plugin",
      "-jboss.jar",
      "jasper",
      "-jboss-web-service.jar",
      "jboss-web-deployer.jar",
      "-srp.jar",
      "wstx-asl",
      "hibernate-3.2",
      "quartz",
      "slf4j",
      "stax-ex-1.2"};
   
   private final String name;
   private final URL url;
   private final List<String> packageNames;
   private final List<String> classNames;

   private ClassPathElementInfo(String name, URL url, List<String> packageNames, List<String> classNames)
   {
      this.name = name;
      this.packageNames = packageNames;
      this.classNames = classNames;
      this.url = url;
   }
   
   public static ClassPathElementInfo of(String name) throws Exception
   {
      if (!isJar(name))
         throw new IllegalArgumentException("Not a jar " + name);
      
      File file = new File(name);
      if (!file.exists())
         throw new IllegalArgumentException("Could not find " + file);
      
      if (isExcluded(name))
         return null;
      
      
      List<String> packages = new ArrayList<String>();
      List<String> classes = new ArrayList<String>();
      try
      {
         JarFile jar = new JarFile(file);
         for (Enumeration<JarEntry> e = jar.entries() ; e.hasMoreElements() ; )
         {
            JarEntry entry = e.nextElement();
            
            //TODO - replace '/' with '.'?
            if (entry.getName().endsWith("/"))
               packages.add(entry.getName().replace('/', '.'));
            else if (entry.getName().endsWith(".class"))
               classes.add(entry.getName().substring(0, entry.getName().indexOf(".class")).replace('/', '.'));
         }
         
         return new ClassPathElementInfo(name, new File(name).toURI().toURL(), packages, classes);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public List<String> getPackageNames()
   {
      return packageNames;
   }

   public List<String> getClassNames()
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

   static boolean isJar(String name)
   {
      return name.endsWith(".jar");
   }
   
   private static boolean isExcluded(String name)
   {
      for (String excluded : EXCLUDED_JARS)
      {
         if (name.contains(excluded))
            return true;
      }
      return false;
   }
   
}
