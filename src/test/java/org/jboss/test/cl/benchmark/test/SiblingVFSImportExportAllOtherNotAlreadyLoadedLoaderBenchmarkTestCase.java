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

import org.jboss.classloading.spi.metadata.ExportAll;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.test.cl.benchmark.BenchmarkScenario;
import org.jboss.test.cl.benchmark.ClassPathElementInfo;
import org.jboss.test.cl.benchmark.VFSClassLoaderInfo;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class SiblingVFSImportExportAllOtherNotAlreadyLoadedLoaderBenchmarkTestCase extends AbstractSiblingVFSClassLoaderBenchmark
{
   public SiblingVFSImportExportAllOtherNotAlreadyLoadedLoaderBenchmarkTestCase(String name)
   {
      super(name);
   }

   public void testOtherLoaderNotAlreadyLoaded() throws Exception
   {
      runBenchmark(new BenchmarkScenario<VFSClassLoaderInfo>()
      {
         
         public List<VFSClassLoaderInfo> createFactories(List<ClassPathElementInfo> infos)
         {
            List<VFSClassLoaderInfo> deploymentInfos = new ArrayList<VFSClassLoaderInfo>();
            
            for (int i = 0 ; i < infos.size() ; i++)
            {
               ClassPathElementInfo info = infos.get(i);
               ClassPathElementInfo other = i < infos.size() - 1 ? infos.get(i+1) : info;
                  
               
               VFSClassLoaderFactory factory = new VFSClassLoaderFactory(info.getName());
               factory.setExportAll(ExportAll.NON_EMPTY);
               factory.setImportAll(true);
               factory.getRoots().add(info.getUrl().toString());
               
//               deploymentInfos.add(createClassLoaderInfo(info, factory, mergeArrays(info.getClassNames(), other.getClassNames())));
               String[] classNames = info == other ? new String[0] : other.getClassNames(); 
               deploymentInfos.add(createClassLoaderInfo(info, factory, mergeArrays(new String[] {info.getClassNames()[0]}, classNames)));

            }      
            
            return deploymentInfos;
         }
      });
   }

}
