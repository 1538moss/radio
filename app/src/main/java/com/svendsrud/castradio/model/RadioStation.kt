package com.svendsrud.castradio.model

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val contentType: String = "audio/mpeg",
    val accentColor: String = "#7C4DFF"
)

object StationRepository {
    val stations = listOf(
        RadioStation("Solørradioen", "https://lyd.radioene.no/solor.mp3", accentColor = "#FF7043"),
        RadioStation("NRK MP3", "https://cdn0-47115-liveicecast0.dna.contentdelivery.net/mp3_mp3_h", accentColor = "#42A5F5"),
        RadioStation("NRK Sport", "https://cdn0-47115-liveicecast0.dna.contentdelivery.net/sport_mp3_h", accentColor = "#66BB6A"),
        RadioStation("NRK P1 (Oslo)", "https://cdn0-47115-liveicecast0.dna.contentdelivery.net/p1_mp3_h", accentColor = "#AB47BC"),
        RadioStation("80s - Depeche Mode", "https://regiocast.streamabc.net/regc-80s80sdm4383853-mp3-192-4947593", accentColor = "#EC407A"),
        RadioStation("Antenne Bayern - 80er Kulthits", "https://s1-webradio.antenne.de/80er-kulthits/stream/mp3", accentColor = "#26A69A"),
        RadioStation("181.fm - Vibe of Vegas", "https://listen.181fm.com/181-vibe_128k.mp3", accentColor = "#FFCA28")
    )
}
