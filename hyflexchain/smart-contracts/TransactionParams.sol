// SPDX-License-Identifier: MIT
    pragma solidity >=0.6.0 <0.9.0;
    
    contract TransactionParams {
        /**
        * Get the Transaction Parameters for this transaction. It is mandatory to
        * decide the Consensus mechanism it will be used to order this transaction.
        * Optionally, other parameters can be provided, ex. batch_metric, signature type, etc.
        * 
        * @param version The version of this transaction
        * @param sender The sender of this transaction
        * @param outputTxsValue The value to transfer for each transaction output
        * @param outputTxsAddress The address of the receiver for each transaction output
        * @param time The current time in seconds
        * @return A string that represents the decided consensus mechanism and parameters for this transaction
        */
        function getTransactionParams(
            string memory version,
            string memory sender,
            int64[] memory outputTxsValue,
            string[] memory outputTxsAddress,
            int64 time
        ) public pure returns (string memory) {
            // options for consensus: pow, bft_smart, pos, poet
            string memory consensus = "\"consensus\": \"pow\"";
            string memory batchMetric = "\"batchMetric\": 10";
            string memory signatureType = "\"signatureType\": \"SHA256withECDSA-secp521r1\"";
            
            //return string.concat("{", consensus, ",",
                // batchMetric, ",", signatureType, "}"); 
            return string.concat("{", consensus, "}"); 
        }
    }