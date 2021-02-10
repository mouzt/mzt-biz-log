package com.mzt.logserver.service;

import com.mzt.logserver.beans.Order;

public interface OrderQueryService {
    Order queryOrder(long parseLong);
}
