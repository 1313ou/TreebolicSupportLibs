#!/usr/bin/bash

echo $1 $2 $3 $4

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

theme="mytheme"
if [ ! -z "$3" ]; then
  theme="$3"
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


name="My${theme^}"

echo -e "${M}mode  ${m}$Z"
echo -e "${M}seeds ${seeds}$Z"
echo -e "${M}theme ${theme}$Z"
echo -e "${M}name  ${name}$Z"

# run

./run.sh -o theme_html -n "$name" ${mode} $seeds -x > "html/theme-${theme}.html"
