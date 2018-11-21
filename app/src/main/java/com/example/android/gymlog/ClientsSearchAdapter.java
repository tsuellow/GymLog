package com.example.android.gymlog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.example.android.gymlog.data.ClientEntry;

import java.util.List;

public class ClientsSearchAdapter extends RecyclerView.Adapter<ClientsSearchAdapter.ViewHolder> {



    //this defines the viewholder and finds and holds on to all its elements
    public class ViewHolder extends RecyclerView.ViewHolder  {
        private TextView mFirstName;
        private TextView mLastName;
        private ConstraintLayout mParentLayout;

        private TextView mOptionsMenu;
        private ImageView mProfileImage;
        //private int mClientId;

        public ViewHolder(View itemView){
            super(itemView);

            mFirstName= (TextView) itemView.findViewById(R.id.tv_first_name_lim);
            mLastName= (TextView) itemView.findViewById(R.id.tv_last_name_lim);
            mParentLayout=(ConstraintLayout)  itemView.findViewById(R.id.cl_parent_layout);

            mOptionsMenu=(TextView) itemView.findViewById(R.id.tv_options_menu_lim);
            mProfileImage=(ImageView) itemView.findViewById(R.id.iv_profile_image_lim);

        }

    }

    //now define the data source create a method to supply data from outside the adapter
    private List<ClientEntry> mClients;
    private Context mContext;
    public ClientsSearchAdapter(Context context){
        //mClients=clients;
        mContext=context;
    }

    //define method to get and set current data source. usefull when clicking on adapter item
    public void setClients(List<ClientEntry> clients){
        mClients=clients;
        notifyDataSetChanged();
    }

    public List<ClientEntry> getClients(){
        return mClients;
    }


    //now override the main methods
    @NonNull
    @Override
    public ClientsSearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater=LayoutInflater.from(mContext);

        //now inflate the view
        View clientView=inflater.inflate(R.layout.limited_client_search_view,viewGroup,false);

        ClientsSearchAdapter.ViewHolder viewHolder=new ClientsSearchAdapter.ViewHolder(clientView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ClientsSearchAdapter.ViewHolder viewHolder, int i) {
        final ClientEntry client=mClients.get(i);
        //mClientId=client.getId();

        //now find the tvs in the viewholder and assign them the correct text
        viewHolder.mFirstName.setText(client.getFirstName());
        viewHolder.mLastName.setText(client.getLastName());

        viewHolder.mParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //retrieveClientData(client.getId());
            }
        });

        if (client.getPhoto()!=null && !client.getPhoto().isEmpty()) {
            String mPath =client.getPhoto().trim();
            int targetW = 96;
            int targetH = 128;

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
//            bmOptions.inPurgeable = true;
            Bitmap bitmap = BitmapFactory.decodeFile(mPath, bmOptions);
            int width  = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = (height > width) ? width : height;
            int newHeight = (height > width)? height - ( height - width) : height;
            int cropW = (width - height) / 2;
            cropW = (cropW < 0)? 0: cropW;
            int cropH = (height - width) / 2;
            cropH = (cropH < 0)? 0: cropH;
            Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
            RoundedBitmapDrawable roundedBitmapDrawable=RoundedBitmapDrawableFactory.create(mContext.getResources(),cropImg);
            roundedBitmapDrawable.setCircular(true);
            viewHolder.mProfileImage.setImageDrawable(roundedBitmapDrawable);
        }else{
            viewHolder.mProfileImage.setImageResource(android.R.drawable.ic_menu_camera);
        }



    }

    @Override
    public int getItemCount() {
        if (mClients==null){
            return  0;
        }else {
            return mClients.size();
        }
    }









}