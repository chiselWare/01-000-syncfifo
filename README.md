# SyncFifo

A highly configurable synchronous FIFO or FIFO controller with dynamic status flags.

## Description

The SyncFifo can be configured to generate either standalone FIFOs built from flip-flops or a FIFO controller that can be connected to an external dual-port synchronous SRAM. The standalone configuration is appropriate for small FIFOs whereas larger FIFOs are more efficiently implemented with an external memory.

Four status flags are provided:
* Empty
* Full
* almostEmpty
* almostFull

The latter two can be programmed to arbitrary values during operation providing the ability for dynamic flow control.

## Getting Started

It is recommended that the user reads the SyncFiFo Users Guide which can be found in the ```./modules/syncfifo/docs/user-guide``` directory.

### Dependencies
The core is dependent on the chiselWare Standard Software Environment that is described in the chiselWare Standard and chiselWare Developers Guide that can be found on the [chiselWare GitHub page](https://github.com/chiselWare)

### Installation

There are no special installation requirements. The code can be cloned in any directory for standalone use.

### Generating Verilog RTL

To generate an example configuration of SystemVerilog RTL, a helper app can be found in the main class file (SyncFifo.scala) and executed as follows:

```
$ sbt
sbt:chiselware>
sbt:chiselware> project core
sbt:chiselware-core-syncfifo> run
```

The RTL will be generated in the ```.modules/syncfifo/generated``` directory.

### Running a Simulation  

Multiple options are available for running simulations:

* iVerilog (open-source Verilog simulator)
* VCS (commercial simulator from Synopsys)
* Verilator (open source compiled Verilog simulator) 

An exhaustive constrained-random verilog test is included and can be executed as follows:

```
$ sbt
sbt:chiselware>
sbt:chiselware> project core
sbt:chiselware-core-syncfifo> test
```

### Synthesis

SyncFifo is a DFT-clean, fully synthesizable core. 

A ```.sdc``` file is generated together with the RTL code for each configuration in the ```./modules/dff/generated/synTestCases/ directory.```  Synthesis scripts for the synthesis tool Yosys and timing analysis scripts for the OpenSTA static timing analysis tool are included in the same directory and can be easily ported to commercial synthesis tools.

Included also is the Nangate 45nm technology library to allow users to run included synthesis regressions out-of-the-box and later change to their technology library of choice.

Included also is the Nangate 45nm technology library to allow users to run included synthesis regressions out-of-the-box and later change to their technology library of choice.

## Authors

Warren Savage [@twsavage59]

## License

See the [LICENSE.MD](LICENSE.MD)
