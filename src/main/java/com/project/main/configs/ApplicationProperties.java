package com.project.main.configs;

import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "properties")
public class ApplicationProperties {
    private GitlabProperties gitlab;
    private BitbucketProperties bitbucket;
    private String pathSave;

    public static class GitlabProperties {
        private String url;
        private String token;
        private String username;
        private UsernamePasswordCredentialsProvider credentialsProvider;

        public void setUrl(String url) {
            this.url = url;
        }

        public void setUsername(String username) {
            this.username = username;
            this.credentialsProvider = new UsernamePasswordCredentialsProvider(this.username, this.token);
        }

        public void setToken(String token) {
            this.token = token;
            this.credentialsProvider = new UsernamePasswordCredentialsProvider(this.username, this.token);
        }

        public String getUrl() {
            return url;
        }

        public String getToken() {
            return token;

        }

        public String getUsername() {
            return username;
        }

        public void setCredentialsProvider(UsernamePasswordCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
        }

        public UsernamePasswordCredentialsProvider getCredentialsProvider() {

            return credentialsProvider;
        }
    }

    public static class BitbucketProperties {
        private String url;
        private String username;
        private String pass;
        private String workspace;
        private UsernamePasswordCredentialsProvider credentialsProvider;

        public void setUrl(String url) {
            this.url = url;
        }

        public void setUsername(String username) {
            this.username = username;
            this.credentialsProvider = new UsernamePasswordCredentialsProvider(this.username, this.pass);
        }

        public void setPass(String pass) {
            this.pass = pass;
            this.credentialsProvider = new UsernamePasswordCredentialsProvider(this.username, this.pass);
        }

        public void setWorkspace(String workspace) {
            this.workspace = workspace;
        }

        public void setCredentialsProvider(UsernamePasswordCredentialsProvider credentialsProvider) {
            this.credentialsProvider = credentialsProvider;
        }


        public String getUrl() {
            return url;
        }

        public String getUsername() {
            return username;
        }

        public String getPass() {
            return pass;
        }

        public String getWorkspace() {
            return workspace;
        }

        public UsernamePasswordCredentialsProvider getCredentialsProvider() {
            return credentialsProvider;
        }
    }

    public GitlabProperties getGitlab() {
        return gitlab;
    }

    public BitbucketProperties getBitbucket() {
        return bitbucket;
    }

    public String getPathSave() {
        return pathSave;
    }

    public void setGitlab(GitlabProperties gitlab) {
        this.gitlab = gitlab;
    }

    public void setBitbucket(BitbucketProperties bitbucket) {
        this.bitbucket = bitbucket;
    }

    public void setPathSave(String pathSave) {
        this.pathSave = pathSave;
    }

}
