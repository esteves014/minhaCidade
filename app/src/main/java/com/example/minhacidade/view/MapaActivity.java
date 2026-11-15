package com.example.minhacidade.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.minhacidade.R;
import com.example.minhacidade.viewmodel.DenunciaViewModel;
import com.example.minhacidade.viewmodel.UsuarioViewModel;
import com.example.minhacidade.model.Cidade;
import com.example.minhacidade.model.Denuncia;
import com.example.minhacidade.repository.Sessao;
import com.example.minhacidade.model.Usuario;
import com.google.android.material.appbar.MaterialToolbar;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapa (OpenStreetMap, sem chave de API) com dois usos:
 * <ul>
 *   <li>ver: mostra as denúncias do cidadão que têm posição; com {@link Ui#EXTRA_ID} já abre uma delas;</li>
 *   <li>escolher ({@link #EXTRA_ESCOLHER}): o cidadão move o mapa até o local e confirma; a posição
 *   volta em {@link #EXTRA_LAT} e {@link #EXTRA_LNG}.</li>
 * </ul>
 */
public class MapaActivity extends AppCompatActivity {
    static final String EXTRA_ESCOLHER = "escolher";
    static final String EXTRA_LAT = "lat";
    static final String EXTRA_LNG = "lng";

    private static final double ZOOM_CIDADE = 14;
    private static final double ZOOM_PONTO = 17;

    private MapView mapa;
    private Cidade cidade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // O servidor de tiles do OSM bloqueia (HTTP 403) User-Agent genérico, como só o applicationId:
        // é preciso identificar o aplicativo. O cache fica no armazenamento do app (sem permissão extra).
        IConfigurationProvider config = Configuration.getInstance();
        config.setUserAgentValue(agenteDeUsuario());
        File base = new File(getCacheDir(), "osmdroid");
        File tiles = new File(base, "tiles");
        if (tiles.isDirectory() || tiles.mkdirs()) {
            config.setOsmdroidBasePath(base);
            config.setOsmdroidTileCache(tiles);
        }

        setContentView(R.layout.activity_mapa);
        Ui.aplicarInsets(findViewById(R.id.raiz), true, true);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        Usuario usuario = Sessao.usuario(this);
        if (usuario == null) {
            finish();
            return;
        }
        cidade = new ViewModelProvider(this).get(UsuarioViewModel.class).buscarCidade(usuario.cidadeId);

        mapa = findViewById(R.id.mapa);
        mapa.setTileSource(TileSourceFactory.MAPNIK);
        mapa.setMultiTouchControls(true);
        // A licença dos dados do OpenStreetMap exige a atribuição visível sobre o mapa.
        mapa.getOverlays().add(new CopyrightOverlay(this));

        if (getIntent().getBooleanExtra(EXTRA_ESCOLHER, false)) {
            toolbar.setTitle(R.string.mapa_escolher_titulo);
            prepararEscolha();
        } else {
            DenunciaViewModel denuncias = new ViewModelProvider(this).get(DenunciaViewModel.class);
            denuncias.getDenuncias().observe(this, this::mostrarDenuncias);
            denuncias.carregarLista(usuario.id);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapa != null) {
            mapa.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapa != null) {
            mapa.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapa != null) {
            mapa.onDetach();
        }
    }

    // ---- Modo escolher ----

    private void prepararEscolha() {
        findViewById(R.id.mira).setVisibility(View.VISIBLE);
        View confirmar = findViewById(R.id.btn_confirmar);
        confirmar.setVisibility(View.VISIBLE);

        Intent in = getIntent();
        if (in.hasExtra(EXTRA_LAT) && in.hasExtra(EXTRA_LNG)) {
            centralizar(new GeoPoint(in.getDoubleExtra(EXTRA_LAT, 0), in.getDoubleExtra(EXTRA_LNG, 0)),
                    ZOOM_PONTO);
        } else {
            centralizarNaCidade();
        }
        confirmar.setOnClickListener(v -> {
            org.osmdroid.api.IGeoPoint centro = mapa.getMapCenter();
            setResult(RESULT_OK, new Intent()
                    .putExtra(EXTRA_LAT, centro.getLatitude())
                    .putExtra(EXTRA_LNG, centro.getLongitude()));
            finish();
        });
    }

    // ---- Modo ver ----

    private void mostrarDenuncias(List<Denuncia> todas) {
        List<GeoPoint> pontos = new ArrayList<>();
        long foco = getIntent().getLongExtra(Ui.EXTRA_ID, -1);
        Denuncia focada = null;
        Drawable icone = iconeDoMarcador();
        for (Denuncia d : todas) {
            if (!d.temLocalizacao()) {
                continue;
            }
            GeoPoint ponto = new GeoPoint(d.latitude, d.longitude);
            pontos.add(ponto);
            Marker marcador = new Marker(mapa);
            marcador.setPosition(ponto);
            marcador.setIcon(icone);
            marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marcador.setTitle(d.categoria);
            marcador.setOnMarkerClickListener((m, view) -> {
                mostrarCartao(d);
                view.getController().animateTo(m.getPosition());
                return true;
            });
            mapa.getOverlays().add(marcador);
            if (d.id == foco) {
                focada = d;
            }
        }

        if (focada != null) {
            centralizar(new GeoPoint(focada.latitude, focada.longitude), ZOOM_PONTO);
            mostrarCartao(focada);
        } else if (pontos.size() > 1) {
            BoundingBox caixa = BoundingBox.fromGeoPoints(pontos);
            mapa.post(() -> mapa.zoomToBoundingBox(caixa, false, 120));
        } else if (pontos.size() == 1) {
            centralizar(pontos.get(0), ZOOM_PONTO);
        } else {
            centralizarNaCidade();
            Toast.makeText(this, R.string.mapa_vazio, Toast.LENGTH_LONG).show();
        }
    }

    private void mostrarCartao(Denuncia d) {
        findViewById(R.id.card).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.card_titulo)).setText(d.categoria);
        ((TextView) findViewById(R.id.card_detalhe)).setText(
                getString(R.string.protocolo_rotulo, d.getProtocolo()) + " · " + d.endereco);
        Ui.aplicarStatus(findViewById(R.id.card_status), d.status);
        findViewById(R.id.btn_abrir).setOnClickListener(v -> startActivity(
                new Intent(this, DenunciaDetalheActivity.class).putExtra(Ui.EXTRA_ID, d.id)));
    }

    /** Ex.: "minhaCidade/1.0 (Android; prototipo academico)". */
    private String agenteDeUsuario() {
        String versao = "1.0";
        try {
            versao = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignorada) {
            // mantém a versão padrão
        }
        return getString(R.string.app_name) + "/" + versao + " (Android; prototipo academico)";
    }

    /** Alfinete do app (o ícone padrão do osmdroid é uma mão verde), desenhado em 40 dp. */
    private Drawable iconeDoMarcador() {
        Drawable pin = ContextCompat.getDrawable(this, R.drawable.ic_pin).mutate();
        pin.setTint(ContextCompat.getColor(this, R.color.status_aberta));
        int px = Math.round(40 * getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        pin.setBounds(0, 0, px, px);
        pin.draw(new Canvas(bitmap));
        return new BitmapDrawable(getResources(), bitmap);
    }

    // ---- Câmera ----

    private void centralizar(GeoPoint ponto, double zoom) {
        mapa.getController().setZoom(zoom);
        mapa.getController().setCenter(ponto);
    }

    private void centralizarNaCidade() {
        if (cidade != null && cidade.latitude != null && cidade.longitude != null) {
            centralizar(new GeoPoint(cidade.latitude, cidade.longitude), ZOOM_CIDADE);
        } else {
            centralizar(new GeoPoint(-14.2350, -51.9253), 4); // visão geral do Brasil
        }
    }
}
