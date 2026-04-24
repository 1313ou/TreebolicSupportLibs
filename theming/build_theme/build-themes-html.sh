#!/usr/bin/bash

source define_colors.sh
source define_data.sh

./convert_all_gpa.sh

all="$@"
if [ -z "$all"]; then
  all="${apps}"
  fi
echo ":$all"
for m in ${all}; do
  seedsDay=${m}-day.txt
  seedsNight=${m}-night.txt
  echo -e "${Y}${m}${Z}"

  ./build-theme-html.sh "$m" "$seedsDay" "$seedsNight"
done  
