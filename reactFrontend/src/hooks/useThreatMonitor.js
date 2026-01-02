import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import { TextEncoder, TextDecoder } from 'text-encoding';

global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;

export const useThreatMonitor = (rawDeviceIds = []) => {
  const [threats, setThreats] = useState({});
  const clientRef = useRef(null);
  const subscriptionsRef = useRef({}); 
  const ttlTimersRef = useRef({});

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
        console.log(`📥 [STOMP RECEIVE] Device ${id} raw payload:`, msg.body);

        try {
            const payload = JSON.parse(msg.body);

            const normalizedPayload = {
            ...payload,
            level: payload.threatLevel,
            deviceId: payload.rawDeviceId,
            object: payload.objectDetected,
            isThreat: payload.threatLevel === 'HIGH',
            lastUpdated: Date.now()
            };

            // 🔥 Update threat immediately
            setThreats(prev => ({
            ...prev,
            [id]: normalizedPayload
            }));

            // ⏱️ CLEAR existing TTL timer (if any)
            if (ttlTimersRef.current[id]) {
            clearTimeout(ttlTimersRef.current[id]);
            }

            // ⏳ START new TTL timer
            ttlTimersRef.current[id] = setTimeout(() => {
            console.log(`🟢 TTL expired → Downgrading device ${id} to LOW`);

            setThreats(prev => ({
                ...prev,
                [id]: {
                ...prev[id],
                level: 'LOW',
                isThreat: false
                }
            }));
            }, 15000); // 15 seconds

        } catch (e) {
            console.error("❌ Payload parse error", e);
        }
        });

      }
    });

    Object.keys(subscriptionsRef.current).forEach(id => {
    if (!ids.includes(id)) {
        console.log(`🔌 Unsubscribing from: ${id}`);
        subscriptionsRef.current[id].unsubscribe();

        if (ttlTimersRef.current[id]) {
        clearTimeout(ttlTimersRef.current[id]);
        delete ttlTimersRef.current[id];
        }

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

        // 🧹 CLEANUP: Clear all active TTL timers on unmount
        Object.values(ttlTimersRef.current).forEach(timer => clearTimeout(timer));
        ttlTimersRef.current = {};
        };
    }, []);

  return threats;
};