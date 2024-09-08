package com.codewithkael.webrtcprojectforrecord.utils

import com.example.telcobrightcaller.models.JanusResponse


interface NewMessageInterface {
    fun onNewMessage(message: JanusResponse)
}