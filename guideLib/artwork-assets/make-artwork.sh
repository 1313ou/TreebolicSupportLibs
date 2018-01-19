#!/bin/bash

source "../../../make-artwork-lib.sh"

help="menu*.svg search*.svg tip*.svg toolbar*.svg"
splash="splash*.svg"

make_assets "${help}" 48
make_assets "${splash}" 150

