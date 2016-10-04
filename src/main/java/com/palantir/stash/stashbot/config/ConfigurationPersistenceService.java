// Copyright 2015 Palantir Technologies
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
package com.palantir.stash.stashbot.config;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.activeobjects.tx.Transactional;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.Repository;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.palantir.stash.stashbot.jobtemplate.JobType;
import com.palantir.stash.stashbot.persistence.JenkinsServerConfiguration;
import com.palantir.stash.stashbot.persistence.JenkinsServerConfiguration.AuthenticationMode;
import com.palantir.stash.stashbot.persistence.PullRequestMetadata;
import com.palantir.stash.stashbot.persistence.RepositoryConfiguration;

@Transactional
public interface ConfigurationPersistenceService {

    public abstract void deleteJenkinsServerConfiguration(String name);

    public abstract JenkinsServerConfiguration getJenkinsServerConfiguration(String name)
        throws SQLException;

    public abstract void setJenkinsServerConfigurationFromRequest(HttpServletRequest req) throws SQLException,
        NumberFormatException;

    /**
     * @deprecated Use
     *             {@link ConfigurationPersistenceImpl#setJenkinsServerConfiguration(String, String, String, String, AuthenticationMode, String, String, Integer)}
     *             instead
     */
    @Deprecated
    public abstract void setJenkinsServerConfiguration(String name, String url,
        String username, String password, String stashUsername, String stashPassword, Integer maxVerifyChain)
        throws SQLException;

    public abstract void setJenkinsServerConfiguration(String name, String url,
        String username, String password, AuthenticationMode authenticationMode, String stashUsername,
        String stashPassword, Integer maxVerifyChain, Boolean isLocked, Boolean foldersEnabled,
        Boolean subfoldersEnabled, String folderPrefix)
        throws SQLException;

    public abstract RepositoryConfiguration getRepositoryConfigurationForRepository(
        Repository repo) throws SQLException;

    public abstract void setRepositoryConfigurationForRepository(Repository repo,
        boolean isCiEnabled, String verifyBranchRegex,
        String verifyBuildCommand, String publishBranchRegex,
        String publishBuildCommand, String prebuildCommand, boolean rebuildOnUpdate)
        throws SQLException, IllegalArgumentException;

    public abstract void setRepositoryConfigurationForRepositoryFromRequest(Repository repo, HttpServletRequest req)
        throws SQLException, NumberFormatException;

    public abstract void
        setRepositoryConfigurationForRepository(Repository repo,
            boolean isCiEnabled, String verifyBranchRegex,
            String verifyBuildCommand, boolean isVerifyPinned,
            String verifyLabel, String publishBranchRegex,
            String publishBuildCommand, boolean isPublishPinned, String publishLabel, String prebuildCommand,
            String jenkinsServerName, boolean rebuildOnUpdate, boolean isJunitEnabled, String junitPath,
            boolean artifactsEnabled, String artifactsPath, Integer maxVerifyChain, EmailSettings emailSettings,
            boolean strictVerifyMode, Boolean preserveJenkinsJobConfig, boolean timestampJobOutputEnabled,
            boolean ansiColorJobOutputEnabled, BuildTimeoutSettings buildTimeoutSettings,
            SlackSettings slackSettings)
            throws SQLException, IllegalArgumentException;

    public abstract ImmutableCollection<JenkinsServerConfiguration> getAllJenkinsServerConfigurations()
        throws SQLException;

    public abstract ImmutableCollection<String> getAllJenkinsServerNames()
        throws SQLException;

    public abstract void validateName(String name) throws IllegalArgumentException;

    public abstract void validateNameExists(String name) throws IllegalArgumentException;

    public abstract PullRequestMetadata getPullRequestMetadata(PullRequest pr);

    public abstract PullRequestMetadata getPullRequestMetadata(int repoId, Long prId, String fromSha, String toSha);

    public abstract ImmutableList<PullRequestMetadata> getPullRequestMetadataWithoutToRef(PullRequest pr);

    // Automatically sets the fromHash and toHash from the PullRequest object
    public abstract void setPullRequestMetadata(PullRequest pr, Boolean buildStarted,
        Boolean success, Boolean override);

    // Allows fromHash and toHash to be set by the caller, in case we are referring to older commits
    public abstract void setPullRequestMetadata(PullRequest pr, String fromHash, String toHash, Boolean buildStarted,
        Boolean success, Boolean override);

    // Allows fromHash and toHash to be set by the caller, in case we are referring to older commits
    public abstract void setPullRequestMetadata(PullRequest pr, String fromHash, String toHash, Boolean buildStarted,
        Boolean success, Boolean override, Boolean failed);

    public abstract Boolean getJobTypeStatusMapping(RepositoryConfiguration rc, JobType jt);

    public abstract void setJobTypeStatusMapping(RepositoryConfiguration rc, JobType jt, Boolean isEnabled);

    public abstract String getDefaultPublicSshKey();

    public abstract String getDefaultPrivateSshKey();

    public static class EmailSettings {

        private final Boolean emailNotificationsEnabled;
        private final String emailRecipients;
        private final Boolean emailForEveryUnstableBuild;
        private final Boolean emailSendToIndividuals;
        private final Boolean emailPerModuleEmail;

        public EmailSettings() {
            this(false, "", false, false, false);
        }

        public EmailSettings(Boolean emailNotificationsEnabled, String emailRecipients,
            Boolean emailForEveryUnstableBuild, Boolean emailSendToIndividuals, Boolean emailPerModuleEmail) {
            this.emailNotificationsEnabled = emailNotificationsEnabled;
            this.emailRecipients = emailRecipients;
            this.emailForEveryUnstableBuild = emailForEveryUnstableBuild;
            this.emailSendToIndividuals = emailSendToIndividuals;
            this.emailPerModuleEmail = emailPerModuleEmail;
        }

        public Boolean getEmailNotificationsEnabled() {
            return emailNotificationsEnabled;
        }

        public String getEmailRecipients() {
            return emailRecipients;
        }

        public Boolean getEmailForEveryUnstableBuild() {
            return emailForEveryUnstableBuild;
        }

        public Boolean getEmailSendToIndividuals() {
            return emailSendToIndividuals;
        }

        public Boolean getEmailPerModuleEmail() {
            return emailPerModuleEmail;
        }
    }

    public static class BuildTimeoutSettings {

        private final Boolean buildTimeoutEnabled;
        private final Integer buildTimeout;

        public BuildTimeoutSettings() {
            this(false, null);
        }

        public BuildTimeoutSettings(Boolean buildTimeoutEnabled, Integer buildTimeout) {
            this.buildTimeoutEnabled = buildTimeoutEnabled;
            this.buildTimeout = buildTimeout;
        }

        public Boolean getBuildTimeoutEnabled() {
            return buildTimeoutEnabled;
        }

        public Integer getBuildTimeout() {
            return buildTimeout;
        }
    }

    public static class SlackSettings {

        private final Boolean slackEnabled;
        private final String slackTeamDomain;
        private final String slackAuthToken;
        private final String slackBuildServerUrl;
        private final String slackRoom;
        private final String slackCommitInfoChoice;
        private final String slackCustomMessage;
        private final Boolean slackStartNotification;
        private final Boolean slackNotifySuccess;
        private final Boolean slackNotifyAborted;
        private final Boolean slackNotifyNotBuilt;
        private final Boolean slackNotifyUnstable;
        private final Boolean slackNotifyFailure;
        private final Boolean slackNotifyBackToNormal;
        private final Boolean slackNotifyRepeatedFailure;
        private final Boolean slackIncludeTestSummary;
        private final Boolean slackIncludeCustomMessage;

        public SlackSettings() {
            this(false,
                 "", "", "", "", "", "",
                 false, false, false, false, false, false, false, false, false, false);
        }

        public SlackSettings(
                        Boolean slackEnabled,
                        String slackTeamDomain,
                        String slackAuthToken,
                        String slackBuildServerUrl,
                        String slackRoom,
                        String slackCommitInfoChoice,
                        String slackCustomMessage,
                        Boolean slackStartNotification,
                        Boolean slackNotifySuccess,
                        Boolean slackNotifyAborted,
                        Boolean slackNotifyNotBuilt,
                        Boolean slackNotifyUnstable,
                        Boolean slackNotifyFailure,
                        Boolean slackNotifyBackToNormal,
                        Boolean slackNotifyRepeatedFailure,
                        Boolean slackIncludeTestSummary,
                        Boolean slackIncludeCustomMessage
                        ) {
            this.slackEnabled = slackEnabled;
            this.slackTeamDomain = slackTeamDomain;
            this.slackAuthToken = slackAuthToken;
            this.slackBuildServerUrl = slackBuildServerUrl;
            this.slackRoom = slackRoom;
            this.slackCommitInfoChoice = slackCommitInfoChoice;
            this.slackCustomMessage = slackCustomMessage;
            this.slackStartNotification = slackStartNotification;
            this.slackNotifySuccess = slackNotifySuccess;
            this.slackNotifyAborted = slackNotifyAborted;
            this.slackNotifyNotBuilt = slackNotifyNotBuilt;
            this.slackNotifyUnstable = slackNotifyUnstable;
            this.slackNotifyFailure = slackNotifyFailure;
            this.slackNotifyBackToNormal = slackNotifyBackToNormal;
            this.slackNotifyRepeatedFailure = slackNotifyRepeatedFailure;
            this.slackIncludeTestSummary = slackIncludeTestSummary;
            this.slackIncludeCustomMessage = slackIncludeCustomMessage;
        }

        public Boolean getSlackEnabled() {
            return slackEnabled;
        }

        public String getSlackTeamDomain() {
            return slackTeamDomain;
        }

        public String getSlackAuthToken() {
            return slackAuthToken;
        }

        public String getSlackBuildServerUrl() {
            return slackBuildServerUrl;
        }

        public String getSlackRoom() {
            return slackRoom;
        }

        public String getSlackCommitInfoChoice() {
            return slackCommitInfoChoice;
        }

        public String getSlackCustomMessage() {
            return slackCustomMessage;
        }

        public Boolean getSlackStartNotification() {
            return slackStartNotification;
        }

        public Boolean getSlackNotifySuccess() {
            return slackNotifySuccess;
        }

        public Boolean getSlackNotifyAborted() {
            return slackNotifyAborted;
        }

        public Boolean getSlackNotifyNotBuilt() {
            return slackNotifyNotBuilt;
        }

        public Boolean getSlackNotifyUnstable() {
            return slackNotifyUnstable;
        }

        public Boolean getSlackNotifyFailure() {
            return slackNotifyFailure;
        }

        public Boolean getSlackNotifyBackToNormal() {
            return slackNotifyBackToNormal;
        }

        public Boolean getSlackNotifyRepeatedFailure() {
            return slackNotifyRepeatedFailure;
        }

        public Boolean getSlackIncludeTestSummary() {
            return slackIncludeTestSummary;
        }

        public Boolean getSlackIncludeCustomMessage() {
            return slackIncludeCustomMessage;
        }

    }

}
