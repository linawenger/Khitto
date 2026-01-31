# Khitto

Khitto is a decentralized, web-based quiz application built on the Ditto peer-to-peer framework. It does not rely on a central server and can operate without a permanent internet connection.

The application is designed for collaborative learning. All participants have equal permissions to create, modify, and start quiz games.

---

## Features

The system uses the Ditto peer-to-peer framework for synchronization, enabling reliable data exchange even in offline-first scenarios. Its frontend is a web-based user interface, making the application easily accessible across platforms. All data is stored and exchanged in JSON format, while the overall architecture is fully decentralized to avoid central dependencies and support resilient, distributed operation.

---

## Game Model

- A game contains multiple questions
- Each question contains multiple answers
- Exactly one answer per question is defined as correct

---

## Gameplay

1. Create or edit a quiz
2. Publish the quiz
3. Start a game session
4. Answer questions sequentially
5. Receive immediate feedback
6. Display results after the final question

All connected peers share the same game state while peer-to-peer connectivity is available.
