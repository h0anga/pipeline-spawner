# Pipeline Spawner

> The Stateless Build Server

Pipeline Spawner is a Continuous Integration server that requires no database, no 
 agents, and minimal configuration. It uses Kubernetes to build 
 [KRR-compliant](https://wiki.sns.sky.com/pages/viewpage.action?pageId=89803475) 
 applications. 

## Concept

Pipeline Spawner exploits the fact that 
[KRR-22](https://cbsjira.bskyb.com/browse/KRR-22) 
defines the build process very strictly, and therefore all compliant applications build 
exactly the same way.

It also exploits the fact that we have a Kubernets cluster, which is pretty good at managing
jobs and distributing them across multiple nodes, to obviate the need for installing and
setting up "agents".  

## Downsides

**Firstly**, it's not ready yet. But with your help, this can change quickly.

**Secondly**, you can't use your build server as an artifact repository, the way we're doing now;
jobs on Kubernetes are volatile, and once terminated, there's no guarantee that their pods 
will stick around for any amount of time. So, push to artifactory anything you want 
to keep (this includes the output of acceptance tests).

**Thirdly**, version numbers don't really exists. Pipeline Spawner can't increment the previous
value of the build number, because that would mean keeping track of which application is
at which version, because that would require having a state, which means having a DataBase
or some other external dependency. Pipeline Spawner is stateless.  

Instead, it uses a unix timestamp as build number. It is a monotonically increasing integer unique
within one application, so it does satisfy the requirements for a build number. You may think
that this is not the best solution, and we're inclined to agree. Please let us know if you can 
think of a better solution. 

## Building and Running

You will need 
[sbt](https://www.scala-sbt.org/download.html)
, and a Kubernetes cluster 
([Minikukbe](https://kubernetes.io/docs/tasks/tools/install-minikube/) 
will do). Make sure you have a valid Kubernetes configuration in `$HOME/.kube/config`.
 
 At the `sbt` console, just run
```sbtshell
backend/run
```
to start the server. 

Then, add a web-hook for the project you want Pipeline Spawner to build for you.
The web-hook should point to `http://<your-ip>:8080/hook/git`. Test the hook, or push a change.

Go to `http://<your-ip>:8080`, and you should see your job listed on the main page.

Click on the ***i*** button to view your build output.
 