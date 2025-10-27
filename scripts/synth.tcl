# Simple Vivado Tcl script (hardcoded values) for Nexys 4 DDR
#
# Run with:
#   vivado -mode batch -source simple_synth.tcl

# ------ Arguments -----------
set SOURCE_FILE "./ChiselCheckers.sv"             ;# path to our SystemVerilog source file
set TOP_MODULE   "ChiselCheckers"                 ;# top-level module name
set PART         "xc7a100t-1csg324"               ;# Nexys 4 DDR default Artix-7 part
set OUT_DIR      "./vivado_build"                 ;# output directory for reports/checkpoints
set PROJ_NAME    "simple_synth_proj"
# ---------------------------

file mkdir $OUT_DIR
create_project $PROJ_NAME $OUT_DIR -part $PART -force
add_files $SOURCE_FILE

# make sure compile order is updated and set top
update_compile_order -fileset sources_1
set_property top $TOP_MODULE [get_filesets sources_1]

# run synthesis for the top
synth_design -top $TOP_MODULE -part $PART -directive Default

# write a utilization report (synthesis)
set UTIL_FILE [file join $OUT_DIR "${TOP_MODULE}_synth_utilization.txt"]
report_utilization -file $UTIL_FILE -hierarchical

set DCP_FILE [file join $OUT_DIR "${TOP_MODULE}_synth.dcp"]
write_checkpoint -force $DCP_FILE

puts "Synthesis complete."
puts "Utilization report: $UTIL_FILE"

exit 0
