package com.tencent.qcloud.tuikit.tuichat.minimalistui.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.tencent.qcloud.tuikit.timcommon.bean.TUIMessageBean;
import com.tencent.qcloud.tuikit.timcommon.component.CustomLinearLayoutManager;
import com.tencent.qcloud.tuikit.timcommon.component.activities.BaseMinimalistLightActivity;
import com.tencent.qcloud.tuikit.timcommon.interfaces.OnItemClickListener;
import com.tencent.qcloud.tuikit.tuichat.R;
import com.tencent.qcloud.tuikit.tuichat.TUIChatConstants;
import com.tencent.qcloud.tuikit.tuichat.bean.ChatInfo;
import com.tencent.qcloud.tuikit.tuichat.bean.message.MergeMessageBean;
import com.tencent.qcloud.tuikit.tuichat.minimalistui.widget.message.MessageAdapter;
import com.tencent.qcloud.tuikit.tuichat.minimalistui.widget.message.MessageRecyclerView;
import com.tencent.qcloud.tuikit.tuichat.presenter.ForwardPresenter;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatLog;
import com.tencent.qcloud.tuikit.tuichat.util.TUIChatUtils;

public class TUIForwardChatMinimalistActivity extends BaseMinimalistLightActivity {
    private static final String TAG = TUIForwardChatMinimalistActivity.class.getSimpleName();

    private MessageRecyclerView mFowardChatMessageRecyclerView;
    private MessageAdapter mForwardChatAdapter;

    private View backBtn;
    private MergeMessageBean mMessageInfo;
    private ChatInfo chatInfo;

    private ForwardPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_minimalist_forward_layout);
        mFowardChatMessageRecyclerView = (MessageRecyclerView) findViewById(R.id.chat_message_layout);
        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.getDrawable().setAutoMirrored(true);
        mFowardChatMessageRecyclerView.setLayoutManager(new CustomLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mForwardChatAdapter = new MessageAdapter();
        mForwardChatAdapter.setForwardMode(true);
        presenter = new ForwardPresenter();
        presenter.initListener();
        presenter.setMessageListAdapter(mForwardChatAdapter);
        presenter.setNeedShowBottom(false);
        mForwardChatAdapter.setPresenter(presenter);

        mFowardChatMessageRecyclerView.setAdapter(mForwardChatAdapter);
        mFowardChatMessageRecyclerView.setPresenter(presenter);
        backBtn = findViewById(R.id.back_btn_area);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mFowardChatMessageRecyclerView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onUserIconClick(View view, TUIMessageBean messageInfo) {
                if (!(messageInfo instanceof MergeMessageBean)) {
                    return;
                }

                Intent intent = new Intent(getBaseContext(), TUIForwardChatMinimalistActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(TUIChatConstants.FORWARD_MERGE_MESSAGE_KEY, messageInfo);
                intent.putExtras(bundle);
                startActivity(intent);
            }

            @Override
            public void onMessageClick(View view, TUIMessageBean messageBean) {
                if (messageBean instanceof MergeMessageBean) {
                    Intent intent = new Intent(view.getContext(), TUIForwardChatMinimalistActivity.class);
                    intent.putExtra(TUIChatConstants.FORWARD_MERGE_MESSAGE_KEY, messageBean);
                    startActivity(intent);
                }
            }
        });

        init();
    }

    private void init() {
        Intent intent = getIntent();
        if (intent != null) {
            mMessageInfo = (MergeMessageBean) intent.getSerializableExtra(TUIChatConstants.FORWARD_MERGE_MESSAGE_KEY);
            chatInfo = (ChatInfo) intent.getSerializableExtra(TUIChatConstants.CHAT_INFO);
            if (null == mMessageInfo) {
                TUIChatLog.e(TAG, "mMessageInfo is null");
                return;
            }
            presenter.setChatInfo(chatInfo);
            presenter.downloadMergerMessage(mMessageInfo);
        }
    }
}
