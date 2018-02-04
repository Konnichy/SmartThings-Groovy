/**
 *  Wi-Fi Presence
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

import groovy.json.JsonSlurper

definition(
    name: "Wi-Fi Presence",
    namespace: "konnichy",
    author: "Konnichy",
    description: "Allows the SmartThings hub to receive presence events sent by the local Wi-Fi access point to update the presence state of household members."
)


preferences {
    section("Person 1") {
		paragraph "Enter the information of one person who must be considered Home when connected to Wi-Fi. You can add more people in sections below."
        input "presence_sensor1", "capability.presenceSensor", title: "Which virtual presence sensor must be updated when this person arrives or leaves?", required: true
        input "mac_address1", "text", title: "What is the MAC address of the Wi-FI device hold by this person?", description: "00:00:00:00:00:00", required: true
    }
    section("Person 2") {
		paragraph "Enter the information of another person who must be considered Home when connected to Wi-Fi."
        input "presence_sensor2", "capability.presenceSensor", title: "Which virtual presence sensor must be updated when this person arrives or leaves?", required: false
        input "mac_address2", "text", title: "What is the MAC address of the Wi-FI device hold by this person?", description: "00:00:00:00:00:00", required: false
    }
    section("Other parameters") {
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	// Source: https://community.smartthings.com/t/poll-or-subscribe-example-to-network-events/72862/15
	subscribe(location, null, handleLANEvent, [filterEvents:false])
}

def handleLANEvent(event)
{
	def message = parseLanMessage(event.value)
    
    // Get the HTTP path used on the first header line
    if (!message.header)
    	return
    def path = message.header.split("\n")[0].split()[1]
    
    // Only handle the event if specifically directed to this application
	if (path == "/presence") {
		// Source: https://community.smartthings.com/t/poll-or-subscribe-example-to-network-events/72862/15
		def slurper = new JsonSlurper();
		def json = slurper.parseText(message.body)
        switch (json.event) {
        	case "AP-STA-CONNECTED":
            	if (json.mac_address.toLowerCase() == mac_address1.toLowerCase()) {
              		log.info "${presence_sensor1.name} connected"
					presence_sensor1.arrived()
                } else if (json.mac_address.toLowerCase() == mac_address2.toLowerCase()) {
              		log.info "${presence_sensor2.name} connected"
					presence_sensor2.arrived()
                }
                break
            case "AP-STA-DISCONNECTED":
            	if (json.mac_address.toLowerCase() == mac_address1.toLowerCase()) {
              		log.info "${presence_sensor1.name} disconnected"
					presence_sensor1.departed()
                } else if (json.mac_address.toLowerCase() == mac_address2.toLowerCase()) {
              		log.info "${presence_sensor2.name} disconnected"
					presence_sensor2.departed()
                }
                break
        }
	}
}
