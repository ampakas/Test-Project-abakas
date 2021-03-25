package com.acme.eshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class OrderServiceImpl implements OrderService{
	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
	//public Long customerId;
	//public Database.CustomerType customerType;
	private Customer Customer;
	private Database.PaymentType PaymentType;
	private BigDecimal TotalAmount = BigDecimal.valueOf(0);
	private Integer TotalQuantity = 0;

	//private List<Item> order;
	private List<Item> order = new ArrayList<>();

	public List<Item> getOrder() {
		return order;
	}

	private PaymentService paymentService;

	public void setPaymentType(Database.PaymentType paymentType) {	PaymentType = paymentType;	}

	public void setCustomer(com.acme.eshop.Customer customer) {		Customer = customer;	}

	public com.acme.eshop.Customer getCustomer() {		return Customer;	}

	public Database.PaymentType getPaymentType() {		return PaymentType;	}

	public BigDecimal getTotalAmount() {		return TotalAmount;	}

	public Integer getTotalQuantity() {		return TotalQuantity;	}


	/*public OrderServiceImpl(PaymentService paymentService) {
		this.paymentService = paymentService;
		order = new ArrayList<>();
	}*/

/*	@Override
	public boolean addItem(Item item) {
		logger.info("ADDING PRODUCT");
		logger.info("firstItem name inside OrderService:{}.", item.getName());
		return item.getQuantity() <= 0 ? false : addAndIncrementQuantity(item);
	}*/

	//private boolean addAndIncrementQuantity(Item item){
		@Override
		public boolean addItem(Item item) {
		logger.info("firstItem name inside addAndIncrementQuantity:{}.", item.getName());
		logger.info("PaymentType :{}.", PaymentType);
		logger.info("Customer Type:{}.", Customer.getType());

		BigDecimal tempAmount= item.getPrice();
		BigDecimal DiscountedAmount= BigDecimal.valueOf(0);

		logger.info("tempAmount:{}.", tempAmount);

		if (PaymentType == Database.PaymentType.CASH); 	{
			switch (Customer.getType()){
				case B2C:
					DiscountedAmount =  tempAmount.multiply(BigDecimal.valueOf(0.9));
					logger.info("tempAmountX0.9:{}.", DiscountedAmount);
				break;
				case B2B:
					DiscountedAmount = tempAmount.multiply(BigDecimal.valueOf(0.7));
				break;
				case B2G:
					DiscountedAmount =  tempAmount.multiply(BigDecimal.valueOf(0.4));
				break;
			}
		}
		if (PaymentType == Database.PaymentType.CREDIT); {
			switch (Customer.getType()) {
				case B2C:
					DiscountedAmount = tempAmount.multiply(BigDecimal.valueOf(0.85));
					break;
				case B2B:
					DiscountedAmount = tempAmount.multiply(BigDecimal.valueOf(0.65));
					break;
				case B2G:
					DiscountedAmount = tempAmount.multiply(BigDecimal.valueOf(0.35));
					break;
			}
		}

		DiscountedAmount = DiscountedAmount.multiply(BigDecimal.valueOf(item.getQuantity()));
		logger.info("tempAmount X :{}.", DiscountedAmount);
		TotalAmount = TotalAmount.add(DiscountedAmount);
			logger.info("TotalAmount:{}.", TotalAmount);

		int tempQuantity = item.getQuantity();
		TotalQuantity =+ tempQuantity ;

		for (Item cartItem : order) {
			if (cartItem.equals(item)) {
				cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
				//cartItem.
				return true;
			}
		}
		return order.add(item);
	}

	@Override
	public boolean removeItem(Item item) {
		return order.remove(item);
	}

	@Override
	public List<Item> getCartItems() {
		return order;
	}


	@Override
	public boolean checkout() {
		order.clear();
		TotalAmount = BigDecimal.valueOf(0);
		TotalQuantity = 0 ;
		return true;
		};

/*
	@Override
	public boolean checkout() {
		if(getTotalPrice().toBigInteger().doubleValue() <= paymentService.balance().toBigInteger().doubleValue()){
			paymentService.withdraw(getTotalPrice());
			order.clear();
			return true;
		}else return false;
	}*/
}
