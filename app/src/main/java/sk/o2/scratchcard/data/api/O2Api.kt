package sk.o2.scratchcard.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import sk.o2.scratchcard.data.model.VersionResponse

interface O2Api {

    @GET("version")
    suspend fun getVersion(@Query("code") code: String): VersionResponse
}
