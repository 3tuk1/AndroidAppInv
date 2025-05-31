package com.inv.inventryapp.utils;

import android.content.Context;
import com.inv.inventryapp.models.HiddenItem;
import com.inv.inventryapp.models.MainItem;
import com.inv.inventryapp.models.MainItemJoin;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.HiddenItemDao;
import com.inv.inventryapp.room.MainItemDao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor; // Executor をインポート
import java.util.concurrent.Executors; // Executors をインポート

public class HidenManage {
    // 数量が0以下のものを非表示にするメソッド
    /**
     * アイテムの数量が0以下の場合、非表示にするかどうかを判断します。
     *
     * @param quantity アイテムの数量
     * @return 数量が0以下の場合はfalse、それ以外はtrue
     */
    Context context;
    private final Executor executor; // Executor を追加

    public HidenManage(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor(); // Executor を初期化
        dao = AppDatabase.getInstance(context).hiddenItemDao();
        mainItemdao = AppDatabase.getInstance(context).mainItemDao();
    }

    HiddenItemDao dao;
    MainItemDao mainItemdao;

    private boolean isItemVisible(int quantity) {
        return quantity > 0;
    }

    // DBから取得した全てのMainItemJoinのリストと、
    // HiddenItemテーブルの情報に基づいて、表示すべきMainItemJoinのリストを返します。
    // このメソッドはDAOアクセスを含むため、バックグラウンドスレッドで呼び出す必要があります。
    public List<MainItemJoin> getVisibleItemsFromDb(List<MainItemJoin> allItemsFromDb) {
        List<HiddenItem> hiddenDbItems = dao.getAll(); // DBから非表示アイテムIDを取得
        HashSet<Integer> hiddenItemIds = new HashSet<>();
        if (hiddenDbItems != null) {
            for (HiddenItem hiddenItem : hiddenDbItems) {
                if (hiddenItem != null) {
                    hiddenItemIds.add(hiddenItem.itemId);
                }
            }
        }

        List<MainItemJoin> visibleItems = new ArrayList<>();
        if (allItemsFromDb != null) {
            for (MainItemJoin itemJoin : allItemsFromDb) {
                // itemJoin と itemJoin.mainItem が null でないこと、
                // かつ、そのIDが非表示IDセットに含まれていないことを確認します。
                if (itemJoin != null && itemJoin.mainItem != null && !hiddenItemIds.contains(itemJoin.mainItem.getId())) {
                    visibleItems.add(itemJoin);
                }
            }
        }
        return visibleItems;
    }

    public void ItemIsNoVisible(List<MainItem> items) {
        executor.execute(() -> { // バックグラウンドで実行
            // dao.deleteAllHiddenItems(); // 注意: これを有効にすると、数量に関わらず全ての非表示情報が一旦クリアされます。
                                    // 個別アイテムの数量に基づいて非表示状態を更新する方が適切な場合があります。
            if (items != null) {
                for (MainItem item : items) {
                    if (item != null) {
                        if (!isItemVisible(item.getQuantity())) { // 数量が0以下なら
                            // 既に非表示リストになければ追加
                            HiddenItem existingHidden = dao.getHiddenItemById(item.getId());
                            if (existingHidden == null) {
                                 dao.insert(new HiddenItem(item.getId()));
                            }
                        } else { // 数量が0より大きいなら
                            // 非表示リストにあれば削除
                            dao.deleteByItemId(item.getId());
                        }
                    }
                }
            }
        });
    }

    public void ItemIsVisible(MainItem item) {
        executor.execute(() -> { // バックグラウンドで実行
            dao.deleteByItemId(item.getId());
        });
    }

    public void loadHiddenItems() {
        executor.execute(() -> { // バックグラウンドで実行
            List<HiddenItem> hiddenItems = dao.getAll();
            for (HiddenItem hiddenItem : hiddenItems) {
                MainItem item = mainItemdao.getMainItemById(hiddenItem.getId());
                if (item != null && item.getQuantity() > 0) { // item が null でないことを確認
                    // アイテムの数量が0以上の場合、非表示リストから削除
                    dao.deleteByItemId(item.getId());
                }
            }
        });
    }
}
