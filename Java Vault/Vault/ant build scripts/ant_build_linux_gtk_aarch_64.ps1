pushd
cd ..

# Do not run tests - tests will attempt to load platform-specific code which will fail in Windows
mvn clean package -DskipTests=true -PLinux-aarch_64

popd

ant -buildfile .\ant_build_linux_gtk_aarch_64.xml