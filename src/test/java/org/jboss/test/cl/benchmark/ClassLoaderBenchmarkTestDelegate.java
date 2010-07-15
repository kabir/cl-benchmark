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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
      
      int packages = 0;
      int classes = 0;
      int excluded = 0;
      for (String jar : jars)
      {
         if (ClassPathElementInfo.isJar(jar))
         {
            ClassPathElementInfo info = ClassPathElementInfo.of(jar); 
            if (info != null)
            {
               classPathElements.add(info);
               packages += info.getPackageNames().size();
               classes += info.getClassNames().size();
            }
            else
               excluded++;
         }
      }
         
      System.out.println(classPathElements.size() + " jars indexed. Classes: " + classes + ". Packages: " + packages + ". Excluded: " + excluded);
      
      URL commonUrl = getClass().getResource("/org/jboss/test/cl/benchmark/Common.xml");
      if (commonUrl == null)
         throw new IllegalStateException("Null common url");
      deploy(commonUrl);      
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
}
