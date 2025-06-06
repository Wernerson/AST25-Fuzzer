FROM theosotr/sqlite3-test:latest

# Install prerequisites & dependencies
RUN sudo apt install wget -y

# Download and make sqlite v3.44.4 (the newest one) to use as oracle
RUN wget https://github.com/sqlite/sqlite/archive/refs/tags/version-3.44.4.tar.gz && \
    tar xzf version-3.44.4.tar.gz && \
    mkdir sqlite3-3.44.4 && \
    cd sqlite3-3.44.4 && \
    ../sqlite-version-3.44.4/configure --enable-gcov --enable-all --enable-debug CFLAGS='-O0 -g -fprofile-arcs -ftest-coverage' && \
    make sqlite3 && \
    rm -rf ../sqlite-version-3.44.4 && \
    rm -rf ../version-3.44.4.tar.gz && \
    sudo cp ./sqlite3 /usr/bin/sqlite3-3.44.4

# Download and make sqlite v3.39.4 (same as buggy one) to use as oracle
RUN wget https://github.com/sqlite/sqlite/archive/refs/tags/version-3.39.4.tar.gz && \
    tar xzf version-3.39.4.tar.gz && \
    mkdir sqlite3-3.39.4 && \
    cd sqlite3-3.39.4 && \
    ../sqlite-version-3.39.4/configure --enable-gcov --enable-all --enable-debug CFLAGS='-O0 -g -fprofile-arcs -ftest-coverage' && \
    make sqlite3 && \
    rm -rf ../sqlite-version-3.39.4 && \
    rm -rf ../version-3.39.4.tar.gz

COPY ./build/native/nativeCompile/AST-fuzzer /usr/bin/test-db
COPY ./configs/ /home/test/configs/