package com.project.main.restservice;
import java.util.List;



public record MainC(List<String> nameRepositories) { 
    public String toStringNameGit()
    {
        if(!nameRepositories.isEmpty() && nameRepositories!=null)
        {
            return "<pre>"+"List of repositories on GitLab: \n"+String.join("\n", nameRepositories)+"</pre>";
        }
        else{
            return "";
        }
    }
    public String toStringNameBit()
    {
        if(!nameRepositories.isEmpty() && nameRepositories!=null)
        {
            return "<pre>"+"List of repositories on BitBucket: \n"+String.join("\n", nameRepositories)+"</pre>";
        }
        else{
            return "";
        }
    }
}