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

  // Ensure deviceIds is always an array of strings
  const deviceIds = Array.isArray(rawDeviceIds) 
    ? rawDeviceIds.map(id => String(id)) 
    : [];

  const syncSubscriptions = (ids) => {
    const client = clientRef.current;
    if (!client || !client.connected) return;

    ids.forEach(id => {
      if (!subscriptionsRef.current[id]) {
        console.log(`[STOMP] Subscribing to: /topic/threats/${id}`);
        
        subscriptionsRef.current[id] = client.subscribe(`/topic/threats/${id}`, (msg) => {
          console.log(`[STOMP RECEIVE] Device ${id} raw payload:`, msg.body);

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

            // Update threat immediately
            setThreats(prev => ({
              ...prev,
              [id]: normalizedPayload
            }));

            // Clear existing TTL timer
            if (ttlTimersRef.current[id]) {
              clearTimeout(ttlTimersRef.current[id]);
            }

            // Start new TTL timer (3 minutes to match backend Redis TTL)
            ttlTimersRef.current[id] = setTimeout(() => {
              console.log(`[TTL] Expired - Downgrading device ${id} to LOW`);

              setThreats(prev => ({
                ...prev,
                [id]: {
                  ...prev[id],
                  level: 'LOW',
                  isThreat: false,
                }
              }));
            }, 180000); // 3 minutes

          } catch (e) {
            console.error("[STOMP] Payload parse error:", e);
          }
        });
      }
    });

    // Unsubscribe from devices no longer in the list
    Object.keys(subscriptionsRef.current).forEach(id => {
      if (!ids.includes(id)) {
        console.log(`[STOMP] Unsubscribing from: ${id}`);
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
    if (deviceIds.length === 0) return;

    if (clientRef.current) {
      if (clientRef.current.connected) {
        syncSubscriptions(deviceIds);
      }
      return; 
    }

    console.log('[STOMP] Initializing WebSocket Connection...');

    const client = new Client({
      webSocketFactory: () => new WebSocket(
        'ws://192.168.2.78:8080/ws-security', 
        ['v12.stomp', 'v11.stomp', 'v10.stomp']
      ),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (str) => {
        console.log('[STOMP Debug]', str);
      },
      onConnect: () => {
        console.log('[STOMP] Connected successfully');
        syncSubscriptions(deviceIds);
      },
      onStompError: (frame) => console.error('[STOMP] Error:', frame.headers['message']),
      onWebSocketError: (err) => console.error('[STOMP] WebSocket Error:', err),
    });

    client.activate();
    clientRef.current = client;

  }, [JSON.stringify(deviceIds)]); 

  useEffect(() => {
    return () => {
      if (clientRef.current) {
        console.log('[STOMP] Deactivating WebSocket Connection');
        clientRef.current.deactivate();
        clientRef.current = null;
      }
      
      // Cleanup all active TTL timers on unmount
      Object.values(ttlTimersRef.current).forEach(timer => clearTimeout(timer));
      ttlTimersRef.current = {};
    };
  }, []);

  return threats;
};