package uz.pdp.musicplayer.classes

data class MusicClass(
    var title: String,
    var author: String,
    var musicUri: String,
    var imageUri: String,
    var duration: Int,
    var isHide: Boolean = false,
    var date: Int
)