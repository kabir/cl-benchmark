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
import java.util.List;

import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.test.kernel.junit.MicrocontainerTestDelegate;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractClassLoaderBenchmarkTestDelegate<T extends ClassLoaderInfo> extends MicrocontainerTestDelegate
{
   AbstractTestSetCreator creator;
   
   public AbstractClassLoaderBenchmarkTestDelegate(Class<?> clazz, AbstractTestSetCreator creator) throws Exception
   {
      super(clazz);
      this.creator = creator;
   }
   
   @Override
   public void setUp() throws Exception
   {
      super.setUp();
      
      URL commonUrl = getClass().getResource("/org/jboss/test/cl/benchmark/Common.xml");
      if (commonUrl == null)
         throw new IllegalStateException("Null common url");
      deploy(commonUrl);      
      creator.createClassesAndJars();
   }
   
   List<ClassPathElementInfo> getClassPathElements()
   {
      return creator.getClassPathElements();
   }
   
   protected String getContextName(VFSClassLoaderFactory factory)
   {
      String contextName = factory.getContextName();
      if (contextName == null)
         contextName = factory.getName() + ":" + factory.getVersion();
      return contextName;
   }
   
   abstract ClassLoader createLoader(T classLoaderInfo) throws Exception;
}
