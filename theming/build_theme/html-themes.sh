#!/usr/bin/bash

source define_colors.sh
source define_data.sh

H=..

./convert_all_gpa.sh

all="$@"
if [ -z "$all" ]; then
  all="${themes}"
  fi
for t in ${all}; do
  res=$H/src/main/res
  seedsDay="${t}-day.txt"
  seedsNight="${t}-night.txt"
  echo -e "${Y}${t}${Z} $seedsDay $seedsNight"

  echo -e "${B}day ${K} $seedsDay${Z}"
  values=$(./run.sh -o map -f "$seedsDay")
  ./run.sh -o colors1  -f "$seedsDay"
  ./run.sh -o html $values > html/${m}-day.html

  echo -e "${B}night ${K} $seedsNight${Z}"
  values=$(./run.sh -o map -d -f "$seedsNight")
  ./run.sh -o colors1 -d -f "$seedsNight"
  ./run.sh -o html $values > html/${m}-night.html
done
