APP_NAME=pipeline-spawner

GO_PIPELINE_LABEL?=0.0.1-dev
PIPELINE_BUILD_IMAGE_VERSION=0.1.2
PIPELINE_DEPLOY_IMAGE_VERSION=0.1.6
BUILD_DIR?=`dirname \`pwd\``
BUILD_DIR_VOLUME=build-dir
TARGET_ENV?=unknown
GPG_KEY_NAME?=unknown
GIT_ACCESS_TOKEN?=unknown
CLUSTER=`echo $(TARGET_ENV) | cut -d'-' -f1`

# Note: The ARTIFACTORY_PASSWORD should be passed in as a pipeline environment variable
ARTIFACTORY_USERNAME?=docker-build
ARTIFACTORY_PASSWORD?=unknown
ARTIFACTORY_URL?=http://repo.sns.sky.com:8081/artifactory
DOCKER_REGISTRY_URL?=repo.sns.sky.com:8185/ukiss
DEPLOY_PRIVATE_KEY_URL=https://git.sns.sky.com/dostadmin/dost-private-keys/raw/master/$(GPG_KEY_NAME)-private.asc?private_token=$(GIT_ACCESS_TOKEN)
DEPLOY_KUBE_CONFIG_URL=https://git.sns.sky.com/dost/vdc-kubeconfig/raw/master/$(TARGET_ENV)/admin.gpg?private_token=$(GIT_ACCESS_TOKEN)

# Construct the image tag.
VERSION=$(GO_PIPELINE_LABEL)

# Construct docker image name.
IMAGE = $(DOCKER_REGISTRY_URL)/$(APP_NAME)

NAMESPACE=mobile

FRONTEND=$(BUILD_DIR)/static/content/target/frontend-fastopt.js
SET_VERSION='set ThisBuild/version := "$(VERSION)"'

all: build

push: build
	cd $(BUILD_DIR) && sbt $(SET_VERSION) docker

build: $(BACKEND)

$(BACKEND):
	cd $(BUILD_DIR) && sbt $(SET_VERSION) fastOptJS backend/test backend/assembly

clean:
	cd $(BUILD_DIR) && sbt clean

.PHONY: clean push