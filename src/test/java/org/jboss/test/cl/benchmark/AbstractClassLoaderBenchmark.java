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
import java.util.Iterator;
import java.util.List;

import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.test.AbstractTestCaseWithSetup;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractClassLoaderBenchmark extends AbstractTestCaseWithSetup
{
   public AbstractClassLoaderBenchmark(String name)
   {
      super(name);
   }

   protected AbstractClassLoaderBenchmarkTestDelegate getBenchmarkTestDelegate()
   {
      return (AbstractClassLoaderBenchmarkTestDelegate)getDelegate();
   }
   
   public static ClassLoaderSystem getClassLoaderSystem()
   {
      DefaultClassLoaderSystem system = new DefaultClassLoaderSystem();
      ClassLoaderDomain defaultDomain = system.getDefaultDomain();
      defaultDomain.setParentPolicy(ParentPolicy.BEFORE_BUT_JAVA_ONLY);
      return system;
   }

   private void trimLoadersWithNoClasses(List<ClassLoaderInfo> infos)
   {
      for (Iterator<ClassLoaderInfo> it = infos.iterator() ; it.hasNext() ; )
      {
         String[] classes = it.next().getClassesToLoad();
         if (classes == null || classes.length == 0)
            it.remove();
      }
   }
   
   private List<ClassLoaderInfo> getLoadersForLoading(List<ClassLoaderInfo> infos)
   {
      int classes = 0;
      List<ClassLoaderInfo> result = new ArrayList<ClassLoaderInfo>();
      for (Iterator<ClassLoaderInfo> it = infos.iterator() ; it.hasNext() ; )
      {
         ClassLoaderInfo current = it.next();
         if (current.isLoadClasses())
         {
            result.add(current);
            classes += current.getClassesToLoad().length;
         }
      }
      System.out.println("Will load a total of " + classes + " classes from " + result.size() + " loaders.");
      return result;
   }
   
   protected void runBenchmark(BenchmarkScenario scenario) throws Exception
   {
      List<ClassLoaderInfo> classLoaderInfos = scenario.createFactories(getBenchmarkTestDelegate().getClassPathElements());
      
      System.out.println("Starting run. " + classLoaderInfos.size() + " jars indexed");
      trimLoadersWithNoClasses(classLoaderInfos);
      System.out.println("Trimmed the empty jars, " + classLoaderInfos.size() + " to deploy.");
      
      LoadingResult result = new LoadingResult();
      
      long start = System.currentTimeMillis();
      for (ClassLoaderInfo info : classLoaderInfos) {
         info.initialize(result, getBenchmarkTestDelegate().install(info.getFactory()));
      }
      long time = System.currentTimeMillis() - start;
      System.out.println("Creating " + classLoaderInfos.size() + " class loaders took." + time + "ms");
      
      List<ClassLoaderInfo> infosToLoad = getLoadersForLoading(classLoaderInfos);
      
      System.out.println("Load classes...");
      
      start = System.currentTimeMillis();
      for (ClassLoaderInfo info : infosToLoad)
      {
         loadClasses(info, info.getClassesToLoad());
      }

      time = System.currentTimeMillis() - start;
      System.out.println("Loading classes  took." + time + "ms");
      System.out.println("\n");
      System.out.println("================================");
      System.out.println("Stats:");
      System.out.println("================================");
      System.out.println("Successful classes:    " + result.getSuccess());
      System.out.println("Failed classes:        " + result.getFailed());
      System.out.println("Wrong loader (filter): " + result.getBadFilter());
      
      if (result.getFailed() > result.getSuccess())
         fail("A lot of failures!");
   }
   
   
   private void loadClasses(ClassLoaderInfo info, String[] names)
   {
      for (int i = 0 ; i < names.length ; i++)
      {
         info.loadClass(names[i]);
      }
   }
   
   protected ClassLoaderInfo createClassLoaderInfo(ClassPathElementInfo elementInfo, VFSClassLoaderFactory factory, String...classesToLoad)
   {
      ClassLoaderInfo info = new ClassLoaderInfo(elementInfo, factory);
      info.addClassesToLoad(classesToLoad);
      
      return info;
   }
   
}
