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
package com.palantir.stash.stashbot.util;

import com.atlassian.bitbucket.comment.CommentService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.atlassian.bitbucket.util.Operation;
import com.atlassian.bitbucket.comment.AddCommentRequest.Builder;

public class PullRequestCommentAddOperation implements Operation<Void, Exception> {

    private final PullRequestService prs;
    private final CommentService commentService;
    private final Integer repoId;
    private final Long prId;
    private final String commentText;

    public PullRequestCommentAddOperation(PullRequestService prs, CommentService commentService, Integer repoId, Long prId, String commentText) {
        this.commentService = commentService;
        this.repoId = repoId;
        this.prId = prId;
        this.commentText = commentText;
        this.prs = prs;
    }

    @Override
    public Void perform() {
        PullRequest pr = prs.getById(repoId, prId);
        commentService.addComment(new Builder(pr, commentText).build());
        return null;
    }
}
