package com.acme.eshop;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
	boolean addItem(Item item);
	boolean removeItem(Item item);
	List<Item> getCartItems();
	boolean checkout();
}
