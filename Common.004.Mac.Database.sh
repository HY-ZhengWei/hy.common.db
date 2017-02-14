#!/bin/sh

cd ./bin

jar cvfm hy.common.db.jar MANIFEST.MF LICENSE org

cp hy.common.db.jar ..
rm hy.common.db.jar
cd ..

