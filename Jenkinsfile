
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build and Test') {
            steps {
                dir('BackEnd') {
                    bat 'mvnw.cmd clean verify'
                }
            }
        }
    }

    post {
        success {
            echo 'NextERP backend build and tests passed.'
        }
        failure {
            echo 'Build or tests failed. Deployment will not proceed.'
        }
    }
}
