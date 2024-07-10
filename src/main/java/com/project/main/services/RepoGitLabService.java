package com.project.main.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import com.project.main.configs.ApplicationProperties;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.URIish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


@Service
public class RepoGitLabService {
    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;

    @Autowired RepoGitLabService(ApplicationProperties applicationProperties, RestTemplate restTemplate)
    {
        this.restTemplate=restTemplate;
        this.applicationProperties=applicationProperties;
    }

    public List<String> getRepositories() {
        String url = applicationProperties.getGitlab().getUrl() + "/users/" + applicationProperties.getGitlab().getUsername() + "/projects";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + applicationProperties.getGitlab().getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        });

        List<Map<String, Object>> projects = response.getBody();
        return projects.stream().map(project -> (String) project.get("name")).toList();
    }

    public String updateRepository(String repoName) throws IOException, GitAPIException {
        String repoUrl = "https://gitlab.com/" + applicationProperties.getGitlab().getUsername() + "/" + repoName;
        File repoDir = new File(applicationProperties.getPathSave(), repoName);
        if (!repoDir.exists()) // eсли нет в папке
        {
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(repoDir)
                    .setCredentialsProvider(applicationProperties.getGitlab().getCredentialsProvider())
                    .setCloneAllBranches(true).call();
            return " был создан на локальном диске";
        } else {
            try (Git git = Git.open(repoDir)) {
                git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish(repoUrl)).call();

                List<String> remoteBranches = git.branchList()
                        .setListMode(ListBranchCommand.ListMode.REMOTE)
                        .call()
                        .stream()
                        .map(ref -> ref.getName()
                                .replace("refs/remotes/origin/", ""))
                        .toList();

                List<String> localBranches = git.branchList()
                        .setListMode(ListBranchCommand.ListMode.ALL)
                        .call()
                        .stream()
                        .map(ref -> ref.getName()
                                .replace("refs/heads/", ""))
                        .toList();

                for (String branch : remoteBranches) {
                    if (!localBranches.contains(branch)) {
                        git.checkout().setName(branch).setCreateBranch(true).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).call();
                    } else {
                        git.checkout().setName(branch).call();
                    }
                    git.pull().setCredentialsProvider(applicationProperties.getGitlab().getCredentialsProvider()).call();
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            return " был обновлен";
        }
    }

    public void updateAllRepositories() throws IOException, GitAPIException {
        List<String> repositories = getRepositories();
        for (String repoName : repositories) {
            updateRepository(repoName);
        }
    }
}
