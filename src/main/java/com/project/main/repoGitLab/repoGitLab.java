package com.project.main.repoGitLab;

import java.util.List;

public interface repoGitLab {
    List<String> getRepositories(String accessToken, String userId);
}
