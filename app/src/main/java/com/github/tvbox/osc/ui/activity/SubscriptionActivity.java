package com.github.tvbox.osc.ui.activity;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.base.BaseActivity;
import com.github.tvbox.osc.bean.Subscription;
import com.github.tvbox.osc.event.RefreshEvent;
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

    private ArrayList<Subscription> mSubscriptions = new ArrayList<>();
    private SubscriptionAdapter mAdapter;
    private String mSelectedUrl = "";
    private String mInitialUrl = "";

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_subscription;
    }

    @Override
    protected void init() {
        mInitialUrl = Hawk.get(HawkConfig.API_URL, "");
        mSubscriptions = Hawk.get(HawkConfig.SUBSCRIPTIONS, new ArrayList<>());
        for (Subscription s : mSubscriptions) {
            if (s.isChecked()) {
                mSelectedUrl = s.getUrl();
                break;
            }
        }

        RecyclerView rv = findViewById(R.id.mGridView);
        rv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        rv.setHasFixedSize(true);

        mAdapter = new SubscriptionAdapter(new SubscriptionAdapter.SubscriptionInterface() {
            @Override
            public void click(Subscription item) {
                if (!TextUtils.isEmpty(item.getMultiUrl())) {
                    pickLine(item);
                } else {
                    activate(item);
                }
            }

            @Override
            public void del(Subscription item) {
                if (item.isChecked()) {
                    Toast.makeText(SubscriptionActivity.this, "不能删除当前使用的订阅", Toast.LENGTH_SHORT).show();
                    return;
                }
                mSubscriptions.remove(item);
                mAdapter.setData(mSubscriptions);
                saveData();
            }
        });
        rv.setAdapter(mAdapter);
        mAdapter.setData(mSubscriptions);

        findViewById(R.id.tvAdd).setOnClickListener(v -> showAddDialog());
    }

    private void activate(Subscription item) {
        for (Subscription s : mSubscriptions) s.setChecked(false);
        item.setChecked(true);
        mSelectedUrl = item.getUrl();
        mAdapter.setData(mSubscriptions);
        saveData();
        Toast.makeText(this, "已选择: " + item.getName(), Toast.LENGTH_SHORT).show();
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
        showLoading();
        OkGo.<String>get(url)
                .tag("fetch_add")
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        showSuccess();
                        String body = response.body();
                        if (body == null) {
                            addEntry(url, null);
                            return;
                        }
                        try {
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            JsonArray urls = json.getAsJsonArray("urls");
                            if (urls != null && urls.size() > 0) {
                                pickLineAndAdd(url, urls);
                            } else {
                                addEntry(url, null);
                            }
                        } catch (Throwable th) {
                            addEntry(url, null);
                        }
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body() != null ? response.body().string() : "";
                    }

                    @Override
                    public void onError(Response<String> response) {
                        showSuccess();
                        addEntry(url, null);
                    }
                });
    }

    private void pickLineAndAdd(String multiUrl, JsonArray urls) {
        List<String> names = new ArrayList<>();
        List<String> lineUrls = new ArrayList<>();
        for (int i = 0; i < urls.size(); i++) {
            try {
                JsonObject obj = urls.get(i).getAsJsonObject();
                names.add(obj.get("name").getAsString().trim());
                lineUrls.add(obj.get("url").getAsString().trim());
            } catch (Throwable ignored) {}
        }
        if (names.isEmpty()) {
            addEntry(multiUrl, null);
            return;
        }
        String[] items = names.toArray(new String[0]);
        new android.app.AlertDialog.Builder(this)
                .setTitle("选择线路")
                .setItems(items, (dialog, which) -> {
                    addEntry(lineUrls.get(which), multiUrl);
                })
                .setOnCancelListener(dialog -> {
                    addEntry(lineUrls.get(0), multiUrl);
                })
                .show();
    }

    private void addEntry(String url, String multiUrl) {
        String name = (multiUrl != null ? "多仓: " : "订阅: ") + (mSubscriptions.size() + 1);
        Subscription sub = new Subscription(name, url);
        sub.setMultiUrl(multiUrl);
        if (mSubscriptions.isEmpty()) {
            sub.setChecked(true);
            mSelectedUrl = url;
        }
        mSubscriptions.add(sub);
        mAdapter.setData(mSubscriptions);
        saveData();
        Toast.makeText(this, "已添加: " + name, Toast.LENGTH_SHORT).show();
    }

    private void pickLine(Subscription item) {
        showLoading();
        OkGo.<String>get(item.getMultiUrl())
                .tag("pick_line")
                .execute(new AbsCallback<String>() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        showSuccess();
                        String body = response.body();
                        if (body == null) return;
                        try {
                            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                            JsonArray urls = json.getAsJsonArray("urls");
                            if (urls != null && urls.size() > 0) {
                                List<String> names = new ArrayList<>();
                                List<String> lineUrls = new ArrayList<>();
                                for (int i = 0; i < urls.size(); i++) {
                                    try {
                                        JsonObject obj = urls.get(i).getAsJsonObject();
                                        names.add(obj.get("name").getAsString().trim());
                                        lineUrls.add(obj.get("url").getAsString().trim());
                                    } catch (Throwable ignored) {}
                                }
                                if (names.isEmpty()) return;
                                String[] items = names.toArray(new String[0]);
                                new android.app.AlertDialog.Builder(SubscriptionActivity.this)
                                        .setTitle("切换线路 - " + item.getName())
                                        .setItems(items, (dialog, which) -> {
                                            item.setUrl(lineUrls.get(which));
                                            item.setName(items[which]);
                                            activate(item);
                                        })
                                        .show();
                            }
                        } catch (Throwable ignored) {}
                    }

                    @Override
                    public String convertResponse(okhttp3.Response response) throws Throwable {
                        return response.body() != null ? response.body().string() : "";
                    }

                    @Override
                    public void onError(Response<String> response) {
                        showSuccess();
                        Toast.makeText(SubscriptionActivity.this, "多仓获取失败", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveData() {
        Hawk.put(HawkConfig.SUBSCRIPTIONS, mSubscriptions);
        if (!TextUtils.isEmpty(mSelectedUrl)) {
            Hawk.put(HawkConfig.API_URL, mSelectedUrl);
        }
    }

    @Override
    public void finish() {
        if (!TextUtils.isEmpty(mSelectedUrl) && !mSelectedUrl.equals(mInitialUrl)) {
            EventBus.getDefault().post(new RefreshEvent(RefreshEvent.TYPE_API_URL_CHANGE, mSelectedUrl));
        }
        super.finish();
    }
}
