package com.fitforbusiness.stripe;


import com.fitforbusiness.stripe.model.Token;

public abstract class TokenCallback {
    public abstract void onError(Exception error);
    public abstract void onSuccess(Token token);
}
