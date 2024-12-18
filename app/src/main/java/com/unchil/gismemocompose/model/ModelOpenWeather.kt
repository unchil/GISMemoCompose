package com.unchil.gismemocompose.model

import com.unchil.gismemocompose.db.CURRENTWEATHER_TBL
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName


@Serializable
data class CurrentWeather(
    @SerialName("coord") var coord: Coord,
    @SerialName("weather") var weather: List<Weather>,
    @SerialName("base") var base: String, //Internal parameter
    @SerialName("main") var main: Main,
    @SerialName("visibility") var visibility: Int, // visibility
    @SerialName("wind") var wind: Wind,
    @SerialName("clouds") var clouds: Clouds,
    @SerialName("dt") var dt: Long, //Time of data calculation, unix, UTC
    @SerialName("sys") var sys: Sys,
    @SerialName("timezone") var timezone: Long, //Shift in seconds from UTC
    @SerialName("id") var id: Long, //City ID
    @SerialName("name") var name: String, //City name
    @SerialName("cod") var cod: Int // Return Result Code
//   var rain: Rain,
//   var snow: Snow,

)
@Serializable
data class Coord (
    @SerialName("lon") var lon: Float, //City geo location, longitude
    @SerialName("lat") var lat: Float //City geo location, latitude
)

@Serializable
data class Weather ( //more info Weather condition codes
    
    @SerialName("id") var id: Int, //Weather condition id
    @SerialName("main") var main: String, //Group of weather parameters (Rain, Snow, Extreme etc.)
    @SerialName("description") var description: String, //Weather condition within the group. You can get the
    @SerialName("icon") var icon : String //Weather icon id
)
@Serializable
data class Main (
    @SerialName("temp") var temp: Float, //Temperature. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    @SerialName("feels_like") var feels_like: Float, //Temperature. This temperature parameter accounts for the human perception of weather. Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    @SerialName("pressure") var pressure: Float, //Atmospheric pressure (on the sea level, if there is no sea_level or grnd_level data), hPa
    @SerialName("humidity") var humidity: Float, //Humidity, %
    @SerialName("temp_min") var temp_min: Float, //Minimum temperature at the moment. This is minimal currently observed temperature (within large megalopolises and urban areas). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    @SerialName("temp_max") var temp_max: Float //Maximum temperature at the moment. This is maximal currently observed temperature (within large megalopolises and urban areas). Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
    //  var sea_level: String, //Atmospheric pressure on the sea level, hPa
    //  var grnd_level: String //Atmospheric pressure on the ground level, hPa
)
@Serializable
data class Wind (
    @SerialName("speed") var speed : Float, //Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
    @SerialName("deg") var deg : Float //Wind direction, degrees (meteorological)
    //  var gust : String //Wind gust. Unit Default: meter/sec, Metric: meter/sec, Imperial:miles/hour
)
@Serializable
data class Clouds (
    @SerialName("all") var all  : Int //Cloudiness, %
)
@Serializable
data class Sys (
    //   var type : Int, //Internal parameter
//    var id : Int, //Internal parameter
    @SerialName("country") var country : String, //Country code (GB, JP etc.)
    @SerialName("sunrise") var sunrise : Long, //Sunrise time, unix, UTC
    @SerialName("sunset") var sunset : Long //Sunset time, unix, UTC
    //    var message : Float, //Internal parameter
)

fun CurrentWeather.toCURRENTWEATHER_TBL(): CURRENTWEATHER_TBL {

    val currentWeatherTbl = CURRENTWEATHER_TBL(
        dt = this.dt,
        base = this.base,
        visibility = this.visibility,
        timezone = this.timezone,
        name = this.name,
        latitude = this.coord.lat,
        longitude = this.coord.lon,
        main = this.weather[0].main,
        description = this.weather[0].description,
        icon = this.weather[0].icon,
        temp = this.main.temp,
        feels_like = this.main.feels_like,
        pressure = this.main.pressure,
        humidity = this.main.humidity,
        temp_min = this.main.temp_min,
        temp_max = this.main.temp_max,
        speed = this.wind.speed,
        deg = this.wind.deg,
        all = this.clouds.all,
        //  type = this.sys.type,
        type = 0,
        country = this.sys.country,
        sunrise = this.sys.sunrise,
        sunset = this.sys.sunset
    )

    return currentWeatherTbl
}
