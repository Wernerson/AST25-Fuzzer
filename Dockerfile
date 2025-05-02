FROM theosotr/sqlite3-test:latest
RUN sudo apt install wget -y && \
    wget https://github.com/sqlite/sqlite/archive/refs/tags/version-3.44.4.tar.gz && \
    tar xzf version-3.44.4.tar.gz && \
    mkdir bld && \
    cd bld && \
    ../sqlite-version-3.44.4/configure && \
    make && \
    sudo cp ./sqlite3 /usr/bin/sqlite3-3.44.4

COPY ./build/native/nativeCompile/AST-fuzzer /home/test/fuzzer