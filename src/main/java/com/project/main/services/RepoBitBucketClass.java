package com.project.main.services;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import com.project.main.configs.Config;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class RepoBitBucketClass{
    private final Config config;
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    public RepoBitBucketClass(Config config) {
        this.config = config;
    }
    public List<String> getRepositories()
    {
        String url = config.getUrlBit() +config.getWorkspaceBit();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(config.getUserNameBit(), config.getPassBit());
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                JsonNode.class
        );
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
    public void updateOrCreateAllRepos(String rootDirPath) throws IOException, GitAPIException {
        List<File> repos = Files.list(Paths.get("C:\\Users\\ASUS\\Desktop\\main\\localGit\\"))
                .filter(Files::isDirectory)
                .map(Path::toFile)
                .toList();

        for (File repoDir : repos) {
            String repoName = repoDir.getName();
            updateOrCreateRemoteRepo(repoName);
        }
    }
   public void updateOrCreateRemoteRepo(String repoName) throws IOException, GitAPIException
   {
       if (!getRepositories().contains(repoName.toLowerCase())) {
           createRepository(repoName);
       }
       File localRepoDir = new File(config.getPathSave()+"/" + repoName);

       try (Git git = Git.open(localRepoDir)) {
           git.remoteSetUrl()
                   .setRemoteName("origin")
                   .setRemoteUri(new URIish("https://bitbucket.org/" + config.getWorkspaceBit()+"/" + repoName))
                   .call();
           List<Ref> branches = git.branchList().call();

           for (Ref branch : branches) {
               git.push()
                       .setRemote("origin")
                       .setRefSpecs(new RefSpec(branch.getName() + ":" + branch.getName()))
                       .setCredentialsProvider(new UsernamePasswordCredentialsProvider(config.getUserNameBit(), config.getPassBit()))
                       .call();
           }
       } catch (URISyntaxException e) {
           throw new RuntimeException(e);
       }
   }

    public void createRepository(String repoName) {
        String url = config.getUrlBit()+ config.getWorkspaceBit() + "/" + repoName.toLowerCase();

        HttpHeaders headers = new HttpHeaders();
        String loginAndPass = config.getUserNameBit()+ ":"+config.getPassBit();
        String authBasic = "Basic " + new String(Base64.getEncoder().encode(loginAndPass.getBytes(StandardCharsets.US_ASCII)));
        headers.set("Authorization", authBasic);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("scm", "git");
        body.put("is_private", "true");


        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}
