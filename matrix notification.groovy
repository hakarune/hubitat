import groovy.json.JsonOutput
import java.net.URLEncoder

metadata {
    definition(name: "Matrix Notification Device", namespace: "your.namespace", author: "your.author", importUrl: "") {
        capability "Notification"
    }
    preferences {
        input name: "apiKey", type: "text", title: "Matrix API Key", required: true, defaultValue: ""
        input name: "roomId", type: "text", title: "Room ID", required: true, defaultValue: ""
        input name: "matrixServer", type: "text", title: "Matrix Server URL", required: true, defaultValue: "https://matrix.org"
    }
}

def deviceNotification(text) {
    try {
        def apiKey = settings.apiKey
        def roomId = settings.roomId
        def matrixServer = settings.matrixServer

        def sendMessage = [
            msgtype: 'm.text',
            body: text
        ]
        
        // Generate a random message ID
        def messageId = UUID.randomUUID().toString()

        def response = sendNotification(sendMessage, apiKey, roomId, messageId, matrixServer)
        if (response.status == 200) {
            log.debug "Message sent successfully: ${text}"
            updateStatus("Message sent successfully", "Connected", text)
        } else {
            log.error "Failed to send message. Status code: ${response.status}, Response: ${response.data}"
            updateStatus("Failed to send message", "Disconnected", text)
        }
    } catch (Exception e) {
        log.error "Error sending message: ${e.message}"
        updateStatus("Error sending message", "Disconnected", "")
    }
}

private sendNotification(message, apiKey, roomId, messageId, matrixServer) {
    def uri = "${matrixServer}/_matrix/client/r0/rooms/${URLEncoder.encode(roomId, 'UTF-8')}/send/m.room.message/${messageId}?access_token=${apiKey}"
    def headers = [
        'Content-Type': 'application/json'
    ]
    def body = JsonOutput.toJson(message)
    
    httpPut(uri: uri, body: body, headers: headers)
}

private updateStatus(logEvent, connectionStatus, lastMessage) {
    sendEvent(name: "lastLogEvent", value: logEvent, displayed: true)
    sendEvent(name: "connectionStatus", value: connectionStatus, displayed: true)
    sendEvent(name: "lastMessage", value: lastMessage, displayed: true)
}
