pushd
cd ..
mvn clean package -DskipTests=true -PWindows-aarch_64
popd

ant -buildfile .\ant_build_win_aarch_64.xml