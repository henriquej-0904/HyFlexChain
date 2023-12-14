# HyFlexChain
A Permissionless Decentralized Ledger with Hybrid and Flexible Consensus Plane

## Installation Instructions
In folder hyflexchain, you can find the build tools and required libraries to install HyFlexChain in the system.
The Script build.sh is responsible for compiling the program and creating a docker image with all required libraries.

## Configuration
In order to execute hyflexchain, some configuration files are needed. An example of such configurations can be found in folder hyflexchain-config.
This folder contains a Propertities file named hyflexchain-general-config.properties, that configures the system version, active consensus mechanisms, keystore configurations
and pre-installed smart contracts.

Then, there are folders with the name of the supported consensus mechanisms. In each folder, you encounter the configuration for a specific consensus mechanism.
These configurations define how many transactions fit in a block and consensus related policies. In BFT, you configure committees, and BFT-SMaRt configurations.

Additionally, in folder keys, you encounter the keystores for each replica, which can be generated on demand.

## Execution
The execution is based on running a docker solution with hyflexchain and related configurations. A script named run-server.sh is available to run the system in a easy way.

## Caliper Workloads

One can also launch our modified version of Hyperledger Caliper available in the folder caliper/caliper-hyflexchain.
Caliper is configured through 2 configuration files. One is named networkconfig.json, which configures the available HyFlexChain Endpoints (HTTPS) and if smart contracts should be piggybacked or referenced when submitting transactions. Another one is the workload configuration (.yaml files), where some examples are placed in that folder, where a consensus mechanism can be specified, the send rate of transactions, the number of transactions to submit, etc. 
