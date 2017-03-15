package com.pixeldp.wellsee;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pixeldp.http.PixelAPI;
import com.pixeldp.http.PixelService;
import com.pixeldp.model.EyeListModel;
import com.pixeldp.model.EyeModel;
import com.pixeldp.model.RecordModel;
import com.pixeldp.model.ResponseModel;
import com.pixeldp.view.widgets.HoloCircleSeekBar;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordListActivity extends PixelActivity {
    final String[] text_levelForOpti = {"매우 낮음", "낮음", "낮음", "낮은 편", "낮은 편", "보통", "보통", "좋은 편", "좋은 편", "매우 좋음"};
    final String[] text_probability = {"없음", "낮음", "높음", "있음"};

    @BindView(R.id.record_textview_opti_result_value)
    TextView textView_levelForOpti;
    @BindView(R.id.record_textview_opti_result)
    TextView textView_opti_result;

    @BindView(R.id.record_textview_astigmatism_probability)
    TextView textView_astigmatism_probability;

    @BindView(R.id.record_textview_blindness_probability)
    TextView textView_colorBlindness_probability;

    @BindView(R.id.record_seekbar_opti_result)
    HoloCircleSeekBar seekbar_record_result;
    @BindView(R.id.record_seekbar_astigmatism_probability)
    HoloCircleSeekBar seekbar_record_astigmatism_probability;
    @BindView(R.id.record_seekbar_blindness_probability)
    HoloCircleSeekBar seekbar_record_colorblind_probability;

    ValueAnimator animator_visualAcuity;
    ValueAnimator animator_astigmatism;
    ValueAnimator animator_colorBlindness;

    ListView listView;
    ListViewAdapter listViewAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
        ButterKnife.bind(this);
        analyticsStart(this);

        getSupportActionBar().setElevation(0.0f);

        listViewAdapter = new ListViewAdapter(getApplicationContext());
        listView = (ListView) findViewById(R.id.record_list_listview);
        listView.setAdapter(listViewAdapter);

        EyeModelsReceiver eyeModelsReceiver = new EyeModelsReceiver();
        eyeModelsReceiver.execute();
    }

    private class EyeModelsReceiver extends AsyncTask<Void, Void, EyeListModel> {
        @Override
        protected EyeListModel doInBackground(Void... voids) {
            PixelAPI api = PixelService.getRetrofit(getApplicationContext());
            Call<EyeListModel> getEye = api.getEyeInfo();

            Response<EyeListModel> response = null;
            boolean isSuccessful = false;
            try {
                response = getEye.execute();
                isSuccessful = (response != null && response.isSuccessful() && response.body() != null && response.body().getCode() == 200);
            } catch (IOException e) {
                isSuccessful = false;
                e.printStackTrace();
            }

            if (!isSuccessful && response != null && response.body() != null) {
                Log.d("debugging_test", response.body().getMessage());
            }

            return (isSuccessful) ? response.body() : null;
        }

        @Override
        protected void onPostExecute(EyeListModel eyeListModel) {
            super.onPostExecute(eyeListModel);

            if (eyeListModel != null) {
                if (eyeListModel.getCode() == 200) {
                    ArrayList<EyeModel> eyeModels = eyeListModel.getEyeModels();
                    if (eyeModels.size() > 0) {
                        listViewAdapter.drawPanel(new RecordModel(eyeModels.get(0)));

                        for (EyeModel eyeModel : eyeModels) {
                            listViewAdapter.add(new RecordModel(eyeModel));
                        }

                        listViewAdapter.notifyDataSetChanged();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT);
                    Log.d("debugging_test", eyeListModel.getMessage());
                }
            }
        }
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<RecordModel> records;

        ListViewAdapter(Context context, ArrayList<RecordModel> records) {
            super();
            this.context = context;
            this.records = records;
        }

        ListViewAdapter(Context context) {
            this(context, new ArrayList<RecordModel>());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_record, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    drawPanel(records.get(position));
                }
            });

            RecordModel recordModel = records.get(position);
            holder.date.setText(recordModel.getDate());
            holder.level.setText((recordModel.getLevel_visualAcuity() + 1) + "단계");
            holder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                PixelAPI api = PixelService.getRetrofit(getApplicationContext());
                                Call<ResponseModel> delete = api.deleteEyeInfo(records.get(position).getId());
                                delete.enqueue(new Callback<ResponseModel>() {
                                    @Override
                                    public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                                        if (response != null && response.isSuccessful() && response.body() != null) {
                                            ResponseModel type = response.body();
                                            if (type.getCode() != 200) {
                                                Log.d("debugging_http", type.toString());
                                                Toast.makeText(getApplicationContext(), "서버에 일시적인 오류가 있습니다.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                listViewAdapter.remove(position);
                                                listViewAdapter.notifyDataSetChanged();
                                                listViewAdapter.drawPanel((RecordModel) listViewAdapter.getItem(0));
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseModel> call, Throwable t) {
                                        Toast.makeText(getApplicationContext(), "인터넷에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                        t.printStackTrace();
                                    }
                                });
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(RecordListActivity.this);
                    builder.setMessage("삭제 하시겠습니까?")
                            .setPositiveButton(Html.fromHtml("<font color='#009688'>네</font>"), dialogClickListener)
                            .setNegativeButton(Html.fromHtml("<font color='#009688'>아니오</font>"), dialogClickListener).show();
                }
            });


            return convertView;
        }

        @Override
        public int getCount() {
            return records.size();
        }

        @Override
        public Object getItem(int position) {
            return records.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        void add(RecordModel recordModel) {
            records.add(recordModel);
        }

        void remove(int position) {
            records.remove(position);
        }

        public void drawPanel(RecordModel recordModel) {
            textView_levelForOpti.setText(Integer.valueOf(recordModel.getLevel_visualAcuity() + 1).toString());
            textView_opti_result.setText(text_levelForOpti[recordModel.getLevel_visualAcuity()]);
            textView_astigmatism_probability.setText(text_probability[recordModel.getLevel_astigmatism()]);
            textView_colorBlindness_probability.setText(text_probability[recordModel.getLevel_colorBlindness()]);

            animator_visualAcuity = ValueAnimator.ofFloat(0, recordModel.getLevel_visualAcuity() * (100.0f / 9.0f));
            animator_astigmatism = ValueAnimator.ofFloat(0, recordModel.getLevel_astigmatism() * (100.0f / 3.0f));
            animator_colorBlindness = ValueAnimator.ofFloat(0, recordModel.getLevel_colorBlindness() * (100.0f / 3.0f));

            animator_visualAcuity.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    seekbar_record_result.setValue((Float) animation.getAnimatedValue());
                }
            });
            animator_astigmatism.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    seekbar_record_astigmatism_probability.setValue((Float) animation.getAnimatedValue());
                }
            });
            animator_colorBlindness.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    seekbar_record_colorblind_probability.setValue((Float) animation.getAnimatedValue());
                }
            });

            animator_visualAcuity.setDuration(300);
            animator_astigmatism.setDuration(300);
            animator_colorBlindness.setDuration(300);

            animator_visualAcuity.start();
            animator_astigmatism.start();
            animator_colorBlindness.start();
        }
    }

    public class ViewHolder {
        @Nullable
        @BindView(R.id.record_item_date)
        TextView date;
        @Nullable
        @BindView(R.id.record_item_level)
        TextView level;
        @Nullable
        @BindView(R.id.record_item_remove)
        LinearLayout remove;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
