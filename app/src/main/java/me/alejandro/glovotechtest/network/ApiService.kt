
import me.alejandro.glovotechtest.model.City
import me.alejandro.glovotechtest.model.CityDetailed
import me.alejandro.glovotechtest.model.Country
import retrofit2.http.GET
import retrofit2.http.Path
import rx.Observable

interface ApiService {

    @GET("api/countries")
    fun getCountries(): Observable<List<Country>>

    @GET("api/cities")
    fun getCities(): Observable<List<City>>

    @GET("api/cities/{code}")
    fun getCity(@Path("code") code: String): Observable<CityDetailed>

}