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
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.jboss.weld.probe.ProbeExtension;
import org.jboss.weld.probe.ProbeServlet;

/**
 * Registers weld-probe servlet and extension if weld development mode is enabled.
 *
 * @author Jozef Hartinger
 *
 */
public class ProbeDeploymentProcessor implements DeploymentUnitProcessor {

    // This context param is used to activate the development mode
    private static final String CONTEXT_PARAM_DEV_MODE = "org.jboss.weld.development";
    private static final String SERVLET_NAME = "weld-probe";
    private static final JBossServletMetaData PROBE_SERVLET;
    private static final ServletMappingMetaData PROBE_SERVLET_MAPPING;

    static {
        PROBE_SERVLET = new JBossServletMetaData();
        PROBE_SERVLET.setName(SERVLET_NAME);
        PROBE_SERVLET.setServletClass(ProbeServlet.class.getName());
        PROBE_SERVLET.setLoadOnStartup("1");

        PROBE_SERVLET_MAPPING = new ServletMappingMetaData();
        PROBE_SERVLET_MAPPING.setServletName(SERVLET_NAME);
        PROBE_SERVLET_MAPPING.setUrlPatterns(Collections.singletonList("/weld-probe/*"));
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

        // Servlet
        if (webMetaData.getServlets() == null) {
            webMetaData.setServlets(new JBossServletsMetaData());
        }
        webMetaData.getServlets().add(PROBE_SERVLET);

        // Servlet mapping
        if (webMetaData.getServletMappings() == null) {
            webMetaData.setServletMappings(new ArrayList<ServletMappingMetaData>());
        }
        webMetaData.getServletMappings().add(PROBE_SERVLET_MAPPING);

        // Injection into servlet
        final EEModuleDescription module = deploymentUnit.getAttachment(Attachments.EE_MODULE_DESCRIPTION);
        final EEApplicationClasses applicationClasses = deploymentUnit.getAttachment(Attachments.EE_APPLICATION_CLASSES_DESCRIPTION);
        registerAsComponent(ProbeServlet.class.getName(), module, deploymentUnit, applicationClasses);

        // ProbeExtension
        WeldPortableExtensions.getPortableExtensions(deploymentUnit).registerExtensionInstance(new ProbeExtension(), deploymentUnit);
    }

    @Override
    public void undeploy(DeploymentUnit unit) {
    }
}
