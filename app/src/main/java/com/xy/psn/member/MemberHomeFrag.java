package com.xy.psn.member;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.xy.psn.R;
import com.xy.psn.data.MyHelper;

import static com.xy.psn.MainActivity.context;
import static com.xy.psn.data.MyHelper.loginUserId;

public class MemberHomeFrag extends Fragment {
    private String[] group = {"個人檔案", "我的最愛", "信箱", "商品管理"};
    private int[] iconId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_member_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        iconId = new int[] {
                R.drawable.icon_profile_boy,
                R.drawable.icon_favorite,
                R.drawable.icon_mailbox,
                R.drawable.icon_package
        };
        if (MyHelper.gender == 0)
            iconId[0] = R.drawable.icon_profile_girl;

        ListView listView = (ListView) getView().findViewById(R.id.lstMemHome);
        listView.setAdapter(MyHelper.getSimpleAdapter(
                context,
                R.layout.lst_text_with_icon,
                R.id.imgIcon,
                R.id.txtTitle,
                iconId,
                group
        ));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemName = ((TextView)view.findViewById(R.id.txtTitle)).getText().toString();
                showActivity(itemName);
            }
        });
    }

    public void showActivity(String itemName) {
        switch (itemName) {
            case "個人檔案":
                Intent it = new Intent(context, MemberProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("memberId", loginUserId);
                it.putExtras(bundle);
                startActivity(it);
                break;

            case "我的最愛":
                startActivity(new Intent(context, MemberFavoriteActivity.class));
                break;

            case "信箱":
                startActivity(new Intent(context, MemberMailboxActivity.class));
                break;

            case "商品管理":
                startActivity(new Intent(context, MemberStockActivity.class));
                break;
        }
    }
}
