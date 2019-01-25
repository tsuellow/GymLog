package com.example.android.gymlog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.gymlog.data.GymDatabase;
import com.example.android.gymlog.data.PaymentEntry;

import java.text.SimpleDateFormat;
import java.util.List;

public class CpPaymentsAdapter extends RecyclerView.Adapter<CpPaymentsAdapter.ViewHolder> {


    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mFrom, mUntil, mProduct, mAmount;
        private Button mDelete;

        public ViewHolder(View itemView){
            super(itemView);

            mFrom= (TextView) itemView.findViewById(R.id.tv_from_cp_pay);
            mUntil= (TextView) itemView.findViewById(R.id.tv_to_cp_pay);
            mProduct = (TextView) itemView.findViewById(R.id.tv_product_cp_pay);
            mAmount=(TextView) itemView.findViewById(R.id.tv_amount_cp_pay);
            mDelete=(Button) itemView.findViewById(R.id.bt_delete_cp_pay);
        }
    }

    private List<PaymentEntry> mPayments;
    private Context mContext;
    private GymDatabase mDb;
    public CpPaymentsAdapter(Context context, GymDatabase database){
        mDb=database;
        mContext=context;
    }

    //define method to get and set current data source. usefull when clicking on adapter item
    public void setPayments(List<PaymentEntry> payments){
        mPayments=payments;
        notifyDataSetChanged();
    }

    public List<PaymentEntry> getPayments(){
        return mPayments;
    }


    @NonNull
    @Override
    public CpPaymentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View paymentView=inflater.inflate(R.layout.adapter_cp_payments_view,viewGroup,false);

        CpPaymentsAdapter.ViewHolder viewHolder=new CpPaymentsAdapter.ViewHolder(paymentView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CpPaymentsAdapter.ViewHolder viewHolder, int i) {
        final PaymentEntry payment=mPayments.get(i);
        final int paymentId=payment.getId();

        //now find the tvs in the viewholder and assign them the correct text
        final String from = new SimpleDateFormat("YYYY-MM-dd").format(payment.getPaidFrom());
        final String until = new SimpleDateFormat("YYYY-MM-dd").format(payment.getPaidUntil());
        viewHolder.mFrom.setText(from);
        viewHolder.mUntil.setText(until);
        viewHolder.mProduct.setText(payment.getProduct());
        final String amount="USD "+Math.round(payment.getAmountUsd());
        viewHolder.mAmount.setText(amount);


        viewHolder.mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(mContext);
                final AlertDialog alertDialog=builder.create();
                alertDialog.setTitle(mContext.getString(R.string.attention));
                alertDialog.setMessage(mContext.getString(R.string.are_you_sure)+" "+amount+" "+mContext.getString(R.string.payment_from_period)+
                        " "+from+" "+mContext.getString(R.string.until)+" "+until+" "+mContext.getString(R.string.question_marc));
                alertDialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, mContext.getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        invalidatePayment(payment);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, mContext.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        if (mPayments==null){
            return  0;
        }else {
            return mPayments.size();
        }
    }

    private void invalidatePayment(final PaymentEntry payment){
        payment.setIsValid(0);
        if (payment.getSyncStatus()==1) {
            payment.setSyncStatus(2);
        }
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.paymentDao().updatePayment(payment);
            }
        });
    }
}
