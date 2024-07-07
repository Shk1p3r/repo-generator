package com.project.main.repoBitBucket;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

public interface repoBitBucket {
   List<String> getRepositories(String userName, String userPass, String workspace);
}
