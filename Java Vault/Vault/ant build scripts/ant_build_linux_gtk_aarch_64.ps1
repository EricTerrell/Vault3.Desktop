pushd
cd ..
mvn clean package -PLinux-aarch_64
popd

ant -buildfile .\ant_build_linux_gtk_aarch_64.xml