# ADR-0005: Plan to Add Network Play (Online Turn-Based Multiplayer)

Status: Proposed
Date: 2025-09-12
Authors: Team MilitaryMadness

Context
- MilitaryMadness is turn-based with a clear separation between GUI and engine (see ADR-0002). GameLoop and GameController coordinate input, updates, and rendering. ImmutableGameState snapshots are used by the renderer and UI.
- The current game supports local human-vs-human play (and an AI plan exists in ADR-0004). There is no networked multiplayer.
- Turn-based play is well-suited to low-bandwidth, deterministic protocols. We can synchronize using discrete, validated actions rather than streaming per-frame state.

Problem
- Allow players on different machines to play a match over the network with minimal disruption to existing architecture, while keeping gameplay fair, deterministic, and robust in variable network conditions.

Goals
1) Enable two-player online matches over LAN/Internet using a simple, robust protocol.
2) Preserve engine authority: only legal, validated actions are applied. Desyncs are detectable and recoverable.
3) Keep UI passive; networking integrates at the controller/loop layer without pushing network code into Swing views.
4) Deterministic and testable: given the same map, version, and action sequence, both sides converge on the same state.
5) Backward-compatible: default behavior remains unchanged unless the user opts into network play.

Non-goals (initial release)
- Spectators, replays, or more than 2 players.
- Real-time simultaneous turns or rollback netcode.
- Cross-version matchmaking or server discovery beyond manual IP/port entry.
- Cryptographic anti-cheat; we assume friendly play with basic sanity checks.

High-Level Design
- Architecture: Client-Server (authoritative server). One instance acts as the server (host). The other connects as a client (join). The server owns the canonical game state and validates/applies actions. The client mirrors state updates.
- Sync strategy: Action-based lockstep per turn.
  - The active player submits a sequence of typed domain actions (Move, Attack, EndTurn, etc.).
  - The server validates and applies each action to its state, then broadcasts the accepted action (and resulting minimal state delta) to the client.
  - The client applies the same action to its local state; optional checksums after significant steps detect divergence.
- Transport: TCP over sockets. Simple JSON messages for readability at first; optional binary framing later. Include protocol version in handshake.
- Determinism: All randomness, if any, must be server-driven. The server sends any random seeds/rolls as part of action results; clients do not use local randomness for authoritative outcomes.
- Versioning: Require same game version and protocol version; handshake will reject mismatched clients.

Key Components (new)
- military.net package with:
  - NetMessage model (sealed-like hierarchy or enum + payload) for: Hello/Handshake, LobbyInfo, StartGame, PlayerAction, ActionAccepted, StateDelta, KeepAlive, Error, ResyncRequest, ResyncState.
  - Connection: blocking read/write worker with thread-safe send queue; JSON serialization via minimal utility (no heavy deps) using existing org.json or custom light encoder.
  - Server: hosts a single match, authoritative Game instance; validates and applies actions; emits deltas.
  - Client: mirrors server state, forwards local user actions to server, applies deltas on receipt.
- NetworkController (adapter): bridges GameController/GameLoop with net layers. When online:
  - If local side is active player: queue local actions to server and wait for acceptance before applying locally.
  - If remote side is active player: ignore local inputs; display remote actions as they are applied.
- Action API: Reuse/extend the typed Action model introduced in ADR-0004 (MoveAction, AttackAction, EndTurnAction, etc.) so both AI and network code share the same domain operations.
- Checkpointing & Resync: Periodic state hash and optional compact snapshot to handle desync or late join (phase 2).

Threading Model
- One IO thread per connection (read) and one for send queue; do not block EDT. Game mutations occur on the Game loop thread. Incoming network actions are posted onto the controller/loop via a thread-safe queue.
- UI updates scheduled on the EDT as usual.

Security/Validation
- Server validates all incoming actions against the current authoritative state.
- Rate limits and simple timeouts to prevent resource abuse.
- Basic input sanitation; JSON size caps.

Protocol Sketch (JSON examples)
- Hello (client->server): {"type":"hello","protocol":1,"gameVersion":"X.Y.Z","playerName":"Alice"}
- HelloAck (server->client): {"type":"helloAck","accept":true,"side":"RED","seed":12345}
- PlayerAction (client->server): {"type":"action","id":42,"action":{"kind":"move","unitId":17,"to":[x,y]}}
- ActionAccepted (server->client): {"type":"actionAccepted","id":42,"result":{"ok":true,"effects":[...]},"stateHash":"abcd"}
- EndTurn (client->server): {"type":"action","id":43,"action":{"kind":"endTurn"}}
- KeepAlive: {"type":"ping","t":169000}

Phased Plan and Milestones
1) Foundations and Contracts
   - Add military.net package with message classes, Connection abstraction, and simple JSON encoding/decoding. 
   - Define Action serialization shared with AI Action types. 
   - Add CLI flags: --host [port], --join <host[:port]>, --nick <name>.
   - Handshake with protocol and version check.
   - Tests: unit tests for (de)serialization of messages and actions; connection loopback test using localhost sockets.

2) Server-Authoritative Single Turn Flow
   - Implement NetServer with authoritative Game instance. Accept one client. Assign sides (RED/BLUE) via CLI or first-come.
   - Implement NetClient to connect, perform handshake, and receive StartGame with map selection and initial seed/state.
   - Wire NetworkController to GameLoop: 
     - Local active player actions are intercepted and sent to server; apply only upon acceptance. 
     - Remote actions are applied when received.
   - Tests: integration test that simulates server and client in-process playing a trivial map; they remain in sync through one full turn each.

3) Robustness: Timeouts, Keep-Alive, Reconnect (optional), and Error Paths
   - Add periodic pings and timeouts; detect disconnects cleanly. 
   - Simple resign/forfeit message. 
   - On desync (hash mismatch), request full ResyncState (phase 4) or end session with error (initial).
   - Tests: simulate dropped connection; verify graceful shutdown.

4) State Delta and Resync Mechanism
   - Define compact StateDelta for units, cities/factories, and turn/phase markers. 
   - Compute periodic stateHash (e.g., SHA-256 over canonicalized state). 
   - Add ResyncRequest/ResyncState to recover from detected divergence.
   - Tests: induce artificial delay/out-of-order (still TCP-ordered) and confirm stable state; test resync applies correctly.

5) UX and Game Flow
   - Lobby/host dialog or CLI-only selection initially. 
   - Show connection status and opponent’s name. 
   - Disable local input when it’s not your turn; display remote actions as toasts/log lines. 
   - Save/Load: include net session metadata in SaveGame but default to offline on load unless user opts to reconnect.
   - Tests: headless run using CLI flags completes a short match segment without exceptions.

6) Polishing and Documentation
   - Javadoc for military.net APIs and Action serialization. 
   - docs/net/README.md with protocol documentation and troubleshooting.
   - Update contributor guidelines with headless/network testing notes and CI exclusions for true socket tests (mark as integration).

Data and Services Needed
- Access to the same Action API used by GUI/AI. 
- Ability to serialize ImmutableGameState or a compact subset for StartGame and resync. 
- Config hooks for map selection and resource resolution on both sides.

Acceptance Criteria (Initial Release)
- Running host: `gradlew run --args "--host 7777 --map Sample_small.txt --side RED"` starts a server waiting for a client. 
- Running client: `gradlew run --args "--join 127.0.0.1:7777 --nick Bob"` connects, both see the same initial state, and can play alternating turns. 
- Only legal actions are applied; clients remain in sync across at least several turns on all shipped maps. 
- If a client disconnects, the server session ends gracefully with a message; the local game returns to menu or exits without throwing. 

Open Questions
- Do we need NAT traversal/stun for WAN play, or is manual port forwarding acceptable initially? 
- Should we implement a simple turn timer to avoid indefinite stalling? 
- How to handle mid-turn undo/redo features if present locally (likely disable in net play)?

Alternatives Considered
- Peer-to-peer lockstep without an authoritative server: simpler hosting but harder desync recovery and cheating concerns; rejected for initial release. 
- Streaming full state after each action: robust but heavier bandwidth; acceptable as a fallback resync but not as the primary flow. 
- Binary protocol from day one: more efficient but slower to debug; JSON is sufficient initially.

Dependencies
- ADR-0002 (controller separation) and ADR-0004 (Action API for AI) to unify domain actions used over the network. 
- No external libraries are required; prefer JDK networking and a tiny JSON encoder.

Risk Mitigation
- Feature-flag via CLI; default app remains offline. 
- Deterministic server outcomes and periodic hashes detect and correct drift. 
- Clear test suite separation: fast unit tests for serialization; slower socket tests marked as integration.
