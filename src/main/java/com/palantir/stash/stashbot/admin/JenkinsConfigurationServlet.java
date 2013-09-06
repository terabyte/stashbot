//   Copyright 2013 Palantir Technologies
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
package com.palantir.stash.stashbot.admin;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.palantir.stash.stashbot.config.ConfigurationPersistenceManager;
import com.palantir.stash.stashbot.config.JenkinsServerConfiguration;
import com.palantir.stash.stashbot.logger.StashbotLoggerFactory;
import com.palantir.stash.stashbot.managers.JenkinsManager;
import com.palantir.stash.stashbot.managers.PluginUserManager;

public class JenkinsConfigurationServlet extends HttpServlet {

    private final String PATH_PREFIX = "/stashbot/jenkins-admin";
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final SoyTemplateRenderer soyTemplateRenderer;
    private final PageBuilderService pageBuilderService;
    private final ConfigurationPersistenceManager configurationPersistanceManager;
    private final PluginUserManager pluginUserManager;
    private final JenkinsManager jenkinsManager;
    private final Logger log;

    public JenkinsConfigurationServlet(SoyTemplateRenderer soyTemplateRenderer,
        PageBuilderService pageBuilderService,
        ConfigurationPersistenceManager configurationPersistenceManager, PluginUserManager pluginUserManager,
        JenkinsManager jenkinsManager, StashbotLoggerFactory lf) {
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.pageBuilderService = pageBuilderService;
        this.configurationPersistanceManager = configurationPersistenceManager;
        this.pluginUserManager = pluginUserManager;
        this.jenkinsManager = jenkinsManager;
        this.log = lf.getLoggerForThis(this);
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        // Handle deletes
        String pathInfo = req.getPathInfo();
        String relUrl = req.getRequestURL().toString();
        relUrl = relUrl
            .replaceAll("/+$", "")
            .replaceAll("/delete/?.*$", "")
            .replaceAll("/reload-all/?.*$", "")
            .replaceAll("/create-new/?.*$", "");

        String[] parts = pathInfo.replaceFirst(PATH_PREFIX, "").split("/");

        if (parts.length >= 2) {
            if (parts[1].equals("delete")) {
                log.info("Deleting configuration " + parts[2]);
                configurationPersistanceManager.deleteJenkinsServerConfiguration(parts[2]);
                res.sendRedirect(relUrl);
                return;
            }
            if (parts[1].equals("reload-all")) {
                jenkinsManager.updateAllJobs();
                res.sendRedirect(relUrl);
            }
            if (parts[1].equals("create-new")) {
                jenkinsManager.createMissingJobs();
                res.sendRedirect(relUrl);
            }
        }

        res.setContentType("text/html;charset=UTF-8");
        try {
            pageBuilderService.resources().requireContext("plugin.page.stashbot");
            ImmutableCollection<JenkinsServerConfiguration> jenkinsConfigs =
                configurationPersistanceManager.getAllJenkinsServerConfigurations();
            soyTemplateRenderer.render(res.getWriter(),
                "com.palantir.stash.stashbot:stashbotConfigurationResources",
                "plugin.page.stashbot.jenkinsConfigurationPanel",
                ImmutableMap.<String, Object> builder()
                    .put("relUrl", relUrl)
                    .put("jenkinsConfigs", jenkinsConfigs)
                    .build()
                );
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            } else {
                throw new ServletException(e);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        String name = req.getParameter("name");
        String url = req.getParameter("url");
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String stashUsername = req.getParameter("stashUsername");
        String stashPassword = req.getParameter("stashPassword");
        Integer maxVerifyChain = Integer.parseInt(req.getParameter("maxVerifyChain"));

        try {
            configurationPersistanceManager.setJenkinsServerConfiguration(name, url, username, password, stashUsername,
                stashPassword, maxVerifyChain);
            pluginUserManager.createStashbotUser(configurationPersistanceManager.getJenkinsServerConfiguration(name));
        } catch (SQLException e) {
            res.sendError(500, e.getMessage());
        }
        doGet(req, res);
    }
}
