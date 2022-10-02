package com.cryptomcgrath.pyrexia.db

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import io.reactivex.Completable
import io.reactivex.Single


@Entity
internal data class Device(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "base_url") val baseUrl: String,
    @ColumnInfo(name = "email") val email: String,
    @ColumnInfo(name = "password") val password: String,
    @ColumnInfo(name = "token") val token: String
)

@Database(entities = [Device::class], version = 1)
internal abstract class PyrexiaDb : RoomDatabase() {
    abstract fun devicesDao(): DeviceDao

    companion object {
        private var instance: PyrexiaDb? = null

        fun getDatabase(context: Context): PyrexiaDb {
            if (instance == null) {
                instance = Room.databaseBuilder(context, PyrexiaDb::class.java, "pyrexia-db").build()
            }
            return instance!!
        }
    }
}

@Dao
internal interface DeviceDao {
    @Query("select * from device")
    fun devicesList(): Single<List<Device>>

    @Insert
    fun addDevice(device: Device): Completable
}