package com.project.main.repoGitLab;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;

public class repoGitLabClass implements repoGitLab {
    private static final String GITLAB_API_BASE_URL = "https://gitlab.com/api/v4";
    
    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<String> getRepositories(String accessToken, String userId) {
        String url = GITLAB_API_BASE_URL + "/users/" + userId + "/projects";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                entity, 
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );
        
        List<Map<String, Object>> projects = response.getBody();
        return projects.stream()
                .map(project -> (String) project.get("name"))
                .toList();
    }
}
