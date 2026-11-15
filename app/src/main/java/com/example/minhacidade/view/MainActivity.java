package com.example.minhacidade.view;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.minhacidade.R;
import com.example.minhacidade.model.Usuario;
import com.example.minhacidade.repository.Sessao;
import com.example.minhacidade.viewmodel.DenunciaViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView nav;
    private DenunciaViewModel denunciaViewModel;

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
        denunciaViewModel = new ViewModelProvider(this).get(DenunciaViewModel.class);
        denunciaViewModel.getNaoLidos().observe(this, this::mostrarSelo);
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
        denunciaViewModel.carregarNaoLidos(usuario.id);
    }

    private void mostrarSelo(int naoLidos) {
        if (naoLidos > 0) {
            BadgeDrawable selo = nav.getOrCreateBadge(R.id.nav_denuncias);
            selo.setVisible(true);
            selo.setNumber(naoLidos);
        } else {
            nav.removeBadge(R.id.nav_denuncias);
        }
    }
}
