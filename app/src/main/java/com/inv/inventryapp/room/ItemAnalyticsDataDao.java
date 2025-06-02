package com.inv.inventryapp.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.inv.inventryapp.models.ItemAnalyticsData;

@Dao
public interface ItemAnalyticsDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ItemAnalyticsData itemAnalyticsData);

    @Update
    void update(ItemAnalyticsData itemAnalyticsData);

    @Delete
    void delete(ItemAnalyticsData itemAnalyticsData);

    @Query("SELECT * FROM item_analytics_data WHERE item_id = :itemId")
    ItemAnalyticsData getItemAnalyticsDataByItemId(int itemId);

    @Query("SELECT * FROM item_analytics_data WHERE item_id = :itemId")
    LiveData<ItemAnalyticsData> getAnalyticsDataByItemIdLiveData(int itemId);
}
