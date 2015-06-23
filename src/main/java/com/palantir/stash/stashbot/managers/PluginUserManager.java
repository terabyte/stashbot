// Copyright 2014 Palantir Technologies
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.palantir.stash.stashbot.managers;

import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;

import com.atlassian.stash.exception.AuthorisationException;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.ssh.api.SshKeyService;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionAdminService;
import com.atlassian.stash.user.SetPermissionRequest;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserAdminService;
import com.atlassian.stash.user.UserService;
import com.palantir.stash.stashbot.config.ConfigurationPersistenceService;
import com.palantir.stash.stashbot.logger.PluginLoggerFactory;
import com.palantir.stash.stashbot.persistence.JenkinsServerConfiguration;

public class PluginUserManager {

    private final String STASH_EMAIL = "nobody@example.com";

    private final UserAdminService uas;
    private final UserService us;
    private final PermissionAdminService pas;
    private final SshKeyService sks;
    private final ConfigurationPersistenceService cps;
    private final Logger log;

    public PluginUserManager(UserAdminService uas, PermissionAdminService pas, UserService us, SshKeyService sks,
        ConfigurationPersistenceService cps, PluginLoggerFactory plf) {
        this.uas = uas;
        this.pas = pas;
        this.us = us;
        this.sks = sks;
        this.cps = cps;
        this.log = plf.getLoggerForThis(this);
    }

    public void createStashbotUser(JenkinsServerConfiguration jsc) {
        StashUser user = us.getUserByName(jsc.getStashUsername());
        if (user == null) {
            // username not found, create it
            uas.createUser(jsc.getStashUsername(), jsc.getStashPassword(), jsc.getStashUsername(), STASH_EMAIL);
            user = us.getUserByName(jsc.getStashUsername());
            if (user == null) {
                throw new RuntimeException("Unable to create user " + jsc.getUsername());
            }
        }
        // add SSH key to user
        // have to do it as admin - but this is only available to system admins, so it should "just work" - otherwise throws AuthorisationException
        // fail silently, because what can you do?
        try {
            sks.addForUser(user, cps.getDefaultPublicSshKey());
        } catch (AuthorisationException e) {
            log.error("Unable to add ssh key - code not running as admin?", e);
        } catch (ConstraintViolationException e) {
            log.error("Unable to add ssh key - not a valid key?", e);
        }
    }

    public void addUserToRepoForReading(String username, Repository repo) {
        StashUser user = us.getUserByName(username);
        Permission repoRead = Permission.REPO_READ;
        SetPermissionRequest spr =
            new SetPermissionRequest.Builder().repositoryPermission(repoRead, repo).user(user).build();
        pas.setPermission(spr);
    }
}
