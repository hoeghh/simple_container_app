// FreeStyle job examples
// This script needs Docker installed on the machine running the job.

folder("Simple-Pipeline")

job("Simple-Pipeline/BuildAndPush") {
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
    scm('* * * * *')
  }

  wrappers {
      credentialsBinding {
          string('dockerloginpwd', 'dockerlogin')
      }
  }

  //Shell step
  steps {
		shell("docker login -u hoeghh -p \$dockerloginpwd")
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
  		trigger("deploy_to_staging") {
  			parameters {
                  gitRevision()
        }
  		}
  	}
  }
}


job("Simple-Pipeline/deploy_to_staging") {
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

  steps {
    shell("kubectl config use-context default-context")
  }
  steps {
    shell("cd simple-container-app/scripts/; ./deploy-staging.sh \$GIT_COMMIT")
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

job("Simple-Pipeline/simple_test") {
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

  steps {
    shell("./simple-container-app/scripts/test.sh \$GIT_COMMIT")
  }
  steps {
    //Automatisk
  	downstreamParameterized {
  		trigger("cleanup_staging") {
  			parameters {
                  gitRevision()
        }
  		}
  	}
  }
}

job("Simple-Pipeline/cleanup_staging") {
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

  steps {
    shell("./simple-container-app/scripts/clean_staging.sh \$GIT_COMMIT")
  }
  publishers {
    buildPipelineTrigger('Simple-Pipeline/redeploy_to_staging, Simple-Pipeline/deploy_to_production') {
        parameters {
            predefinedProp('GIT_COMMIT', '$GIT_COMMIT')
        }
    }
  }
}

job("Simple-Pipeline/redeploy_to_staging") {
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

  steps {
    shell("kubectl config use-context default-context")
  }
  steps {
    shell("cd simple-container-app/scripts/; ./deploy-staging.sh \$GIT_COMMIT")
  }

  publishers {
    buildPipelineTrigger('Simple-Pipeline/recleanup_staging') {
        parameters {
            predefinedProp('GIT_COMMIT', '$GIT_COMMIT')
        }
    }
  }
}

job("Simple-Pipeline/recleanup_staging") {
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

  steps {
    shell("./simple-container-app/scripts/clean_staging.sh \$GIT_COMMIT")
  }
}

job("Simple-Pipeline/deploy_to_production") {
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

  steps {
    shell("cd simple-container-app/scripts/; ./deploy-production.sh \$GIT_COMMIT")
  }

  publishers {
    buildPipelineTrigger('Simple-Pipeline/rollback-production') {
        parameters {
            predefinedProp('GIT_COMMIT', '$GIT_COMMIT')
        }
    }
  }
}

job("Simple-Pipeline/rollback-production") {
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

  steps {
    shell("./simple-container-app/scripts/prod_rollback.sh \$GIT_COMMIT")
  }
}
