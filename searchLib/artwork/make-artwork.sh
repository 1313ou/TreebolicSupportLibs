#!/bin/bash

source "../../../make-artwork-lib.sh"

bar="ic_action_*.svg"
search="ic_search_*.svg"

make_res "${bar}" 24
make_res "${search}" 48

