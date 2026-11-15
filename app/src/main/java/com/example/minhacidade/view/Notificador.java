package com.example.minhacidade.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.example.minhacidade.R;
import com.example.minhacidade.model.Cidade;
import com.example.minhacidade.model.Denuncia;
import com.example.minhacidade.model.Parecer;

/** Avisa o cidadão, com uma notificação do Android, quando a prefeitura responde a uma denúncia. */
public final class Notificador {
    private static final String CANAL = "pareceres";

    private Notificador() {
    }

    public static void criarCanal(Context contexto) {
        NotificationChannel canal = new NotificationChannel(CANAL,
                contexto.getString(R.string.notif_canal_nome), NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription(contexto.getString(R.string.notif_canal_descricao));
        contexto.getSystemService(NotificationManager.class).createNotificationChannel(canal);
    }

    @SuppressLint("MissingPermission") // A permissão (Android 13+) é verificada logo abaixo.
    public static void notificarParecer(Context contexto, Denuncia denuncia, Parecer parecer, Cidade cidade) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(contexto,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return; // o cidadão ainda verá o selo dentro do app
        }
        Intent abrir = new Intent(contexto, DenunciaDetalheActivity.class)
                .putExtra(Ui.EXTRA_ID, denuncia.id);
        PendingIntent intencao = TaskStackBuilder.create(contexto)
                .addNextIntentWithParentStack(abrir)
                .getPendingIntent((int) denuncia.id, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String detalhe = contexto.getString(R.string.notif_texto_grande,
                contexto.getString(R.string.protocolo_rotulo, denuncia.getProtocolo()) + " · " + denuncia.categoria,
                parecer.texto);
        Notification notificacao = new NotificationCompat.Builder(contexto, CANAL)
                .setSmallIcon(R.drawable.ic_report)
                .setContentTitle(contexto.getString(R.string.notif_titulo, cidade.nome))
                .setContentText(parecer.texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(detalhe))
                .setContentIntent(intencao)
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat.from(contexto).notify((int) parecer.id, notificacao);
    }
}
