import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { TextEncoder, TextDecoder } from 'text-encoding';

global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;

export const useThreatMonitor = (rawDeviceIds = []) => {
  const [threats, setThreats] = useState({});
  const clientRef = useRef(null);
  const subscriptionsRef = useRef({}); 

  const deviceIds = Array.isArray(rawDeviceIds) 
    ? rawDeviceIds.map(id => String(id)) 
    : [];

  const syncSubscriptions = (ids) => {
    const client = clientRef.current;
    if (!client || !client.connected) return;

    ids.forEach(id => {
      if (!subscriptionsRef.current[id]) {
        console.log(`📡 Subscribing to: /topic/threats/${id}`);
        
        subscriptionsRef.current[id] = client.subscribe(`/topic/threats/${id}`, (msg) => {
          // 🛑 DEBUG LOG: See the raw data from Spring
          console.log(`📥 [STOMP RECEIVE] Device ${id} raw payload:`, msg.body);

          try {
            const payload = JSON.parse(msg.body);
            
            // 🔍 INSPECTION LOG: See the parsed object keys
            console.log(`📋 [PARSED DATA] Keys detected:`, Object.keys(payload));

            setThreats(prev => {
            const normalizedPayload = {
                ...payload,
                // Normalize keys so the UI doesn't have to guess
                level: payload.threatLevel, 
                deviceId: payload.rawDeviceId,
                object: payload.objectDetected,
                isThreat: payload.threatLevel === 'HIGH' || payload.threatLevel === 'CRITICAL'
            };
            
            return { ...prev, [id]: normalizedPayload };
            });
          } catch (e) {
            console.error("❌ Payload parse error", e);
          }
        });
      }
    });

    // Unsubscribe logic remains the same
    Object.keys(subscriptionsRef.current).forEach(id => {
      if (!ids.includes(id)) {
        console.log(`🔌 Unsubscribing from: ${id}`);
        subscriptionsRef.current[id].unsubscribe();
        delete subscriptionsRef.current[id];
      }
    });
  };

  useEffect(() => {
    console.log('🔄 Hook Pulse - Device IDs:', deviceIds);

    if (deviceIds.length === 0) return;

    if (clientRef.current) {
      if (clientRef.current.connected) {
        syncSubscriptions(deviceIds);
      }
      return; 
    }

    console.log('🚀 Initializing WebSocket Connection...');

    const client = new Client({

      // Add 'v12.stomp' as a sub-protocol to satisfy Spring's requirements
        webSocketFactory: () => new WebSocket(
        'ws://192.168.2.18:8080/ws-security', 
        ['v12.stomp', 'v11.stomp', 'v10.stomp'] // Explicitly list supported protocols
        ),

        forceBinaryWSFrames: true,

        appendMissingNULLonIncoming: true,


      
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      // ADDED: This will print every STOMP frame sent/received to your terminal
      debug: (str) => {
        console.log('🐝 STOMP Debug:', str);
      },

      onConnect: () => {
        console.log('✅ STOMP Connected');
        syncSubscriptions(deviceIds);
      },
      onStompError: (frame) => console.error('❌ STOMP Error:', frame.headers['message']),
      onWebSocketError: (err) => console.error('❌ WS Error:', err),
    });

    client.activate();
    clientRef.current = client;

  }, [JSON.stringify(deviceIds)]); 

    useEffect(() => {
        return () => {
            if (clientRef.current) {
                console.log('💤 Deactivating WebSocket Connection (Unmount)');
                clientRef.current.deactivate();
                clientRef.current = null;
            }
        };
    }, []);

  return threats;
};