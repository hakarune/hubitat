metadata {
    definition(name: "Textbelt Notification Device", namespace: "hakarune", author: "hakarune", importUrl: "") {
        capability "Notification"
    }
    preferences {
        input name: "apiKey", type: "text", title: "Textbelt API Key", required: true, defaultValue: ""
        input name: "phoneNumber", type: "text", title: "Recipient Phone Number", required: true, defaultValue: ""
    }
}

def deviceNotification(text) {
    try {
        def params = [
            uri: "https://textbelt.com/text",
            body: [
                phone: phoneNumber,
                message: text,
                key: apiKey
            ],
            contentType: "application/x-www-form-urlencoded"
        ]

        httpPost(params) { response ->
            if (response.status == 200) {
                log.debug "SMS notification sent successfully."
            } else {
                log.debug "Failed to send SMS notification. Status code: ${response.status}"
            }
        }
    } catch (e) {
        log.error "Error sending SMS notification: ${e.message}"
    }
}
