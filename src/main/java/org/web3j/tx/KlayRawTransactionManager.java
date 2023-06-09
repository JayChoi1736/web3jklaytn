/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.tx;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.crypto.Hash;
import org.web3j.crypto.KlayCredentials;
import org.web3j.crypto.KlayRawTransaction;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.transaction.type.TxType.Type;
import org.web3j.protocol.KlaytnWeb3j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.service.TxKlaySignServiceImpl;
import org.web3j.service.TxSignService;
import org.web3j.service.TxSignServiceImpl;
import org.web3j.tx.exceptions.TxHashMismatchException;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;
import org.web3j.utils.TxHashVerifier;

import org.web3j.crypto.Credentials;

/**
 * TransactionManager implementation using Ethereum wallet file to create and sign transactions
 * locally.
 *
 * <p>This transaction manager provides support for specifying the chain id for transactions as per
 * <a href="https://github.com/ethereum/EIPs/issues/155">EIP155</a>, as well as for locally signing
 * RawTransaction instances without broadcasting them.
 */
public class KlayRawTransactionManager extends TransactionManager {
    private final Web3j web3j;
    private final TxKlaySignServiceImpl txSignService;

    private final long chainId;

    protected TxHashVerifier txHashVerifier = new TxHashVerifier();

    public KlayRawTransactionManager(Web3j web3j, Credentials credentials, long chainId) {
        super(web3j, credentials.getAddress());
        this.web3j = web3j;
        this.chainId = chainId;
        this.txSignService = new TxKlaySignServiceImpl(credentials);
    }

    public KlayRawTransactionManager(Web3j web3j, KlayCredentials credentials, long chainId) {
        super(web3j, credentials.getAddress());
        this.web3j = web3j;
        this.chainId = chainId;
        this.txSignService = new TxKlaySignServiceImpl(credentials);
    }

    public KlayRawTransactionManager(Web3j web3j, TxSignService txSignService, long chainId) {
        super(web3j, txSignService.getAddress());
        this.web3j = web3j;
        this.chainId = chainId;
        this.txSignService = (TxKlaySignServiceImpl) txSignService;
    }

    public KlayRawTransactionManager(
            Web3j web3j,
            KlayCredentials credentials,
            long chainId,
            TransactionReceiptProcessor transactionReceiptProcessor) {
        super(transactionReceiptProcessor, credentials.getAddress());

        this.web3j = web3j;
        this.chainId = chainId;
        this.txSignService = new TxKlaySignServiceImpl(credentials);
    }

    public KlayRawTransactionManager(
            Web3j web3j, Credentials credentials, long chainId, int attempts, long sleepDuration) {
        super(web3j, attempts, sleepDuration, credentials.getAddress());

        this.web3j = web3j;
        this.chainId = chainId;
        this.txSignService = new TxKlaySignServiceImpl(credentials);
    }
    public KlayRawTransactionManager(
            Web3j web3j, KlayCredentials credentials, long chainId, int attempts, long sleepDuration) {
        super(web3j, attempts, sleepDuration, credentials.getAddress());

        this.web3j = web3j;
        this.chainId = chainId;
        this.txSignService = new TxKlaySignServiceImpl(credentials);
    }
    public KlayRawTransactionManager(Web3j web3j, Credentials credentials) {
        this(web3j, credentials, ChainId.NONE);
    }

    public KlayRawTransactionManager(
            Web3j web3j, Credentials credentials, int attempts, int sleepDuration) {
        this(web3j, credentials, ChainId.NONE, attempts, sleepDuration);
    }

    protected BigInteger getNonce() throws IOException {
        EthGetTransactionCount ethGetTransactionCount =
                web3j.ethGetTransactionCount(
                                this.getFromAddress(), DefaultBlockParameterName.PENDING)
                        .send();

        return ethGetTransactionCount.getTransactionCount();
    }

    public TxHashVerifier getTxHashVerifier() {
        return txHashVerifier;
    }

    public void setTxHashVerifier(TxHashVerifier txHashVerifier) {
        this.txHashVerifier = txHashVerifier;
    }



    public EthSendTransaction sendValueTransferTransaction(
            long chainId,
            BigInteger gasPrice,
            BigInteger gas,
            String to,
            BigInteger value,
            String from)
            throws IOException {

        BigInteger nonce = getNonce();
        KlayRawTransaction rawTransaction =
                KlayRawTransaction.createTransaction(
                        Type.VALUE_TRANSFER,
                        nonce,
                        gasPrice,
                        gas,
                        to,
                        value,
                        from);

        return signAndSend(rawTransaction);
    }



    @Override
    public String sendCall(String to, String data, DefaultBlockParameter defaultBlockParameter)
            throws IOException {
        EthCall ethCall =
                web3j.ethCall(
                                Transaction.createEthCallTransaction(getFromAddress(), to, data),
                                defaultBlockParameter)
                        .send();

        assertCallNotReverted(ethCall);
        return ethCall.getValue();
    }

    @Override
    public EthGetCode getCode(
            final String contractAddress, final DefaultBlockParameter defaultBlockParameter)
            throws IOException {
        return web3j.ethGetCode(contractAddress, defaultBlockParameter).send();
    }



    /*
     * @param rawTransaction a RawTransaction istance to be signed
     * @return The transaction signed and encoded without ever broadcasting it
     */
    public String sign(KlayRawTransaction rawTransaction) {

        byte[] signedMessage = txSignService.sign(rawTransaction, chainId);

        return Numeric.toHexString(signedMessage);
    }

    public EthSendTransaction signAndSend(KlayRawTransaction rawTransaction) throws IOException {
        String hexValue = sign(rawTransaction);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction != null && !ethSendTransaction.hasError()) {
            String txHashLocal = Hash.sha3(hexValue);
            String txHashRemote = ethSendTransaction.getTransactionHash();
            if (!txHashVerifier.verify(txHashLocal, txHashRemote)) {
                throw new TxHashMismatchException(txHashLocal, txHashRemote);
            }
        }

        return ethSendTransaction;
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data,
            BigInteger value, boolean constructor) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendTransaction'");
    }

    @Override
    public EthSendTransaction sendEIP1559Transaction(long chainId, BigInteger maxPriorityFeePerGas,
            BigInteger maxFeePerGas, BigInteger gasLimit, String to, String data, BigInteger value, boolean constructor)
            throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendEIP1559Transaction'");
    }
}