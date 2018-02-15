def mvn = "mvn -s nexusconfigurations/nexus.xml"

pipeline {
 agent any

 stages {
	 stage('Check Entered Parameters'){
		steps{ 
		echo "Production App Name - ${PROD_NAME}" 
		echo "Application Name - ${APP_NAME}" 
		echo "Development App Name - ${DEV_NAME}"
		echo "Master Host - ${MASTER_URL}"
	 }}

  // Using Maven build the war file
  // Do not run tests in this step 
  stage('Build Artifact') {
	  steps {
   withMaven(maven: 'apache-maven-3.3.9') {
    bat "${mvn} clean install -DskipTests=true"
	
	 archive 'target/*.jar'
   }
  }
  }

  // Using Maven run the unit tests
  stage('jUnit Tests') {
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
   stage('Publish to Nexus Repository') {
    steps {
     withMaven(maven: 'apache-maven-3.3.9') {
      bat "${mvn} deploy -DskipTests=true"
     }
   }}

    stage('Approve to Deploy on Openshift?') {
     steps {
      timeout(time: 2, unit: 'DAYS') {
       input message: 'Do you want to Approve?'
      }
     }
    }
    stage('Openshift New Build') {
     steps {
      sh 'oc login ${MASTER_URL} --token=${OAUTH_TOKEN} --insecure-skip-tls-verify' 

      sh 'oc project ${DEV_NAME}'
	  
	  sh 'oc delete all --all'

      sh 'oc new-build --name=${APP_NAME} redhat-openjdk18-openshift --binary=true'
     }
    }

    stage('Openshift Start Build') {
     steps {
		//sh "pwd" 
		sh " curl -O -X GET -u admin:admin123 http://localhost:8081/repository/snapshot/com/openshift/test/openshift-jenkins/0.0.1-SNAPSHOT/openshift-jenkins-0.0.1-20180214.210246-15.jar "
      sh "rm -rf oc-build && mkdir -p oc-build/deployments"
      sh "cp ./openshift-jenkins-0.0.1-20180214.210246-15.jar oc-build/deployments/ROOT.jar"

      sh 'oc start-build ${APP_NAME} --from-dir=oc-build --wait=true  --follow'
     }
    }
    stage('Deploy in Development') {
     steps {
      sh 'oc new-app ${APP_NAME}'
      sh 'oc expose svc/${APP_NAME}'
     }
    }
    stage('Scaling Application') {
     steps {
      sh ' oc scale --replicas=2 dc ${APP_NAME}'
     }
    }
	
              stage('Promote to Production?') {
                steps {
                  timeout(time:2, unit:'DAYS') {
                      input message: "Promote to Production?", ok: "Promote"
			  }	}}
			  
			  stage('Deploy in Prodiuction') {
                steps {
					 // tag for stage
               sh "oc tag ${DEV_NAME}/${APP_NAME}:latest ${PROD_NAME}/${APP_NAME}:${env.BUILD_ID}"
               // clean up. keep the imagestream
               sh "oc delete bc,dc,svc,route -l app=${APP_NAME} -n ${PROD_NAME}"
               // deploy stage image
               sh "oc new-app ${APP_NAME}:${env.BUILD_ID} -n ${PROD_NAME}"
               sh "oc expose svc/${APP_NAME} -n ${PROD_NAME}"
                  	}}
			  
	
   }
  }