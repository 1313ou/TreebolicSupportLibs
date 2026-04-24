#!/usr/bin/bash

declare -A theme2mode
export theme2mode=(
[default-day]=day
[default-night]=night
)

export themes="${!theme2mode[@]}"
