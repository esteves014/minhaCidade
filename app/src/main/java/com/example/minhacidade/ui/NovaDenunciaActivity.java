package com.example.minhacidade.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import com.example.minhacidade.R;
import com.example.minhacidade.data.Db;
import com.example.minhacidade.data.Denuncia;
import com.example.minhacidade.data.Sessao;
import com.example.minhacidade.data.Usuario;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NovaDenunciaActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private TextInputLayout categoriaLayout;
    private TextInputLayout enderecoLayout;
    private TextInputLayout descricaoLayout;
    private MaterialAutoCompleteTextView categoria;
    private TextInputEditText endereco;
    private TextInputEditText descricao;
    private ImageView preview;
    private Button btnFoto;
    private String fotoPath;
    private Usuario usuario;

    // Posição opcional da denúncia: vem do GPS ou do ponto escolhido no mapa.
    private Double latitude;
    private Double longitude;
    private TextView localizacaoStatus;
    private Button btnGps;
    private Button btnRemoverLocal;
    private CancellationSignal buscandoPosicao;

    private final androidx.activity.result.ActivityResultLauncher<PickVisualMediaRequest> seletorFoto =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::aoEscolherFoto);

    private final androidx.activity.result.ActivityResultLauncher<String[]> pedirLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                if (Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION))) {
                    obterPosicao();
                } else {
                    Toast.makeText(this, R.string.erro_localizacao_negada, Toast.LENGTH_LONG).show();
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<Intent> escolherNoMapa =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                Intent dados = resultado.getData();
                if (resultado.getResultCode() == RESULT_OK && dados != null
                        && dados.hasExtra(MapaActivity.EXTRA_LAT) && dados.hasExtra(MapaActivity.EXTRA_LNG)) {
                    definirPosicao(dados.getDoubleExtra(MapaActivity.EXTRA_LAT, 0),
                            dados.getDoubleExtra(MapaActivity.EXTRA_LNG, 0));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nova_denuncia);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        ((MaterialToolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());

        usuario = Sessao.usuario(this);
        if (usuario == null) {
            finish();
            return;
        }

        categoriaLayout = findViewById(R.id.categoria_layout);
        enderecoLayout = findViewById(R.id.endereco_layout);
        descricaoLayout = findViewById(R.id.descricao_layout);
        categoria = findViewById(R.id.categoria);
        endereco = findViewById(R.id.endereco);
        descricao = findViewById(R.id.descricao);
        preview = findViewById(R.id.preview);
        btnFoto = findViewById(R.id.btn_foto);

        categoria.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(R.array.categorias_denuncia)));

        Ui.aoDigitar(categoria, () -> categoriaLayout.setError(null));
        Ui.aoDigitar(endereco, () -> enderecoLayout.setError(null));
        Ui.aoDigitar(descricao, () -> descricaoLayout.setError(null));

        btnFoto.setOnClickListener(v -> seletorFoto.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        localizacaoStatus = findViewById(R.id.localizacao_status);
        btnGps = findViewById(R.id.btn_gps);
        btnRemoverLocal = findViewById(R.id.btn_remover_local);
        btnGps.setOnClickListener(v -> aoTocarUsarLocalizacao());
        findViewById(R.id.btn_mapa).setOnClickListener(v -> {
            Intent intent = new Intent(this, MapaActivity.class).putExtra(MapaActivity.EXTRA_ESCOLHER, true);
            if (latitude != null && longitude != null) {
                intent.putExtra(MapaActivity.EXTRA_LAT, latitude).putExtra(MapaActivity.EXTRA_LNG, longitude);
            }
            escolherNoMapa.launch(intent);
        });
        btnRemoverLocal.setOnClickListener(v -> {
            latitude = null;
            longitude = null;
            atualizarStatusPosicao();
        });
        // Atalho: o local da denúncia costuma ser perto de casa; o cidadão ainda pode editar o texto.
        Button btnMeuEndereco = findViewById(R.id.btn_meu_endereco);
        if (usuario.temEndereco()) {
            btnMeuEndereco.setVisibility(android.view.View.VISIBLE);
            btnMeuEndereco.setOnClickListener(v -> {
                endereco.setText(usuario.endereco);
                if (usuario.latitude != null && usuario.longitude != null) {
                    definirPosicao(usuario.latitude, usuario.longitude);
                }
            });
        }
        findViewById(R.id.btn_enviar).setOnClickListener(v -> enviar());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        if (buscandoPosicao != null) {
            buscandoPosicao.cancel();
        }
    }

    // ---- Posição ----

    private void aoTocarUsarLocalizacao() {
        if (permitido(Manifest.permission.ACCESS_FINE_LOCATION)
                || permitido(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            obterPosicao();
        } else {
            pedirLocalizacao.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private boolean permitido(String permissao) {
        return ContextCompat.checkSelfPermission(this, permissao) == PackageManager.PERMISSION_GRANTED;
    }

    /** Pede uma posição atual (uma só vez) ao aparelho; só roda depois de o cidadão tocar no botão. */
    @SuppressLint("MissingPermission") // Fine ou coarse já foi verificada em aoTocarUsarLocalizacao.
    private void obterPosicao() {
        LocationManager gerente = getSystemService(LocationManager.class);
        String provedor = null;
        if (permitido(Manifest.permission.ACCESS_FINE_LOCATION)
                && gerente.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provedor = LocationManager.GPS_PROVIDER;
        } else if (gerente.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provedor = LocationManager.NETWORK_PROVIDER;
        }
        if (provedor == null) {
            Toast.makeText(this, R.string.erro_localizacao_desligada, Toast.LENGTH_LONG).show();
            return;
        }
        btnGps.setEnabled(false);
        localizacaoStatus.setText(R.string.localizacao_obtendo);
        CancellationSignal cancelamento = new CancellationSignal();
        buscandoPosicao = cancelamento;
        LocationManagerCompat.getCurrentLocation(gerente, provedor, cancelamento,
                ContextCompat.getMainExecutor(this), local -> {
                    buscandoPosicao = null;
                    if (isDestroyed()) {
                        return;
                    }
                    btnGps.setEnabled(true);
                    if (local == null) {
                        Toast.makeText(this, R.string.erro_localizacao_falhou, Toast.LENGTH_LONG).show();
                        atualizarStatusPosicao();
                    } else {
                        definirPosicao(local.getLatitude(), local.getLongitude());
                    }
                });
    }

    private void definirPosicao(double lat, double lng) {
        latitude = lat;
        longitude = lng;
        atualizarStatusPosicao();
        preencherEnderecoDaPosicao(lat, lng);
    }

    /** Se o cidadão ainda não digitou o endereço, descobre o da posição marcada (GPS ou mapa). */
    private void preencherEnderecoDaPosicao(double lat, double lng) {
        if (!Ui.texto(endereco).isEmpty()) {
            return;
        }
        executor.execute(() -> {
            String texto = EnderecoTexto.resolver(this, lat, lng);
            runOnUiThread(() -> {
                if (texto != null && !isDestroyed() && Ui.texto(endereco).isEmpty()) {
                    endereco.setText(texto);
                }
            });
        });
    }

    private void atualizarStatusPosicao() {
        boolean marcada = latitude != null && longitude != null;
        localizacaoStatus.setText(marcada
                ? getString(R.string.localizacao_marcada, latitude, longitude)
                : getString(R.string.localizacao_nenhuma));
        btnRemoverLocal.setVisibility(marcada ? android.view.View.VISIBLE : android.view.View.GONE);
    }

    private void aoEscolherFoto(Uri uri) {
        if (uri == null) {
            return;
        }
        // O acesso do seletor é temporário: copia para o armazenamento do app.
        executor.execute(() -> {
            File destino = new File(getFilesDir(), "denuncias/" + UUID.randomUUID() + ".jpg");
            try {
                copiar(uri, destino);
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.erro_foto, Toast.LENGTH_SHORT).show());
                return;
            }
            Bitmap bitmap = Ui.carregarBitmap(destino.getAbsolutePath(), 1000);
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    return;
                }
                if (bitmap == null) {
                    destino.delete();
                    Toast.makeText(this, R.string.erro_foto, Toast.LENGTH_SHORT).show();
                    return;
                }
                fotoPath = destino.getAbsolutePath();
                preview.setImageBitmap(bitmap);
                preview.setVisibility(android.view.View.VISIBLE);
                btnFoto.setText(R.string.btn_trocar_foto);
            });
        });
    }

    private void copiar(Uri origem, File destino) throws IOException {
        File pasta = destino.getParentFile();
        if (pasta != null && !pasta.exists() && !pasta.mkdirs()) {
            throw new IOException("Não foi possível criar " + pasta);
        }
        try (InputStream in = getContentResolver().openInputStream(origem);
             OutputStream out = new FileOutputStream(destino)) {
            if (in == null) {
                throw new IOException("Foto indisponível");
            }
            byte[] buffer = new byte[8192];
            int lidos;
            while ((lidos = in.read(buffer)) != -1) {
                out.write(buffer, 0, lidos);
            }
        }
    }

    private void enviar() {
        String cat = categoria.getText().toString().trim();
        String end = textoDe(endereco);
        String desc = textoDe(descricao);

        categoriaLayout.setError(cat.isEmpty() ? getString(R.string.erro_categoria) : null);
        enderecoLayout.setError(end.isEmpty() ? getString(R.string.erro_endereco) : null);
        descricaoLayout.setError(desc.length() < 10 ? getString(R.string.erro_descricao) : null);
        if (cat.isEmpty() || end.isEmpty() || desc.length() < 10) {
            return;
        }

        Denuncia criada = Db.get(this).inserirDenuncia(usuario, cat, end, desc, fotoPath, latitude, longitude);
        Toast.makeText(this, getString(R.string.denuncia_registrada, criada.getProtocolo()),
                Toast.LENGTH_LONG).show();
        finish();
    }

    private static String textoDe(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
