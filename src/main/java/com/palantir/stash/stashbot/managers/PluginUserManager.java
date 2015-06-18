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

import org.slf4j.Logger;

import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.Permission;
import com.atlassian.stash.user.PermissionAdminService;
import com.atlassian.stash.user.SetPermissionRequest;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserAdminService;
import com.atlassian.stash.user.UserService;
import com.palantir.stash.stashbot.logger.PluginLoggerFactory;
import com.palantir.stash.stashbot.persistence.JenkinsServerConfiguration;

public class PluginUserManager {

    private final String STASH_EMAIL = "nobody@example.com";
    private final int KEY_SIZE = 2048;

    private final UserAdminService uas;
    private final UserService us;
    private final PermissionAdminService pas;
    private final Logger log;

    public PluginUserManager(UserAdminService uas, PermissionAdminService pas, UserService us, PluginLoggerFactory plf) {
        this.uas = uas;
        this.pas = pas;
        this.us = us;
        this.log = plf.getLoggerForThis(this);
    }

    public void createStashbotUser(JenkinsServerConfiguration jsc) {
        StashUser user = us.getUserByName(jsc.getStashUsername());
        if (user != null) {
            return;
        }

        // username not found, create it
        uas.createUser(jsc.getStashUsername(), jsc.getStashPassword(), jsc.getStashUsername(), STASH_EMAIL);
        user = us.getUserByName(jsc.getStashUsername());
        if (user == null) {
            throw new RuntimeException("Unable to create user " + jsc.getUsername());
        }
    }

    // TODO: Need to figure this out
    public void addKeyToRepoForReading(String pubKey, Repository repo) {
        log.error("TODO: SSH Deploy Keys have not yet been implemented");
        log.error("Please manually add this key to the repo " + repo.getSlug());
        log.error("\n" + pubKey);
    }

    public void addUserToRepoForReading(String username, Repository repo) {
        StashUser user = us.getUserByName(username);
        Permission repoRead = Permission.REPO_READ;
        SetPermissionRequest spr =
            new SetPermissionRequest.Builder().repositoryPermission(repoRead, repo).user(user).build();
        pas.setPermission(spr);
    }
}
