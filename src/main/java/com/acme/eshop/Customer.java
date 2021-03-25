package com.acme.eshop;



public class Customer {


    enum CustomerType {
        B2C, B2B, B2G;
    }

    private Long Id;
    private String Name;
    private CustomerType Type;

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public CustomerType getType() {
        return Type;
    }

    public void setType(CustomerType type) {
        Type = type;
    }
}
