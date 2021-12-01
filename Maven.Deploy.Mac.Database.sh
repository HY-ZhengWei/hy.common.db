#!/bin/sh

mvn deploy:deploy-file -Dfile=hy.common.db.jar                              -DpomFile=./src/META-INF/maven/org/hy/common/db/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:1481/repository/thirdparty
mvn deploy:deploy-file -Dfile=hy.common.db-sources.jar -Dclassifier=sources -DpomFile=./src/META-INF/maven/org/hy/common/db/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:1481/repository/thirdparty
