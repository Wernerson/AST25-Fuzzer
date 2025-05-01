
```bash
sudo podman load < ./sqlite3-test.tar
```

```bash
sudo podman run -v .sudo podman:/home/test -it localhost/latest
```

```bash
sudo podman build . -t fuzzer
sudo podman run -it fuzzer
```

```bash
sudo podman run -v .docker/:/tmp -it alpine/sqlite /tmp/test.db
```

```bash
export JAVA_HOME=../.jdks/graalvm-jdk-23.0.2
./gradlew nativeCompile
sudo podman build . -t fuzzer
sudo podman run -v .docker/:/home/test/.docker -it fuzzer:latest
```