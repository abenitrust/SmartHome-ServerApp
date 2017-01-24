package it.polimi.deib.p2pchat.discovery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import it.polimi.deib.p2pchat.R;

/**
 * Created by KidusMT on 12/29/2016.
 */

public class SignupActivity extends AppCompatActivity {

    DatabaseHelper helper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void onSignUpClick(View v){
        if(v.getId()== R.id.btn_signup )
        {
            EditText uname = (EditText) findViewById(R.id.signup_uname);
            EditText pass1 = (EditText) findViewById(R.id.signup_pass1);//first password
            EditText pass2 = (EditText) findViewById(R.id.signup_pass2);//second password
            EditText securityAnswer = (EditText) findViewById(R.id.signup_security_answer);

            String username = uname.getText().toString();
            String password1 = pass1.getText().toString();
            String password2 = pass2.getText().toString();
            String answer = securityAnswer.getText().toString();

            //If there is a mistake in confirming the password
            if(!password1.equals(password2))
            {
                //popup messge
                Toast.makeText(SignupActivity.this, "Password don't match", Toast.LENGTH_SHORT).show();
                System.out.println(password1);
                System.out.println(password2);
            }
            else
            {
                //insert into database
                Members m = new Members();
                m.setName(username);
                m.setPword(password1);
                m.setSecutiry_questions(answer);

                helper.insertMembers(m);
                Toast.makeText(SignupActivity.this, "Congrats! You're now a user member!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
