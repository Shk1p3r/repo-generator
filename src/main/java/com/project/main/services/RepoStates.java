package com.project.main.services;

public class RepoStates {
    private String repoName;
    private String state;

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public RepoStates(String repoName, String state) {
        this.repoName = repoName;
        this.state = state;
    }
}
