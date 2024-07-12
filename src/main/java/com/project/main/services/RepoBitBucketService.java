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
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
    }


    public List<RepoStates> getRepositories() {
        String url = applicationProperties.getBitbucket().getUrl() + applicationProperties.getBitbucket().getWorkspace();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(applicationProperties.getBitbucket().getUsername(), applicationProperties.getBitbucket().getPass());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
        JsonNode valuesNode = response.getBody().get("values");
        List<RepoStates> repositories = new ArrayList<>();


        if (valuesNode != null && valuesNode.isArray()) {
            for (JsonNode repoNode : valuesNode) {
                JsonNode nameNode = repoNode.get("name");
                if (nameNode != null) {
                    repositories.add(new RepoStates(nameNode.asText(), ""));
                }
            }
        }
        return repositories;
    }

    public List<RepoStates> updateOrCreateAllRepos() throws IOException, GitAPIException {
        List<File> repos = Files.list(Paths.get(applicationProperties.getPathSave())).filter(Files::isDirectory).map(Path::toFile).toList();
        List<RepoStates> statesRepos = new ArrayList<>();
        for (File repoDir : repos) {
            String repoName = repoDir.getName();
            statesRepos.add(updateOrCreateRemoteRepo(new RepoStates(repoName, "")));
        }
        return statesRepos;
    }

    public RepoStates updateOrCreateRemoteRepo(RepoStates repoState) throws IOException, GitAPIException {
        boolean exists = false;
        List<RepoStates> list = getRepositories();
        for (RepoStates repo : list) {
            if (repo.getRepoName().equalsIgnoreCase(repoState.getRepoName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            repoState = createRepository(repoState.getRepoName());
        }
        File localRepoDir = new File(applicationProperties.getPathSave() + "/" + repoState.getRepoName());

        try (Git git = Git.open(localRepoDir)) {
            git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish("https://bitbucket.org/" + applicationProperties.getBitbucket().getWorkspace() + "/" + repoState.getRepoName())).call();
            List<Ref> branches = git.branchList().call();
            for (Ref branch : branches) {
                git.push().setRemote("origin").setRefSpecs(new RefSpec(branch.getName() + ":" + branch.getName())).setCredentialsProvider(applicationProperties.getBitbucket().getCredentialsProvider()).call();
            }
            if (exists) {
                repoState.setState("обновлен");

            }
            exists = false;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return repoState;
    }

    public RepoStates createRepository(String repoName) {
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
        return new RepoStates(repoName, "создан");
    }
}
