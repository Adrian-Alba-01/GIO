package com.examp.gio.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.examp.gio.R;
import com.examp.gio.models.Tarea;

import java.util.List;

public class TareaAdapter extends RecyclerView.Adapter<TareaAdapter.TareaViewHolder> {

    private final List<Tarea> tareas;
    private final boolean mostrarDetalle; // true = vista completa (TareasActivity), false = compacta (Home)

    public TareaAdapter(List<Tarea> tareas, boolean mostrarDetalle) {
        this.tareas = tareas;
        this.mostrarDetalle = mostrarDetalle;
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = tareas.get(position);
        holder.tvNombre.setText(tarea.nombre);

        if (mostrarDetalle) {
            if (holder.tvDescripcion != null) {
                holder.tvDescripcion.setVisibility(View.VISIBLE);
                holder.tvDescripcion.setText(tarea.descripcion);
            }
            if (holder.tvFechas != null) {
                holder.tvFechas.setVisibility(View.VISIBLE);
                holder.tvFechas.setText(tarea.fechaInicio + " → " + tarea.fechaFin);
            }
        } else {
            if (holder.tvDescripcion != null) holder.tvDescripcion.setVisibility(View.GONE);
            if (holder.tvFechas != null) holder.tvFechas.setVisibility(View.GONE);
        }

        // Color según estado
        if (holder.tvEstado != null) {
            holder.tvEstado.setText(tarea.estado);
            switch (tarea.estado) {
                case "en_proceso":
                    holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"));
                    break;
                case "pendiente":
                    holder.tvEstado.setTextColor(Color.parseColor("#FFC107"));
                    break;
                case "completada":
                    holder.tvEstado.setTextColor(Color.parseColor("#2196F3"));
                    break;
                default:
                    holder.tvEstado.setTextColor(Color.GRAY);
            }
        }
    }

    @Override
    public int getItemCount() {
        return tareas.size();
    }

    static class TareaViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDescripcion, tvEstado, tvFechas;

        TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre      = itemView.findViewById(R.id.tvNombreTarea);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTarea);
            tvEstado      = itemView.findViewById(R.id.tvEstadoTarea);
            tvFechas      = itemView.findViewById(R.id.tvFechasTarea);
        }
    }
}