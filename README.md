# StudyBoard

A study-tracking web application (Java, Servlets, Tomcat) delivered by a complete CI/CD pipeline:
**GitHub -> Jenkins -> Maven -> Docker -> Docker Hub -> Ansible -> Tomcat**.

## Features

- **Home**: progress ring and the next tasks.
- **My tasks**: add, complete, delete and filter by category (Course, Lab, Project, Exam).
- **Deployment**: version, Jenkins build number and the container that answers (proof of the automatic deployment).
- **/health**: health check used by the pipeline's smoke test.

Data is kept in memory (no database): it is reset on every deployment.

## Structure

```
studyboard/
├── pom.xml                      Maven build (WAR, JUnit tests)
├── Dockerfile                   Tomcat image + application
├── Jenkinsfile                  Pipeline: test, package, docker, push, deploy, smoke test
├── ansible/                     Production server configuration and deployment
│   ├── ansible.cfg
│   ├── inventory.ini
│   └── deploy.yml
└── src/
    ├── main/java/com/wiem/studyboard/
    │   ├── model/Task.java            Data
    │   ├── service/TaskService.java   Business logic
    │   ├── AppInfo.java               Version and build
    │   └── web/                       Pages (servlets) and HTML template
    ├── main/resources/app.properties  Version injected by Maven
    ├── main/webapp/                   CSS, web.xml, entry page
    └── test/java/                     Unit tests
```

## Run locally (inside the `ci` VM)

```bash
mvn -B clean package                    # compile, test, create target/studyboard.war
docker build -t studyboard:local .
docker run -d -p 8081:8080 --name studyboard-local studyboard:local
# then open http://CI_VM_IP:8081
docker rm -f studyboard-local           # to stop it
```

## Before running the pipeline: 3 values to replace

| File | Value | Replace with |
|---|---|---|
| `Jenkinsfile` | `YOUR_DOCKERHUB_USER` | your Docker Hub username |
| `Jenkinsfile` | `APP_VM_IP` | the IP of the `app` VM |
| `ansible/inventory.ini` | `APP_VM_IP` | the IP of the `app` VM |

## Push the project to GitHub

```bash
git init
git add .
git commit -m "feat: StudyBoard project and CI/CD pipeline"
git branch -M main
git remote add origin https://github.com/YOUR_GITHUB_USER/studyboard.git
git push -u origin main
```

## Modify the application

1. Change the code (for example the home page title in `HomeServlet.java`).
2. `git add .`, `git commit -m "feat: ..."`, `git push`.
3. Jenkins detects the commit (about 2 minutes), runs the pipeline and redeploys.
4. Reload the "Deployment" page: the build number has changed.

## Tests

`mvn test` runs the tests for `TaskService`, the HTML template and the version information.
