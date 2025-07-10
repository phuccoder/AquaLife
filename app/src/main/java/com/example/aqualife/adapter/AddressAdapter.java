package com.example.aqualife.adapter;

import android.accounts.Account;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aqualife.R;
import com.example.aqualife.model.AccountInfor;
import com.example.aqualife.model.AddressResponse;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.AddressViewHolder> {
    private List<AddressResponse> addressList;
    private AccountInfor account;
    private int selectedPosition = -1;
    private final OnAddressSelectListener listener;

    public interface OnAddressSelectListener {
        void onSelect(AddressResponse address);
        void onEdit(AddressResponse address);
    }

    public AddressAdapter(List<AddressResponse> list, AccountInfor account, OnAddressSelectListener listener) {
        this.addressList = list;
        this.account = account;
        this.listener = listener;
        for (int i = 0; i < list.size(); i++) {
            if (Boolean.TRUE.equals(list.get(i).isDefault())) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_address_item, parent, false);
        return new AddressViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressResponse addr = addressList.get(position);

        holder.txtFullNamePhone.setText(account.getFullName() +" | " + addr.getShippingPhone());
        holder.txtAddress.setText(addr.getShippingAddress());
        holder.txtIsDefault.setVisibility(Boolean.TRUE.equals(addr.isDefault()) ? View.VISIBLE : View.GONE);

        holder.rbDefault.setChecked(position == selectedPosition);

        holder.rbDefault.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                selectedPosition = currentPos;
                listener.onSelect(addressList.get(currentPos));
                notifyDataSetChanged();
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                listener.onEdit(addressList.get(currentPos));
            }
        });
    }

    @Override
    public int getItemCount() {
        return addressList.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbDefault;
        TextView txtFullNamePhone, txtAddress, txtIsDefault, btnEdit;

        public AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            rbDefault = itemView.findViewById(R.id.radioDefault);
            txtFullNamePhone = itemView.findViewById(R.id.txtNamePhone);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtIsDefault = itemView.findViewById(R.id.txtDefaultLabel);
            btnEdit = itemView.findViewById(R.id.tvEdit);
        }
    }
}
