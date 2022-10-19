package com.mzt.logserver;

import com.mzt.logserver.pojo.ObjectSku;

/**
 * @author wulang
 **/
public interface SkuService {

    Long createObjectSkuNoJoinTransaction(ObjectSku sku);

    Long createObjectBusinessError(ObjectSku sku);

    Long createObjectBusinessError2(ObjectSku sku);

    Long createObjectSkuNoJoinTransactionRollBack(ObjectSku sku);
}
