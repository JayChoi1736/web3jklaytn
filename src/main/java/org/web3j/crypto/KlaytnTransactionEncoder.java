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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.exception.CryptoWeb3jException;
import org.web3j.crypto.transaction.type.AbstractTxType;
import org.web3j.crypto.transaction.type.TxType;
import org.web3j.crypto.transaction.type.TxTypeFeeDelegatedValueTransfer;
import org.web3j.crypto.transaction.type.TxTypeValueTransfer;
import org.web3j.crypto.transaction.type.TxTypeValueTransferMemo;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.BytesUtils;
import org.web3j.utils.Numeric;

import static org.web3j.crypto.Sign.CHAIN_ID_INC;
import static org.web3j.crypto.Sign.LOWER_REAL_V;

/**
 * Create RLP encoded transaction, implementation as per p4 of the <a
 * href="http://gavwood.com/paper.pdf">yellow paper</a>.
 */
public class KlaytnTransactionEncoder {

    /**
     * Use for new transactions Eip1559 (this txs has a new field chainId) or an old
     * one before
     * Eip155
     *
     * @return signature
     */
    public static byte[] signMessage(KlayRawTransaction rawTransaction, long chainId, KlayCredentials credentials) {

        TxTypeValueTransfer tx = TxTypeValueTransfer.createTransaction(rawTransaction.getNonce(), rawTransaction.getGasPrice(), rawTransaction.getGasLimit(), rawTransaction.getTo(), rawTransaction.getValue(), credentials.getAddress());
        return tx.sign(credentials, chainId).getRaw();

    }

    public static byte[] signMessageAsFeePayer(KlayRawTransaction rawTransaction, long chainId, KlayCredentials credentials) {

        TxTypeFeeDelegatedValueTransfer tx = TxTypeFeeDelegatedValueTransfer.createTransaction(rawTransaction.getNonce(), rawTransaction.getGasPrice(), rawTransaction.getGasLimit(), rawTransaction.getTo(), rawTransaction.getValue(), credentials.getAddress());
        return tx.sign(credentials, chainId).getRaw();

    }




}
