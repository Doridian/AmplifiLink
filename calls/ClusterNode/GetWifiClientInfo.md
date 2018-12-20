```
{"iface":"com.ubnt.UnifiHome.ClusterNode","method":"GetWifiClientInfo","payload":{"msgpack":"ksDA"},"type":"call","seqId":3}
{"payload": {"msgpack": "[SNIP]"}, "seqId": 3, "type": "response"}

{
    APMAC: {
        BAND: {
            ROLE: {
                DEVMAC: {
                       Device info
                },
                ...
            },
            ...
        },
        ...
    },
    ...
}
```

The msgpack sent by the client is an array of either `[null, null]` to get all clients
or `[[role1, role2, ...], null]` to get only those specific roles
The second field of the array is currently unknown, but likely for filtering further

Role:
1. initial setup network
2. internal network (used for the AP to reach wireless mesh nodes)
3. user network
4. guest network
5. device specific network
6. relay network

Band:
1. 2.4GHz
2. 5GHz

Device info:
1. N/A
2. Mode
      1. A
      2. B
      3. G
      4. N
      5. AC
3. Signal Quality (0 - 100)
4. Inactive
5. RX bytes
6. TX bytes
18. Device name
