

cd .\bin


rd /s/q .\org\hy\common\configfile\junit
rd /s/q .\org\hy\common\db\junit

jar cvfm hy.common.db.jar MANIFEST.MF META-INF org

copy hy.common.db.jar ..
del /q hy.common.db.jar
cd ..





cd .\src
jar cvfm hy.common.db-sources.jar MANIFEST.MF META-INF org 
copy hy.common.db-sources.jar ..
del /q hy.common.db-sources.jar
cd ..
