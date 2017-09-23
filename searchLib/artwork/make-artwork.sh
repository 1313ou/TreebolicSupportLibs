#!/bin/bash

source "../../../make-artwork-lib.sh"

bar_list="ic_action_*.svg"
search_list="ic_search_*.svg"

make_res "${bar_list}" 24
make_res "${search_list}" 48

