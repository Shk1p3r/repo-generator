import { Component, OnInit } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RepoState } from './repoState.model';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'mainAngular';
  name="";
  repoStatesGit: RepoState[] = [];
  repoStateGit: RepoState = { repoName: '', state: '' };
  reposStatesLocal: RepoState[] = [];
  repoStateBit: RepoState = { repoName: '', state: '' };
  private gitlabUrl = 'http://localhost:8080/gitlab';
  private bitbucketUrl = 'http://localhost:8080/bitbucket';

  constructor(private http: HttpClient) { }

  ngOnInit(): void {
    //this.loadRepos();
  }

  loadReposGitLab(): void {
    this.getGitLabRepos().subscribe(data => this.repoStatesGit = data);
  }
  loadReposLocal(): void {
    this.getLocalRepos().subscribe(data => this.reposStatesLocal = data);
  }

  updateGitLabRepoHandler(repoName: string): void {
    this.updateGitLabRepo(repoName).subscribe(response => {
      this.repoStatesGit = this.repoStatesGit.map(repoStateElement => 
        repoStateElement.repoName === repoName ? response : repoStateElement
      );
    });
  }

  updateAllGitLabReposHandler(): void {
    this.updateAllGitLabRepos().subscribe(response => {
      this.repoStatesGit=response;
    });
  }

  updateBitBucketRepoHandler(repoName: string): void {
    this.updateBitBucketRepo(repoName).subscribe(response => {
      this.reposStatesLocal = this.reposStatesLocal.map(repoStateElement => 
        repoStateElement.repoName === repoName ? response : repoStateElement
    );
  });
}

  updateAllBitBucketReposHandler(): void {
    this.updateAllBitBucketRepos().subscribe(response => {
      this.reposStatesLocal=response;
    });
  }

  private getGitLabRepos(): Observable<RepoState[]> {
    return this.http.get<RepoState[]>(this.gitlabUrl);
  }

  private getLocalRepos(): Observable<RepoState[]> {
    return this.http.get<RepoState[]>(this.gitlabUrl+'/local');
  }

  private updateGitLabRepo(repoName: string): Observable<RepoState> {
    let params = new HttpParams().set('repoName', repoName);
    return this.http.post<RepoState>(`${this.gitlabUrl}/update`, {}, { params });
  }

  private updateAllGitLabRepos(): Observable<RepoState[]> {
    return this.http.post<RepoState[]>(`${this.gitlabUrl}/updateAll`, {});
  }

  private updateBitBucketRepo(repoName: string): Observable<RepoState> {
    let params = new HttpParams().set('repoName', repoName);
    return this.http.post<RepoState>(`${this.bitbucketUrl}/update`, {}, { params });
  }

  private updateAllBitBucketRepos(): Observable<RepoState[]> {
    return this.http.post<RepoState[]>(`${this.bitbucketUrl}/updateAll`, {});
  }

}
