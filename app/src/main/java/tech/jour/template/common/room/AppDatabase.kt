package tech.jour.template.common.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import tech.jour.template.common.model.db.LocalLocationBean
import tech.jour.template.base.BaseApplication
import tech.jour.template.base.utils.ioThread
import tech.jour.template.common.room.dao.LocationDao

/**
 * Created by journey on 2020/5/18.
 */
@Database(
	entities = [LocalLocationBean::class],
	version = 2,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
	abstract fun locationDao(): LocationDao

	companion object {
		private const val DATABASE_NAME: String = "room.db"

		// For Singleton instantiation
		@Volatile
		private var instance: AppDatabase? = null
		fun getInstance(context: Context = BaseApplication.context): AppDatabase {
			return instance ?: synchronized(this) {
				instance ?: buildDatabase(context).also { instance = it }
			}
		}

		// Create and pre-populate the database. See this article for more details:
		// https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
		private fun buildDatabase(context: Context): AppDatabase {
			return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
				.fallbackToDestructiveMigration()
				.allowMainThreadQueries()
//				.addCallback(object : RoomDatabase.Callback() {
//					override fun onCreate(db: SupportSQLiteDatabase) {
//						super.onCreate(db)
//						ioThread {
//							getInstance(context).locationDao()
//								.insert(
//									LocalLocationBean(
//										timestamp = System.currentTimeMillis()
//									)
//								)
//						}
//					}
//				})
				.build()
		}
	}
}
