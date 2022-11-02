start mvn install:install-file -Dfile=hy.common.db.jar                              -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.db/pom.xml
start mvn install:install-file -Dfile=hy.common.db-sources.jar -Dclassifier=sources -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.db/pom.xml

start mvn deploy:deploy-file   -Dfile=hy.common.db.jar                              -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.db/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty
start mvn deploy:deploy-file   -Dfile=hy.common.db-sources.jar -Dclassifier=sources -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.db/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty
