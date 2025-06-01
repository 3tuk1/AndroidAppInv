package com.inv.inventryapp.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.inv.inventryapp.models.MainItem;
import com.inv.inventryapp.room.AppDatabase;
import com.inv.inventryapp.room.MainItemDao;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainItemViewModel extends AndroidViewModel {

    private MainItemDao mainItemDao;
    private ExecutorService executorService;

    public MainItemViewModel(@NonNull Application application) {
        super(application);
        // AppDatabaseのインスタンス取得方法をプロジェクトに合わせて修正
        AppDatabase db = AppDatabase.getInstance(application); // または AppDatabase.getDatabase(application)
        mainItemDao = db.mainItemDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<MainItem> getItemById(int itemId) {
        MutableLiveData<MainItem> itemLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            MainItem item = mainItemDao.findItemById(itemId); // MainItemDaoのメソッドを使用
            itemLiveData.postValue(item);
        });
        return itemLiveData;
    }

    public LiveData<List<MainItem>> getAllItems() {
        MutableLiveData<List<MainItem>> itemsLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            List<MainItem> items = mainItemDao.getAllMainItems();
            itemsLiveData.postValue(items);
        });
        return itemsLiveData;
    }

    public void insert(MainItem item) {
        executorService.execute(() -> mainItemDao.insert(item));
    }

    public void update(MainItem item) {
        executorService.execute(() -> mainItemDao.update(item));
    }

    public void delete(MainItem item) {
        executorService.execute(() -> mainItemDao.delete(item));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
