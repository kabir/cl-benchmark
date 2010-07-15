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

import java.util.Iterator;
import java.util.List;

import org.jboss.classloader.plugins.system.DefaultClassLoaderSystem;
import org.jboss.classloader.spi.ClassLoaderDomain;
import org.jboss.classloader.spi.ClassLoaderSystem;
import org.jboss.classloader.spi.ParentPolicy;
import org.jboss.test.AbstractTestCaseWithSetup;
import org.jboss.test.AbstractTestDelegate;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractClassLoaderBenchmark extends AbstractTestCaseWithSetup
{
   protected static final int NUM_CLASSES_TO_LOAD = 5;
   
   public AbstractClassLoaderBenchmark(String name)
   {
      super(name);
   }

   public static AbstractTestDelegate getDelegate(Class<?> clazz) throws Exception
   {
      return new ClassLoaderBenchmarkTestDelegate(clazz);
   }
   
   protected ClassLoaderBenchmarkTestDelegate getBenchmarkTestDelegate()
   {
      return (ClassLoaderBenchmarkTestDelegate)getDelegate();
   }
   
   public static ClassLoaderSystem getClassLoaderSystem()
   {
      DefaultClassLoaderSystem system = new DefaultClassLoaderSystem();
      ClassLoaderDomain defaultDomain = system.getDefaultDomain();
      defaultDomain.setParentPolicy(ParentPolicy.BEFORE_BUT_JAVA_ONLY);
      return system;
   }

   private void trim(List<ClassLoaderInfo> infos)
   {
      for (Iterator<ClassLoaderInfo> it = infos.iterator() ; it.hasNext() ; )
      {
         if (it.next().getClassNames().size() == 0)
            it.remove();
      }
   }
   
   protected void runBenchmark(BenchmarkScenario scenario) throws Exception
   {
      List<ClassLoaderInfo> classLoaderInfos = scenario.createFactories(getBenchmarkTestDelegate().getClassPathElements());
      
      System.out.println("Starting run. " + classLoaderInfos.size() + " jars indexed, will attempt to load " + NUM_CLASSES_TO_LOAD + " class from each.");
      trim (classLoaderInfos);
      System.out.println("Trimmed the empty jars, " + classLoaderInfos.size() + " to deploy.");
      
      LoadingResult result = new LoadingResult();
      
      long start = System.currentTimeMillis();
      for (ClassLoaderInfo info : classLoaderInfos) {
         info.initialize(result, getBenchmarkTestDelegate().install(info.getFactory()), NUM_CLASSES_TO_LOAD);
      }
      long time = System.currentTimeMillis() - start;
      System.out.println("Creating " + classLoaderInfos.size() + " class loaders took." + time + "ms");
      
      System.out.println("Load classes...");
      
      start = System.currentTimeMillis();
      for (ClassLoaderInfo info : classLoaderInfos)
      {
         loadClasses(info, info.getOwnClassesToLoad());
         loadClasses(info, info.getOtherClassesToLoad());
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
   }
   
   private void loadClasses(ClassLoaderInfo info, String[] names)
   {
      for (int i = 0 ; i < names.length ; i++)
      {
         info.loadClass(names[i]);
      }
   }
}
