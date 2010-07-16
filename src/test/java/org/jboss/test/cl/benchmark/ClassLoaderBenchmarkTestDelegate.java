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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.resource.spi.IllegalStateException;

import junit.framework.AssertionFailedError;

import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;
import org.jboss.test.kernel.junit.MicrocontainerTestDelegate;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ClassLoaderBenchmarkTestDelegate extends MicrocontainerTestDelegate
{
   private List<ClassPathElementInfo> classPathElements = new ArrayList<ClassPathElementInfo>();
   
   public ClassLoaderBenchmarkTestDelegate(Class<?> clazz) throws Exception
   {
      super(clazz);
   }
   
   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      
      String classpath = System.getProperty("java.class.path");
      String[] jars = classpath.split(File.pathSeparator);
      
      createClassPathElements(jars);
      
      URL commonUrl = getClass().getResource("/org/jboss/test/cl/benchmark/Common.xml");
      if (commonUrl == null)
         throw new IllegalStateException("Null common url");
      deploy(commonUrl);      
   }
   
   private void createClassPathElements(String[] jars) throws IOException
   {
      Set<String> doneAlready = new HashSet<String>();
      int skippedPackages = 0;
      int skippedClasses = 0;
      int packages = 0;
      int classes = 0;
      int noclasses = 0;
      for (String jar : jars)
      {
         if (isJar(jar))
         {
            File file = new File(jar);
            if (!file.exists())
               throw new IllegalArgumentException("Could not find " + file);

            List<String> classNames = new ArrayList<String>();
            List<String> packageNames = new ArrayList<String>();
            JarFile jarFile = new JarFile(file);
            for (Enumeration<JarEntry> e = jarFile.entries() ; e.hasMoreElements() ; )
            {
               JarEntry entry = e.nextElement();
               
               //TODO - replace '/' with '.'?
               if (entry.getName().endsWith("/")) 
               {
                  if (doneAlready.contains(entry.getName()))
                  {
                     skippedPackages++;
                     continue;
                  }
                  packageNames.add(entry.getName().replace('/', '.'));
               }
               else if (entry.getName().endsWith(".class"))
               {
                  if (doneAlready.contains(entry.getName()))
                  {
                     skippedClasses++;
                     continue;
                  }
                  classNames.add(entry.getName().substring(0, entry.getName().indexOf(".class")).replace('/', '.'));
               }

               doneAlready.add(entry.getName());
            }
            
            
            if (classNames.size() > 0)
            {
               classPathElements.add(new ClassPathElementInfo(jar, new File(jar).toURI().toURL(), packageNames, classNames));
               packages += packageNames.size();
               classes += classNames.size();
            }
            else
               noclasses++;
         }
      }
      System.out.println(classPathElements.size() + " jars indexed. Classes: " + classes + ". Packages: " + packages + 
            ". Excluded: (Empty jars: " + noclasses + ". Duplicate packages: " + skippedPackages + ". Duplicate classes: " + skippedClasses + ")");
   }
   

   List<ClassPathElementInfo> getClassPathElements()
   {
      return Collections.unmodifiableList(classPathElements);
   }
   
   ClassLoader install(VFSClassLoaderFactory factory) throws Exception
   {
      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName(factory.getName() + ":" + factory.getVersion());
      deployment.setBeanFactories(Collections.singletonList((BeanMetaDataFactory) factory));
      deploy(deployment);
      
      Object object = getBean(getContextName(factory), ControllerState.INSTALLED);
      if (object instanceof ClassLoader == false)
         throw new AssertionFailedError(object + " is not a classloader");
      
      return (ClassLoader)object;
   }

   protected String getContextName(VFSClassLoaderFactory factory)
   {
      String contextName = factory.getContextName();
      if (contextName == null)
         contextName = factory.getName() + ":" + factory.getVersion();
      return contextName;
   }

   static boolean isJar(String name)
   {
      return name.endsWith(".jar");
   }
}
