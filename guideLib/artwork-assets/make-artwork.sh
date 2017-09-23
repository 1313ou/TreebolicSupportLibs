#!/bin/bash

source "../../../make-artwork-lib.sh"

help_list="menu*.svg search*.svg tip*.svg toolbar*.svg"
splash_list="splash*.svg"

make_assets "${help_list}" 48
make_assets "${splash_list}" 150

