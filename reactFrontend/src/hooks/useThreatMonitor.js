import * as SecureStore from 'expo-secure-store';
import { useEffect, useRef, useState, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import { TextEncoder, TextDecoder } from 'text-encoding';

global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;

export const useThreatMonitor = (rawDeviceIds = []) => {
  const [threats, setThreats] = useState({});

  const clientRef = useRef(null);
  const subscriptionsRef = useRef({});
  const ttlTimersRef = useRef({});
  const deviceIdsRef = useRef([]);

  // Normalize IDs to strings
  const deviceIds = Array.isArray(rawDeviceIds)
    ? rawDeviceIds.map(id => String(id))
    : [];

  // Update ref so syncSubscriptions always has the freshest list
  deviceIdsRef.current = deviceIds;

  const syncSubscriptions = useCallback(() => {
    const client = clientRef.current;
    if (!client || !client.connected) {
      console.log('[STOMP] Sync delayed: Client not connected');
      return;
    }

    const ids = deviceIdsRef.current;

    // 1. Subscribe to new IDs
    ids.forEach(id => {
      if (!subscriptionsRef.current[id]) {
        console.log(`[STOMP] Subscribing to /topic/threats/${id}`);

        subscriptionsRef.current[id] = client.subscribe(
          `/topic/threats/${id}`,
          (msg) => {
            console.log(`[STOMP RECEIVE] Device ${id}`, msg.body);
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

              setThreats(prev => ({
                ...prev,
                [id]: normalizedPayload
              }));

              // Handle TTL
              if (ttlTimersRef.current[id]) clearTimeout(ttlTimersRef.current[id]);
              ttlTimersRef.current[id] = setTimeout(() => {
                console.log(`[TTL] Expired - Downgrading device ${id}`);
                setThreats(prev => ({
                  ...prev,
                  [id]: { ...prev[id], level: 'LOW', isThreat: false }
                }));
              }, 180000);

            } catch (e) {
              console.error("[STOMP] Payload parse error:", e);
            }
          }
        );
      }
    });

    // 2. Unsubscribe from removed IDs
    Object.keys(subscriptionsRef.current).forEach(id => {
      if (!ids.includes(id)) {
        console.log(`[STOMP] Unsubscribing ${id}`);
        subscriptionsRef.current[id].unsubscribe();
        if (ttlTimersRef.current[id]) {
          clearTimeout(ttlTimersRef.current[id]);
          delete ttlTimersRef.current[id];
        }
        delete subscriptionsRef.current[id];
      }
    });
  }, []);

  // EFFECT 1: Persistent Connection Lifecycle
  // This ONLY runs once when the hook is first mounted.
  useEffect(() => {
    const initConnection = async () => {
      console.log('[STOMP] Starting initConnection sequence...');
      
      try {
        // 1. Verify Token Retrieval
        const token = await SecureStore.getItemAsync('userToken');
        if (!token) {
          console.error('[STOMP Error] No userToken found in SecureStore. Connection aborted.');
          return;
        }
        console.log('[STOMP] Token retrieved successfully. Length:', token.length);

        const client = new Client({
          brokerURL: 'wss://guardian-backend-api-cehse7acc4bfhcdq.canadacentral-01.azurewebsites.net/ws-security/websocket',
          connectHeaders: {
            Authorization: `Bearer ${token}`,
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 10000,
          heartbeatOutgoing: 10000,
          
          // Enhanced Debugging
          debug: (str) => {
            if (str.includes('>>>')) console.log('[STOMP OUTGOING]', str);
            else if (str.includes('<<<')) console.log('[STOMP INCOMING]', str);
            else console.log('[STOMP Internal]', str);
          },
          
          onConnect: (frame) => {
            console.log('[STOMP] SUCCESS: Connected to broker');
            console.log('[STOMP] Server Info:', frame.headers['server'] || 'N/A');
            syncSubscriptions();
          },

          onWebSocketClose: (evt) => {
            console.log(`[STOMP] WebSocket Closed. Code: ${evt.code}, Reason: ${evt.reason || 'None'}`);
          },

          onWebSocketError: (error) => {
            console.error('[STOMP] WebSocket Error Event:', error);
          },

          onStompError: (frame) => {
            console.error('[STOMP] BROKER ERROR:', frame.headers['message']);
            console.error('[STOMP] Error Body:', frame.body);
          },

          onDisconnect: () => {
            console.log('[STOMP] Disconnected from broker');
          }
        });

        console.log('[STOMP] Activating client...');
        client.activate();
        clientRef.current = client;

        // Diagnostic Interval: Log status every 10 seconds while this hook is alive
        const statusCheck = setInterval(() => {
          if (clientRef.current) {
            console.log(`[STOMP Health] Active: ${clientRef.current.active}, Connected: ${clientRef.current.connected}, State: ${clientRef.current.state}`);
          }
        }, 10000);

        return statusCheck;

      } catch (err) {
        console.error('[STOMP] Critical error during initialization:', err);
      }
    };

    const statusInterval = initConnection();

    return () => {
      console.log('[STOMP] Deactivating persistent connection');
      statusInterval.then(intervalId => clearInterval(intervalId));
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
      Object.values(ttlTimersRef.current).forEach(timer => clearTimeout(timer));
    };
  }, [syncSubscriptions]);

  // EFFECT 2: Dynamic Subscription Management
  // This runs whenever the deviceIds change, without resetting the connection.
  useEffect(() => {
    if (clientRef.current?.connected) {
      syncSubscriptions();
    }
  }, [deviceIds.join(','), syncSubscriptions]);

  return threats;
};