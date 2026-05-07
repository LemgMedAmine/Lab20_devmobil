package com.example.numberbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<Contact> contacts;

    public ContactAdapter(List<Contact> contacts) {
        this.contacts = contacts;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Commentaire LEMGHILI Mohammed Amine: on utilise un layout personnalise pour une liste plus propre.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        String name = contact.getName() == null ? "" : contact.getName();
        String phone = contact.getPhone() == null ? "" : contact.getPhone();

        holder.tvContactName.setText(name);
        holder.tvContactPhone.setText(phone);
        holder.tvContactInitial.setText(getInitial(name));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void updateData(List<Contact> newContacts) {
        this.contacts = newContacts;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvContactInitial;
        TextView tvContactName;
        TextView tvContactPhone;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContactInitial = itemView.findViewById(R.id.tvContactInitial);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
        }
    }

    private String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }
        return name.trim().substring(0, 1).toUpperCase();
    }
}
