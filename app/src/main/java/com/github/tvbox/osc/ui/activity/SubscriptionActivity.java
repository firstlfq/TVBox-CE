package com.github.tvbox.osc.ui.activity;

import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.Subscription;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.ui.adapter.LineAdapter;
import com.github.tvbox.osc.ui.adapter.SubscriptionAdapter;
import com.github.tvbox.osc.util.HawkConfig;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class SubscriptionActivity extends BaseActivity {

    private ArrayList<Subscription> mSources = new ArrayList<>();
    private SubscriptionAdapter mSourceAdapter;
    private LineAdapter mLineAdapter;
    private RecyclerView mRecyclerView;
    private String mInitialUrl = "";
    private boolean mShowingLines = false;
    private Subscription mCurrentSource = null;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_subscription;
    }

    @Override
    protected void init() {
        mInitialUrl = Hawk.get(HawkConfig.API_URL, "");
        mSources = Hawk.get(HawkConfig.SUBSCRIPTIONS, new ArrayList<>());
        migrateLegacyData();

        mRecyclerView = findViewById(R.id.mGridView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);

        mSourceAdapter = new SubscriptionAdapter(new SubscriptionAdapter.SourceInterface() {
            @Override
            public void onSourceClick(Subscription item) {
                showLines(item);
            }

            @Override
            public void onSourceDelete(Subscription item) {
                mSources.remove(item);
                refreshSourceList();
                saveData();
                Toast.makeText(SubscriptionActivity.this, "已删除", Toast.LENGTH_SHORT).show();
            }
        });

        mLineAdapter = new LineAdapter((item, index) -> {
            if (mCurrentSource != null) {
                mCurrentSource.setSelectedIndex(index);
                saveData();
                showSources();
                Toast.makeText(SubscriptionActivity.this, "已选择: " + item.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView.setAdapter(mSourceAdapter);
        refreshSourceList();

        findViewById(R.id.tvAdd).setOnClickListener(v -> showAddDialog());
        findViewById(R.id.tvBack).setOnClickListener(v -> {
            if (mShowingLines) {
                showSources();
            } else {
                finish();
            }
        });
        updateTitleBar();
    }

    private void migrateLegacyData() {
        boolean changed = false;
        for (Subscription s : mSources) {
            if (s.getLines() == null) {
                List<Subscription.Line> lines = new ArrayList<>();
                lines.add(new Subscription.Line(s.getName(), s.getUrl()));
                s.setLines(lines);
                s.setSelectedIndex(s.isChecked() ? 0 : -1);
                changed = true;
            }
        }
        if (changed) {
            Hawk.put(HawkConfig.SUBSCRIPTIONS, mSources);
        }
    }

    private void showSources() {
        mShowingLines = false;
        mCurrentSource = null;
        mRecyclerView.setAdapter(mSourceAdapter);
        refreshSourceList();
        updateTitleBar();
        findViewById(R.id.tvAdd).setVisibility(android.view.View.VISIBLE);
    }

    private void showLines(Subscription source) {
        if (source.getLines() == null || source.getLines().isEmpty()) {
            Toast.makeText(this, "此源没有线路，尝试重新获取", Toast.LENGTH_SHORT).show();
            return;
        }
        mShowingLines = true;
        mCurrentSource = source;
        mLineAdapter.submitList(new ArrayList<>(source.getLines()));
        mLineAdapter.setSelectedIndex(source.getSelectedIndex());
        mRecyclerView.setAdapter(mLineAdapter);
        updateTitleBar();
        findViewById(R.id.tvAdd).setVisibility(android.view.View.GONE);
    }

    private void refreshSourceList() {
        mSourceAdapter.submitList(new ArrayList<>(mSources));
    }

    private void updateTitleBar() {
        TextView titleView = findViewById(R.id.tvTitle);
        TextView backView = findViewById(R.id.tvBack);
        if (mShowingLines && mCurrentSource != null) {
            titleView.setText(mCurrentSource.getName());
            backView.setVisibility(android.view.View.VISIBLE);
        } else {
            titleView.setText(R.string.sub_management);
            backView.setVisibility(android.view.View.GONE);
        }
    }

    private void showAddDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("添加订阅");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        android.widget.EditText urlInput = new android.widget.EditText(this);
        urlInput.setHint("配置地址 URL (支持单仓/多仓)");
        urlInput.setTextColor(0xFF000000);
        urlInput.setHintTextColor(0xFF888888);

        layout.addView(urlInput);
        builder.setView(layout);

        builder.setPositiveButton("确认", (dialog, which) -> {
            String url = urlInput.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                Toast.makeText(this, "地址不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            fetchAndAdd(url);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void fetchAndAdd(String url) {
        for (Subscription s : mSources) {
            if (url.equals(s.getMultiUrl()) || url.equals(s.getUrl())) {
                Toast.makeText(this, "此地址已存在", Toast.LENGTH_SHORT).show();
                showLines(s);
                return;
            }
        }

        showLoading();
        OkGo.<String>get(url)
                .tag("fetch_add")
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        showSuccess();
                        String body = response.body();
                        if (body == null) {
                            createSingleSource(url);
                            return;
                        }
                        try {
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            JsonArray urls = json.getAsJsonArray("urls");
                            if (urls != null && urls.size() > 0) {
                                createMultiSource(url, parseLines(urls));
                            } else {
                                createSingleSource(url);
                            }
                        } catch (Throwable th) {
                            createSingleSource(url);
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body() != null ? response.body().string() : "";
                    }

                    @Override
                    public void onError(Response<String> response) {
                        showSuccess();
                        createSingleSource(url);
                    }
                });
    }

    private List<Subscription.Line> parseLines(JsonArray urls) {
        List<Subscription.Line> lines = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            try {
                JsonObject obj = urls.get(i).getAsJsonObject();
                String name = obj.get("name").getAsString().trim();
                String lineUrl = obj.get("url").getAsString().trim();
                lines.add(new Subscription.Line(name, lineUrl));
            } catch (Throwable ignored) {}
        }
        return lines;
    }

    private void createSingleSource(String url) {
        String name = "订阅: " + (mSources.size() + 1);
        Subscription sub = new Subscription(name, url);
        List<Subscription.Line> lines = new ArrayList<>();
        lines.add(new Subscription.Line(name, url));
        sub.setLines(lines);
        sub.setSelectedIndex(0);
        mSources.add(sub);
        refreshSourceList();
        saveData();
        Toast.makeText(this, "已添加: " + name, Toast.LENGTH_SHORT).show();
    }

    private void createMultiSource(String multiUrl, List<Subscription.Line> lines) {
        if (lines.isEmpty()) {
            createSingleSource(multiUrl);
            return;
        }
        String name = "多仓: " + (mSources.size() + 1);
        Subscription sub = new Subscription(name, multiUrl);
        sub.setMultiUrl(multiUrl);
        sub.setLines(lines);
        sub.setSelectedIndex(-1);
        mSources.add(sub);
        refreshSourceList();
        saveData();

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("选择线路 - " + name);
        String[] items = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            items[i] = lines.get(i).getName();
        }
        builder.setItems(items, (dialog, which) -> {
            sub.setSelectedIndex(which);
            saveData();
            Toast.makeText(this, "已选择: " + lines.get(which).getName(), Toast.LENGTH_SHORT).show();
            refreshSourceList();
        });
        builder.setCancelable(true);
        builder.setOnCancelListener(dialog -> {
            showLines(sub);
        });
        builder.show();
    }

    private void saveData() {
        Hawk.put(HawkConfig.SUBSCRIPTIONS, mSources);
        String effectiveUrl = getEffectiveUrl();
        if (!TextUtils.isEmpty(effectiveUrl)) {
            Hawk.put(HawkConfig.API_URL, effectiveUrl);
        }
    }

    private String getEffectiveUrl() {
        for (Subscription s : mSources) {
            Subscription.Line line = s.getSelectedLine();
            if (line != null) {
                return line.getUrl();
            }
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        if (mShowingLines) {
            showSources();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        String effectiveUrl = getEffectiveUrl();
        if (!TextUtils.isEmpty(effectiveUrl) && !effectiveUrl.equals(mInitialUrl)) {
            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_API_URL_CHANGE, effectiveUrl));
        }
        super.finish();
    }
}
