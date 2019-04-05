package com.learn.bitcoin.blockchain;

/*
   Helpful link
   https://www.coursera.org/learn/cryptocurrency/discussions/all/threads/JzhcPAgkEeil8Q4pjViktg/replies/j9vlkAihEeil8Q4pjViktg

 */

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {

        UTXOPool toIndentifyDoubleSpend = new UTXOPool();
        double totalInputValue = 0;

        for(Transaction.Input input : tx.getInputs()){
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            if(!utxoPool.contains(utxo)){
                return false;
            }

            Transaction.Output output = utxoPool.getTxOutput(utxo);
            if(output == null){
                return false;
            }

            int input_index = tx.getInputs().indexOf(input);
            if(!Crypto.verifySignature(output.address, tx.getRawDataToSign(input_index), input.signature)){
                return false;
            }

            if(toIndentifyDoubleSpend.contains(utxo)){
                return false;
            }

            toIndentifyDoubleSpend.addUTXO(utxo, output);
            totalInputValue += output.value;
        }

        double totalOutputValue = 0;

        for(Transaction.Output output : tx.getOutputs()){
            if(output.value < 0)
                return false;
            totalOutputValue += output.value;
        }

        if(!(totalInputValue >= totalOutputValue)){
            return false;
        }

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {

        Transaction[] handleTxs = new Transaction[possibleTxs.length];

        int index = 0;
        for(Transaction transaction : possibleTxs){

            UTXOPool oldUTXOPool = new UTXOPool(this.utxoPool);
            if(isValidTx(transaction)){
                handleTxs[index++] = transaction;
                for(Transaction.Input input : transaction.getInputs()){
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    oldUTXOPool.addUTXO(utxo, transaction.getOutput(input.outputIndex));
                }
            }
            utxoPool = new UTXOPool(oldUTXOPool);
        }
        return handleTxs;
    }

    public UTXOPool getUTXOPool(){
        return utxoPool;
    }
}
