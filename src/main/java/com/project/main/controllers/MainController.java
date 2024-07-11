package com.project.main.controllers;

import java.io.IOException;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.main.services.RepoBitBucketService;
import com.project.main.services.RepoGitLabService;

@RestController
public class MainController {

    private final RepoGitLabService repoGitLabService;
    private final RepoBitBucketService repoBitBucketService;

    @Autowired
    public MainController(RepoGitLabService repoGitLabService, RepoBitBucketService repoBitBucketService) {
        this.repoGitLabService = repoGitLabService;
        this.repoBitBucketService = repoBitBucketService;
    }

    @Operation(summary = "Получить все репозитории", description = "Возвращает список всех репозиториев из GitLab")
    @GetMapping("/gitlab")
    public String outNameRepoGit() {
        List<String> repos = repoGitLabService.getRepositories();
        if (!repos.isEmpty() && repos != null) {
            return "Список репозиториев на GitLab: \n" + String.join("\n", repos);
        } else {
            return "";
        }
    }

    @Operation(summary = "Получить все репозитории", description = "Возвращает список всех репозиториев из BitBucket")
    @GetMapping("/bitbucket")
    public String outNameRepoBit() {
        List<String> repos = repoBitBucketService.getRepositories();
        if (!repos.isEmpty() && repos != null) {
            return "Список репозиториев на BitBucket: \n" + String.join("\n", repos);
        } else {
            return "";
        }
    }

    @Operation(summary = "Обновить заданный репозиторий", description = "Обновляет репозиторий, если он есть на локальном диске или клонирует, если его нет")
    @PostMapping("/gitlab/update")
    public String updateGitLab(@RequestParam String repoName) throws IOException, GitAPIException {
        return "Репозиторий " + repoName + repoGitLabService.updateRepository(repoName);
    }

    @Operation(summary = "Обновить все репозитории", description = "Обновляет репозитории, если они есть на локальном диске или клонирует, если их нет")
    @PostMapping("/gitlab/updateAll")
    public String updateAllGitLab() throws GitAPIException, IOException {
        repoGitLabService.updateAllRepositories();
        return "Обновлены все репозитории";
    }

    @PostMapping("/bitbucket/update")
    @Operation(summary = "Обновить заданный репозиторий", description = "Обновляет репозиторий, если он есть в BitBucket или создает и пушит каждые ветки, если его нет")
    public String updateBitBucket(@RequestParam String repoName) throws GitAPIException, IOException {
        repoBitBucketService.updateOrCreateRemoteRepo(repoName);
        return "Репозиторий " + repoName + " обновлен";
    }

    @PostMapping("/bitbucket/updateAll")
    @Operation(summary = "Обновить все репозитории", description = "Обновляет репозитории, если они есть в BitBucket или создает и пушит каждые ветки, если их нет")
    public String updateAllBitBucket() throws GitAPIException, IOException {
        repoBitBucketService.updateOrCreateAllRepos();
        return "Репозитории загружены или обновлены";
    }

}
