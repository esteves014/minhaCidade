package com.example.minhacidade.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Adapter genérico: cada tela informa o layout do item e como preenchê-lo. */
public class ListaAdapter<T> extends RecyclerView.Adapter<ListaAdapter.Holder> {

    public interface Binder<T> {
        void vincular(View item, T dado);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        Holder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private final int layout;
    private final Binder<T> binder;
    private List<T> dados = new ArrayList<>();

    public ListaAdapter(int layout, Binder<T> binder) {
        this.layout = layout;
        this.binder = binder;
    }

    public void setDados(List<T> novos) {
        dados = novos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        binder.vincular(holder.itemView, dados.get(position));
    }

    @Override
    public int getItemCount() {
        return dados.size();
    }
}
