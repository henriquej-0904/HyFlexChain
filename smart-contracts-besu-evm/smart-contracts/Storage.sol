// SPDX-License-Identifier: GPL-3.0

pragma solidity >=0.8.2 <0.9.0;

/**
 * @title Storage
 * @dev Store & retrieve value in a variable
 * @custom:dev-run-script ./scripts/deploy_with_ethers.ts
 */
contract Storage {

    int32 number;

    /**
     * @dev Set contract deployer as owner
     */
    constructor(int32 num) {
        number = num;
    }

    /**
     * @dev Store value in variable
     * @param num value to store
     */
    function store(int32 num) public {
        number = num;
    }

    /**
     * @dev Return value 
     * @return value of 'number'
     */
    function retrieve() public view returns (int32){
        return number;
    }
}