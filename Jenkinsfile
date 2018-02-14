def mvn = "mvn -s nexusconfigurations/nexus.xml"

pipeline {
 agent any

 stages {

  // Using Maven build the war file
  // Do not run tests in this step 
  stage('Build JAR') {
	  steps {
   withMaven(maven: 'apache-maven-3.3.9') {
    bat "${mvn} clean install -DskipTests=true"
	
	 archive 'target/*.jar'
   }
  }
  }

  // Using Maven run the unit tests
  stage('Unit Tests') {
   steps {
    withMaven(maven: 'apache-maven-3.3.9') {
     bat "${mvn} test"
    }
   }
  }

  // Using Maven call SonarQube for Code Analysis
  stage('Sonar Code Analysis') {
   steps {
    withMaven(maven: 'apache-maven-3.3.9') {
     bat "${mvn} sonar:sonar -Dsonar.host.url=http://localhost:9000   -Dsonar.login=aab02659e091858dfd99ddace56d44c604390a52"
    }
   }
  }

   // Publish the latest war file to Nexus. This needs to go into <nexusurl>/repository/releases.
   stage('Publish to Nexus') {
    steps {
     withMaven(maven: 'apache-maven-3.3.9') {
      bat "${mvn} deploy -DskipTests=true"
     }
   }}

    stage('Approve to Deploy on Openshift') {
     steps {
      timeout(time: 2, unit: 'DAYS') {
       input message: 'Do you want to Approve?'
      }
     }
    }
    stage('New Build') {
     steps {
      sh 'oc login https://192.168.99.100:8443 --token=G2AsDzhLjmwyBsRCYmRu0EekAGGetQlFJewtR2XmyVA --insecure-skip-tls-verify'

      sh 'oc project open1'
	  
	  sh 'oc delete all --all'

      sh 'oc new-build --name=abc redhat-openjdk18-openshift --binary=true'
     }
    }

    stage('Start Build') {
     steps {
		//sh "pwd" 
		sh " curl -O -X GET -u admin:admin123 http://localhost:8081/repository/snapshot/com/openshift/test/openshift-jenkins/0.0.1-SNAPSHOT/openshift-jenkins-0.0.1-20180214.210246-15.jar "
      sh "rm -rf oc-build && mkdir -p oc-build/deployments"
      sh "cp ./openshift-jenkins-0.0.1-20180214.184225-1.jar oc-build/deployments/ROOT.jar"

      sh 'oc start-build abc --from-dir=oc-build --wait=true  --follow'
     }
    }
    stage('Deploy & Expose') {
     steps {
      sh 'oc new-app abc'
      sh 'oc expose svc/abc'
     }
    }
    stage('Scaling') {
     steps {
      sh ' oc scale --replicas=2 dc abc'
     }
    }

   }
  }