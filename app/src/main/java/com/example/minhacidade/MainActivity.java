package com.example.minhacidade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.minhacidade.data.Db;
import com.example.minhacidade.data.Sessao;
import com.example.minhacidade.data.Usuario;
import com.example.minhacidade.ui.DenunciasFragment;
import com.example.minhacidade.ui.LoginActivity;
import com.example.minhacidade.ui.NoticiasFragment;
import com.example.minhacidade.ui.Notificador;
import com.example.minhacidade.ui.PerfilFragment;
import com.example.minhacidade.ui.Ui;
import com.example.minhacidade.ui.VagasFragment;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView nav;

    // Sem a permissão (Android 13+) o app continua funcionando: o aviso aparece só como selo na aba.
    private final ActivityResultLauncher<String> pedirPermissaoNotificacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Sessao.usuario(this) == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Ui.aplicarInsets(findViewById(R.id.main), true, false);
        Notificador.criarCanal(this);
        if (savedInstanceState == null && Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            pedirPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        nav = findViewById(R.id.nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;
            if (id == R.id.nav_noticias) {
                fragment = new NoticiasFragment();
            } else if (id == R.id.nav_denuncias) {
                fragment = new DenunciasFragment();
            } else if (id == R.id.nav_perfil) {
                fragment = new PerfilFragment();
            } else {
                fragment = new VagasFragment();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.conteudo, fragment)
                    .commit();
            return true;
        });
        if (savedInstanceState == null) {
            nav.setSelectedItemId(R.id.nav_vagas);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nav != null) {
            atualizarSeloDeDenuncias();
        }
    }

    /** Mostra na aba Denúncias quantos pareceres da prefeitura o cidadão ainda não leu. */
    private void atualizarSeloDeDenuncias() {
        Usuario usuario = Sessao.usuario(this);
        if (usuario == null) {
            return;
        }
        int naoLidos = Db.get(this).contarPareceresNaoLidos(usuario.id);
        if (naoLidos > 0) {
            BadgeDrawable selo = nav.getOrCreateBadge(R.id.nav_denuncias);
            selo.setVisible(true);
            selo.setNumber(naoLidos);
        } else {
            nav.removeBadge(R.id.nav_denuncias);
        }
    }
}
