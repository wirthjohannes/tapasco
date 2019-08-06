#!/bin/bash
#
# Copyright (C) 2014 Jens Korinth, TU Darmstadt
#
# This file is part of Tapasco (TPC).
#
# Tapasco is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Tapasco is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with Tapasco.  If not, see <http://www.gnu.org/licenses/>.
#
# @file		hls_patch_supported_families.sh
# @brief	Patches the run_ippack.tcl generated by Vivado HLS to include
#               all Zynq, Virtex7, Artix7 and Kintex7 FPGAs in the supported
#		device families for the IP cores.
# @authors	J. Korinth (jk@esa.cs.tu-darmstadt.de
#
sed -i 's/set_property supported_families .*$/set_property supported_families [list zynq Pre-Production virtex7 Pre-Production kintex7 Pre-Production artix7 Pre-Production zynquplus Pre-Production virtex7 Pre-Production qvirtex7 Pre-Production kintex7 Pre-Production kintex7l Pre-Production qkintex7 Pre-Production qkintex7l Pre-Production artix7 Pre-Production artix7l Pre-Production aartix7 Pre-Production qartix7 Pre-Production zynq Pre-Production qzynq Pre-Production azynq Pre-Production spartan7 Pre-Production virtexu Pre-Production virtexuplus Pre-Production kintexuplus Pre-Production zynquplus Pre-Production kintexu Pre-Production] $core/g' run_ippack.tcl
sed -i 's/\(set DisplayName.*\)$/\1\nset IPName $DisplayName/g' run_ippack.tcl