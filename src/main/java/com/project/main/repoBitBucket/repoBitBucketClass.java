package com.project.main.repoBitBucket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class repoBitBucketClass implements  repoBitBucket{
     private static final String BITBUCKET_API_BASE_URL = "https://api.bitbucket.org/2.0/repositories/";
    
    private RestTemplate restTemplate = new RestTemplate();

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<String> getRepositories(String userName, String userPass, String workspace)
    {
        String url = BITBUCKET_API_BASE_URL +workspace;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(userName, userPass);
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
    
}
