package com.vplay.live;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlanExpirationActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView titleText;
    private TextView planDescriptionText;
    private TextView priceText;
    private TextView benefitsText;
    private Button renewPlanButton;
    private Button contactSupportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_expiration);

        initViews();
        setupClickListeners();
        loadPlanInfo();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        titleText = findViewById(R.id.titleText);
        planDescriptionText = findViewById(R.id.planDescriptionText);
        priceText = findViewById(R.id.priceText);
        benefitsText = findViewById(R.id.benefitsText);
        renewPlanButton = findViewById(R.id.renewPlanButton);
        contactSupportButton = findViewById(R.id.contactSupportButton);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> finish());
        
        renewPlanButton.setOnClickListener(v -> {
            // Aqui você pode implementar a lógica de pagamento
            // Por exemplo, abrir um link de pagamento ou integrar com uma API de pagamento
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://checkout.vplay.com.br"));
            startActivity(intent);
        });
        
        contactSupportButton.setOnClickListener(v -> {
            // Abrir email para suporte
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:suporte@vplay.com.br"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Suporte - Renovação de Plano");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Olá, preciso de ajuda com a renovação do meu plano.");
            
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            }
        });
    }

    private void loadPlanInfo() {
        titleText.setText("Renove seu Plano");
        planDescriptionText.setText("O Vplay coleta várias listas IPTV de alta qualidade para oferecer a melhor experiência de streaming com canais de TV ao vivo, filmes e séries.");
        priceText.setText("R$ 4,00");
        
        String benefits = "✓ Acesso a todos os canais\n" +
                         "✓ Filmes e séries em HD\n" +
                         "✓ Conteúdo sempre atualizado\n" +
                         "✓ Suporte técnico\n" +
                         "✓ Múltiplas listas IPTV\n" +
                         "✓ Sem anúncios";
        
        benefitsText.setText(benefits);
    }
}