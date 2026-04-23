#!/usr/bin/bash

source define_colors.sh
source define_data.sh

all="$@"
if [ -z "$all"]; then
  all="${themes}"
  fi

for theme in ${all}; do
  mode=${theme2mode[$theme]}
  name="${theme}-${mode}"
  seeds="${theme}.txt"
  echo -e "${Y}${theme}${Z} ${mode} ${res} ${seeds}"

  ./build-theme-html.sh "$mode" "$seeds" "$theme"
done  
