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
package org.web3j.crypto;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.web3j.crypto.transaction.type.ITransaction;
import org.web3j.crypto.transaction.type.LegacyTransaction;
import org.web3j.crypto.transaction.type.Transaction1559;
import org.web3j.crypto.transaction.type.Transaction2930;
import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.crypto.transaction.type.TxType;
import org.web3j.crypto.transaction.type.TxTypeSmartContractDeploy;
import org.web3j.crypto.transaction.type.TxTypeValueTransfer;
import org.web3j.crypto.transaction.type.TxTypeValueTransferMemo;
import org.web3j.crypto.transaction.type.TxTypeCancel;
import org.web3j.crypto.transaction.type.TxType.Type;

/**
 * Transaction class used for signing transactions locally.<br>
 * For the specification, refer to p4 of the
 * <a href="http://gavwood.com/paper.pdf">yellow
 * paper</a>.
 */
public class KlayRawTransaction extends RawTransaction {
    private byte[] value;
    private Set<KlaySignatureData> signatureData;

    public KlayRawTransaction(ITransaction transaction, Set<KlaySignatureData> signatureData) {
        super(transaction);
        this.signatureData = signatureData;
    }

    public KlayRawTransaction(ITransaction transaction, KlaySignatureData signatureData) {
        super(transaction);
        this.signatureData = new HashSet<>(Arrays.asList(signatureData));
    }

    public KlayRawTransaction(ITransaction transaction, byte[] value, Set<KlaySignatureData> signatureData) {
        super(transaction);
        this.value = value;
        this.signatureData = signatureData;
    }

    public KlayRawTransaction(ITransaction transaction, byte[] value, KlaySignatureData signatureData) {
        super(transaction);
        this.value = value;
        this.signatureData = new HashSet<>(Arrays.asList(signatureData));
    }

    public KlaySignatureData getSignatureData() {
        try {
            return signatureData.iterator().next();
        } catch (Exception e) {
            throw new RuntimeException("Called without signature data");
        }
    }

    protected KlayRawTransaction(ITransaction transaction) {
        super(transaction);
    }

    public static KlayRawTransaction createTransaction(
            TxType.Type type,
            BigInteger nonce,
            BigInteger gasPrice,
            BigInteger gas,
            String to,
            BigInteger value,
            String from) {
  
        return new KlayRawTransaction(
                TxTypeValueTransfer.createTransaction(
                        nonce,
                        gasPrice,
                        gas,
                        to,
                        value,
                        from));
    }

    public static KlayRawTransaction createTransaction(
        TxType.Type type,
        BigInteger nonce,
        BigInteger gasPrice,
        BigInteger gas,
        String from
        ) {
    
    return new KlayRawTransaction(
            TxTypeCancel.createTransaction(
                    nonce,
                    gasPrice,
                    gas,
                    from));
    }


    public static KlayRawTransaction createTransaction(
        TxType.Type type,    
        BigInteger nonce,
        BigInteger gasPrice,
        BigInteger gas,
        String to,
        BigInteger value,
        String from,
        byte[] payload) {
    
            if(type == Type.VALUE_TRANSFER_MEMO){
    return new KlayRawTransaction(
            TxTypeValueTransferMemo.createTransaction(
                    nonce,
                    gasPrice,
                    gas,
                    to,
                    value,
                    from,
                    payload));
            }

            else if(type == Type.SMART_CONTRACT_EXECUTION){ 
                return new KlayRawTransaction(
                    TxTypeValueTransferMemo.createTransaction(
                            nonce,
                            gasPrice,
                            gas,
                            to,
                            value,
                            from,
                            payload));
            }

            else {
                return new KlayRawTransaction(
                    TxTypeValueTransferMemo.createTransaction(
                            nonce,
                            gasPrice,
                            gas,
                            to,
                            value,
                            from,
                            payload));
            }
}

public static KlayRawTransaction createTransaction(
    TxType.Type type,
    BigInteger nonce,
    BigInteger gasPrice,
    BigInteger gas,
    String to,
    BigInteger value,
    String from,
    byte[] payload,
    BigInteger codeFormat
    ) {

return new KlayRawTransaction(
        TxTypeSmartContractDeploy.createTransaction(
                nonce,
                gasPrice,
                gas,
                value,
                from,
                payload,
                codeFormat));
}

    public byte[] getRaw() {
        return value;
    }



}
