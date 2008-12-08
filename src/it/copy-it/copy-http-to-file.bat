REM work with full path on target's file protocol
REM

mvn -e org.codehaus.mojo:wagon-maven-plugin:1.0-beta-1:copy -Dwagon.source=http://people.apache.org/~olamy/staging-repo -Dwagon.target=file:///dev/mojo/sandbox/wagon-maven-plugin/src/it/copy-it/target/maven-repo -Djava.io.tmpdir=target