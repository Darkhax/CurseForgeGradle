#!/usr/bin/env groovy

pipeline {

    agent any
    
    tools {
        jdk "jdk8u292-b10"
    }
    
    stages {
        
        stage('Build') {
        
            steps {
            
                withCredentials([ file(credentialsId: 'gradle_secrets', variable: 'ORG_GRADLE_PROJECT_secretFile') ]) {
            
                    echo 'Building project.'
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build publishPlugins --stacktrace --warn'
                }
            }
        }
    }
}