package com.example.doctorhealthapp.Service;

import com.example.doctorhealthapp.Common.Common;
import com.example.doctorhealthapp.model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseIdService extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();


        updateTokenToServer(refreshedToken);
    }

    private void updateTokenToServer(String refreshToken) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        DatabaseReference tokens = db.getReference(Common.token_tb1);

        Token token = new Token(refreshToken);

        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                   .setValue(token) ;

    }
}
