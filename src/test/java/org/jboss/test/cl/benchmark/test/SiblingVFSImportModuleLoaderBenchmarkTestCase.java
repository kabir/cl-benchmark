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

import org.jboss.classloading.plugins.metadata.ModuleRequirement;
import org.jboss.classloading.plugins.metadata.PackageRequirement;
import org.jboss.classloading.spi.metadata.ClassLoadingMetaDataFactory;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.test.cl.benchmark.BenchmarkScenario;
import org.jboss.test.cl.benchmark.ClassPathElementInfo;
import org.jboss.test.cl.benchmark.VFSClassLoaderInfo;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class SiblingVFSImportModuleLoaderBenchmarkTestCase extends AbstractSiblingVFSClassLoaderBenchmark
{
   public SiblingVFSImportModuleLoaderBenchmarkTestCase(String name)
   {
      super(name);
   }
   
   public void testLoadClassesFromOtherLoader() throws Exception
   {
      runBenchmark(new BenchmarkScenario<VFSClassLoaderInfo>()
      {
         
         public List<VFSClassLoaderInfo> createFactories(List<ClassPathElementInfo> infos)
         {
            List<VFSClassLoaderInfo> deploymentInfos = new ArrayList<VFSClassLoaderInfo>();
            
            for (int i = 0 ; i < infos.size() ; i++)
            {
               ClassPathElementInfo info = infos.get(i);
               
               VFSClassLoaderFactory factory = new VFSClassLoaderFactory(info.getName());
               ClassLoadingMetaDataFactory metaData = ClassLoadingMetaDataFactory.getInstance();
               for (String pkg : info.getPackageNames())
                  factory.getCapabilities().addCapability(metaData.createPackage(pkg));
               factory.getCapabilities().addCapability(metaData.createModule(info.getName()));
               factory.getRoots().add(info.getUrl().toString());
               
               String[] otherClasses = info.getClassNames();
               ClassPathElementInfo other = i > 0 ? infos.get(i - 1) : null;
               if (other != null)
               {
                  factory.getRequirements().addRequirement(new ModuleRequirement(other.getName()));
                  otherClasses = other.getClassNames();
               }
               deploymentInfos.add(createClassLoaderInfo(info, factory, mergeArrays(info.getClassNames(), otherClasses)));
            }      
            
            return deploymentInfos;
         }
      });
   }

}
