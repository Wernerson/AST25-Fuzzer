
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
sudo podman run -it alpine/sqlite test.db
```