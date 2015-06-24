package com.palantir.stash.stashbot.managers;

import java.io.IOException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.atlassian.stash.nav.NavBuilder;
import com.atlassian.stash.repository.Repository;
import com.google.common.collect.ImmutableList;
import com.palantir.stash.stashbot.logger.PluginLoggerFactory;

/**
 * This class makes rest calls to stash's own API on behalf of some plugin code, and returns the results as JSON.
 * 
 * It requires a username and a password of a user to make the request as, unfortunately.
 * 
 * This is useful because many APIs are still not public to plugins, but available via rest.
 * 
 * @author cmyers
 */
public class RestApiManager {

    private final NavBuilder nb;
    private final Logger log;

    public RestApiManager(PluginLoggerFactory plf) {
        this.nb = null;
        this.log = plf.getLoggerForThis(this);
    }

    public String getRepoAccessKeyRestUrl(Repository repo) {
        return nb.buildAbsolute() + "/rest/keys/1.0/projects/" + repo.getProject().getKey() + "/repos/"
            + repo.getSlug() + "/ssh";
    }

    /**
     * Use the rest API to post a new ssh key to have read access for a given repo
     * 
     * These keys *can* collide with eachother (i.e. you can give the same key access to multiple repos) but they
     * MAY NOT collide with users (i.e. you may not give a user's ssh key access to a repo)
     * 
     * impl details:
     * Posting: /rest/keys/1.0/projects/{projectKey}/repos/{repositorySlug}/ssh
     * content: {
     * key": {
     * "text": "ssh-rsa AAAAB3... me@127.0.0.1"
     * },
     * "permission": "REPO_READ"
     * }
     * // reference:
     * https://bitbucket.org/atlassianlabs/stash-java-client/src/01f59a7c9aafd02b52a0e5d682d67c3aece6e0c0/core
     * /src/main/java/com/atlassian/stash/rest/client/core/StashClientImpl.java?at=stash-java-client-1.3.9
     * // reference: https://developer.atlassian.com/static/rest/stash/3.10.0/stash-ssh-rest.html#idp243664
     * 
     */
    public void postAccessKeyForRepo(String pubKey, Repository repo) {
        try {
            String url = getRepoAccessKeyRestUrl(repo);
            JSONObject args = new JSONObject();
            args.put("key", new JSONObject().put("text", pubKey));
            args.put("permission", "REPO_READ");

            JSONObject resp = postRestCall(url, args, false);

        } catch (JSONException e) {
            throw new RuntimeException("Unable to form JSON for rest call", e);
        } catch (ClientProtocolException e) {
            throw new RuntimeException("Error during REST call", e);
        } catch (IOException e) {
            throw new RuntimeException("Error during REST call", e);
        }
    }

    public JSONObject postRestCall(String requestUrl, JSONObject requestJson, boolean anonymousCall)
        throws ClientProtocolException, IOException {
        String requestData = (requestJson != null ? requestJson.toString() : null);
        HttpPost post = new HttpPost(requestUrl);
        try (CloseableHttpClient chc = HttpClients.createDefault()) {
            post.setHeader(HTTP.CONTENT_TYPE, "application/json");

            List<NameValuePair> args = ImmutableList.of(new BasicNameValuePair("json", requestData));
            post.setEntity(new UrlEncodedFormEntity(args));

            try (CloseableHttpResponse chr = chc.execute(post)) {

            }

        }
        return null;
    }
}
