pipeline {
    agent any

    environment {
        MYSQL_DB_HOST = "jdbc:mariadb://localhost"
        MYSQL_DB_PORT = "3306"
    }

    stages {
        // Stage to pull the code from the development branch of your Git repository
        stage('Checkout Code') {
            steps {
                script {
                    git branch: 'development', url: 'https://github.com/ajilenakh/book-library.git'
                }
            }
        }

        // Stage to set the credentials
        stage('Setting Secret Env') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'MYSQL_DB_CREDENTIALS', usernameVariable: 'LIBRARY_MYSQL_DB_USERNAME', passwordVariable: 'LIBRARY_MYSQL_DB_PASSWORD')]) {

                        // Exporting these custom variables for use in the shell commands
                        sh """
                            export LIBRARY_MYSQL_DB_USERNAME=${LIBRARY_MYSQL_DB_USERNAME}
                            export LIBRARY_MYSQL_DB_PASSWORD=${LIBRARY_MYSQL_DB_PASSWORD}
                        """
                    }
                }
            }
        }

        // Stage to update application.properties with the correct Spring profile
        stage('Set Spring Profile') {
            steps {
                script {
                    // Ensure the application.properties file exists, otherwise create it
                    if (!fileExists('src/main/resources/application.properties')) {
                        sh 'echo "spring.profiles.active=dev" >> src/main/resources/application.properties'
                    } else {
                        // Update the application.properties to set the Spring profile
                        sh 'echo "spring.profiles.active=dev" >> src/main/resources/application.properties'
                    }
                }
            }
        }

        // Stage to run the Spring Boot application
        stage('Run Spring Boot Application') {
            steps {
                script {
                    // Run Spring Boot application using the environment variables
                    sh """
                        mvn spring-boot:run
                    """
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
