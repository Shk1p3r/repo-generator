package com.project.main.services;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    RepoGitLabService(ApplicationProperties applicationProperties, RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.applicationProperties = applicationProperties;
    }

    public List<RepoStates> getRepositories() {
        String url = applicationProperties.getGitlab().getUrl() + "/users/" + applicationProperties.getGitlab().getUsername() + "/projects";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + applicationProperties.getGitlab().getToken());
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
        });

        List<Map<String, Object>> projects = response.getBody();

        List<RepoStates> repositories = new ArrayList<>();

        if (projects != null) {
            for (Map<String, Object> project : projects) {
                String repoName = (String) project.get("name");
                repositories.add(new RepoStates(repoName, ""));
            }
        }
        return repositories;
    }

    public RepoStates updateRepository(RepoStates repoState) throws IOException, GitAPIException {
        String repoUrl = "https://gitlab.com/" + applicationProperties.getGitlab().getUsername() + "/" + repoState.getRepoName();
        File repoDir = new File(applicationProperties.getPathSave(), repoState.getRepoName());
        if (!repoDir.exists()) // eсли нет в папке
        {
            Git.cloneRepository().setURI(repoUrl).setDirectory(repoDir).setCredentialsProvider(applicationProperties.getGitlab().getCredentialsProvider()).setCloneAllBranches(true).call();
            return new RepoStates(repoState.getRepoName(), "создан");
        } else {
            try (Git git = Git.open(repoDir)) {
                git.remoteSetUrl().setRemoteName("origin").setRemoteUri(new URIish(repoUrl)).call();

                List<String> remoteBranches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call().stream().map(ref -> ref.getName().replace("refs/remotes/origin/", "")).toList();

                List<String> localBranches = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call().stream().map(ref -> ref.getName().replace("refs/heads/", "")).toList();

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
            return new RepoStates(repoState.getRepoName(),"обновлен");
        }
    }

    public List<RepoStates> updateAllRepositories() throws IOException, GitAPIException {
        List<RepoStates> repositories = getRepositories();
        List<RepoStates> statesRepo = new ArrayList<>();
        for(int i =0;i<repositories.size();i++)
        {
            RepoStates repoState = repositories.get(i);
            statesRepo.add(updateRepository(repoState));
        }
        return statesRepo;
    }
}
