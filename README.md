Sample Project for Akka Cluster with Persistence
---

Intended to demonstrate a working project with Akka Clusters and Persistence.

Implemented a sharded counter.

- Given three nodes Node 1, 2, and 3
- And three counters C1, C2, and C3 located in the node of the same number
- When there's a request in (Node 3) to increment counter (C1)
- Then (Node 3) should forward the processing to (Node 1)

To test:

- Run three processes
```shell script
$ sbt "runMain com.rmpader.gitprojects.MainApp1"
```
```shell script
$ sbt "runMain com.rmpader.gitprojects.MainApp2"
```
```shell script
$ sbt "runMain com.rmpader.gitprojects.MainApp3"
```

- Send a cURL to the three servers for varying counters
```shell script
$ curl http://localhost:8081/count/1
$ curl http://localhost:8081/count/2
$ curl http://localhost:8081/count/3
```
```shell script
$ curl http://localhost:8082/count/1
$ curl http://localhost:8082/count/2
$ curl http://localhost:8082/count/3
```
```shell script
$ curl http://localhost:8083/count/1
$ curl http://localhost:8083/count/2
$ curl http://localhost:8083/count/3
```

### Kubernetes Deployment

Also included are sample Kubernetes service and deployment definition files in the `./kube` directory.
