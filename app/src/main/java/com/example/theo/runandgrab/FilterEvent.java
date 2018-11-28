package com.example.theo.runandgrab;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class FilterEvent extends AppCompatActivity {
    private SessionHandler session;

    Button btnViewProducts;
    Button btnFilterProducts;
    EditText inputville;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_event);

        session = new SessionHandler(getApplicationContext());
        User user = session.getUserDetails();

        inputville = (EditText) findViewById(R.id.inputville);
        // Buttons
        btnFilterProducts = (Button) findViewById(R.id.btnFilterProducts);
        btnViewProducts = (Button) findViewById(R.id.btnViewProducts);


        // view products click event

        btnFilterProducts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching All products Activity
                String pid_ville = inputville.getText().toString();
                Intent i = new Intent(getApplicationContext(), AllEventFiltered.class);
                i.putExtra("ville",pid_ville);
                startActivity(i);
            }
        });
        btnViewProducts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching All products Activity
                Intent i = new Intent(getApplicationContext(), AllProductsActivity.class);
                startActivity(i);

            }
        });


    }
}


