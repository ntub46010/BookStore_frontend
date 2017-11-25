package com.xy.psn.type;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.xy.psn.MainActivity;
import com.xy.psn.R;

import static com.xy.psn.MainActivity.context;
import static com.xy.psn.MainActivity.mViewPager;
import static com.xy.psn.data.MyHelper.fromClickDep;
import static com.xy.psn.data.MyHelper.setBoardTitle;
import static com.xy.psn.product.ProductHomeFrag.getBitmap;
import static com.xy.psn.product.ProductHomeFrag.isProductHomeShown;
import static com.xy.psn.product.ProductHomeFrag.myAdapter;
import static com.xy.psn.product.ProductHomeFrag.productTask;
import static com.xy.psn.product.ProductHomeFrag.recyclerView;

public class DepartmentFrag extends Fragment {
    private Button[] btnDep;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_department_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        btnDep = new Button[] {
                (Button) getView().findViewById(R.id.btnDepAll),
                (Button) getView().findViewById(R.id.btnDepGN),
                (Button) getView().findViewById(R.id.btnDepAI),
                (Button) getView().findViewById(R.id.btnDepFN),
                (Button) getView().findViewById(R.id.btnDepFT),
                (Button) getView().findViewById(R.id.btnDepIB),
                (Button) getView().findViewById(R.id.btnDepBM),
                (Button) getView().findViewById(R.id.btnDepIM),
                (Button) getView().findViewById(R.id.btnDepAFL),
                (Button) getView().findViewById(R.id.btnDepCD),
                (Button) getView().findViewById(R.id.btnDepDM),
                (Button) getView().findViewById(R.id.btnDepCC)
        };

        //指派偵聽器
        for (int i=0; i<btnDep.length; i++) {
            final int index = i;

            btnDep[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDepFrag(btnDep[index].getId());
                }
            });
        }
    }

    private void showDepFrag(int btnId) {
        //根據不同按鈕(ID)設定看板名稱
        switch (btnId) {
            case R.id.btnDepAll:
                MainActivity.board = "-1";
                break;
            case R.id.btnDepGN:
                MainActivity.board = "00";
                break;
            case R.id.btnDepAI:
                MainActivity.board = "01";
                break;
            case R.id.btnDepFN:
                MainActivity.board = "02";
                break;
            case R.id.btnDepFT:
                MainActivity.board = "03";
                break;
            case R.id.btnDepIB:
                MainActivity.board = "04";
                break;
            case R.id.btnDepBM:
                MainActivity.board = "05";
                break;
            case R.id.btnDepIM:
                MainActivity.board = "06";
                break;
            case R.id.btnDepAFL:
                MainActivity.board = "07";
                break;
            case R.id.btnDepCD:
                MainActivity.board = "A";
                break;
            case R.id.btnDepCC:
                MainActivity.board = "B";
                break;
            case R.id.btnDepDM:
                MainActivity.board = "C";
                break;
        }

        fromClickDep = true;
        isProductHomeShown = false;
        myAdapter.setCanCheckLoop(false);

        productTask.cancel(true);
        getBitmap.cancel(true);
        mViewPager.setCurrentItem(1);

        setBoardTitle();
        try {
            recyclerView.setVisibility(View.INVISIBLE);
        }catch (NullPointerException e) {
            //Toast.makeText(context, "NullPointerException", Toast.LENGTH_SHORT).show();
        }
    }

}
