pushd
cd ..
mvn clean package -PWindows-x86_64
popd

ant -buildfile .\ant_build_win_x86_64.xml