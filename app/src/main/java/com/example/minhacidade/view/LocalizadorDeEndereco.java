package com.example.minhacidade.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;

import com.example.minhacidade.R;

import java.util.concurrent.ExecutorService;

/**
 * "Usar minha localização": pede a permissão, obtém a posição atual uma única vez (só depois de o
 * cidadão tocar no botão) e a converte em endereço. Precisa ser criado no onCreate da activity.
 */
final class LocalizadorDeEndereco {

    interface Ouvinte {
        void aoIniciar();

        /** O endereço pode ser o texto do geocoder ou, se ele falhar, as coordenadas. */
        void aoEncontrar(String endereco, double latitude, double longitude);

        void aoFalhar(int mensagem);
    }

    private final AppCompatActivity activity;
    private final ExecutorService executor;
    private final Ouvinte ouvinte;
    private final ActivityResultLauncher<String[]> pedirPermissao;
    private CancellationSignal cancelamento;

    LocalizadorDeEndereco(AppCompatActivity activity, ExecutorService executor, Ouvinte ouvinte) {
        this.activity = activity;
        this.executor = executor;
        this.ouvinte = ouvinte;
        this.pedirPermissao = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                    if (temPermissao()) {
                        obterPosicao();
                    } else {
                        ouvinte.aoFalhar(R.string.erro_endereco_localizacao_negada);
                    }
                });
    }

    void buscar() {
        if (temPermissao()) {
            obterPosicao();
        } else {
            pedirPermissao.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    void cancelar() {
        if (cancelamento != null) {
            cancelamento.cancel();
            cancelamento = null;
        }
    }

    private boolean temPermissao() {
        return permitido(Manifest.permission.ACCESS_FINE_LOCATION)
                || permitido(Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    private boolean permitido(String permissao) {
        return ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission") // temPermissao() já foi verificado por quem chama.
    private void obterPosicao() {
        LocationManager gerente = activity.getSystemService(LocationManager.class);
        String provedor = null;
        if (permitido(Manifest.permission.ACCESS_FINE_LOCATION)
                && gerente.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provedor = LocationManager.GPS_PROVIDER;
        } else if (gerente.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            provedor = LocationManager.NETWORK_PROVIDER;
        }
        if (provedor == null) {
            ouvinte.aoFalhar(R.string.erro_endereco_localizacao_desligada);
            return;
        }
        ouvinte.aoIniciar();
        CancellationSignal sinal = new CancellationSignal();
        cancelamento = sinal;
        LocationManagerCompat.getCurrentLocation(gerente, provedor, sinal,
                ContextCompat.getMainExecutor(activity), local -> {
                    cancelamento = null;
                    if (activity.isDestroyed()) {
                        return;
                    }
                    if (local == null) {
                        ouvinte.aoFalhar(R.string.erro_endereco_localizacao_falhou);
                        return;
                    }
                    traduzir(local.getLatitude(), local.getLongitude());
                });
    }

    /** O geocoder usa a rede, então roda fora da thread principal. */
    private void traduzir(double latitude, double longitude) {
        executor.execute(() -> {
            String texto = EnderecoTexto.resolver(activity, latitude, longitude);
            String resultado = texto != null ? texto : EnderecoTexto.coordenadas(latitude, longitude);
            activity.runOnUiThread(() -> {
                if (!activity.isDestroyed()) {
                    ouvinte.aoEncontrar(resultado, latitude, longitude);
                }
            });
        });
    }
}
