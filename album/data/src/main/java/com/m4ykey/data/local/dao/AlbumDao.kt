package com.m4ykey.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.m4ykey.data.local.model.AlbumEntity
import com.m4ykey.data.local.model.IsAlbumSaved
import com.m4ykey.data.local.model.IsListenLaterSaved
import com.m4ykey.data.local.model.relations.AlbumWithStates
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album : AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAlbum(isAlbumSaved: IsAlbumSaved)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListenLaterAlbum(isListenLaterSaved: IsListenLaterSaved)

    @Query("SELECT * FROM album_table WHERE id = :albumId")
    suspend fun getAlbum(albumId : String) : AlbumEntity?

    @Query("SELECT * FROM album_saved_table WHERE albumId = :albumId")
    suspend fun getSavedAlbumState(albumId : String) : IsAlbumSaved?

    @Query("SELECT * FROM listen_later_table WHERE albumId = :albumId")
    suspend fun getListenLaterState(albumId : String) : IsListenLaterSaved?

    @Transaction
    @Query("SELECT * FROM album_table WHERE id = :albumId")
    suspend fun getAlbumWithStates(albumId: String) : AlbumWithStates?

    @Query("DELETE FROM album_table WHERE id = :albumId")
    suspend fun deleteAlbum(albumId: String)

    @Query("DELETE FROM album_saved_table WHERE albumId = :albumId")
    suspend fun deleteSavedAlbumState(albumId: String)

    @Query("DELETE FROM listen_later_table WHERE albumId = :albumId")
    suspend fun deleteListenLaterState(albumId: String)

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1 ORDER BY save_time DESC")
    fun getSavedAlbums() : PagingSource<Int, AlbumEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN listen_later_table ON " +
            "album_table.id = listen_later_table.albumId WHERE " +
            "listen_later_table.isListenLaterSaved = 1 ORDER BY name ASC")
    fun getListenLaterAlbums() : PagingSource<Int, AlbumEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN listen_later_table ON " +
            "album_table.id = listen_later_table.albumId WHERE " +
            "listen_later_table.isListenLaterSaved = 1 ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomAlbum() : AlbumEntity?

    @Query("SELECT COUNT(*) FROM listen_later_table WHERE isListenLaterSaved = 1")
    fun getListenLaterCount() : Flow<Int>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1 AND album_table.album_type = :albumType")
    fun getAlbumType(albumType : String) : PagingSource<Int, AlbumEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1 AND album_table.name " +
            "LIKE '%' || :searchQuery || '%'")
    fun searchAlbumsByName(searchQuery : String) : PagingSource<Int, AlbumEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN listen_later_table ON " +
            "album_table.id = listen_later_table.albumId WHERE " +
            "listen_later_table.isListenLaterSaved = 1 AND album_table.name " +
            "LIKE '%' || :searchQuery || '%'")
    fun searchAlbumsListenLater(searchQuery: String) : PagingSource<Int, AlbumEntity>

    @Query("SELECT COUNT(*) FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1 AND album_type = :albumType")
    fun getAlbumTypeCount(albumType : String) : Flow<Int>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1 ORDER BY name")
    fun getAlbumSortedByName() : PagingSource<Int, AlbumEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1 ORDER BY save_time ASC")
    fun getSavedAlbumAsc() : PagingSource<Int, AlbumEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT COUNT(*) FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1")
    fun getAlbumCount() : Flow<Int>

    @Query("SELECT SUM(album_table.total_tracks) FROM album_table INNER JOIN album_saved_table ON " +
            "album_table.id = album_saved_table.albumId WHERE " +
            "album_saved_table.isAlbumSaved = 1")
    fun getTotalTracksCount() : Flow<Int>

}