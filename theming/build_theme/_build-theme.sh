#!/usr/bin/bash

source define_colors.sh

m="$1"
if [ -z "$m" ]; then
  echo -e "${R}Mode needed${Z}"      
  exit 1
  fi

if [ ! -z "$2" ]; then
  seeds=$(readlink -f "$2")
  seeds="-f $seeds"
else
        echo -e "${R}Seeds needed${Z}"  
        exit 2
  fi

D="./output"
if [ ! -z "$3" ]; then
  D="$3"
  fi

theme="mytheme"
if [ ! -z "$4" ]; then
  theme="$4"
  fi

# arg checking

case "$m" in
        day)   
                mode=""
                ;;
        night) 
                mode="-d"
                ;;
        *) 
                echo -e "${R}Illegal mode${Z}"
                exit 3
                ;;
esac

if [ ! -e "${D}" ]; then
        echo -e "${R}dest   ${D}${Z}"
        exit 4
fi

name="My${theme^}BaseTheme"

echo -e "${M}mode  ${mode}$Z"
echo -e "${M}seeds ${seeds}$Z"
echo -e "${M}dest  ${D}$Z"
echo -e "${M}theme ${theme}$Z"
echo -e "${M}name  ${name}$Z"

# run

mkdir -p "$D/values"

./run.sh -o attrs > "$D/values/attrs_custom.xml"

./run.sh -o theme_with_colors -n "$name" ${mode} $seeds  \
| sed 's/Theme.Material3.\(Dark\|Light\).NoActionBar/MyTreebolicTheme/g' \
| sed 's/<!-- \(.*\) -->/\1/g' \
> "$D/values/theme_${theme}_base.xml"
