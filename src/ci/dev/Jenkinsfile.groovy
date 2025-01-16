pipeline {
    agent any

    environment {
        MYSQL_DB_HOST = "jdbc:mariadb://localhost"
        MYSQL_DB_PORT = "3306"
        MYSQL_DB_USERNAME = credentials('MYSQL_DB_CREDENTIALS_USERNAME')
        MYSQL_DB_PASSWORD = credentials('MYSQL_DB_CREDENTIALS_PASSWORD')
    }

    stages {
        // Stage to pull the code from the development branch of your Git repository
        stage('Checkout Code') {
            steps {
                script {
                    // Checkout the development branch from your Git repo
                    git branch: 'development', url: 'https://github.com/ajilenakh/book-library.git'
                }
            }
        }

        // Stage to run Maven clean install
        stage('Build and Install') {
            steps {
                script {
                    echo 'Running mvn clean install...'
                    sh 'mvn clean install'
                }
            }
        }

        // Stage to run Maven tests
        stage('Run Tests') {
            steps {
                script {
                    echo 'Running mvn test...'
                    sh 'mvn test'
                }
            }
        }

        // Stage to update application.properties
        stage('Set Spring Profile') {
            steps {
                script {
                    echo "Setting active profile to dev..."
                    if (!fileExists('src/main/resources/application.properties')) {
                        echo "application.properties not found! Creating it."
                        sh 'echo "spring.profiles.active=dev" >> src/main/resources/application.properties'
                    } else {
                        sh 'echo "spring.profiles.active=dev" >> src/main/resources/application.properties'
                    }
                }
            }
        }

        // Stage to run Spring Boot application
        stage('Run Spring Boot Application') {
            steps {
                script {
                    echo 'Running Spring Boot application...'
                    sh 'mvn spring-boot:run'
                }
            }
        }
    }

    post {
        success {
            echo "Deployment to dev environment completed successfully."
        }
        failure {
            echo "Build or deployment failed."
        }
    }
}