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
package org.jboss.test.cl.benchmark.test;

import java.util.ArrayList;
import java.util.List;

import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.test.cl.benchmark.BenchmarkScenario;
import org.jboss.test.cl.benchmark.ClassLoaderInfo;
import org.jboss.test.cl.benchmark.ClassPathElementInfo;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ThreeDeepImportPackageLoaderBenchmarkTestCase extends AbstractThreeDeepClassLoaderBenchmark
{
   public ThreeDeepImportPackageLoaderBenchmarkTestCase(String name)
   {
      super(name);
   }
   
   public void testLoadClassesFromOwnLoader() throws Exception
   {
      runBenchmark(new BenchmarkScenario()
      {
         
         public List<ClassLoaderInfo> createFactories(List<ClassPathElementInfo> infos)
         {
            List<ClassLoaderInfo> deploymentInfos = new ArrayList<ClassLoaderInfo>();
            
            for (ClassPathElementInfo info : infos)
            {
               if (!info.isLoadClasses())
                  continue;

               ClassPathElementInfo parent = info.getImportedJars().get(0);
               ClassPathElementInfo grandParent = parent.getImportedJars().get(0);
               
               deploymentInfos.add(createGrandParent(grandParent));
               deploymentInfos.add(createParent(parent, grandParent));
               deploymentInfos.add(createImpl(info, parent));
            }      
            
            return deploymentInfos;
         }
      });
   }

   ClassLoaderInfo createGrandParent(ClassPathElementInfo info)
   {
      VFSClassLoaderFactory factory = new VFSClassLoaderFactory(info.getName());
      ClassLoadingMetaDataFactory metaData = ClassLoadingMetaDataFactory.getInstance();
      for (String pkg : info.getPackageNames())
         factory.getCapabilities().addCapability(metaData.createPackage(pkg));
      factory.getRoots().add(info.getUrl().toString());
      
      return createClassLoaderInfo(info, factory, info.getClassNames());
      
   }
   
   ClassLoaderInfo createParent(ClassPathElementInfo info, ClassPathElementInfo grandParent)
   {
      VFSClassLoaderFactory factory = new VFSClassLoaderFactory(info.getName());
      ClassLoadingMetaDataFactory metaData = ClassLoadingMetaDataFactory.getInstance();
      for (String pkg : info.getPackageNames())
         factory.getCapabilities().addCapability(metaData.createPackage(pkg));
      for (String pkg : grandParent.getPackageNames())
         factory.getRequirements().addRequirement(metaData.createReExportPackage(pkg));
      factory.getRoots().add(info.getUrl().toString());
      
      return createClassLoaderInfo(info, factory, info.getClassNames());
   }
   
   ClassLoaderInfo createImpl(ClassPathElementInfo info, ClassPathElementInfo parent)
   {
      VFSClassLoaderFactory factory = new VFSClassLoaderFactory(info.getName());
      ClassLoadingMetaDataFactory metaData = ClassLoadingMetaDataFactory.getInstance();
      for (String pkg : parent.getPackageNames())
         factory.getRequirements().addRequirement(metaData.createReExportPackage(pkg));
      factory.getRoots().add(info.getUrl().toString());
      
      return createClassLoaderInfo(info, factory, info.getClassNames());
   }
}
