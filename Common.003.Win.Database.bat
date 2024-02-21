

del /Q hy.common.db.jar
del /Q hy.common.db-sources.jar


call mvn clean package
cd .\target\classes


rd /s/q .\org\hy\common\configfile\junit
rd /s/q .\org\hy\common\db\junit

jar cvfm hy.common.db.jar META-INF/MANIFEST.MF META-INF org

copy hy.common.db.jar ..\..
del /q hy.common.db.jar
cd ..\..





cd .\src\main\java
xcopy /S ..\resources\* .
jar cvfm hy.common.db-sources.jar META-INF\MANIFEST.MF META-INF org 
copy hy.common.db-sources.jar ..\..\..
del /Q hy.common.db-sources.jar
rd /s/q META-INF
cd ..\..\..

pause