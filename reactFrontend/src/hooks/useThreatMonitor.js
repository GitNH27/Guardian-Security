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
    console.log('[STOMP] Initializing persistent connection');

    const client = new Client({
      brokerURL: 'wss://guardian-backend-api-cehse7acc4bfhcdq.canadacentral-01.azurewebsites.net/ws-security',
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      debug: (str) => console.log('[STOMP Debug]', str),
      
      onConnect: () => {
        console.log('[STOMP] Connected');
        syncSubscriptions();
      },
      onWebSocketError: (error) => console.error('[STOMP] WebSocket error', error),
      onStompError: (frame) => console.error('[STOMP] Broker error', frame.headers['message']),
    });

    client.activate();
    clientRef.current = client;

    // Cleanup: Only close the socket when the app/hook is destroyed
    return () => {
      console.log('[STOMP] Deactivating persistent connection');
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
      Object.values(ttlTimersRef.current).forEach(timer => clearTimeout(timer));
    };
  }, [syncSubscriptions]); // Added syncSubscriptions as stable dependency

  // EFFECT 2: Dynamic Subscription Management
  // This runs whenever the deviceIds change, without resetting the connection.
  useEffect(() => {
    if (clientRef.current?.connected) {
      syncSubscriptions();
    }
  }, [deviceIds.join(','), syncSubscriptions]);

  return threats;
};