package com.cinestream.live;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PlanExpiredActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_expired);

        TextView titleText = findViewById(R.id.title_text);
        TextView descriptionText = findViewById(R.id.description_text);
        TextView priceText = findViewById(R.id.price_text);
        TextView featuresText = findViewById(R.id.features_text);
        Button rechargeButton = findViewById(R.id.recharge_button);
        Button backToLoginButton = findViewById(R.id.back_to_login_button);

        titleText.setText("⏰ Plano Expirado");
        descriptionText.setText("Seu plano expirou! Para continuar aproveitando o melhor do entretenimento, renove agora mesmo.");
        priceText.setText("💰 Apenas R$ 4,00");
        
        String features = "🎯 O que você terá acesso:\n\n" +
                "📺 Múltiplas listas IPTV\n" +
                "• O app coleta e disponibiliza diversas listas IPTV de alta qualidade\n" +
                "• Sistema inteligente que seleciona automaticamente a melhor lista disponível\n" +
                "• Acesso a centenas de canais nacionais e internacionais\n\n" +
                "🎬 Conteúdo Premium\n" +
                "• Canais de filmes, séries, esportes e documentários\n" +
                "• Qualidade HD/Full HD\n" +
                "• Atualizações automáticas das listas\n\n" +
                "⚡ Experiência Otimizada\n" +
                "• Interface moderna e intuitiva\n" +
                "• Histórico de reprodução\n" +
                "• Favoritos personalizados\n" +
                "• Suporte técnico dedicado";
        
        featuresText.setText(features);

        rechargeButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/VPlay0"));
            startActivity(browserIntent);
        });

        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(PlanExpiredActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(PlanExpiredActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}