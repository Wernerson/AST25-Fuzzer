FROM localhost/latest
# RUN sudo apt install wget unzip && \
#    wget https://sqlite.org/2025/sqlite-tools-linux-x64-3490100.zip && \
#    unzip sqlite-tools-linux-x64-3490100.zip && \

COPY ./build/native/nativeCompile/AST-fuzzer /home/test/fuzzer