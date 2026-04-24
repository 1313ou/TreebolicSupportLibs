#!/usr/bin/bash

source define_colors.sh
source define_data.sh

H=..

./convert_all_gpa.sh

all="$@"
if [ -z "$all" ]; then
  all="${themes}"
  fi
  
for theme in ${all}; do
  mode=${theme2mode[$theme]}
  res="$H/src/main/res"
  name="${theme}-${mode}"
  seeds="${theme}.txt"
  echo -e "${Y}${theme}${Z} ${mode} ${res} ${seeds}"

  ./build-theme.sh "$mode" "$seeds" "$res" "${theme}"
done  
