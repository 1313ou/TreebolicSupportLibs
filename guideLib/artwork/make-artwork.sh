#!/bin/bash

source "../../../make-artwork-lib.sh"

tips_list="ic_tip_*.svg"
tips_close_list="ic_tip_close.svg"
about_list="ic_about_logo.svg"

make_res "${tips_list}" 48
make_res "${tips_close_list}" 32
make_res "${about_list}" 32

