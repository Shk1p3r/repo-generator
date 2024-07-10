package com.project.main.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import com.project.main.configs.ApplicationProperties;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class RepoBitBucketService {
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;

    @Autowired
    public RepoBitBucketService(ApplicationProperties applicationProperties, RestTemplate restTemplate) {
        this.restTemplate=restTemplate;
        this.applicationProperties=applicationProperties;
    }


    public List<String> getRepositories() {
        String url = applicationProperties.getBitbucket().getUrl() + applicationProperties.getBitbucket().getWorkspace();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(applicationProperties.getBitbucket().getUsername(), applicationProperties.getBitbucket().getPass());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        List<String> repositoryNames = new ArrayList<>();

        JsonNode valuesNode = response.getBody().get("values");

        if (valuesNode != null && valuesNode.isArray()) {
            for (JsonNode repoNode : valuesNode) {
                JsonNode nameNode = repoNode.get("name");
                if (nameNode != null) {
                    repositoryNames.add(nameNode.asText());
                }
            }
        }
        return repositoryNames;
    }

    public void updateOrCreateAllRepos() throws IOException, GitAPIException {
        List<File> repos = Files.list(Paths.get(applicationProperties.getPathSave())).filter(Files::isDirectory).map(Path::toFile).toList();

        for (File repoDir : repos) {
            String repoName = repoDir.getName();
            updateOrCreateRemoteRepo(repoName);
        }
    }

    public void updateOrCreateRemoteRepo(String repoName) throws IOException, GitAPIException {
        if (!getRepositories().contains(repoName.toLowerCase())) {
            createRepository(repoName);
        }
        File localRepoDir = new File(applicationProperties.getPathSave() + "/" + repoName);

        try (Git git = Git.open(localRepoDir)) {
            git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish("https://bitbucket.org/" + applicationProperties.getBitbucket().getWorkspace() + "/" + repoName)).call();
            List<Ref> branches = git.branchList().call();
            for (Ref branch : branches) {
                git.push().setRemote("origin").setRefSpecs(new RefSpec(branch.getName() + ":" + branch.getName())).setCredentialsProvider(applicationProperties.getBitbucket().getCredentialsProvider()).call();
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void createRepository(String repoName) {
        String url = applicationProperties.getBitbucket().getUrl() + applicationProperties.getBitbucket().getWorkspace() + "/" + repoName.toLowerCase();

        HttpHeaders headers = new HttpHeaders();
        String loginAndPass = applicationProperties.getBitbucket().getUsername() + ":" + applicationProperties.getBitbucket().getPass();
        String authBasic = "Basic " + new String(Base64.getEncoder().encode(loginAndPass.getBytes(StandardCharsets.US_ASCII)));
        headers.set("Authorization", authBasic);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("scm", "git");
        body.put("is_private", "true");


        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
