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

import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.util.Assert;

import com.offbytwo.jenkins.JenkinsServer;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.repository.Repository;
import com.palantir.stash.stashbot.managers.JenkinsClientManager;
import com.palantir.stash.stashbot.persistence.JenkinsServerConfiguration;
import com.palantir.stash.stashbot.persistence.RepositoryConfiguration;

public class JenkinsClientManagerTest {

    private static final String JENKINS_URL = "http://www.example.com:8080/jenkins";
    private static final String JENKINS_USERNAME = "jenkins_user";
    private static final String JENKINS_PW = "jenkins_pw";
    private static final String PREFIX_TMPL = "";
    private static final Integer REPO_ID = 5678;
    @Mock
    private RepositoryConfiguration rc;
    @Mock
    private JenkinsServerConfiguration jsc;
    @Mock
    private Project proj;
    @Mock
    private Repository repo;

    private JenkinsClientManager jcm;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);

        Mockito.when(jsc.getUrl()).thenReturn(JENKINS_URL);
        Mockito.when(jsc.getUsername()).thenReturn(JENKINS_USERNAME);
        Mockito.when(jsc.getPassword()).thenReturn(JENKINS_PW);
        Mockito.when(jsc.getUrlForRepo(repo)).thenReturn(JENKINS_URL + PREFIX_TMPL);
        Mockito.when(repo.getId()).thenReturn(REPO_ID);
        Mockito.when(repo.getSlug()).thenReturn("slug");
        Mockito.when(repo.getProject()).thenReturn(proj);
        Mockito.when(repo.getName()).thenReturn("repoName");
        Mockito.when(proj.getKey()).thenReturn("projectKey");
        jcm = new JenkinsClientManager();
    }

    @Test
    public void testJCM() throws URISyntaxException {
        JenkinsServer js = jcm.getJenkinsServer(jsc, rc, repo);
        Assert.notNull(js);
    }
}
