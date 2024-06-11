package com.tencent.qcloud.tuikit.tuisearch.minimalistui.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.tencent.qcloud.tuicore.TUIThemeManager;
import com.tencent.qcloud.tuikit.timcommon.component.impl.GlideEngine;
import com.tencent.qcloud.tuikit.timcommon.util.TUIUtil;
import com.tencent.qcloud.tuikit.tuisearch.R;
import com.tencent.qcloud.tuikit.tuisearch.TUISearchConstants;
import com.tencent.qcloud.tuikit.tuisearch.bean.SearchDataBean;
import com.tencent.qcloud.tuikit.tuisearch.interfaces.ISearchResultAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ISearchResultAdapter {
    private Context context;
    private String text;

    private int mViewType = -1;
    private int mShowCount = 0;
    private boolean mIsShowAll = false;
    private int mTotalCount = 0;

    // data list
    private List<SearchDataBean> mDataList;

    public void setText(String text) {
        this.text = text;
    }

    public SearchResultAdapter(Context context) {
        this.context = context;
    }

    public int getShowCount() {
        if (mIsShowAll) {
            return mShowCount;
        } else {
            if (mShowCount <= 3) {
                return mShowCount;
            } else {
                return 3;
            }
        }
    }

    public void setShowCount(int mShowCount) {
        this.mShowCount = mShowCount;
    }

    @Override
    public void onIsShowAllChanged(boolean mIsShowAll) {
        this.mIsShowAll = mIsShowAll;
    }

    public int getTotalCount() {
        return mTotalCount;
    }

    @Override
    public void onTotalCountChanged(int mTotalCount) {
        this.mTotalCount = mTotalCount;
    }

    @Override
    public void onDataSourceChanged(List<SearchDataBean> dataSource, int viewType) {
        if (dataSource == null || dataSource.isEmpty()) {
            if (mDataList != null) {
                mDataList.clear();
                mDataList = null;
            }
            setShowCount(0);
        } else {
            mDataList = dataSource;
            setShowCount(mDataList.size());
        }

        mViewType = viewType;
        notifyDataSetChanged();
    }

    public List<SearchDataBean> getDataSource() {
        return mDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ContactViewHolder holder = new ContactViewHolder(LayoutInflater.from(context).inflate(R.layout.minimalist_item_contact_search, parent, false));
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        /*If there is no search operation or the delete button is clicked after the search, we will empty the text in MainActivity and pass it over.*/

        ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
        if (contactViewHolder != null && mDataList != null && mDataList.size() > 0 && position < mDataList.size()) {
            SearchDataBean searchDataBean = mDataList.get(position);
            String title = searchDataBean.getTitle();
            String subTitle = searchDataBean.getSubTitle();
            String subTitleLabel = searchDataBean.getSubTitleLabel();
            if (mViewType == TUISearchConstants.CONTACT_TYPE) {
                subTitleLabel = holder.itemView.getResources().getString(R.string.search_ID_label);
                title = searchDataBean.getSubTitle();
                subTitle = searchDataBean.getTitle();
            }

            contactViewHolder.mSubTvLabelText.setText(subTitleLabel);
            int avatarDefaultIconResID;
            if (searchDataBean.isGroup()) {
                avatarDefaultIconResID = TUIUtil.getDefaultGroupIconResIDByGroupType(context, searchDataBean.getGroupType());
            } else {
                avatarDefaultIconResID = TUIThemeManager.getAttrResId(
                    contactViewHolder.mUserIconView.getContext(), com.tencent.qcloud.tuikit.timcommon.R.attr.core_default_user_icon);
            }
            String path = searchDataBean.getIconPath();
            GlideEngine.loadImageSetDefault(contactViewHolder.mUserIconView, path, avatarDefaultIconResID);
            if (text != null) {
                if (mViewType == TUISearchConstants.CONVERSATION_TYPE) {
                    contactViewHolder.mTvText.setText(title);

                    int isSubTextMatch = mDataList.get(position).getIsSubTextMatch();
                    if (isSubTextMatch == 1) {
                        SpannableString subString = matcherSearchText(Color.rgb(0, 0, 255), subTitle, text);
                        contactViewHolder.mSubTvText.setText(subString);
                    } else {
                        contactViewHolder.mSubTvText.setText(subTitle);
                    }
                } else {
                    SpannableString string = matcherSearchText(Color.rgb(0, 0, 255), title, text);
                    contactViewHolder.mTvText.setText(string);

                    SpannableString subString = matcherSearchText(Color.rgb(0, 0, 255), subTitle, text);
                    contactViewHolder.mSubTvText.setText(subString);
                }
            } else {
                contactViewHolder.mTvText.setText(title);
                contactViewHolder.mSubTvText.setText(subTitle);
            }
            if (TextUtils.isEmpty(contactViewHolder.mSubTvText.getText())) {
                contactViewHolder.mSubTvText.setVisibility(View.GONE);
                contactViewHolder.mSubTvLabelText.setVisibility(View.GONE);
            } else {
                contactViewHolder.mSubTvText.setVisibility(View.VISIBLE);
                contactViewHolder.mSubTvLabelText.setVisibility(View.VISIBLE);
            }
            contactViewHolder.mLlItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(view, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return getShowCount();
    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }

    public interface OnItemClickListener {
        void onClick(View view, int pos);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLlItem;
        private ImageView mUserIconView;
        private TextView mTvText;
        private TextView mSubTvText;
        private TextView mSubTvLabelText;

        public ContactViewHolder(View itemView) {
            super(itemView);
            mLlItem = itemView.findViewById(R.id.ll_item);
            mUserIconView = itemView.findViewById(R.id.ivAvatar);
            mTvText = itemView.findViewById(R.id.conversation_title);
            mSubTvText = itemView.findViewById(R.id.conversation_sub_title);
            mSubTvLabelText = itemView.findViewById(R.id.conversation_sub_title_label);
        }
    }

    private SpannableString matcherSearchText(int color, String text, String keyword) {
        if (text == null || TextUtils.isEmpty(text)) {
            return SpannableString.valueOf("");
        }
        SpannableString spannableString = new SpannableString(text);
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(spannableString);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            spannableString.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannableString;
    }
}
