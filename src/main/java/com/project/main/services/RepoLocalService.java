package com.project.main.services;

import com.project.main.configs.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class RepoLocalService {
    private final ApplicationProperties applicationProperties;

    @Autowired
    RepoLocalService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    public List<RepoStates> getLocalRepositories() {
        File repoDirectory = new File(applicationProperties.getPathSave());
        List<RepoStates> repoList = new ArrayList<>();

        if (repoDirectory.exists() && repoDirectory.isDirectory()) {
            File[] files = repoDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        repoList.add(new RepoStates(file.getName(), ""));
                    }
                }
            }
        }
        return repoList;
    }
}
