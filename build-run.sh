export JAVA_HOME=../.jdks/graalvm-jdk-23.0.2 && \
./gradlew nativeCompile && \
podman build . -t fuzzer && \
podman run -v .docker/:/home/test/.docker -it fuzzer:latest