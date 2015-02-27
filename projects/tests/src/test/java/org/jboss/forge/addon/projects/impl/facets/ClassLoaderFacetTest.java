/**
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.projects.impl.facets;

import java.net.URLClassLoader;
import java.util.Arrays;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.ClassLoaderFacet;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
@RunWith(Arquillian.class)
public class ClassLoaderFacetTest
{
   @Deployment
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.addon:parser-java"),
            @AddonDeployment(name = "org.jboss.forge.addon:resources"),
            @AddonDeployment(name = "org.jboss.forge.addon:maven"),
            @AddonDeployment(name = "org.jboss.forge.addon:projects")
   })
   public static AddonArchive getDeployment()
   {
      AddonArchive archive = ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:parser-java")
               );

      return archive;
   }

   @Inject
   private ProjectFactory projectFactory;

   @Test
   public void testNewClassReflection() throws Exception
   {
      Project project = projectFactory.createTempProject(Arrays
               .<Class<? extends ProjectFacet>> asList(JavaSourceFacet.class));
      JavaSourceFacet facet = project.getFacet(JavaSourceFacet.class);
      Assert.assertNotNull(facet);
      JavaClassSource javaClass = Roaster.create(JavaClassSource.class).setName("Foo").setPackage("com.example");
      javaClass.addMethod("public static String hello() {return \"Hello\";}");
      facet.saveJavaSource(javaClass);
      Assert.assertTrue(project.hasFacet(ClassLoaderFacet.class));
      ClassLoaderFacet classLoaderFacet = project.getFacet(ClassLoaderFacet.class);
      try (URLClassLoader classLoader = classLoaderFacet.getClassLoader())
      {
         Assert.assertNotNull(classLoader);
         Class<?> clazz = classLoader.loadClass(javaClass.getQualifiedName());
         Assert.assertSame(classLoader, clazz.getClassLoader());
         Assert.assertEquals("Hello", clazz.getMethod("hello").invoke(null));
      }
   }

}
