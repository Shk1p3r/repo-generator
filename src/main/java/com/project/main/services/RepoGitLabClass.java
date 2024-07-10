package com.project.main.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.project.main.configs.Config;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class RepoGitLabClass {
    private final Config config;
    public RepoGitLabClass(Config config) {
        this.config = config;
    }
    private final RestTemplate restTemplate = new RestTemplate();
    public List<String> getRepositories() {
        String url = config.getUrlGit() + "/users/" + config.getUserNameGit() + "/projects";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + config.getTokenGit());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<>() {}
        );
        
        List<Map<String, Object>> projects = response.getBody();
        return projects.stream()
                .map(project -> (String) project.get("name"))
                .toList();
    }
    
    public String updateRepository(String repoName) throws IOException, GitAPIException
    {
        String repoUrl ="https://gitlab.com/"+ config.getUserNameGit()+"/"+repoName;
        File repoDir = new File(config.getPathSave(), repoName);
        if(!repoDir.exists()) // eсли нет в папке
        {
            Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(repoDir)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(config.getUserNameGit(), config.getTokenGit()))
                .setCloneAllBranches(true)
                .call();
            return " был создан на локальном диске";
        } 
        else { // eсли есть
            try (Git git = Git.open(repoDir)) {
                git.remoteSetUrl()
                        .setRemoteName("origin")
                        .setRemoteUri(new URIish(repoUrl))
                        .call();
                git.pull()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(config.getUserNameGit(), config.getTokenGit()))
                    .call();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return " был обновлен";
        }
    }
    public void updateAllRepositories() throws IOException, GitAPIException {
        List<String> repositories = getRepositories();
        for (String repoName : repositories) {
            updateRepository( repoName);
        }
    }
}
