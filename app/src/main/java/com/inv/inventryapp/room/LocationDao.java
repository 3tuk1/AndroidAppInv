package com.inv.inventryapp.room;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.inv.inventryapp.models.Location;

import java.util.List;

public interface LocationDao {
    // Insert a new location
    @Insert
    void insert(Location location);

    // Update an existing location
    @Update
    void update(Location location);

    // Delete a location
    @Delete
    void delete(Location location);

    // Get all locations
    @Query("SELECT * FROM locations")
    List<Location> getAllLocations();

    // Get a location by its ID
    @Query("SELECT * FROM locations WHERE id = :id")
    Location getLocationById(int id);
}
