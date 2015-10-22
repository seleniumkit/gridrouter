def project = 'gridrouter';
def repo = 'seleniumkit/gridrouter'

def buildWarJob = mavenJob("${project}_build-war")
def e2eTestsJob = job("${project}_e2e-tests")
def sonarJob = mavenJob("${project}_sonar")
def sonarIncrJob = mavenJob("${project}_sonar-incr")
def deployJob = mavenJob("${project}_deploy")

def pullRequestJob = multiJob("${project}_pull-reqest_flow")
def snapshotJob = multiJob("${project}_snapshot_flow")
def releaseJob = mavenJob("${project}_release_flow")

buildWarJob.with {

    label('maven')
    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
                refspec('${GIT_REFSPEC}')
            }
            branch('${GIT_COMMIT}')
            localBranch('master')
        }
    }
    goals('clean package')

    publishers {
        archiveArtifacts('proxy/target/*.war')
    }
}

e2eTestsJob.with {

    label('e2e')
    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
                refspec('${GIT_REFSPEC}')
            }
            branch('${GIT_COMMIT}')
            localBranch('master')
        }
    }

    steps {
        copyArtifacts(buildWarJob.name) {
            includePatterns('proxy/target/*.war')
            buildSelector {
                buildNumber('${WAR_BUILD_NUMBER}')
            }
        }
        shell('ansible-playbook -e "workspace=/tmp/e2e/${JOB_NAME}/${BUILD_NUMBER}" testing/start.yml')
        shell('ansible-playbook -e "workspace=/tmp/e2e/${JOB_NAME}/${BUILD_NUMBER}" testing/test.yml')
        shell('ansible-playbook -e "workspace=/tmp/e2e/${JOB_NAME}/${BUILD_NUMBER}" testing/stop.yml')
    }

    publishers {
        archiveJunit('testing/target/surefire-reports/*.xml')
    }
}

sonarJob.with {

    label('maven')
    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
                refspec('${GIT_REFSPEC}')
            }
            branch('${GIT_COMMIT}')
            localBranch('master')
        }
    }

    publishers {
        sonar()
    }
}

sonarIncrJob.with {

    label('maven')
    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
                refspec('${GIT_REFSPEC}')
            }
            branch('${GIT_COMMIT}')
            localBranch('master')
        }
    }

    configure {
        it / 'publishers' / 'hudson.plugins.sonar.SonarPublisher' {
            jdk('(Inherit From Job)')
            branch()
            language()
            jobAdditionalProperties('-Dsonar.analysis.mode=incremental -Dsonar.github.pullRequest=${ghprbPullId} -Dsonar.github.repository=' + repo)
            settings(class: 'jenkins.mvn.DefaultSettingsProvider')
            globalSettings(class: 'jenkins.mvn.DefaultGlobalSettingsProvider')
            usePrivateRepository(false)
        }
    }
}

deployJob.with {

    label('maven')

    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
                refspec('${GIT_REFSPEC}')
            }
            branch('${GIT_COMMIT}')
            localBranch('master')
        }
    }

    goals('clean deploy')

}

pullRequestJob.with {

    label('master')
    displayName('Grid Router Pull Requests Flow')

    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
                refspec('+refs/pull/*:refs/remotes/origin/pr/*')
            }
            branch('${sha1}')
        }
    }

    triggers {
        pullRequest {
            orgWhitelist(['seleniumkit'])
            permitAll()
            useGitHubHooks()
        }
    }

    steps {
        phase('Build war file') {
            job(sonarIncrJob.name) {
                prop('GIT_REFSPEC', '+refs/pull/*:refs/remotes/origin/pr/*');
                prop('GIT_COMMIT', '\${sha1}');
            }
            job(buildWarJob.name) {
                prop('GIT_REFSPEC', '+refs/pull/*:refs/remotes/origin/pr/*');
                prop('GIT_COMMIT', '\${sha1}');
            }
        }
        phase('Run e2e tests', 'UNSTABLE') {
            job(e2eTestsJob.name) {
                prop('WAR_BUILD_NUMBER', '\${' + buildWarJob.name.toUpperCase().replace("-", "_") + '_BUILD_NUMBER}');
                prop('GIT_REFSPEC', '+refs/pull/*:refs/remotes/origin/pr/*');
                prop('GIT_COMMIT', '\${sha1}');
            }
        }
    }

    publishers {
        aggregateDownstreamTestResults()
    }
}

snapshotJob.with {

    label('default')
    displayName('Grid Router Snapshot Flow')

    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
            }
            localBranch('master')
            branch('master')
        }
    }
    triggers {
        githubPush()
    }

    steps {
        phase('Build war file') {
            job(buildWarJob.name) {
                prop('GIT_COMMIT', '\${GIT_COMMIT}');
                prop('GIT_REFSPEC', '');
            }
        }
        phase('Run e2e tests') {
            job(e2eTestsJob.name) {
                prop('WAR_BUILD_NUMBER', '\${' + buildWarJob.name.toUpperCase().replace("-", "_") + '_BUILD_NUMBER}');
                prop('GIT_COMMIT', '\${GIT_COMMIT}');
                prop('GIT_REFSPEC', '');
            }
            job(sonarJob.name) {
                prop('GIT_COMMIT', '\${GIT_COMMIT}');
                prop('GIT_REFSPEC', '');
            }
        }
        phase('Deploy war') {
            job(deployJob.name) {
                prop('GIT_COMMIT', '\${GIT_COMMIT}');
                prop('GIT_REFSPEC', '');
            }
        }
    }

    publishers {
        aggregateDownstreamTestResults()
    }
}

releaseJob.with {

    label('maven')
    displayName('Grid Router Release Flow')

    scm {
        git {
            remote {
                github(repo, 'https', 'github.com')
            }
            localBranch('master')
            branch('master')
        }
    }

    goals('clean deploy')

    wrappers {
        mavenRelease {
            releaseGoals('release:clean release:prepare release:perform')
            dryRunGoals('-DdryRun=true release:prepare')
            numberOfReleaseBuildsToKeep(10)
        }
    }
}

listView(project) {
    jobs {
        regex("${project}_.*_flow")
    }

    columns {
        status()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}