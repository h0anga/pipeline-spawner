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

deploy: decrypt-secrets
	$(call pipeline_deploy,$(GPG_KEY_NAME),$(GO_PIPELINE_LABEL),$(TARGET_ENV),package-chart helm-upgrade helm-list)

decrypt-secrets:
	$(call decrypt_secrets,$(APP_NAME)/_$(CLUSTER)-values-secrets.enc.yaml,$(APP_NAME)/_$(CLUSTER)-values-secrets.dec.yaml)
	ls -l $(APP_NAME)/_$(CLUSTER)-values-secrets.dec.yaml

helm-upgrade:
	helm upgrade --install --namespace $(NAMESPACE) --wait --timeout=300 $(APP_NAME) ./$(APP_NAME)-${VERSION}.tgz --values $(APP_NAME)/_$(TARGET_ENV)-values.yaml -f $(APP_NAME)/_$(CLUSTER)-values-secrets.dec.yaml

package-chart:
	helm package --save=false --version $(VERSION) -d ./ $(APP_NAME)

helm-list:
	helm list

define pipeline_deploy
   $(call curl,sh -c "cd /work && curl --insecure -o $(1)-private.asc $(DEPLOY_PRIVATE_KEY_URL)")
   $(call curl,sh -c "cd /work && curl --insecure -o admin.enc $(DEPLOY_KUBE_CONFIG_URL)")
   ls -l $(1)-private.asc
   ls -l admin.enc
   docker run \
   --name $(APP_NAME)-$(VERSION)-PIPELINE_DEPLOY \
   --rm \
   -e GPG_KEY_NAME=$(1) \
   -e GO_PIPELINE_LABEL=$(2) \
   -e TARGET_ENV=$(3) \
   -v `pwd`:/deploy:rw \
   -v `pwd`/$(1)-private.asc:/secrets/private-key.asc:ro \
   -v `pwd`/admin.enc:/secrets/admin.enc:ro \
   repo.sns.sky.com:8186/dost/pipeline-deploy:$(PIPELINE_DEPLOY_IMAGE_VERSION) \
   make $(4)
endef