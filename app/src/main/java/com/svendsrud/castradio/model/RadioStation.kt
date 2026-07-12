package com.svendsrud.castradio.model

import com.svendsrud.castradio.R

data class RadioStation(
    val name: String,
    val streamUrl: String,
    val contentType: String = "audio/mpeg",
    val accentColor: Long = 0xFF7C4DFF,
    val logoRes: Int = R.drawable.logo_generic
)

object StationRepository {
    val stations = listOf(
        RadioStation("Solørradioen", "https://lyd.radioene.no/solor.mp3", accentColor = 0xFFFF7043, logoRes = R.drawable.logo_solorradioen),
        RadioStation("NRK MP3", "https://cdn0-47115-liveicecast0.dna.contentdelivery.net/mp3_mp3_h", accentColor = 0xFF42A5F5, logoRes = R.drawable.logo_nrk_mp3),
        RadioStation("NRK Sport", "https://cdn0-47115-liveicecast0.dna.contentdelivery.net/sport_mp3_h", accentColor = 0xFF66BB6A, logoRes = R.drawable.logo_nrk_sport),
        RadioStation("NRK P1 (Oslo)", "https://cdn0-47115-liveicecast0.dna.contentdelivery.net/p1_mp3_h", accentColor = 0xFFAB47BC, logoRes = R.drawable.logo_nrk_p1),
        RadioStation("80s - Depeche Mode", "https://regiocast.streamabc.net/regc-80s80sdm4383853-mp3-192-4947593", accentColor = 0xFFEC407A, logoRes = R.drawable.logo_80s_dm),
        RadioStation("Antenne Bayern - 80er Kulthits", "https://s1-webradio.antenne.de/80er-kulthits/stream/mp3", accentColor = 0xFF26A69A, logoRes = R.drawable.logo_generic),
        RadioStation("181.fm - Vibe of Vegas", "https://listen.181fm.com/181-vibe_128k.mp3", accentColor = 0xFFFFCA28, logoRes = R.drawable.logo_181fm)
    )
}
