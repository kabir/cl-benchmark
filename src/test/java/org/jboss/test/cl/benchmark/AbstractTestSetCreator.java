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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javassist.Modifier;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ClassFileWriter;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.bytecode.ClassFileWriter.ConstPoolWriter;
import javassist.bytecode.ClassFileWriter.MethodWriter;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

/**
 * 
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public abstract class AbstractTestSetCreator
{
   private List<ClassPathElementInfo> classPathElements = new ArrayList<ClassPathElementInfo>();

   protected final File classesDir;
   
   protected final File jarsDir;
   
   AbstractTestSetCreator(String suffix) throws Exception
   {
      URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
      File file = new File(url.toURI());
      if (!file.exists())
         throw new IllegalStateException("Could not find file " + file);
      if (!file.isDirectory())
         throw new IllegalStateException(file + " is not a directory");
      
      classesDir = new File(file.getParentFile(), "generated-classes-" + suffix); 
      jarsDir = new File(file.getParentFile(), "generated-jars-" + suffix);
      
      if (!jarsDir.exists())
      {
         if (!classesDir.exists())
            classesDir.mkdir();
         jarsDir.mkdir();
         createClassesAndJars();
      }
      createClassPathElementInfos();
   }

   List<ClassPathElementInfo> getClassPathElements()
   {
      return classPathElements;
   }
   
   protected abstract void createClassesAndJars() throws Exception;
   
   protected abstract void createClassPathElementInfos() throws Exception;
   
   protected void addClassPathElement(ClassPathElementInfo info)
   {
      classPathElements.add(info);
   }

   protected void createJar(String name, File classesDir) throws IOException
   {
      JavaArchive archive = ShrinkWrap.create(name, JavaArchive.class);

      ArchivePath path = ArchivePaths.create("/");
      addDirectoryToJar(archive, path, classesDir);
      
      //System.out.println(archive.toString(true));
      
      archive.as(ZipExporter.class).exportZip(new File(jarsDir, name), true);
      
      cleanDirectory(classesDir);
   }
   
   protected ClassPathElementInfo createClassPathElementInfo(String name, List<String> packages, List<String> classNames, boolean load, ClassPathElementInfo...imports)
         throws Exception
   {
      URL url = new File(jarsDir, name).toURI().toURL();
      ClassPathElementInfo info = new ClassPathElementInfo(name, url, packages, classNames, load);
      for (ClassPathElementInfo imported : imports)
         info.addImportedJar(imported);
      return info;
   }

   protected void createInterface(String name, String...interfaces) throws Exception
   {
      createClass(true, name, Object.class.getName(), interfaces);
   }

   protected void createClass(String name, String superclass, String...interfaces) throws Exception
   {
      createClass(false, name, superclass, interfaces);
   }

   private void createClass(boolean isInterface, String name, String superclass, String...interfaces) throws Exception
   {
      name = name.replace('.', '/');
      ClassFileWriter clazz = new ClassFileWriter(ClassFile.JAVA_5, 0);   
      ConstPoolWriter cp = clazz.getConstPool();
      int thisClass = cp.addClassInfo(name);
      int superClazz = cp.addClassInfo(superclass.replace('.', '/'));
      String[] ifaces = new String[interfaces.length];
      for (int i = 0 ; i < ifaces.length ; i++)
         ifaces[i] = interfaces[i].replace('.', '/');
      int[] ifacez = cp.addClassInfo(ifaces);
      
      if (!isInterface)
      {
         //Add default constructor
         MethodWriter mw = clazz.getMethodWriter();   
         mw.begin(Modifier.PUBLIC, MethodInfo.nameInit, "()V",  null, null);
         mw.add(Opcode.ALOAD_0);
         mw.add(Opcode.INVOKESPECIAL);
         int signature = cp.addNameAndTypeInfo(MethodInfo.nameInit, "()V");
         mw.add16(cp.addMethodrefInfo(superClazz, signature));
         mw.add(Opcode.RETURN);
         mw.codeEnd(1, 1);
         mw.end(null, null);
      }
      
      int modifier = isInterface ? Modifier.PUBLIC | Modifier.INTERFACE | Modifier.ABSTRACT : Modifier.PUBLIC;
      
      byte[] bytes = clazz.end(modifier, thisClass, superClazz, ifacez, null);
      
      
      File file = classesDir;
      String[] nameElements = name.split("/");
      for (int i = 0 ; i < nameElements.length ; i++)
      {
         if (i < nameElements.length - 1)
         {
            file = new File(file, nameElements[i]);
            if (!file.exists())
               file.mkdir();
         }
         else
         {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file, nameElements[i] + ".class")));
            try
            {
               out.write(bytes);
            }
            finally
            {
               if (out != null)
               {
                  try
                  {
                     out.close();
                  }
                  catch(IOException ignore)
                  {
                  }
               }
            }
         }
      }
   }

   private static void addDirectoryToJar(JavaArchive archive, ArchivePath currentPath, File currentDir)
   {
      String[] files = currentDir.list();
      for (String fileName : files)
      {
         ArchivePath filePath = ArchivePaths.create(currentPath, fileName);
         File file = new File(currentDir, fileName);
         if (file.isDirectory())
         {
            addDirectoryToJar(archive, filePath, file);
         }
         else
         {
            archive.addResource(file, filePath);
         }
      }
   }
   
   protected static void cleanDirectory(File currentDir)
   {
      String[] files = currentDir.list();
      for (String fileName : files)
      {
         File file = new File(currentDir, fileName);
         if (file.isDirectory())
            cleanDirectory(file);

         file.delete();
      }
   }
   
}
