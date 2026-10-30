pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    triggers {
        pollSCM('H/2 * * * *')
    }

    environment {
        IMAGE = 'wiemassadi/studyboard'
        APP_HOST = '10.97.0.20'
        ANSIBLE_CONFIG = 'ansible/ansible.cfg'
    }

    stages {

        stage('Test') {
            steps {
                sh 'mvn -B test'
            }

            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn -B -DskipTests -Dbuild.number=$BUILD_NUMBER package'
            }
        }

        stage('Docker build') {
            steps {
                sh 'docker build -t $IMAGE:$BUILD_NUMBER -t $IMAGE:latest .'
            }
        }

        stage('Docker push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DH_USER',
                        passwordVariable: 'DH_PASS'
                    )
                ]) {
                    sh 'printf "%s" "$DH_PASS" | docker login -u "$DH_USER" --password-stdin'
                    sh 'docker push $IMAGE:$BUILD_NUMBER'
                    sh 'docker push $IMAGE:latest'
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'ansible-playbook -i ansible/inventory.ini ansible/deploy.yml -e image=$IMAGE:$BUILD_NUMBER'
            }
        }

        stage('Smoke test') {
            steps {
                sh '''
                    for i in $(seq 1 15); do
                        if curl -fs http://$APP_HOST:8080/health | grep -q OK &&
                           curl -fs http://$APP_HOST:8080/home | grep -q "Build $BUILD_NUMBER<"; then
                            echo "Smoke test successful: build $BUILD_NUMBER"
                            exit 0
                        fi

                        echo "Application not ready yet. Attempt $i/15"
                        sleep 3
                    done

                    echo "Smoke test failed"
                    exit 1
                '''
            }
        }
    }

    post {
        always {
            sh 'docker logout || true'
        }
    }
}
