package com.learn.bitcoin.blockchain;
// Block Chain should maintain only limited block nodes to satisfy the functions
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

import java.util.*;

public class BlockChain {

    public static final int CUT_OFF_AGE = 10;

    private Map<ByteArrayWrapper, NodeWithHeight> blockchain;
    private int current_max_height;

    private TransactionPool transactionPool;
//    private Map<ByteArrayWrapper, UTXOPool> utxoPoolMap;


    /**
     * create an empty block chain with just a genesis block. Assume {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        blockchain = new LinkedHashMap<ByteArrayWrapper, NodeWithHeight>();

        ByteArrayWrapper genesisBlockhash = new ByteArrayWrapper(genesisBlock.getHash());

        transactionPool = new TransactionPool();
        for(Transaction tx : genesisBlock.getTransactions())
            transactionPool.addTransaction(tx);

//        utxoPoolMap = new HashMap<>();

        UTXOPool utxoPool = new UTXOPool();
        TxHandler txHandler = new TxHandler(utxoPool);
        Transaction []txs = new Transaction[genesisBlock.getTransactions().size()];
        txHandler.handleTxs(genesisBlock.getTransactions().toArray(txs));
        updateUTXOPoolWithCoinBase(genesisBlock, txHandler.getUTXOPool());
        blockchain = new LinkedHashMap<>();
        blockchain.put(genesisBlockhash, NodeWithHeight.create(genesisBlock, txHandler.getUTXOPool(), 0));
//        utxoPoolMap.put(new ByteArrayWrapper(genesisBlock.getHash()), txHandler.getUTXOPool());
        current_max_height = 0;
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {

        /* Since blockchain is linkedmap, first value match would be oldest */
        for(Map.Entry<ByteArrayWrapper, NodeWithHeight> entry : blockchain.entrySet()){
            if(entry.getValue().height == current_max_height){
                return entry.getValue()._block;
            }
        }

        return null;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {

        Block maxHeightBlock = getMaxHeightBlock();
        if(maxHeightBlock == null)
            return null;
        return blockchain.get(new ByteArrayWrapper(maxHeightBlock.getHash())).utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {

        return transactionPool;
    }

    /**
     * Add {@code block} to the block chain if it is valid. For validity, all transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)}.
     * 
     * <p>
     * For example, you can try creating a new block over the genesis block (block height 2) if the
     * block chain height is {@code <=
     * CUT_OFF_AGE + 1}. As soon as {@code height > CUT_OFF_AGE + 1}, you cannot create a new block
     * at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {

        if(block.getPrevBlockHash() == null)
            return false;

        ByteArrayWrapper preBlockHash = new ByteArrayWrapper(block.getPrevBlockHash());
        ByteArrayWrapper blockHash = new ByteArrayWrapper(block.getHash());

//        dumpBlockChain();
//        System.out.println("block "+block);
//        System.out.println("preBlockHash "+preBlockHash);

        NodeWithHeight prevBlockNode = blockchain.get(preBlockHash);

        if(prevBlockNode == null)
            return false;

        /* Got previous utxopool to validate the block */

        TxHandler txHandler = new TxHandler(prevBlockNode.utxoPool);
        Transaction []txs = new Transaction[block.getTransactions().size()];
        Transaction []validtxs = txHandler.handleTxs(block.getTransactions().toArray(txs));

        if(txs.length != validtxs.length){
            return false;
        }

        updateUTXOPoolWithCoinBase(block, txHandler.getUTXOPool());
//        utxoPoolMap.put(preBlockHash, txHandler.getUTXOPool());

        if(!((prevBlockNode.height + 1) > current_max_height - CUT_OFF_AGE))
            return false;

        blockchain.put(blockHash, NodeWithHeight.create(block,
                txHandler.getUTXOPool(),
                prevBlockNode.height + 1));

        return true;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        transactionPool.addTransaction(tx);
    }

    static class NodeWithHeight{
        public final Block _block;
        public final int height;
        public final UTXOPool utxoPool;

        public NodeWithHeight(Block block, UTXOPool utxoPool, int height){
            this._block = block;
            this.height = height;
            this.utxoPool = utxoPool;
        }

        public static NodeWithHeight create(Block block, UTXOPool utxoPool, int height){
            return new NodeWithHeight( block,  utxoPool,  height);
        }

        @Override
        public String toString() {
            return "NodeWithHeight{" +
                    "_block=" + _block +
                    ", height=" + height +
                    ", utxoPool=" + utxoPool +
                    '}';
        }
    }

    private void updateUTXOPoolWithCoinBase(Block block, UTXOPool utxoPool) {
        Transaction coinbase = block.getCoinbase();
        for (int i = 0; i < coinbase.numOutputs(); i++) {
            Transaction.Output out = coinbase.getOutput(i);
            UTXO utxo = new UTXO(coinbase.getHash(), i);
            utxoPool.addUTXO(utxo, out);
        }
    }

    public void dumpBlockChain(){
        for(Map.Entry<ByteArrayWrapper, NodeWithHeight> entry : blockchain.entrySet()){
            System.out.println("{ "+entry.getKey()+" --> "+entry.getValue()+" }");
        }
    }

}