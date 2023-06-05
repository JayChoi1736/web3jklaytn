package org.web3j.sample;

import java.math.BigInteger;

import org.web3j.crypto.KlayCredentials;
import org.web3j.crypto.KlayRawTransaction;
import org.web3j.crypto.KlaytnTransactionEncoder;
import org.web3j.crypto.transaction.type.TxType;
import org.web3j.crypto.transaction.type.TxType.Type;
import org.web3j.protocol.KlaytnWeb3j;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

public class Application {

        // String url = "http://127.0.0.1:8551";
        String url = "https://public-en-baobab.klaytn.net";

        public static void main(String[] args) throws Exception {
                new Application().run();
        }

        BigInteger getNonce(String address) throws Exception {
                Web3j web3j = KlaytnWeb3j.build(new HttpService(url));
                EthGetTransactionCount ethGetTransactionCount = web3j
                                .ethGetTransactionCount(address, DefaultBlockParameterName.LATEST)
                                .sendAsync()
                                .get();

                return ethGetTransactionCount.getTransactionCount();
        }

        private void run() throws Exception {

                Web3j web3j = KlaytnWeb3j.build(new HttpService(
                                url));

                // address
                // 0xe6c26dfdafa63d5baa7749063a44b761c3f505b8
                KlayCredentials cred = KlayCredentials
                                .create("0x508d200557f1d2e26cd296ace3dc9ded5a1077f197a62a152d940bb432bfeb4f");

                // address
                // 0x9753da8c4d4325408d40fa19d3b446ebe93d926a
                // privatekey :
                // 0xb56b610a706e2535e4a6020a9dfe36b60d49755e014e1f88acbd8628216ff3ac
                KlayCredentials cred_accountpublic = KlayCredentials.createWithKlaytnWalletKey(
                                "0xb56b610a706e2535e4a6020a9dfe36b60d49755e014e1f88acbd8628216ff3ac0x000x9753da8c4d4325408d40fa19d3b446ebe93d926a");

                // address
                // 0x0d84fab826aef693447400cb149279272eeb3390
                // privatekey :
                // 0x0ed62f9eb2b15f96b9acb4005cde58832a0174009908af068aec8582a7a43d3e
                // calculated address with privatekey :
                // 0xdCd34B40D3eF20B1EDCA68C799776033bb0eB695
                KlayCredentials cred_keyupdate = KlayCredentials.create(
                                "0x0ed62f9eb2b15f96b9acb4005cde58832a0174009908af068aec8582a7a43d3e",
                                "0x0d84fab826aef693447400cb149279272eeb3390");

                KlayCredentials cred_role = KlayCredentials.create(
                                "0x19d9ee783512c5eda84732d503d91d9ad21f8864c7b7e423c048d7b6a5f462dc",
                                "0xfbc847c8552fe5c084ee546cb85c7c8f5edb0c6c");


                // partial credentials for multi-sig
                // threshold : 2
                KlayCredentials cred_multi1 = KlayCredentials.create(
                                "0x8e34790f597803df3eab473a55f3f1d3e4889830a751b693b79aa0faf8a2fba1",
                                "0x79eeec0990ad9ee4cb4305ecd4fe3358ae3f3c72");
                KlayCredentials cred_multi2 = KlayCredentials.create(
                                "0x6002ee2f1bd3a774d59928147c06cba9f6b2f04ac747973997fc8836f5376289",
                                "0x79eeec0990ad9ee4cb4305ecd4fe3358ae3f3c72");
                KlayCredentials cred_multi3 = KlayCredentials.create(
                                "0xdfa83641713e7f6fe88e2cd928b1c659d8848eb91c0c1dab8e7786e33d5dd71f",
                                "0x79eeec0990ad9ee4cb4305ecd4fe3358ae3f3c72");

                // single credentials for multi-sig
                String[] multi_privatekeys = { "0x8e34790f597803df3eab473a55f3f1d3e4889830a751b693b79aa0faf8a2fba1",
                                "0x6002ee2f1bd3a774d59928147c06cba9f6b2f04ac747973997fc8836f5376289",
                                "0xdfa83641713e7f6fe88e2cd928b1c659d8848eb91c0c1dab8e7786e33d5dd71f" };
                KlayCredentials cred_multiall = KlayCredentials.create(multi_privatekeys,
                                "0x79eeec0990ad9ee4cb4305ecd4fe3358ae3f3c72");






                BigInteger GAS_PRICE = BigInteger.valueOf(500000000000L);
                BigInteger GAS_LIMIT = BigInteger.valueOf(6721950);

                // test data
                KlayCredentials cred_test = cred_multi1;
                BigInteger nonce = getNonce(cred_test.getAddress());
                String from = cred_test.getAddress();
                String data = "0xd5fa2b";
                long chainId = 1001;
                String to = "0x44cDeFF44c6fEb6e00E7D08f9aeb524558A80a76";
                byte[] payload = data.getBytes();
                TxType.Type type = Type.FEE_DELEGATED_SMART_CONTRACT_DEPLOY;

                KlayRawTransaction raw = KlayRawTransaction.createTransaction(
                                type,
                                nonce,
                                GAS_PRICE,
                                GAS_LIMIT,
                                to,
                                BigInteger.ONE,
                                from,
                                payload);

                byte[] signedMessage = KlaytnTransactionEncoder.signMessage(raw, chainId, cred_multi1);
                signedMessage = KlaytnTransactionEncoder.signMessage(signedMessage, chainId, cred_multi2);
                signedMessage = KlaytnTransactionEncoder.signMessageAsFeePayer(signedMessage, chainId, cred_keyupdate);
                String hexValue2 = Numeric.toHexString(signedMessage);
                System.out.println(hexValue2);
                EthSendTransaction transactionResponse = web3j.ethSendRawTransaction(hexValue2).send();
                System.out.println(transactionResponse.getResult());
                String txHash = transactionResponse.getResult();
        }
  }
