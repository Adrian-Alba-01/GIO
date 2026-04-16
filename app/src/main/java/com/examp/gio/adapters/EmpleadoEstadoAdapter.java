package com.examp.gio.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.examp.gio.R;
import com.examp.gio.models.EmpleadoEstado;

import java.util.List;

public class EmpleadoEstadoAdapter extends RecyclerView.Adapter<EmpleadoEstadoAdapter.EmpleadoViewHolder> {

    private final List<EmpleadoEstado> empleados;

    public EmpleadoEstadoAdapter(List<EmpleadoEstado> empleados) {
        this.empleados = empleados;
    }

    @NonNull
    @Override
    public EmpleadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_empleado_estado, parent, false);
        return new EmpleadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmpleadoViewHolder holder, int position) {
        EmpleadoEstado emp = empleados.get(position);
        holder.tvNombre.setText(emp.nombre);

        String estadoTexto;
        int color;
        switch (emp.estado) {
            case "activo":
                estadoTexto = "Activo";
                color = Color.parseColor("#4CAF50"); // verde
                break;
            case "descansando":
                estadoTexto = "Descansando";
                color = Color.parseColor("#FFC107"); // amarillo
                break;
            case "ausente":
            default:
                estadoTexto = "Ausente";
                color = Color.parseColor("#F44336"); // rojo
                break;
        }

        holder.tvEstado.setText("● " + estadoTexto);
        holder.tvEstado.setTextColor(color);
        holder.tvIndicador.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return empleados.size();
    }

    static class EmpleadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvEstado;
        View tvIndicador;

        EmpleadoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre     = itemView.findViewById(R.id.tvNombreEmpleado);
            tvEstado     = itemView.findViewById(R.id.tvEstadoEmpleado);
            tvIndicador  = itemView.findViewById(R.id.viewIndicador);
        }
    }
}