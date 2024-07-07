package com.project.main.restservice;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.project.main.repoBitBucket.repoBitBucketClass;
import com.project.main.repoGitLab.repoGitLabClass;

@RestController
public class MainController {

	repoGitLabClass rGLC = new repoGitLabClass();
	repoBitBucketClass rBBC = new repoBitBucketClass();

	@GetMapping("/gitlab")
	public String outNameRepoGit() {
		List<String> repos =rGLC.getRepositories("glpat-AZ6DMekfZ-4LiAxEzLzh", "qwerty62806");
		return new MainC(repos).toStringNameGit();
	}
	@GetMapping("/bitbucket")
	public String outNameRepoBit() {
		List<String> repos =rBBC.getRepositories("qwerty62806", "ATBB8qmRcXbS6vCV4jqHtjA8yxzM8A8BF6DB", "task_clone");
		return new MainC(repos).toStringNameBit();
	}

}
