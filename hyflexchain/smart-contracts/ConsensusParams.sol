// SPDX-License-Identifier: MIT
pragma solidity >=0.6.0 <0.9.0;

contract ConsensusParams {

    function getConsensusParams(string memory version,
        string memory hash,
        string memory sender,
        string memory signatureType,
        string memory signature,
        int64 nonce, int32 inputTxsCount, int32 outputTxsCount, int64 totalValue, int64 time)
    public pure returns (string memory) {
        return "pow";
    }
}