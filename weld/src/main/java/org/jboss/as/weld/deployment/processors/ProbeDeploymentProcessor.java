/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.weld.deployment.processors;

import static org.jboss.as.weld.util.Utils.getRootDeploymentUnit;
import static org.jboss.as.weld.util.Utils.registerAsComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.as.ee.component.Attachments;
import org.jboss.as.ee.component.EEApplicationClasses;
import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.ee.weld.WeldDeploymentMarker;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.as.weld.deployment.WeldAttachments;
import org.jboss.as.weld.deployment.WeldPortableExtensions;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.DispatcherType;
import org.jboss.metadata.web.spec.FilterMappingMetaData;
import org.jboss.metadata.web.spec.FilterMetaData;
import org.jboss.metadata.web.spec.FiltersMetaData;
import org.jboss.weld.probe.ProbeExtension;

/**
 * Registers weld-probe servlet and extension if weld development mode is enabled.
 *
 * @author Jozef Hartinger
 *
 */
public class ProbeDeploymentProcessor implements DeploymentUnitProcessor {

    // This context param is used to activate the development mode
    private static final String CONTEXT_PARAM_DEV_MODE = "org.jboss.weld.development";
    private static final String FILTER_NAME = "weld-probe-filter";
    private static final FilterMetaData PROBE_FILTER;
    private static final FilterMappingMetaData PROBE_FILTER_MAPPING;

    // TODO use ProbeFilter.class.getName() once 3.0.0.Alpha7 is released
    private static final String FILTER_CLASS_NAME = "org.jboss.weld.probe.ProbeFilter";

    static {
        PROBE_FILTER = new FilterMetaData();
        PROBE_FILTER.setName(FILTER_NAME);
        PROBE_FILTER.setFilterClass(FILTER_CLASS_NAME);

        PROBE_FILTER_MAPPING = new FilterMappingMetaData();
        PROBE_FILTER_MAPPING.setFilterName(FILTER_NAME);
        PROBE_FILTER_MAPPING.setUrlPatterns(Collections.singletonList("/*"));
        PROBE_FILTER_MAPPING.setDispatchers(Collections.singletonList(DispatcherType.REQUEST));
    }

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return; // Skip non web deployments
        }
        if (!WeldDeploymentMarker.isWeldDeployment(deploymentUnit)) {
            return; // skip non weld deployments
        }

        if (deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY) == null) {
            return;
        }
        final JBossWebMetaData webMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY).getMergedJBossWebMetaData();
        if (webMetaData == null) {
            return;
        }

        List<ParamValueMetaData> contextParams = webMetaData.getContextParams();
        if (contextParams == null) {
            return;
        }

        boolean developmentMode = false;
        for (ParamValueMetaData param : contextParams) {
            if (CONTEXT_PARAM_DEV_MODE.equals(param.getParamName())) {
                developmentMode = Boolean.valueOf(param.getParamValue());
            }
        }
        if (!developmentMode) {
            return;
        }
        deploymentUnit.putAttachment(WeldAttachments.DEVELOPMENT_MODE, true);
        getRootDeploymentUnit(deploymentUnit).putAttachment(WeldAttachments.DEVELOPMENT_MODE, true);

        // Filter
        if (webMetaData.getFilters() == null) {
            webMetaData.setFilters(new FiltersMetaData());
        }
        webMetaData.getFilters().add(PROBE_FILTER);

        // Filter mapping
        if (webMetaData.getFilterMappings() == null) {
            webMetaData.setFilterMappings(new ArrayList<FilterMappingMetaData>());
        }
        webMetaData.getFilterMappings().add(0, PROBE_FILTER_MAPPING);

        // Injection into servlet and filter
        final EEModuleDescription module = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
        final EEApplicationClasses applicationClasses = deploymentUnit.getAttachment(Attachments.EE_APPLICATION_CLASSES_DESCRIPTION);
        registerAsComponent(FILTER_CLASS_NAME, module, deploymentUnit, applicationClasses);

        // ProbeExtension
        WeldPortableExtensions.getPortableExtensions(deploymentUnit).registerExtensionInstance(new ProbeExtension(), deploymentUnit);
    }

    @Override
    public void undeploy(DeploymentUnit unit) {
    }
}
