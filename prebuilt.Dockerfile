FROM theosotr/sqlite3-test:latest
RUN sudo apt install wget -y && \
    wget https://github.com/sqlite/sqlite/archive/refs/tags/version-3.44.4.tar.gz && \
    tar xzf version-3.44.4.tar.gz && \
    mkdir sqlite3-3.44.4 && \
    cd sqlite3-3.44.4 && \
    ../sqlite-version-3.44.4/configure --enable-gcov --enable-all --enable-debug CFLAGS='-O0 -g -fprofile-arcs -ftest-coverage' && \
    make sqlite3 && \
    sudo cp ./sqlite3 /usr/bin/sqlite3-3.44.4

COPY ./build/native/nativeCompile/AST-fuzzer /usr/bin/test-db