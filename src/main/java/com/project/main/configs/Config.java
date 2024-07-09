package com.project.main.configs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

    private String urlGit;
    private String tokenGit;
    private String userNameGit;

    private String urlBit;
    private String userNameBit;
    private String passBit;
    private String workspaceBit;
    private String pathSave;

    @Autowired
    public Config(@Value("${gitlab.url}") String urlGit, @Value("${gitlab.token}") String tokenGit, @Value("${gitlab.username}") String userNameGit,
                  @Value("${bitbucket.url}") String urlBit, @Value("${bitbucket.username}") String userNameBit,
                  @Value("${bitbucket.pass}") String passBit, @Value("${bitbucket.workspace}") String workspaceBit, @Value("${path_save}") String pathSave) {
        this.urlGit = urlGit;
        this.tokenGit=tokenGit;
        this.userNameGit=userNameGit;
        this.urlBit=urlBit;
        this.userNameBit=userNameBit;
        this.passBit=passBit;
        this.workspaceBit=workspaceBit;
        this.pathSave=pathSave;
    }

    public String getUrlGit() {
        return urlGit;
    }

    public String getPathSave() {
        return pathSave;
    }

    public String getTokenGit() {
        return tokenGit;
    }

    public String getUserNameGit() {
        return userNameGit;
    }

    public String getUrlBit() {
        return urlBit;
    }

    public String getUserNameBit() {
        return userNameBit;
    }

    public String getPassBit() {
        return passBit;
    }

    public String getWorkspaceBit() {
        return workspaceBit;
    }
}