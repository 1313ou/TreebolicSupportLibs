#!/usr/bin/bash

source define_colors.sh
source define_data.sh

H=..

./convert_all_gpa.sh

all="$@"
if [ -z "$all"]; then
  all="${themes}"
  fi
for t in ${all}; do
  res=$H/src/main/res
  seedsDay="${t}-day.txt"
  seedsNight="${t}-night.txt"
  echo -e "${bY}${K}${t}${Z} $seedsDay $seedsNight"

  ./build-theme.sh "$res" "$seedsDay" "$seedsNight"
done  
