pushd
cd ..
mvn clean package -PLinux-x86_64
popd

ant -buildfile .\ant_build_linux_gtk_x86_64.xml