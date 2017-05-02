#!/bin/bash

thisdir="`dirname $(readlink -m $0)`"
thisdir="$(readlink -m ${thisdir})"
dirres=../src/main/res
dirassets=../src/main/assets
dirapp=..

declare -A button_res
button_res=([mdpi]=48 [hdpi]=72 [xhdpi]=96 [xxhdpi]=144 [xxxhdpi]=192)
button_list="ic_download.svg"

declare -A icon_res
icon_res=([mdpi]=64 [hdpi]=96 [xhdpi]=128 [xxhdpi]=192 [xxxhdpi]=256)
icon_list="ic_download_*.svg"

for svg in ${button_list}; do
	for r in ${!button_res[@]}; do 
		d="${dirres}/drawable-${r}"
		mkdir -p ${d}
		png="${svg%.svg}.png"
		echo "${svg}.svg -> ${d}/${png}.png @ resolution ${button_res[$r]}"
		inkscape ${svg} --export-png=${d}/${png} -h${button_res[$r]} > /dev/null 2> /dev/null
	done
done

for svg in ${icon_list}; do
	for r in ${!icon_res[@]}; do 
		d="${dirres}/drawable-${r}"
		mkdir -p ${d}
		png="${svg%.svg}.png"
		echo "${svg}.svg -> ${d}/${png}.png @ resolution ${icon_res[$r]}"
		inkscape ${svg} --export-png=${d}/${png} -h${icon_res[$r]} > /dev/null 2> /dev/null
	done
done

