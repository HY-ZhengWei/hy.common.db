start mvn install:install-file -Dfile=hy.common.db.jar                              -DpomFile=./src/META-INF/maven/org/hy/common/db/pom.xml
start mvn install:install-file -Dfile=hy.common.db-sources.jar -Dclassifier=sources -DpomFile=./src/META-INF/maven/org/hy/common/db/pom.xml

start mvn deploy:deploy-file   -Dfile=hy.common.db.jar                              -DpomFile=./src/META-INF/maven/org/hy/common/db/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty
start mvn deploy:deploy-file   -Dfile=hy.common.db-sources.jar -Dclassifier=sources -DpomFile=./src/META-INF/maven/org/hy/common/db/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty
