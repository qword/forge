package org.jboss.forge.addon.parser.java.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.parser.java.facets.JavaSourceFacet;
import org.jboss.forge.addon.parser.java.ui.methods.JavaGetSetMethodsCommand;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.input.UISelectMany;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDeployment;
import org.jboss.forge.arquillian.AddonDeployments;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the {@link org.jboss.forge.addon.parser.java.ui.methods.JavaGetSetMethodsCommand} using two different types of tests according to the builder flag.
 * @author <a href="mbriskar@redhat.com">Matej Briškár</a>
 */
@RunWith(Arquillian.class)
public class JavaGetSetMethodsCommandTest
{

   @Deployment
   @AddonDeployments({
            @AddonDeployment(name = "org.jboss.forge.addon:parser-java"),
            @AddonDeployment(name = "org.jboss.forge.addon:ui-test-harness"),
            @AddonDeployment(name = "org.jboss.forge.addon:projects"),
            @AddonDeployment(name = "org.jboss.forge.addon:maven"),
            @AddonDeployment(name = "org.jboss.forge.furnace.container:cdi")
   })
   public static AddonArchive getDeployment()
   {
      return ShrinkWrap
               .create(AddonArchive.class)
               .addBeansXML()
               .addAsAddonDependencies(
                        AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:projects"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:parser-java"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
                        AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
               );
   }

   @Inject
   private ProjectFactory projectFactory;

   @Inject
   private UITestHarness testHarness;

   @Inject
   private FacetFactory facetFactory;

   private Project project;

   private JavaClassSource targetClass;

   private CommandController commandController;

   @Before
   public void setup() throws Exception
   {
      createTempProject();
      
   }

   @Test
   public void testNoPropertiesDefault() throws Exception
   {
      createTargetClass("public class Test{}");
      createCommandController();
      
      commandController.initialize();
      //properties field cannot have any value
      assertFalse(commandController.isValid());
   }
   
   @Test
   public void testNoPropertiesBuilder() throws Exception
   {
      createTargetClass("public class Test{}");
      createCommandController();
      
      commandController.initialize();
      setBuilderPattern(true);
      //properties field cannot have any value
      assertFalse(commandController.isValid());
   }

   @Test
   public void testWithoutAccessorDefault() throws Exception
   {
      createTargetClass("public class Test{private String simpleString; public void setSimpleString(String simple){simpleString=simple;}}");
      createCommandController();
      commandController.initialize();
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertNotNull(targetClass.getField("simpleString"));
      assertTrue(targetClass.getField("simpleString").isPrivate());
      assertNotNull(targetClass.getMethod("setSimpleString", String.class));
      assertNotNull(targetClass.getMethod("getSimpleString"));
   }
   
   @Test
   public void testWithoutAccessorBuilder() throws Exception
   {
      createTargetClass("public class Test{private String simpleString; public Test setSimpleString(String simple){simpleString=simple;}}");
      createCommandController();
      
      commandController.initialize();
      setBuilderPattern(true);
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertNotNull(targetClass.getField("simpleString"));
      assertTrue(targetClass.getField("simpleString").isPrivate());
      assertNotNull(targetClass.getMethod("getSimpleString"));
   }

   @Test
   public void testWithoutMutatorDefault() throws Exception
   {
      createTargetClass("public class Test{private String simpleString; public String getSimpleString(){return simpleString;}}");
      createCommandController();
      
      commandController.initialize();
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertNotNull(targetClass.getField("simpleString"));
      assertTrue(targetClass.getField("simpleString").isPrivate());
      assertNotNull(targetClass.getMethod("setSimpleString", String.class));
      assertNotNull(targetClass.getMethod("getSimpleString"));
   }
   
   @Test
   public void testWithoutMutatorBuilder() throws Exception
   {
      createTargetClass("public class Test{private String simpleString; public String getSimpleString(){return simpleString;}}");
      createCommandController();
      
      commandController.initialize();
      setBuilderPattern(true);
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertNotNull(targetClass.getField("simpleString"));
      assertTrue(targetClass.getField("simpleString").isPrivate());
      assertNotNull(targetClass.getMethod("setSimpleString", String.class));
      assertNotNull(targetClass.getMethod("getSimpleString"));
   }
   
   @Test
   public void testBadMutatorDefault() throws Exception
   {
      createTargetClass("public class Test{private String simpleString; public Test setSimpleString(String simple){simpleString=simple;}}");
      createCommandController();
      
      commandController.initialize();
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertNotNull(targetClass.getField("simpleString"));
      assertTrue(targetClass.getField("simpleString").isPrivate());
      assertNotNull(targetClass.getMethod("setSimpleString", String.class));
      assertNotNull(targetClass.getMethod("getSimpleString"));
      assertEquals("void",targetClass.getMethod("setSimpleString", String.class).getReturnType().getName());
   }
   
   @Test
   public void testBadMutatorBuilder() throws Exception
   {
      createTargetClass("public class Test{private String simpleString; public void setSimpleString(String simple){simpleString=simple;}}");
      createCommandController();
      
      commandController.initialize();
      setBuilderPattern(true);
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertNotNull(targetClass.getField("simpleString"));
      assertTrue(targetClass.getField("simpleString").isPrivate());
      assertNotNull(targetClass.getMethod("setSimpleString", String.class));
      assertNotNull(targetClass.getMethod("getSimpleString"));
      assertEquals("Test",targetClass.getMethod("setSimpleString", String.class).getReturnType().getName());
   }
   
   @Test
   public void testNoAccessorMutatorDefault() throws Exception
   {
      createTargetClass("public class Test{private String simpleString;}");
      createCommandController();
      
      commandController.initialize();
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertEquals(1,targetClass.getFields().size());
      assertEquals(2,targetClass.getMethods().size());
      assertNotNull(targetClass.getMethod("setSimpleString", String.class));
      assertNotNull(targetClass.getMethod("getSimpleString"));
      assertEquals("void",targetClass.getMethod("setSimpleString", String.class).getReturnType().getName());
   }
   
   @Test
   public void testNoAccessorMutatorBuilder() throws Exception
   {
      createTargetClass("public class Test{private String simpleString;}");
      createCommandController();
      
      commandController.initialize();
      setBuilderPattern(true);
      UISelectMany<String> component = (UISelectMany<String>) commandController.getInputs().get("properties");
      Iterable<String> valueChoices = component.getValueChoices();
      commandController.setValueFor("properties", valueChoices);
      commandController.execute();
      reloadTargetClass();
      
      assertEquals(1,targetClass.getFields().size());
      assertEquals(2,targetClass.getMethods().size());
      assertNotNull(targetClass.getMethod("setSimpleString", String.class));
      assertNotNull(targetClass.getMethod("getSimpleString"));
      assertEquals("Test",targetClass.getMethod("setSimpleString", String.class).getReturnType().getName());
   }
   
   private void createTempProject()
   {
      project = projectFactory.createTempProject();
      facetFactory.install(project, JavaSourceFacet.class);
   }

   private void createTargetClass(String classString) throws FileNotFoundException
   {
      targetClass = Roaster.parse(JavaClassSource.class, classString);
      project.getFacet(JavaSourceFacet.class).saveJavaSource(targetClass);
   }

   private void createCommandController() throws Exception
   {
      commandController = testHarness.createCommandController(JavaGetSetMethodsCommand.class,
               project.getFacet(JavaSourceFacet.class).getJavaResource(targetClass));
   }

   private void reloadTargetClass() throws FileNotFoundException
   {
      targetClass = Roaster.parse(JavaClassSource.class,
               project.getFacet(JavaSourceFacet.class).getJavaResource(targetClass)
                        .getUnderlyingResourceObject());
   }

   private void setBuilderPattern(boolean builder)
   {
      commandController.setValueFor("builderPattern", builder);
   }

}