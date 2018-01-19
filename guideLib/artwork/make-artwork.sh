#!/bin/bash

source "../../../make-artwork-lib.sh"

tips="ic_tip_*.svg"
tips_close="ic_tip_close.svg"
about="ic_about_logo.svg"

make_res "${tips}" 48
make_res "${tips_close}" 32
make_res "${about}" 32

