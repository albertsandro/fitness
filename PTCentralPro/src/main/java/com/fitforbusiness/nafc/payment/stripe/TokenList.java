package com.fitforbusiness.nafc.payment.stripe;


import com.fitforbusiness.stripe.model.Token;

public interface TokenList {
    public void addToList(Token token);
}
