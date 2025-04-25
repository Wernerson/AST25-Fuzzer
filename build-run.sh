export JAVA_HOME=../.jdks/graalvm-jdk-23.0.2 && \
./gradlew nativeCompile && \
sudo podman build . -t fuzzer && \
sudo podman run -v .docker/:/home/test/.docker -it fuzzer:latest