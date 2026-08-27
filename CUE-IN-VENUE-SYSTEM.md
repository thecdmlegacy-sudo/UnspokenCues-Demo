# Cue-In + Venue System

## Core purpose
Unspoken Cues is an in-person social interaction layer for places where people are already gathered.

Cue-In answers a specific question that the physical wristband, watch, and window profile cannot answer when someone is not currently visible: **Is a person I already connected with around right now?**

## Cue-In
A participating venue/event has a unique Cue-In QR. Scanning it activates event presence on the user's existing Unspoken Cues profile. It does not create another profile.

When active, the profile may display:
- CUE-IN
- venue/event name
- currently here / event-active state
- optional attendee verification

When Cue-In expires or the user checks out, the profile returns to normal and no longer implies physical presence.

### Privacy
Presence visibility must be user-controlled:
- Everyone
- S.W.A.P.s only
- Hidden

Default product direction: S.W.A.P.s only, because the strongest use case is rediscovering people already met without turning Unspoken Cues into a dating-site people search.

## Existing-connection discovery
A user can open a previously collected S.W.A.P. card from their Digital Binder. If that person has Cue-In active and permits the viewer to see it, the card/profile indicates that they are currently at the same participating venue/event.

This creates the return loop:
**Meet → S.W.A.P. → Save → Return later → Cue-In → Rediscover.**

## In-room interaction loop
Unspoken Cues remains physical-first:
**Already here → Discover → Read the cues → Approach → S.W.A.P. → Remember → Rediscover next time.**

Entry paths:
1. Previous connection: open collected S.W.A.P. card → Cue-In says they are around.
2. Nearby discovery: see/scan window profile → learn who they are and whether there is compatibility.
3. Physical encounter: see wristband/watch status → understand approachability → S.W.A.P. provides an icebreaker and persistent connection.

## Wearable design doctrine
The physical status signal remains the center of Unspoken Cues. Build it around these rules:
- **Instant readability:** the current status must be understandable at a glance and from normal social distance.
- **Persistent visibility:** prioritize display/lighting approaches that can remain visible without unnecessary battery drain.
- **Manual control wins:** the wearer explicitly controls the status other people see.
- **Future smart suggestions are private:** biometrics or contextual sensing may eventually suggest a status to the wearer, but must never automatically broadcast availability or consent.
- **Wearability matters:** future hardware should support attractive, interchangeable/customizable band options so it feels like something guests choose to wear rather than event equipment.
- **Keep the four-state language:** Green, Yellow, Red, and Purple remain the Unspoken Cues status system.

Do not add fitness tracking or biometric complexity to the current product merely because wearable competitors use it. Those features are only worth considering if they directly improve the in-person interaction problem.

## Venue analytics
Add privacy-preserving aggregate analytics for participating venues/events. This is host value, not the consumer experience.

Track:
- total Cue-Ins
- unique Cue-Ins
- repeat attendees
- active attendees now
- S.W.A.P.s made at the event
- number of attendees using Unspoken Cues
- interaction/compatibility checks as aggregate counts only
- return attendance across events

Do **not** expose private preferences, boundaries, compatibility details, private card collections, or individual interaction histories to venues.

## Venue dashboard direction
A future host dashboard can show:
- Live attendance count
- Total/unique Cue-Ins
- New vs returning attendees
- S.W.A.P. activity
- Engagement rate
- Repeat-event rate

This gives venues measurable value without requiring Unspoken Cues to become an event-management platform.

## Strategic boundary
Do not copy full event operations from competitors unless needed. Unspoken Cues does not need to own ticketing, generic event discovery, or a pre-event dating feed to deliver its core value.

The product should own the **last mile of in-person interaction**: knowing who is around, understanding whether interaction makes sense, reducing approach uncertainty, creating the conversation, and preserving the connection afterward.
