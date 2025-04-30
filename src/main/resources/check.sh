#!/usr/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

diff <(${TEST_PATH} test.db < reduced_test.sql) <(${ORACLE_PATH} test.db < reduced_test.sql)

if [ $? -eq 0 ]; then
  echo -e "${RED}No diff${NC}"
  exit 0
else
  echo -e "${GREEN}Diff!${NC}"
  exit 1
fi