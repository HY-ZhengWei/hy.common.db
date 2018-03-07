#!/bin/sh

cd ./bin


rm -R ./org/hy/common/configfile/junit
rm -R ./org/hy/common/db/junit

jar cvfm hy.common.db.jar MANIFEST.MF META-INF org

cp hy.common.db.jar ..
rm hy.common.db.jar
cd ..





cd ./src
jar cvfm hy.common.db-sources.jar MANIFEST.MF META-INF org 
cp hy.common.db-sources.jar ..
rm hy.common.db-sources.jar
cd ..
