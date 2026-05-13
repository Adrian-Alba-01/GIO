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

public class TareaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TAREA   = 0;
    private static final int TYPE_VER_MAS = 1;

    // Cuántas tareas mostrar antes del botón "Ver más"
    private static final int LIMITE_VISIBLE = 3;

    private final List<Tarea> lista;
    private final boolean     modoDetalle;   // true → muestra descripción y fechas
    private       boolean     expandida = false;

    public interface OnCompletarListener {
        void onCompletar(int idTarea, int position);
    }

    private OnCompletarListener completarListener;

    public void setOnCompletarListener(OnCompletarListener l) {
        this.completarListener = l;
    }

    public TareaAdapter(List<Tarea> lista, boolean modoDetalle) {
        this.lista       = lista;
        this.modoDetalle = modoDetalle;
    }

    // ─────────────────────────────────────────────
    // Cuántos items renderizamos realmente
    // ─────────────────────────────────────────────
    @Override
    public int getItemCount() {
        int total = lista.size();

        if (total <= LIMITE_VISIBLE || expandida) {
            return total;          // todas las tareas, sin botón extra
        }
        return LIMITE_VISIBLE + 1; // las primeras 3 + fila "Ver más"
    }

    @Override
    public int getItemViewType(int position) {
        if (!expandida && lista.size() > LIMITE_VISIBLE && position == LIMITE_VISIBLE) {
            return TYPE_VER_MAS;
        }
        return TYPE_TAREA;
    }

    // ─────────────────────────────────────────────
    // ViewHolders
    // ─────────────────────────────────────────────
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_VER_MAS) {
            View v = inflater.inflate(R.layout.item_ver_mas_tareas, parent, false);
            return new VerMasViewHolder(v);
        }

        View v = inflater.inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VerMasViewHolder) {
            int restantes = lista.size() - LIMITE_VISIBLE;
            ((VerMasViewHolder) holder).bind(restantes);
            return;
        }

        TareaViewHolder vh    = (TareaViewHolder) holder;
        Tarea           tarea = lista.get(position);

        // ── Nombre ──────────────────────────────
        vh.tvNombre.setText(tarea.nombre);

        // ── Descripción ─────────────────────────
        if (modoDetalle && tarea.descripcion != null && !tarea.descripcion.isEmpty()) {
            vh.tvDescripcion.setText(tarea.descripcion);
            vh.tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            vh.tvDescripcion.setVisibility(View.GONE);
        }

        // ── Fechas ──────────────────────────────
        boolean tieneFechas = (tarea.fechaInicio != null && !tarea.fechaInicio.isEmpty())
                || (tarea.fechaFin   != null && !tarea.fechaFin.isEmpty());

        if (modoDetalle && tieneFechas) {
            String inicio = (tarea.fechaInicio != null) ? tarea.fechaInicio : "—";
            String fin    = (tarea.fechaFin    != null) ? tarea.fechaFin    : "—";
            vh.tvFechas.setText(inicio + " → " + fin);
            vh.tvFechas.setVisibility(View.VISIBLE);
            vh.llFechas.setVisibility(View.VISIBLE);
        } else {
            vh.llFechas.setVisibility(View.GONE);
        }

        // ── Estado: badge + barra lateral ───────
        int   colorBarra;
        int   colorBadge;
        String textoEstado;

        switch (tarea.estado) {
            case "en_proceso":
                colorBarra  = Color.parseColor("#2196F3"); // azul
                colorBadge  = Color.parseColor("#1565C0");
                textoEstado = "En proceso";
                break;
            case "completada":
                colorBarra  = Color.parseColor("#4CAF50"); // verde
                colorBadge  = Color.parseColor("#2E7D32");
                textoEstado = "Completada";
                break;
            default: // pendiente
                colorBarra  = Color.parseColor("#FFC107"); // ámbar
                colorBadge  = Color.parseColor("#B45309");
                textoEstado = "Pendiente";
                break;
        }

        vh.viewIndicador.setBackgroundColor(colorBarra);
        vh.tvEstado.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(colorBadge)
        );
        vh.tvEstado.setText(textoEstado);

        // ── Botón completar ──────────────────────
        if (!"completada".equals(tarea.estado)) {
            vh.btnCompletar.setVisibility(View.VISIBLE);
            vh.btnCompletar.setOnClickListener(v -> {
                if (completarListener != null) {
                    completarListener.onCompletar(tarea.idTarea, holder.getAdapterPosition());
                }
            });
        } else {
            vh.btnCompletar.setVisibility(View.GONE);
        }
    }

    // ─────────────────────────────────────────────
    // ViewHolder — Tarea
    // ─────────────────────────────────────────────
    static class TareaViewHolder extends RecyclerView.ViewHolder {
        View     viewIndicador;
        TextView tvNombre, tvDescripcion, tvFechas, tvEstado, btnCompletar;
        View     llFechas;

        TareaViewHolder(@NonNull View v) {
            super(v);
            viewIndicador = v.findViewById(R.id.viewIndicadorTarea);
            tvNombre      = v.findViewById(R.id.tvNombreTarea);
            tvDescripcion = v.findViewById(R.id.tvDescripcionTarea);
            tvFechas      = v.findViewById(R.id.tvFechasTarea);
            tvEstado      = v.findViewById(R.id.tvEstadoTarea);
            btnCompletar  = v.findViewById(R.id.btnCompletarTarea);
            llFechas      = v.findViewById(R.id.llFechas);
        }
    }

    // ─────────────────────────────────────────────
    // ViewHolder — Ver más
    // ─────────────────────────────────────────────
    class VerMasViewHolder extends RecyclerView.ViewHolder {
        TextView tvVerMas;

        VerMasViewHolder(@NonNull View v) {
            super(v);
            tvVerMas = v.findViewById(R.id.tvVerMasTareas);
        }

        void bind(int restantes) {
            tvVerMas.setText("Ver " + restantes + " tarea" + (restantes == 1 ? "" : "s") + " más ↓");
            tvVerMas.setOnClickListener(v -> {
                expandida = true;
                notifyDataSetChanged();
            });
        }
    }
}