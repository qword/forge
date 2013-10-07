/**
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.jboss.forge.addon.projects.mock;

import java.util.Collections;

import org.jboss.forge.addon.projects.BuildSystemFacet;
import org.jboss.forge.addon.projects.ProjectFacet;
import org.jboss.forge.addon.projects.ProjectType;
import org.jboss.forge.addon.ui.wizard.UIWizardStep;

/**
 * 
 * @author <a href="ggastald@redhat.com">George Gastaldi</a>
 */
public class MockProjectType implements ProjectType
{
   @Override
   public String getType()
   {
      return "mock";
   }

   @Override
   public Class<? extends UIWizardStep> getSetupFlow()
   {
      return null;
   }

   @Override
   public Iterable<Class<? extends BuildSystemFacet>> getRequiredBuildSystemFacets()
   {
      return Collections.emptySet();
   }

   @Override
   public Iterable<Class<? extends ProjectFacet>> getRequiredFacets()
   {
      return Collections.emptyList();
   }

   @Override
   public String toString()
   {
      return getType();
   }
}
