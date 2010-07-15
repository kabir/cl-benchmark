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
import org.jboss.test.cl.benchmark.AbstractClassLoaderBenchmark;
import org.jboss.test.cl.benchmark.BenchmarkScenario;
import org.jboss.test.cl.benchmark.ClassPathElementInfo;
import org.jboss.test.cl.benchmark.ClassLoaderInfo;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class ImportExportAllOtherAlreadyLoadedBenchmarkTestCase extends AbstractClassLoaderBenchmark
{
   public ImportExportAllOtherAlreadyLoadedBenchmarkTestCase(String name)
   {
      super(name);
   }
   
   public void testLoadClassesFromOtherLoaderAlreadyLoader() throws Exception
   {
      runBenchmark(new BenchmarkScenario()
      {
         
         public List<ClassLoaderInfo> createFactories(List<ClassPathElementInfo> infos)
         {
            List<ClassLoaderInfo> classLoaderInfos = new ArrayList<ClassLoaderInfo>();
            
            ClassLoaderInfo last = null;
            for (ClassPathElementInfo info : infos)
            {
               VFSClassLoaderFactory factory = new VFSClassLoaderFactory(info.getName());
               factory.setExportAll(ExportAll.NON_EMPTY);
               factory.setImportAll(true);
               factory.getRoots().add(info.getUrl().toString());
               //Needed???
               //factory.setIncludedPackages()
               
               ClassLoaderInfo classLoaderInfo = new ClassLoaderInfo(info, factory);
               classLoaderInfos.add(classLoaderInfo);
               
               if (last != null)
                  classLoaderInfo.setLoaderWhoseClassesToLoad(last);
            }      
            
            
            classLoaderInfos.get(0).setLoaderWhoseClassesToLoad(classLoaderInfos.get(classLoaderInfos.size() - 1));
            return classLoaderInfos;
         }
      });
   }
}
