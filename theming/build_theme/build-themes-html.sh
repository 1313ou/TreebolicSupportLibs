#!/usr/bin/bash

source define_colors.sh
source define_data.sh

./convert_all_gpa.sh

all="$@"
if [ -z "$all"]; then
  all="${themes}"
  fi
for t in ${all}; do
  res=$H/src/main/res
  seedsDay="${t}-day.txt"
  seedsNight="${t}-night.txt"
  echo -e "${Y}${t}${Z} $seedsDay $seedsNight"

  ./build-theme-html.sh "$t" "$seedsDay" "$seedsNight"
done  
