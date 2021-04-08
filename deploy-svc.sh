#!/usr/local/bin/bash

bash --version

export OMS_ROOT=`pwd`

SVC=$1

declare -A containers
containers=( ["GatewaySvc"]="gateway-svc" ["AuthSvc"]="auth-svc" ["ProductSvc"]="product-svc" [OrderSvc]="order-svc" ["InventorySvc"]="inventory-svc" ["UserProfileSvc"]="user-profile-svc" ["AdminSvc"]="admin-svc" )

cd $OMS_ROOT/services/$SVC
mvn clean package
cp ./target/${SVC}.war ../target

cd $OMS_ROOT/staging/bin
./pull-artifacts.sh

cd $OMS_ROOT/docker/bin
./pull-artifacts.sh

cd $OMS_ROOT/docker
docker-compose build rest
docker-compose stop ${containers["$SVC"]}
docker-compose rm -f ${containers["$SVC"]}
docker-compose up -d ${containers["$SVC"]}
docker-compose logs -f ${containers["$SVC"]}
