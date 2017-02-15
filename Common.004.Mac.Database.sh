#!/bin/sh

cd ./bin

rm -R ./org/hy/common/configfile/junit
rm -R ./org/hy/common/db/junit


jar cvfm hy.common.db.jar MANIFEST.MF LICENSE org

cp hy.common.db.jar ..
rm hy.common.db.jar
cd ..

