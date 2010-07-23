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

import java.util.Collections;

import junit.framework.AssertionFailedError;

import org.jboss.beans.metadata.spi.BeanMetaDataFactory;
import org.jboss.classloading.spi.vfs.metadata.VFSClassLoaderFactory;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.plugins.deployment.AbstractKernelDeployment;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractVFSCLassLoaderBenchmarkTestDelegate extends AbstractClassLoaderBenchmarkTestDelegate<VFSClassLoaderInfo>
{
   public AbstractVFSCLassLoaderBenchmarkTestDelegate(Class<?> clazz, AbstractTestSetCreator creator) throws Exception
   {
      super(clazz, creator);
   }
   
   @Override
   ClassLoader createLoader(VFSClassLoaderInfo classLoaderInfo) throws Exception
   {
      VFSClassLoaderFactory factory = classLoaderInfo.getFactory();
      AbstractKernelDeployment deployment = new AbstractKernelDeployment();
      deployment.setName(factory.getName() + ":" + factory.getVersion());
      deployment.setBeanFactories(Collections.singletonList((BeanMetaDataFactory) factory));
      deploy(deployment);
 
      Object object = getBean(getContextName(factory), ControllerState.INSTALLED);
      if (object instanceof ClassLoader == false)
         throw new AssertionFailedError(object + " is not a classloader");
 
      return (ClassLoader)object;
   }
}
