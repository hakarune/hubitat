metadata {
    definition (name: "Matrix Messenger", namespace: "hakarune", author: "hakarune") {
        capability "Actuator"
        command "sendMessage"
        
        attribute "lastMessageSent", "String"
        attribute "lastMessageSentDateTime", "String"
        attribute "connectionStatus", "String"
        attribute "lastLogEvent", "String"
        
        input "apiKey", "text", title: "Matrix API Key", required: true
        input "roomId", "text", title: "Room ID", required: true
        input "matrixServer", "text", title: "Matrix Server URL", required: true
        input "testMessage", "text", title: "Test Message", required: false
    }
}


def sendMessage(String message) {
    def accessToken = settings.apiKey
    def roomId = settings.roomId
    def serverUrl = settings.matrixServer

    def sendMessage = [
        roomId: roomId,
        msgtype: 'm.text',
        body: message
    ]

    try {
        def response = new hubitat.device.HubAction(
            method: "POST",
            path: "/_matrix/client/r0/rooms/${URLEncoder.encode(roomId, 'UTF-8')}/send/m.room.message",
            headers: [
                Host: serverUrl.split('/')[2],
                Authorization: "Bearer $accessToken",
                'Content-Type': 'application/json'
            ],
            body: sendMessage as String
        )

        def hubResponse = sendHubCommand(response)
        
        if (hubResponse) {
            if (hubResponse.status == 200) {
                log.debug "Message sent successfully: ${message}"
                sendEvent(name: "lastMessageSent", value: message, displayed: true)
                sendEvent(name: "lastMessageSentDateTime", value: new Date().toString(), displayed: true)
                sendEvent(name: "connectionStatus", value: "Connected", displayed: true)
                sendEvent(name: "lastLogEvent", value: "Message sent successfully", displayed: true)
            } else {
                log.error "Failed to send message. Status code: ${hubResponse.status}, Response: ${hubResponse.data}"
                sendEvent(name: "connectionStatus", value: "Disconnected", displayed: true)
                sendEvent(name: "lastLogEvent", value: "Failed to send message", displayed: true)
            }
        } else {
            log.error "No response received."
            sendEvent(name: "connectionStatus", value: "Disconnected", displayed: true)
            sendEvent(name: "lastLogEvent", value: "No response received", displayed: true)
        }
    } catch (Exception e) {
        log.error "Error sending message: ${e.message}"
        sendEvent(name: "connectionStatus", value: "Disconnected", displayed: true)
        sendEvent(name: "lastLogEvent", value: "Error sending message", displayed: true)
    }
}
