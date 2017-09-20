#!/bin/bash

source "../../../make-artwork-lib.sh"

button_list="ic_download.svg"
icon_list="ic_download_*.svg"

make_res "${button_list}" 64
make_res "${icon_list}" 48

