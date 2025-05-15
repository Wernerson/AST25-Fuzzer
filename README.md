# AST15 Fuzzer

> By Sebastian Brunner (Team Nimish Seb)

https://github.com/Wernerson/AST25-Fuzzer

# Quickstart

The image is published on docker.io as `wernerson/ast25-fuzzer`.

Run the following command to generate a create script and print 1000 queries to the stdout, one per line:

```bash
docker run wernerson/ast25-fuzzer test-db
```

Or run this to run a configuration file:

```bash
docker run wernerson/ast25-fuzzer test-db configs/3_26_0_diff.json
```

`3_26_0_diff.json` is simply a preset configuration as JSON file copied into the docker, there are more:

- `configs/3_26_0.json`: runs 100 queries against `sqlite3-3.26.0` and looks for interesting error codes (crashes)
- `configs/3_26_0_diff.json`: runs 100 queries against `sqlite3-3.26.0` and compares the result with `sqlite3-3.44.4`
- `configs/3_26_0_mut.json`: runs 100 queries against `./sqlite/sqlite3`
- `configs/3_26_0_mut_diff.json`: runs 100 queries against `./sqlite/sqlite3`, mutates them if they increase coverage and compares the result with `sqlite3-3.44.4`
- `configs/3_39_4.json`: runs 100 queries against `sqlite3-3.39.4` and looks for interesting error codes (crashes)
- `configs/3_39_4_diff.json`: runs 100 queries against `sqlite3-3.39.4` and compares the result with `./sqlite3-3.39.4/sqlite3`
- `configs/3_44_4.json`: runs 100 queries against `sqlite3-3.44.4` and looks for interesting error codes (crashes)

Note that the generated queries are intentionally kept at 100 to lower runtime, set to higher (e.g. 1000, 10000) if more thorough tests are desired.

For more info on available SQLite executables, see below.

Bugs by default are stored into `./bugs` directory.
You can use volumes to map your own configurations into the container and/or extract bugs.

For example:

```bash
docker run -v ./configs/:/home/test/configs/:ro -v ./bugs/:/home/test/bugs/ wernerson/ast25-fuzzer test-db configs/myconf.json
```
You can define that the `test.db` file should be created in a volume mapped location in the config (default `./test.db` and thus lost after completion). 

## Custom Configuration

You can define the following configurations in the JSON file:

- `seed`:the random seed to be used (good for reproducibility), if null, just uses random one, default: null
- `subject`: path to test subject SQLite executable, e.g. `sqlite3-3.26.0` or `./sqlite3-3.39.4/sqlite3`, if null, just print to console, default: null
- `oracle`: path to SQLite executable used as oracle, if null no oracle is used, default: null
- `coverage`: true/false, if true, `subject` must support code coverage, default: false 
- `queries`: number of queries to be executed, default 1000 
- `mutations`: number of mutations to be made per query, if null, no mutations are made, default: null
- `noTables`: number of tables to generate, default 20 
- `noColumns`: number of columns per table to generate, default 5
- `testDb`: path to test.db file, e.g. `./test.db`, if null, use in-memory db, default: null
- `archiveDir`: paht to dir where bugs should be archived, e.g. `./bugs`, if null, bugs are not archived, default: null 
- `generator`: generator config to be used, must be one of `v3_26_0`, `v3_39_4`, `v3_44_4`, default: `v3_26_0` 

**Note that since the tool is highly configurable, not all combinations were tested thoroughly!**

The preset configurations were tested and work!

# Build Docker Image from Scratch

Build the Docker image from scratch:

```bash
docker build . -t ast25-fuzzer
```

This image is based on the provided image `theosotr/sqlite3-test`
and installs all dependencies, clones the source code, builds the tool and copies
all configuration presets so they can be used easily.

## Docker Image Structure

In the home dir, there are the following directories:

- `sqlite/`: already in base image, SQLite v3.26.0 already compiled with gcov
- `sqlite3-3.39.4`: source code of SQLite v3.39.4 freshly compiled with gcov
- `sqlite3-3.44.4`: source code of SQLite v3.44.4 freshly compiled with gcov

These SQLite executables are available in the image:

- `sqlite3-3.26.0`: already in base image, standard SQLite v3.26.0
- `sqlite3-3.39.4`: already in base image, modified SQLite v3.39.4
- `sqlite3-3.44.4`: freshly compiled, standard SQLite v3.39.4
- `./sqlite/sqlite3`: already in base image, standard SQLite v3.26.0 with coverage
- `./sqlite3-3.39.4/sqlite3`: freshly compiled, standard SQLite v3.39.4 with coverage
- `./sqlite3-3.44.4/sqlite3`: freshly compiled, standard SQLite v3.44.4 with coverage
