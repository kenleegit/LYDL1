package net.agnusvox.lydl1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.List;

/**
 * Created by ken on 6/9/2017.
 * Reference: https://www.ptt.cc/bbs/AndroidDev/M.1356579180.A.F98.html
 * https://pastebin.com/GxBfpdCB
 */

public class MyAdapter extends BaseAdapter {
    private LayoutInflater myInflater;
    private List<PrfProgram> prfprograms;

    public MyAdapter(Context context, List<PrfProgram> ListOfPrfPrograms){
        this.myInflater = LayoutInflater.from(context);
        this.prfprograms = ListOfPrfPrograms;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView txtTitle;
        //TextView txtPicture;
        ToggleButton btnLiked;

        //2017-09-17 No longer using Picture field
        //public ViewHolder(TextView txtTitle, TextView txtPicture, ToggleButton btnLiked){
        public ViewHolder(TextView txtTitle, ToggleButton btnLiked){
            this.txtTitle = txtTitle;
            //this.txtPicture = txtPicture;
            this.btnLiked = btnLiked;
        }
    }

    @Override
    public int getCount() {
        return prfprograms.size();
    }

    @Override
    public Object getItem(int arg0) {
        return prfprograms.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return prfprograms.indexOf(getItem(position));
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView==null){
            convertView = myInflater.inflate(R.layout.list_item_prg, null);
            holder = new ViewHolder(
                    (TextView) convertView.findViewById(R.id.title_prg),
                    //(TextView) convertView.findViewById(R.id.picture),
                    (ToggleButton) convertView.findViewById(R.id.likebutton)
            );
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        PrfProgram prfProgram = (PrfProgram)getItem(position);
        //0 = movie, 1 = title, 2 = nine
        //int colors[] = {Color.BLACK, Color.WHITE,Color.YELLOW};

        holder.txtTitle.setText(prfProgram.getTitle());

        /* Hard code at list_item_prg
        holder.txtTitle.setTextColor(colors[2]);
        holder.txtTitle.setBackgroundColor(colors[0]);
        */

        /*
        holder.txtPicture.setText(prfProgram.getPicture());
        holder.txtPicture.setTextColor(colors[0]);
        holder.txtPicture.setBackgroundColor(colors[1]);
        */

        holder.btnLiked.setChecked(prfProgram.getLiked());
        holder.btnLiked.setSelected(prfProgram.getLiked());
        holder.btnLiked.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Anonymous Inner Class
                //Reference: https://stackoverflow.com/questions/18447295/android-listview-onclicklistener-custom-adapter
                Log.d("n", "onClick is fired at position " + position);

                //toggleLike flip the state of the attribute
                // and it has an effect to "remember" the state of button. If absent, state resets when it leaves the screen
                prfprograms.get(position).toggleLiked();

                //Start to update database of the new state
                /*boolean likedNow = prfprograms.get(position).getLiked();
                int pidNow = prfprograms.get(position).getPid();
                DbOpenHelper mHelper = new DbOpenHelper(ImportProgramsActivity.this);
                SQLiteDatabase mDB = null;
                mDB = mHelper.getWritableDatabase();
                mDB.execSQL("UPDATE PrfPrograms SET _LIKED = " + likedNow + " WHERE _PID = " + pidNow);
                mDB.close();
                */
            }
        });


        return convertView;
    }
}
