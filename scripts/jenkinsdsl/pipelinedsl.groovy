// FreeStyle job examples
// This script needs Docker installed on the machine running the job.

folder("Telenor-Pipeline")

job("Telenor-Pipeline/BuildAndPush") {
  scm {
      git {
          remote {
              name('Simple-Container-App')
              url('https://github.com/hoeghh/simple_container_app.git')
          }
          extensions {
              cleanAfterCheckout()
              relativeTargetDirectory('simple-container-app')
          }
      }
  }

  triggers {
    scm('H/15 * * * *')
  }

  //Shell step
  steps {
		shell("echo 'Hi Henrik', checking out... \$GIT_COMMIT")
	}
  steps {
		shell("./simple-container-app/scripts/build.sh \$GIT_COMMIT")
	}
  steps {
		shell("./simple-container-app/scripts/push.sh \$GIT_COMMIT")
	}

  steps {
  	//Automatisk
  	downstreamParameterized {
  		trigger("deploy_to_stagging") {
  			parameters {
                  gitRevision()
        }
  		}
  	}
  }
}


job("Telenor-Pipeline/deploy_to_stagging") {
  steps {
    shell("./simple-container-app/scripts/stag-deploy.sh \$GIT_COMMIT")
  }

  steps {
    //Automatisk
  	downstreamParameterized {
  		trigger("simple_test") {
  			parameters {
                  gitRevision()
        }
  		}
  	}
  }
}

job("Telenor-Pipeline/simple_test") {
  steps {
    shell("./simple-container-app/scripts/test.sh \$GIT_COMMIT")
  }
}


job("Telenor-Pipeline/deploy_to_production") {
  steps {
    shell("./simple-container-app/scripts/prod_deploy.sh \$GIT_COMMIT")
  }
}

job("Telenor-Pipeline/rollback-production") {
  steps {
    shell("./simple-container-app/scripts/prod_rollback.sh \$GIT_COMMIT")
  }
}

