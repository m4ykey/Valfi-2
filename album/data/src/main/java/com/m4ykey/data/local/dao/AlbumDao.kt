package com.m4ykey.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.m4ykey.data.local.model.AlbumEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album : AlbumEntity)

    @Delete
    suspend fun deleteAlbum(album: AlbumEntity)

    @Query("SELECT * FROM album ORDER BY name COLLATE NOCASE ASC")
    fun getAlbumSortedAlphabetical() : PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM album ORDER BY saveTime DESC")
    fun getAlbumsRecentlyAdded() : PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM album WHERE albumType = 'Album' ORDER BY name ASC")
    fun getAlbumsOfTypeAlbumPaged() : PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM album WHERE albumType = 'EP' ORDER BY name ASC")
    fun getAlbumsOfTypeEPPaged() : PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM album WHERE albumType = 'Single' ORDER BY name ASC")
    fun getAlbumsOfTypeSinglePaged() : PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM album WHERE albumType = 'Compilation' ORDER BY name ASC")
    fun getAlbumsOfTypeCompilationPaged() : PagingSource<Int, AlbumEntity>

    @Query("SELECT * FROM album WHERE id = :albumId")
    fun getLocalAlbumById(albumId : String) : Flow<AlbumEntity>

    @Query("SELECT COUNT(*) FROM album")
    fun getAlbumCount() : Int

    @Query("SELECT SUM(totalTracks) FROM album")
    fun getTotalTracksCount() : Int

    @Query("SELECT * FROM album WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchAlbumsByName(searchQuery : String) : PagingSource<Int, AlbumEntity>

}