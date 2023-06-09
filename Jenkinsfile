pipeline {
    agent any
    stages {
        stage('Detect language') {
            steps {
                step([$class: 'LanguageDetector', personalToken: 'num', repoURL: 'https://github.com/shaN480/xtryger' ])
            }
            
        
        }
        
    }
}
