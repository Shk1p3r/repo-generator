package com.project.main.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.project.main.services.RepoLocalService;
import com.project.main.services.RepoStates;
import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.main.services.RepoBitBucketService;
import com.project.main.services.RepoGitLabService;

@RestController
public class MainController {

    private final RepoGitLabService repoGitLabService;
    private final RepoBitBucketService repoBitBucketService;
    private final RepoLocalService repoLocalService;

    @Autowired
    public MainController(RepoGitLabService repoGitLabService, RepoBitBucketService repoBitBucketService, RepoLocalService repoLocalService) {
        this.repoGitLabService = repoGitLabService;
        this.repoBitBucketService = repoBitBucketService;
        this.repoLocalService = repoLocalService;
    }

    @Operation(summary = "Получить все репозитории", description = "Возвращает список всех репозиториев из GitLab")
    @GetMapping("/gitlab")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<RepoStates> outNameRepoGit() {
        List<RepoStates> repos = repoGitLabService.getRepositories();
        if (!repos.isEmpty() && repos != null) {
            return repos;
        } else {
            return null;
        }
    }

    @Operation(summary = "Получить все репозитории", description = "Возвращает список всех репозиториев из BitBucket")
    @GetMapping("/bitbucket")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<RepoStates> outNameRepoBit() {
        List<RepoStates> repos = repoBitBucketService.getRepositories();
        if (!repos.isEmpty() && repos != null) {
            return repos;
        }
        return null;
    }

    @Operation(summary = "Обновить заданный репозиторий", description = "Обновляет репозиторий, если он есть на локальном диске или клонирует, если его нет")
    @PostMapping("/gitlab/update")
    @CrossOrigin(origins = "http://localhost:4200")
    public RepoStates updateGitLab(@RequestParam String repoName) throws IOException, GitAPIException {
        return repoGitLabService.updateRepository(new RepoStates(repoName, ""));
    }

    @Operation(summary = "Обновить все репозитории", description = "Обновляет репозитории, если они есть на локальном диске или клонирует, если их нет")
    @PostMapping("/gitlab/updateAll")
    @CrossOrigin(origins = "http://localhost:4200")
    public List<RepoStates> updateAllGitLab() throws GitAPIException, IOException {
        return repoGitLabService.updateAllRepositories();
    }

    @PostMapping("/bitbucket/update")
    @CrossOrigin(origins = "http://localhost:4200")
    @Operation(summary = "Обновить заданный репозиторий", description = "Обновляет репозиторий, если он есть в BitBucket или создает и пушит каждые ветки, если его нет")
    public RepoStates updateBitBucket(@RequestParam String repoName) throws GitAPIException, IOException {
        return repoBitBucketService.updateOrCreateRemoteRepo(new RepoStates(repoName, ""));
    }

    @PostMapping("/bitbucket/updateAll")
    @CrossOrigin(origins = "http://localhost:4200")
    @Operation(summary = "Обновить все репозитории", description = "Обновляет репозитории, если они есть в BitBucket или создает и пушит каждые ветки, если их нет")
    public List<RepoStates> updateAllBitBucket() throws GitAPIException, IOException {
        return repoBitBucketService.updateOrCreateAllRepos();
    }

    @GetMapping("/gitlab/local")
    @CrossOrigin(origins = "http://localhost:4200")
    @Operation(summary = "Получить все локальные репозитории", description = "Получает все репозитории, расположенные в папке на компьютере")
    public List<RepoStates> getLocalRepos() {
        return repoLocalService.getLocalRepositories();
    }

}
