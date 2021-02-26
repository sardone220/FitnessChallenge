package it.fitnesschallenge.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import it.fitnesschallenge.HomeActivity;
import it.fitnesschallenge.R;
import it.fitnesschallenge.model.room.entity.Exercise;

public class ExerciseList {

    private  List<Exercise> mList;
    private Context mContext;

    public ExerciseList(){
        mContext = HomeActivity.getHomeActivityContext();
        createContent();
    }

    private void createContent(){
        mList = new ArrayList<>();
        mList.add(new Exercise(R.mipmap.curl_2_manubri,
                mContext.getString(R.string.curl_hummer),
                mContext.getString(R.string.curl_hammer_description)));
        mList.add(new Exercise(R.mipmap.curl_bilanciere,
                mContext.getString(R.string.barbell_curl),
                mContext.getString(R.string.barbell_curl_description)));
        mList.add(new Exercise(R.mipmap.curl_concentrato,
                mContext.getString(R.string.alternate_curl),
                mContext.getString(R.string.alternate_curl_description)));
        mList.add(new Exercise(R.mipmap.pettorali_panca_inclinata,
                mContext.getString(R.string.dumbbell_flyes),
                mContext.getString(R.string.bumbbell_flyes_description)));
        mList.add(new Exercise(R.mipmap.pectoral,
                mContext.getString(R.string.pectoral_machine),
                mContext.getString(R.string.pectoral_machine_description)));
        mList.add(new Exercise(R.mipmap.panca_piana,
                mContext.getString(R.string.bench_press),
                mContext.getString(R.string.bench_press_description)));
    }

    public List<Exercise> getList(){
        return mList;
    }
}
