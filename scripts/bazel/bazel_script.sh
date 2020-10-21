set -ex

local_repo=${HOME}/.m2/repository
BAZEL_ARGUMENTS=
if [ "${PLATFORM}" == "jenkins" ]
then
  GCP="--google_credentials=${GCP_KEY}"
  bazelrc=--bazelrc=bazelrc.remote
  local_repo=/root/.m2/repository
  if [ ! -z "${DISTRIBUTE_TESTING_WORKER}" ]
  then
    BAZEL_ARGUMENTS="${BAZEL_ARGUMENTS} --test_env=DISTRIBUTE_TESTING_WORKER=${DISTRIBUTE_TESTING_WORKER}"
    BAZEL_ARGUMENTS="${BAZEL_ARGUMENTS} --test_env=DISTRIBUTE_TESTING_WORKERS=${DISTRIBUTE_TESTING_WORKERS}"
  fi
fi

BAZEL_DIRS=${HOME}/.bazel-dirs
BAZEL_ARGUMENTS="${BAZEL_ARGUMENTS} --experimental_convenience_symlinks=normal --symlink_prefix=${BAZEL_DIRS}/"

if [[ ! -z "${OVERRIDE_LOCAL_M2}" ]]; then
  local_repo=${OVERRIDE_LOCAL_M2}
fi

if [ "${STEP}" == "dockerization" ]
then
  GCP=""
fi

if [ "${RUN_BAZEL_TESTS}" == "true" ]
then
  bazel ${bazelrc} test --keep_going ${GCP} ${BAZEL_ARGUMENTS} -- //... -//product/... -//commons/... -//71-rest/... -//260-delegate/...
  # 71-rest and 260-delegate modules are excluded.
fi

build_bazel_module() {
  module=$1
  bazel ${bazelrc} build //${module}:module ${GCP} ${BAZEL_ARGUMENTS}

  mvn -B install:install-file \
   -Dfile=${BAZEL_DIRS}/bin/${module}/libmodule.jar \
   -DgroupId=software.wings \
   -DartifactId=${module} \
   -Dversion=0.0.1-SNAPSHOT \
   -Dpackaging=jar \
   -DgeneratePom=true \
   -DpomFile=${module}/pom.xml \
   -DlocalRepositoryPath=${local_repo}
}

build_bazel_tests() {
  module=$1

  bazel ${bazelrc} build //${module}:supporter-test ${GCP} ${BAZEL_ARGUMENTS}

  mvn -B install:install-file \
   -Dfile=${BAZEL_DIRS}/bin/${module}/libsupporter-test.jar \
   -DgroupId=software.wings \
   -DartifactId=${module} \
   -Dversion=0.0.1-SNAPSHOT \
   -Dclassifier=tests \
   -Dpackaging=jar \
   -DgeneratePom=true \
   -DpomFile=${module}/pom.xml \
   -DlocalRepositoryPath=${local_repo}
}

build_java_proto_module() {
  module=$1
  modulePath=$module/src/main/proto

  build_proto_module $module $modulePath
}

build_proto_module() {
  module=$1
  modulePath=$2
  bazel ${bazelrc} build //${modulePath}:all ${GCP} ${BAZEL_ARGUMENTS} --experimental_remote_download_outputs=all

  bazel_library=`echo ${module} | tr '-' '_'`

  mvn -B install:install-file \
   -Dfile=${BAZEL_DIRS}/bin/${modulePath}/lib${bazel_library}_java_proto.jar \
   -DgroupId=software.wings \
   -DartifactId=${module}-proto \
   -Dversion=0.0.1-SNAPSHOT \
   -Dpackaging=jar \
   -DgeneratePom=true \
   -DlocalRepositoryPath=${local_repo} \
   -f scripts/bazel/proto_pom.xml
}

build_bazel_module 990-commons-test
build_bazel_module 12-commons
build_bazel_module 13-ng-commons
build_bazel_module 13-grpc-api
build_bazel_module 14-api-services-beans
build_bazel_module 15-api-services
build_bazel_module 16-expression-service
build_bazel_module 19-delegate-tasks-beans
build_bazel_module 20-delegate-beans
build_bazel_module 20-delegate-tasks
build_bazel_module 20-ng-core-beans
build_bazel_module 21-delegate-agent-beans
build_bazel_module 21-persistence
build_bazel_tests 21-persistence
build_bazel_module 22-delegate-service-beans
build_bazel_module 22-ng-core
build_bazel_module 22-ng-core-clients
build_bazel_module 22-ng-delegate-service-beans
build_bazel_module 22-ng-project-n-orgs
build_bazel_module 22-rbac-core
build_bazel_module 22-secret-manager-client
build_bazel_module 22-timeout-engine
build_bazel_module 22-wait-engine
build_bazel_module 23-delegate-service-driver
build_bazel_module 23-sm-core
build_bazel_module 24-common-entities
build_bazel_module 27-orchestration-persistence
build_bazel_module 28-pms-beans
build_bazel_module 29-orchestration-beans
build_bazel_module 31-orchestration
build_bazel_module 32-orchestration-steps
build_bazel_module 33-orchestration-visualization
build_bazel_module 34-walktree-visitor
build_bazel_module 35-yaml-beans
build_bazel_module 38-execution-plan
build_bazel_module 39-ng-pipeline-commons
build_bazel_module 47-pipeline-service
build_bazel_module 50-delegate-task-grpc-service
build_bazel_module 57-command-library-common
build_bazel_module 64-events-framework
build_bazel_module 69-connector-nextgen
build_bazel_module 70-cv-nextgen-commons
build_bazel_module 70-delegate-agent
build_bazel_module 70-delegate-service
build_bazel_module 450-ce-views
build_bazel_module 490-ce-commons
build_java_proto_module 13-grpc-api
build_java_proto_module 19-delegate-tasks-beans
build_java_proto_module 20-delegate-beans
build_java_proto_module 21-delegate-agent-beans
build_java_proto_module 22-delegate-service-beans
build_java_proto_module 50-delegate-task-grpc-service proto

build_proto_module 16-expression-service 16-expression-service/src/main/proto/io/harness/expression/service
build_proto_module ciscm product/ci/scm/proto
build_proto_module ciengine product/ci/engine/proto
