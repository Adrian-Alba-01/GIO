package com.examp.gio.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.examp.gio.R;
import com.examp.gio.models.Incidencia;

import java.util.List;

public class IncidenciaAdapter extends RecyclerView.Adapter<IncidenciaAdapter.IncidenciaViewHolder> {

    private final List<Incidencia> incidencias;

    public IncidenciaAdapter(List<Incidencia> incidencias) {
        this.incidencias = incidencias;
    }

    @NonNull
    @Override
    public IncidenciaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incidencia, parent, false);
        return new IncidenciaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidenciaViewHolder holder, int position) {
        Incidencia inc = incidencias.get(position);

        holder.tvDescripcion.setText(inc.descripcion);
        holder.tvFecha.setText(inc.fecha);
        holder.tvUsuario.setText("Reportado por: " + inc.nombreUsuario);

        String estadoTexto;
        int color;
        switch (inc.estado) {
            case "resuelta":
                estadoTexto = "Resuelta";
                color = Color.parseColor("#4CAF50");
                break;
            case "en_proceso":
                estadoTexto = "En proceso";
                color = Color.parseColor("#FFC107");
                break;
            case "pendiente":
            default:
                estadoTexto = "Pendiente";
                color = Color.parseColor("#F44336");
                break;
        }
        holder.tvEstado.setText(estadoTexto);
        holder.tvEstado.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return incidencias.size();
    }

    static class IncidenciaViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion, tvFecha, tvEstado, tvUsuario;

        IncidenciaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionIncidencia);
            tvFecha       = itemView.findViewById(R.id.tvFechaIncidencia);
            tvEstado      = itemView.findViewById(R.id.tvEstadoIncidencia);
            tvUsuario     = itemView.findViewById(R.id.tvUsuarioIncidencia);
        }
    }
}